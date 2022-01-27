package com.tanhua.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author：zhangben
 * @Date: 2021/10/6 10:40
 */
@Data
public class AudioVo implements Serializable {
    private Integer id; // 用户id
    private String avatar;
    private String nickname;
    private String gender;
    private Integer age;
    private String soundUrl; // 语音地址
    private Integer remainingTimes; // 剩余次数
}
