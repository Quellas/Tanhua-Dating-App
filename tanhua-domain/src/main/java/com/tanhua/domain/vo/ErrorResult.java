package com.tanhua.domain.vo;

import lombok.Builder;
import lombok.Data;

/**
 * vo: value object (数据传递)
 */
@Data
@Builder //链式写法
public class ErrorResult {
    private String errCode; //错误码
    private String errMessage;//错误消息

    public static ErrorResult error(String errCode, String errMessage){

        return ErrorResult.builder().errCode(errCode).errMessage(errMessage).build();
    }

    public static ErrorResult error() {
        return ErrorResult.builder().errCode("999999").errMessage("系统异常，稍后再试").build();
    }

    public static ErrorResult fail() {
        return ErrorResult.builder().errCode("000000").errMessage("发送验证码失败").build();
    }

    public static ErrorResult duplicate() {
        return ErrorResult.builder().errCode("000001").errMessage("上一次发送的验证码还未失效").build();
    }

    public static ErrorResult loginError() {
        return ErrorResult.builder().errCode("000002").errMessage("验证码失效").build();
    }

    public static ErrorResult faceError() {
        return ErrorResult.builder().errCode("000003").errMessage("图片非人像，请重新上传!").build();
    }

    public static ErrorResult validateCodeError() {
        return ErrorResult.builder().errCode("000004").errMessage("验证码不正确").build();
    }

    public static ErrorResult loginTimeout() {
        return ErrorResult.builder().errCode("000005").errMessage("登录超时了").build();
    }

    public static ErrorResult acceptError() {
        return ErrorResult.builder().errCode("000010").errMessage("接受次数超过3次").build();
    }

    public static ErrorResult voiceFrequencyLimit() {
        return ErrorResult.builder().errCode("000006").errMessage("每天只能发三次语音哦,明天再来吧!").build();
    }
    public static ErrorResult voiceLimit() {
        return ErrorResult.builder().errCode("000007").errMessage("RedisService异常").build();
    }


}