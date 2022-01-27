package com.tanhua.server.interceptor;

import com.tanhua.domain.db.User;
import com.tanhua.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 统一处理token
 * 基于拦截器+ThreadLocal
 */
@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    /**
     * 进入controller之前拦截
     * 判断用户是否登录，如果没有登录则直接返回。如果已经登录，则将用户信息存入ThreadLocal
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("进入拦截器了。。。。");
        //1获取请求头中token
        String headToken = request.getHeader("Authorization");
        //2.根据请求头中token获取用户对象
        User user = userService.getUserByToken(headToken);
        //3.如果user对象为null,直接返回false response设置状态码：权限不足401 没有权限需要登录
        if(user == null){
            response.setStatus(401);
            return false;
        }
        //4.需要将user对象存入ThreadLocal 后续业务代码中需要使用
        UserHolder.setUser(user);
        return true;
    }
}
