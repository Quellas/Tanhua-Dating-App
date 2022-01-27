package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.PageResult;

/**
 * 评论服务接口
 */
public interface CommentApi {
    /**
     * 动态点赞
     * @param comment
     * @return
     */
    int like(Comment comment);

    /**
     * 动态取消点赞
     * @param comment
     * @return
     */
    int dislike(Comment comment);

    /**
     * 评论列表分页查询
     * movementId:动态id
     */
    PageResult<Comment> findPageCommentById(int page, int pagesize, String publishId);

    /**
     * 根据发布动态用户id 和 评论类型 分页查询动态评论表
     * @param userId 根据发布动态用户id
     * @param type  评论类型，1-点赞，2-评论，3-喜欢
     * @param page 1
     * @param pagesize 10
     * @return
     */
    PageResult<Comment> findPageByType(Long userId, int type, int page, int pagesize);
}
