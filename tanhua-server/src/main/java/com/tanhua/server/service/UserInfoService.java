package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.Settings;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.db.BlackListApi;
import com.tanhua.dubbo.api.db.QuestionApi;
import com.tanhua.dubbo.api.db.SettingsApi;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.FriendApi;
import com.tanhua.dubbo.api.mongo.UserLikeApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.GetAgeUtil;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户基础信息业务处理层
 */
@Service
public class UserInfoService {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Reference
    private UserInfoApi userInfoApi;

    @Value("${tanhua.tokenKey}")
    private String tokenKey;

    @Autowired
    private UserService userService;

    @Reference
    private QuestionApi questionApi;

    @Reference
    private SettingsApi settingsApi;

    @Reference
    private BlackListApi blackListApi;

    @Reference
    private FriendApi friendApi;

    @Reference
    private UserLikeApi userLikeApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    /**
     * 查询用户基础信息
     */
    public UserInfoVo findUserInfoById(Long userID, Long huanxinID) {
        Long myCurrentUserId;
        //1.如果userID不为空 以此id 查询用户基础信息
        if(!StringUtils.isEmpty(userID)){
            myCurrentUserId = userID;
        }else  if(!StringUtils.isEmpty(huanxinID)) {
            //2.如果huanxinID不为空 以此id 查询用户基础信息
            myCurrentUserId = huanxinID;
        }else {
            //3.从token中获取userId,查询用户基础信息 UserInfo
            myCurrentUserId = UserHolder.getUserId();
        }
        //4.根据userId调用服务获取用户基础信息
        UserInfo userInfo = userInfoApi.findUserInfoById(myCurrentUserId);
        //5.将UserInfo转为UserInfoVo (注意返回数据类型 跟 接口文档要一致 数据不能为空)
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo,userInfoVo);
        if(!StringUtils.isEmpty(userInfo.getAge())) {
            userInfoVo.setAge(String.valueOf(userInfo.getAge()));
        }
        return userInfoVo;
    }

    /**
     * 更新用户基础信息
     */
    public void editUserInfoById(UserInfoVo userInfoVo) {
        Long userId = UserHolder.getUserId();
        //2.将UserInfoVo转为userInfo
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userInfoVo,userInfo);
        userInfo.setId(userId);//设置用户id
        if(!StringUtils.isEmpty(userInfoVo.getBirthday())) {
            int age = GetAgeUtil.getAge(userInfoVo.getBirthday());//age单独处理下  根据生日计算age
            userInfo.setAge(age);
        }
        //3.调用userInfoApi更新用户信息
        userInfoApi.editUserInfo(userInfo);
    }

    /**
     * 通知设置查询
     */
    public SettingsVo findSettings() {
        SettingsVo vo = new SettingsVo();
        //1.获取手机号 用户id
        User user = UserHolder.getUser();
        Long userId = user.getId();
        String mobile = user.getMobile();//手机号
        //2.调用服务获取陌生人问题
        Question question = questionApi.findByUserId(userId);
        String strangerQuestion = "约不约?";//设置默认问题
        //如果为空
        if(question != null && !StringUtils.isEmpty(question.getTxt())){
            strangerQuestion = question.getTxt();
        }
        //3.调用服务获取通用设置数据
        Settings settings = settingsApi.findByUserId(userId);
        if(settings != null){
            vo.setGonggaoNotification(settings.getGonggaoNotification());
            vo.setLikeNotification(settings.getLikeNotification());
            vo.setPinglunNotification(settings.getPinglunNotification());
        }else
        {
            vo.setGonggaoNotification(true);//装新app默认都是通知的
            vo.setLikeNotification(true);
            vo.setPinglunNotification(true);
        }
        //4.封装VO
        vo.setPhone(mobile);
        vo.setStrangerQuestion(strangerQuestion);
        vo.setId(userId);
        return vo;
    }

    /**
     * 通知设置 - 保存
     */
    public void saveOreditSettings(Boolean likeNotification, Boolean pinglunNotification, Boolean gonggaoNotification) {
        //1.获取用户id
        Long userId = UserHolder.getUserId();   
        //2.根据用户id查询通知设置表记录是否存在
        Settings settings = settingsApi.findByUserId(userId);
        //3.如果不存在 则直接保存
        if(settings == null){
            settings = new Settings();
            settings.setUserId(userId);//用户id
            settings.setLikeNotification(likeNotification);//喜欢
            settings.setPinglunNotification(pinglunNotification);//评论
            settings.setGonggaoNotification(gonggaoNotification);//公告
            settingsApi.saveSettings(settings);
        }else {
            //4.如果存在，则更新通知设置
            settings.setLikeNotification(likeNotification);//喜欢
            settings.setPinglunNotification(pinglunNotification);//评论
            settings.setGonggaoNotification(gonggaoNotification);//公告
            settingsApi.editSettings(settings);
        }
    }

    /**
     * 黑名单分页查询
     * select tui.* from tb_user_info tui,tb_black_list tbl where tui.id = tbl.black_user_id and tbl.user_id = 10005 limit 1,10
     */
    public PageResult<UserInfoVo> findPageBlackList(int page, int pagesize) {
        PageResult<UserInfoVo> vo = new PageResult<>();
        PageResult<UserInfo> pageResult = blackListApi.findPageBlackList(page,pagesize,UserHolder.getUserId());
        BeanUtils.copyProperties(pageResult,vo);
        return vo;
    }

    /**
     * 移除黑名单
     */
    public void deleteBlackUser(Long blackUserId) {
        blackListApi.deleteBlackUser(UserHolder.getUserId(),blackUserId);
    }

    /**
     * 更新陌生人问题
     */
    public void saveOreditQuestions(String content) {
        Long userId = UserHolder.getUserId();
        //1根据用户id查询问题表记录是否存在
        Question question = questionApi.findByUserId(userId);
        //2如果不存在则保存
        if(question == null){
            question = new Question();
            question.setTxt(content);//陌生人问题
            question.setUserId(userId);//用户id
            questionApi.saveQuestions(question);
        }else {
            //3如果存在 则更新
            question.setTxt(content);
            questionApi.editQuestions(question);
        }
    }

    /**
     * 互相喜欢、喜欢、粉丝统计
     */
    public CountsVo counts() {
        Long userId = UserHolder.getUserId();
        Long eachLoveCount = friendApi.findCountById(userId);////互相喜欢
        Long loveCount = userLikeApi.findCountByUserId(userId);//喜欢
        Long fanCount = userLikeApi.findCountByLikeUserId(userId);//喜欢
        CountsVo vo = new CountsVo();
        vo.setFanCount(fanCount);
        vo.setLoveCount(loveCount);
        vo.setEachLoveCount(eachLoveCount);
        return vo;
    }

    /**
     * 互相喜欢、喜欢、粉丝、谁看过我 - 翻页列表
     * 1 互相喜欢
     * 2 我喜欢
     * 3 粉丝
     * 4 谁看过我
     */
    public PageResult<FriendVo> findPageByType(int type, int page, int pagesize) {
        //1.调用服务分页查询RecommendUser表
        Long userId = UserHolder.getUserId();
        PageResult<RecommendUser> pageResult = new PageResult();
        switch (type){
            case 1:
                pageResult = userLikeApi.findPageLikeEachOther(userId,page,pagesize); //互相喜欢
                break;
            case 2:
                pageResult = userLikeApi.findPageOneSideLike(userId,page,pagesize);//我喜欢
                break;
            case 3:
                pageResult = userLikeApi.findPageFens(userId,page,pagesize);//粉丝
                break;
            case 4:
                pageResult = userLikeApi.findPageMyVisitors(userId,page,pagesize);//谁看过我
                break;
            default: break;
        }
        if(pageResult == null || StringUtils.isEmpty(pageResult.getItems())){
            return new PageResult<>(0l, (long)pagesize,0l, (long)page,null);
        }
        //2.根据RecommendUser表中userId查询UserInfo
        List<FriendVo> friendVoList = new ArrayList<>();
        for (RecommendUser recommendUser : pageResult.getItems()) {
            FriendVo friendVo = new FriendVo();
            Long recommendUserId = recommendUser.getUserId();
            UserInfo userInfo = userInfoApi.findUserInfoById(recommendUserId);
            BeanUtils.copyProperties(userInfo,friendVo);//用户信息
            friendVo.setMatchRate(recommendUser.getScore().intValue());//缘分值
            friendVoList.add(friendVo);
        }
        //3.封装Vo返回
        PageResult<FriendVo> friendVoPageResult = new PageResult<>();
        BeanUtils.copyProperties(pageResult,friendVoPageResult);
        friendVoPageResult.setItems(friendVoList);
        return friendVoPageResult;
    }


    /**
     * 粉丝-喜欢
     */
    public void fansLike(Long fansUserId) {
        Long userId = UserHolder.getUserId();
        //1根据当前登录用户id 跟 粉丝id 调用服务删除喜欢表中记录
        userLikeApi.removeFansLike(fansUserId,userId);
        //2根据当前登录用户id 跟 粉丝id 调用服务往好友表保存2条记录
        friendApi.saveContacts(userId,fansUserId);
        //3调用环信云makeFriends成为好友
        huanXinTemplate.makeFriends(userId,fansUserId);
    }
}
