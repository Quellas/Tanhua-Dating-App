package com.tanhua.manage.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.tanhua.manage.domain.Admin;
import com.tanhua.manage.interceptor.AdminHolder;
import com.tanhua.manage.service.AdminService;
import com.tanhua.manage.vo.AdminVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/system/users")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 后台登陆时 图片验证码 生成
     */
    @GetMapping("/verification")
    public void showValidateCodePic(String uuid,HttpServletRequest req, HttpServletResponse res){
        res.setDateHeader("Expires",0);
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        res.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        res.setHeader("Pragma", "no-cache");
        res.setContentType("image/jpeg");
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(299, 97);
        String code = lineCaptcha.getCode();
        log.debug("uuid={},code={}",uuid,code);
        adminService.saveCode(uuid,code);
        try {
            lineCaptcha.write(res.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 登录
     */
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ResponseEntity login(@RequestBody Map<String,String> params){
        String username =  params.get("username");//账号
        String password =  params.get("password");//密码
        String verificationCode =  params.get("verificationCode");//验证码
        String uuid =  params.get("uuid");//uuid
        String token = adminService.login(username,password,verificationCode,uuid);
        Map map = new HashMap();
        map.put("token",token);
        return ResponseEntity.ok(map);
    }

    /**
     * 登录后获取用户信息
     */
    @RequestMapping(value = "/profile",method = RequestMethod.POST)
    public ResponseEntity getAdminInfo(){
        Admin admin = AdminHolder.getAdmin();
        AdminVo adminVo = new AdminVo();
        BeanUtils.copyProperties(admin,adminVo);
        return ResponseEntity.ok(adminVo);
    }



    /**
     * 退出
     */
    @RequestMapping(value = "/logout",method = RequestMethod.POST)
    public ResponseEntity logout(@RequestHeader("Authorization") String headToken){
        //Bearer eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwiaWF0IjoxNjMyNjI0NjcyLCJ1c2VybmFtZSI6ImFkbWluIn0.7w6pCZ1h3J3aDr38dCHCv2zOfRnZ06ui11i2KwwXZ84
        adminService.logout(headToken.replaceAll("Bearer ",""));
        return ResponseEntity.ok(null);
    }
}