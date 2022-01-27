package com.tanhua.manage.service;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanhua.manage.domain.Admin;
import com.tanhua.manage.exception.BusinessException;
import com.tanhua.manage.mapper.AdminMapper;
import com.tanhua.manage.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AdminService extends ServiceImpl<AdminMapper, Admin> {

    private static final String CACHE_KEY_CAP_PREFIX = "MANAGE_CAP_";
    public static final String CACHE_KEY_TOKEN_PREFIX="MANAGE_TOKEN_";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 保存生成的验证码
     * @param uuid
     * @param code
     */
    public void saveCode(String uuid, String code) {
        String key = CACHE_KEY_CAP_PREFIX + uuid;
        // 缓存验证码，10分钟后失效
        redisTemplate.opsForValue().set(key,code, Duration.ofMinutes(10));
    }

    /**
     * 获取登陆用户信息
     * @return
     */
    public Admin getByToken(String authorization) {
        String token = authorization.replaceFirst("Bearer ","");
        String tokenKey = CACHE_KEY_TOKEN_PREFIX + token;
        String adminString = (String) redisTemplate.opsForValue().get(tokenKey);
        Admin admin = null;
        if(StringUtils.isNotEmpty(adminString)) {
            admin = JSON.parseObject(adminString, Admin.class);
            // 延长有效期 30分钟
            redisTemplate.expire(tokenKey,30, TimeUnit.MINUTES);
        }
        return admin;
    }

    /**
     * 登录
     */
    public String login(String username, String password, String verificationCode, String uuid) {
        //1.先根据uuid获取redis中验证码
        String key = CACHE_KEY_CAP_PREFIX + uuid;
        String redisCode = (String)redisTemplate.opsForValue().get(key);
        //2.用户输入的验证码 跟 redis验证码对比  如果验证失败则直接返回
        if(StringUtils.isEmpty(verificationCode) || StringUtils.isEmpty(redisCode) || !redisCode.equals(verificationCode)){
            throw new BusinessException("验证码错误，请重试！");
        }
        //3.根据用户名查询tb_admin表 看用户是否存在 如果不存在 则直接返回
        Admin admin = query().eq("username", username).one();
        if(admin == null){
            throw new BusinessException("用户名输入错误！");
        }
        //4.用户输入的密码 跟数据库查询的密码对比 如果错误 则直接返回
        if(!SecureUtil.md5(password).equals(admin.getPassword())){
            throw new BusinessException("密码输入错误！");
        }
        //5.生成token存入redis 2小时有效
        String token = jwtUtils.createJWT(username, admin.getId());
        String tokenKey = CACHE_KEY_TOKEN_PREFIX+token;//token作为key
        String adminStr = JSON.toJSONString(admin); //admin对象作为value
        redisTemplate.opsForValue().set(tokenKey,adminStr,2,TimeUnit.HOURS);
        //6.将token返回
        //7.登录成功后，将验证码删除
        redisTemplate.delete(key);
        return token;
    }


    /**
     * 退出
     */
    public void logout(String token) {
        String tokenKey = CACHE_KEY_TOKEN_PREFIX+token;//token作为key
        redisTemplate.delete(tokenKey);
    }
}
