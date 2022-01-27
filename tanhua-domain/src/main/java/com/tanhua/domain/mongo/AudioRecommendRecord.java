package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @Author：zhangben
 * @Date: 2021/10/6 9:45
 */
@Data
@Document(collection = "taohua_recommend_record")
public class AudioRecommendRecord implements Serializable {
    private ObjectId id; //主键id
    @Indexed
    private Long userId; //用户id
    private Long toUserId; //登录用户id
    private String recommendRecordTime;// 用户推荐的时间
    private String status;// 标记用户是否喜欢    1:喜欢   0:不喜欢
    private String audioId; // 音频的主键id值
}
