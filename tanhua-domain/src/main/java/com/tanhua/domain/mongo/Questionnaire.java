package com.tanhua.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "soul_questionnaire")
public class Questionnaire implements Serializable {
    @Id
    private ObjectId id;// 主键
    @Indexed
    private Integer questionnaireId;// 问卷编号 优化:可用主键id代替 todo
    private String name; // 问卷名称
    private String coverUrl;  // 封面 优化:可改名字跟vo相同 todo
    private String level; // 级别
    private Integer star; // 星级
    private Integer isLock; // 锁定状态

}