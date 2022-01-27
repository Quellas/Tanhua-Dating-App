package com.tanhua.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireVo implements Serializable {
    private String id;// 问卷编号
    private String name; // 问卷名称
    private String cover;  // 封面
    private String level; // 级别
    private Integer star; // 星级
    private Integer isLock = 1; // 默认锁定状态
    private String reportId;

    private List<SoulQuestionVo> questions;

}