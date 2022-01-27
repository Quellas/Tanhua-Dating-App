package com.tanhua.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 两颗小红心
 * @Date 2021/10/6 17:18
 */
@Data
public class AnswerVo implements Serializable {

    private String questionId;  //试题编号
    private String optionId;   //选项编号
}
