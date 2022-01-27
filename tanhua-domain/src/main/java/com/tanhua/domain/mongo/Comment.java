package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 评论表
 */
@Data
@Document(collection = "quanzi_comment")
public class Comment implements Serializable {

    private ObjectId id;

    private ObjectId publishId;    //针对动态:发布id  针对评论:评论id
    private Integer commentType;   //评论类型，1-点赞，2-评论，3-喜欢
    private Integer pubType;       //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
    private String content;        //评论内容
    private Long userId;           //评论人
    private Integer likeCount = 0; //点赞数
    private Long created; //发表时间
    //根据动态发布的用户id 评论类型 分页查询
    private Long publishUserId; //被评论人ID
    //动态选择更新的字段
    public String getCol() {
        return this.commentType == 1 ? "likeCount" : commentType==2? "commentCount"
            : "loveCount";
    }
}