package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 接收用户测试结果
 */

@Data
@Document(collection = "soul_userresult")
public class SoulUserResult implements Serializable {

    @Id
    private ObjectId id; //主键id
    @Indexed
    private Long userId; //用户id

    private String outGoing; //外向纬度
    private String judge; //判断纬度
    private String abstraction;//抽象纬度
    private String reason;  //理性纬度
    private String conclusionType;  //用户类型
    private String level;  //等级
    private Long createDate;
    private Long updateDate;
}
