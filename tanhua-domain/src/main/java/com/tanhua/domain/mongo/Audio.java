package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @Author：zhangben
 * @Date: 2021/10/6 9:37
 */
@Data
@Document(collection = "taohua_audio")
public class Audio implements Serializable {
    private ObjectId id; //主键id
    @Indexed
    private Long userId; //用户id
    private String audioUrl; //音频的储存地址
    private String createTime;//音频的创建时间
    private Integer state=0;// 状态0：待审核，1：已审核，2：已驳回
}
