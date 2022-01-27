package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.FaceTemplate;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.commons.templates.SmsTemplate;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.dubbo.api.db.UserApi;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.omg.SendingContext.RunTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 消费者-用户业务处理类
 */
@Service
@Slf4j//日志注解
public class UserService {
    //调用服务 进行业务逻辑处理
    @Reference
    private UserApi userApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private SmsTemplate smsTemplate;

    @Value("${tanhua.redisValidateCodeKeyPrefix}")
    private String validateCodePredix;

    @Value("${tanhua.tokenKey}")
    private String tokenKey;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private FaceTemplate faceTemplate;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 完成用户保存返回用户id
     */
    public Long saveUser(User user) {
        return userApi.saveUser(user);
    }

    /**
     * 根据手机号码查询用户功能
     */
    public User findByMobile(String mobile) {
        return userApi.findByMobile(mobile);
    }

    /**
     * 获取验证码
     * @param phone
     */
    public void sendCode(String phone) {
        //1. 根据手机号查询redis是否存在获取验证码记录
        String key = validateCodePredix+phone;
        String redisCode = redisTemplate.opsForValue().get(key);
        //2. 如果获取过验证码，则提示验证码还未失效
        if(!StringUtils.isEmpty(redisCode)){
            throw new TanHuaException(ErrorResult.duplicate());
        }
        //3. 如果没有获取过验证码，生成验证码调用短信接口发送短信
        String num = RandomStringUtils.randomNumeric(6);
        num = "123456";
        log.debug("*********手机号{}，验证码{}*********",phone,num);
        if(false) {//为了省钱。。。
            Map<String, String> rsMap = smsTemplate.sendValidateCode(phone, num);
            if (rsMap != null) {
                throw new TanHuaException(ErrorResult.fail());
            }
        }
        //4.将验证码存入redis
        redisTemplate.opsForValue().set(key,num,5, TimeUnit.MINUTES);
    }
    /**
     * 登录注册
     */
    public Map loginVerification(String phone, String verificationCode) {
        boolean isNew = false;//老用户
        //1接收登录注册请求参数手机号 验证码
        String key  = validateCodePredix+phone;
        //2根据手机号查询redis验证码是否存在
        String redisCode = redisTemplate.opsForValue().get(key);
        //3如果验证码不存在，则说明验证码已经失效了
        if(StringUtils.isEmpty(redisCode)){
            throw new TanHuaException(ErrorResult.loginError());
        }
        //4如果验证码存在，校验验证码
        if(!redisCode.equals(verificationCode)){
            //5如果校验验证码失败了，则提示验证码错误
            throw new TanHuaException(ErrorResult.validateCodeError());
        }
        //6如果校验通过了，根据手机号查询用户表记录是否存在
        User user = userApi.findByMobile(phone);
        String type = "0101";//默认登录
        //7用户不存在，则自动注册 先跳转完善个人信息页面  再直接登录
        if(user == null){
            user = new User();
            user.setMobile(phone);//设置手机号码
            Long userId = userApi.saveUser(user);  //保存用户后返回userId
            //设置下userId
            user.setId(userId);

            isNew = true;
            //调用环信云 注册api
            huanXinTemplate.register(userId);
            log.debug("******************环信注册成功了******************");
            type = "0102";
        }
        //8用户存在，则直接登录（token需要单独处理分析）
        Map rsMap = new HashMap();
        String token = jwtUtils.createJWT(phone, user.getId());//根据手机号 用户id生成token字符串
        rsMap.put("token",token);
        rsMap.put("isNew",isNew);
        //9将验证码从redis删除
        redisTemplate.delete(key);
        //10.登录成功后 将用户信息存入redis（key=Token_token字符串 value=用户对象） 用于后续校验用户是否登录
        String userStr = JSON.toJSONString(user);
        redisTemplate.opsForValue().set(tokenKey+token,userStr,1,TimeUnit.DAYS);
        log.debug("************用户{}登录成功了****token{}*******isNew{}*********",phone,token,isNew);
        Map<String,String> message = new HashMap<>();
        message.put("userId",user.getId().toString());
        message.put("logTime",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        message.put("type",type);
        rocketMQTemplate.convertAndSend("tanhua-logs",message);
        log.debug("******************往rocketmq日志主题写日志成功了******************");
        return rsMap;
    }

    /**
     * 完善个人信息-保存用户基础信息
     */
    public void loginReginfo(UserInfoVo userInfoVo) {
        Long userId = UserHolder.getUserId();
        //c.如果存在，则调用服务保存tb_user_info即可
        UserInfo userInfo = new UserInfo();
        //方式二： 将相同的属性的名称 类型的值 从A对象copy到B对象上
        BeanUtils.copyProperties(userInfoVo,userInfo);
        userInfo.setId(userId);//尽量放到BeanUtils.copyProperties后面 防止被覆盖
        log.debug("******userInfo*****{}*****",userInfo);
        userInfoApi.saveUserInfo(userInfo);
    }

    /**
     * 根据token获取user对象
     * @param token
     * @return
     */
    public User getUserByToken(String token) {
        String key = tokenKey+token;
        //a.根据请求头token 查询redis用户信息是否存在
        String userStr = redisTemplate.opsForValue().get(key);
        if(StringUtils.isEmpty(userStr)){
            //b.如果不存在，则重新登录
            return null;
        }
        redisTemplate.expire(key,1, TimeUnit.DAYS);//续期1天
        return JSON.parseObject(userStr, User.class);
    }

    /**
     * 完善个人信息-更新头像
     * @param headPhoto
     */
    public void loginReginfoHead(MultipartFile headPhoto) throws IOException {
        Long userId = UserHolder.getUserId();

        //c.调用百度人脸识别组件 如果失败，则提示用户重新上传头像
        boolean detect = faceTemplate.detect(headPhoto.getBytes());
        if(!detect){
            throw new TanHuaException(ErrorResult.faceError());
        }
        //d.调用阿里云OSS保存头像文件，返回头像地址。
        String headUrl = ossTemplate.upload(headPhoto.getOriginalFilename(), headPhoto.getInputStream());
        if(StringUtils.isEmpty(headUrl)){
            throw new TanHuaException(ErrorResult.error());
        }
        //e.根据用户id(主键)更新头像tb_user_info
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setAvatar(headUrl);
        userInfoApi.editUserInfo(userInfo);
    }
}
