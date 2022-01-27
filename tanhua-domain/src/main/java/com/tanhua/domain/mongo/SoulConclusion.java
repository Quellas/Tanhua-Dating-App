package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 *
 */
@Data
@Document(collection = "soul_conclusion")
public class SoulConclusion implements Serializable {

    @Id
    private ObjectId id; //主键id
    private String conclusionType;  //用户类型
    private String conclusion;  //鉴定结果
    private String coverUrl;  //封面
}
