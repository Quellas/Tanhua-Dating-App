package com.tanhua.server.controller;

import com.tanhua.domain.db.User;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 消费者-登录控制层
 */
@RestController
@RequestMapping("/user")
public class LoginController {

    //调用业务逻辑处理层（在业务逻辑处理中 可以调用一个或多个服务 完成业务功能）
    @Autowired
    private UserService userService;


    /**
     * 完成用户保存返回用户id
     */
    @RequestMapping(value = "/saveUser", method = RequestMethod.POST)
    public ResponseEntity saveUser(@RequestBody User user) {
        Long userId = userService.saveUser(user);
        return ResponseEntity.ok(userId);
    }


    /**
     * 根据手机号码查询用户功能
     */
    @RequestMapping(value = "/findByMobile", method = RequestMethod.GET)
    public ResponseEntity findByMobile(String mobile) {
        User user = userService.findByMobile(mobile);
        return ResponseEntity.ok(user);
    }

    /**
     * 获取验证码
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity sendCode(@RequestBody Map<String, String> map) {
        String phone = map.get("phone");//获取前端输入的手机号
        userService.sendCode(phone);
        return ResponseEntity.ok(null);
    }


    /**
     * 登录注册
     */
    @RequestMapping(value = "/loginVerification", method = RequestMethod.POST)
    public ResponseEntity loginVerification(@RequestBody Map<String, String> map) {
        String phone = map.get("phone");//获取前端输入的手机号
        String verificationCode = map.get("verificationCode");//获取前端输入的验证码
        Map rsMap = userService.loginVerification(phone,verificationCode);
        //rsMap token  isNew:true新注册用户 false老用户
        return ResponseEntity.ok(rsMap);
    }


    /**
     * 完善个人信息-保存基础信息
     */
    @RequestMapping(value = "/loginReginfo", method = RequestMethod.POST)
    public ResponseEntity loginReginfo(@RequestBody UserInfoVo userInfoVo) {
        userService.loginReginfo(userInfoVo);
        return ResponseEntity.ok(null);
    }



    /**
     * 完善个人信息-更新头像
     * 方式一：@RequestParam("headPhoto")MultipartFile abc
     * 方式二：MultipartFile headPhoto
     */
    @RequestMapping(value = "/loginReginfo/head", method = RequestMethod.POST)
    public ResponseEntity loginReginfoHead(MultipartFile headPhoto) throws IOException {
        userService.loginReginfoHead(headPhoto);
        return ResponseEntity.ok(null);
    }


}
