package com.tanhua.server.controller;

import com.tanhua.commons.vo.HuanXinUser;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 环信管理控制层
 */
@RestController
@RequestMapping("/huanxin")
@Slf4j
public class HuanXinController {


    /**
     * app发送获取当前用户账号 和 密码
     * app获取账号 密码 后 发起登录请求
     */
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public ResponseEntity huanxinUser() {
        String userId = UserHolder.getUserId().toString();
        HuanXinUser huanXinUser = new HuanXinUser(userId,"123456","老王666");
        log.debug("*****************环信用户登录了***********************");
        return ResponseEntity.ok(huanXinUser);
    }
}
