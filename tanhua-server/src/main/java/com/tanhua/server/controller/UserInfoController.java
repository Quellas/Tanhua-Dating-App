package com.tanhua.server.controller;

import com.tanhua.domain.vo.*;
import com.tanhua.server.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户基础信息管理控制层
 */
@RestController
@RequestMapping("/users")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 查询用户基础信息
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity findUserInfoById(Long userID, Long huanxinID){
        UserInfoVo userInfoVo = userInfoService.findUserInfoById(userID,huanxinID);
        return ResponseEntity.ok(userInfoVo);
    }


    /**
     * 更新用户基础信息
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity editUserInfoById(@RequestBody UserInfoVo userInfoVo){
        userInfoService.editUserInfoById(userInfoVo);
        return ResponseEntity.ok(null);
    }

    /**
     * 通知设置查询
     */
    @RequestMapping(value = "/settings",method = RequestMethod.GET)
    public ResponseEntity findSettings(){
        SettingsVo settingsVo = userInfoService.findSettings();
        return ResponseEntity.ok(settingsVo);
    }


    /**
     * 通知设置 - 保存
     */
    @RequestMapping(value = "/notifications/setting",method = RequestMethod.POST)
    public ResponseEntity saveOreditSettings(@RequestBody Map<String,Boolean> map){
        Boolean likeNotification = map.get("likeNotification");
        Boolean pinglunNotification = map.get("pinglunNotification");
        Boolean gonggaoNotification = map.get("gonggaoNotification");
        userInfoService.saveOreditSettings(likeNotification,pinglunNotification,gonggaoNotification);
        return ResponseEntity.ok(null);
    }


    /**
     * 黑名单分页查询
     */
    @RequestMapping(value = "/blacklist",method = RequestMethod.GET)
    public ResponseEntity findPageBlackList(@RequestParam(defaultValue = "1") int page,@RequestParam(defaultValue = "10") int pagesize){
        PageResult<UserInfoVo> pageResult = userInfoService.findPageBlackList(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 移除黑名单
     */
    @RequestMapping(value = "/blacklist/{uid}",method = RequestMethod.DELETE)
    public ResponseEntity deleteBlackUser(@PathVariable("uid") Long blackUserId){
        userInfoService.deleteBlackUser(blackUserId);
        return ResponseEntity.ok(null);
    }


    /**
     * 更新陌生人问题
     */
    @RequestMapping(value = "/questions",method = RequestMethod.POST)
    public ResponseEntity saveOreditQuestions(@RequestBody Map<String,String> params){
        String content = params.get("content");//接收修改陌生人问题
        userInfoService.saveOreditQuestions(content);
        return ResponseEntity.ok(null);
    }

    /**
     * 互相喜欢、喜欢、粉丝统计
     */
    @RequestMapping(value = "/counts",method = RequestMethod.GET)
    public ResponseEntity counts(){
        CountsVo countsVo = userInfoService.counts();
        return ResponseEntity.ok(countsVo);
    }


    /**
     * 互相喜欢、喜欢、粉丝、谁看过我 - 翻页列表
     */
    @RequestMapping(value = "/friends/{type}",method = RequestMethod.GET)
    public ResponseEntity findPageByType(@PathVariable("type") int type,@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize){
        PageResult<FriendVo> pageResult = userInfoService.findPageByType(type,page,pagesize);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 粉丝-喜欢
     */
    @RequestMapping(value = "/fans/{uid}",method = RequestMethod.POST)
    public ResponseEntity fansLike(@PathVariable("uid") Long fansUserId){
        userInfoService.fansLike(fansUserId);
        return ResponseEntity.ok(null);
    }

}
