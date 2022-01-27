package com.tanhua.server.service;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论管理业务逻辑处理类
 */
@Service
@Slf4j
public class CommentsService {

    @Reference
    private CommentApi commentApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 评论列表分页查询
     * movementId:动态id
     */
    public PageResult<CommentVo> findPageCommentById(int page, int pagesize, String publishId) {
        //1.根据动态发布id 分页查询动态评论列表数据
        PageResult<Comment> commentPageResult = commentApi.findPageCommentById(page,pagesize,publishId);
        if(commentPageResult == null || StringUtils.isEmpty(commentPageResult.getItems())){
            return new PageResult<>(0l, (long)pagesize,0l, (long)page,null);
        }
        //2.根据动态评论表中用户id 查询用户基础信息
        //将List<Comment>转为 List<CommentVo>
        List<CommentVo> commentVoList = new ArrayList<>();
        for (Comment comment : commentPageResult.getItems()) {
            CommentVo commentVo = new CommentVo();

            Long userId = comment.getUserId();//评论的用户id
            UserInfo userInfo = userInfoApi.findUserInfoById(userId);
            BeanUtils.copyProperties(userInfo,commentVo);//头像 昵称
            BeanUtils.copyProperties(comment,commentVo);//评论内容 点赞数

            commentVo.setCreateDate(new DateTime(comment.getCreated()).toString("yyyy年MM月dd日HH:mm"));//时间展示

            String key = "comment_like_"+UserHolder.getUserId()+"_"+comment.getId().toHexString();
            log.debug("**********findPageCommentById**********"+key+"*************************");
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key))){
                commentVo.setHasLiked(0);//是否点赞（1是，0否）
            }else{
                commentVo.setHasLiked(1);//是否点赞（1是，0否）
            }
            commentVo.setId(comment.getId().toHexString()); //评论表的主键id
            commentVoList.add(commentVo);
        }

        //3.封装vo返回
        PageResult<CommentVo> voPageResult = new PageResult<>();
        BeanUtils.copyProperties(commentPageResult,voPageResult);
        voPageResult.setItems(commentVoList);
        return voPageResult;
    }

    /**
     * 动态发布评论
     */
    public int saveComment(String publishId, String content) {
        Long userId = UserHolder.getUserId();//当前用户id
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));//动态发布id
        comment.setContent(content);//评论内容
        comment.setCommentType(2);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(userId);//当前对动态评论的用户id
        //1.调用服务 保存喜欢记录（评论表-保存记录 发布表-更新+1 查询评论数量 ）
        int count = commentApi.like(comment);
        //2.返回喜欢数量
        return count;
    }

    /**
     * 动态评论点赞
     * commentId:评论id（评论表的主键id）
     */
    public int like(String commentId) {
        Long userId = UserHolder.getUserId();//当前用户id
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(commentId));//动态评论主键id
        comment.setCommentType(1);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(3);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(userId);//当前对动态评论点赞的用户id
        //1.调用服务 保存点赞记录（评论表-保存记录 评论表-更新+1 查询点赞数量 ）
        int count = commentApi.like(comment);
        //2.将点赞记录写入redis 重点将key定义好即可
        String key = "comment_like_"+userId+"_"+commentId;
        redisTemplate.opsForValue().set(key,"1");//redis有记录说明当前用户对此动态已经点赞了
        //3.返回点赞数量
        return count;
    }

    /**
     * 动态评论取消点赞
     * commentId:评论id
     */
    public int dislike(String commentId) {
        Long userId = UserHolder.getUserId();//当前用户id
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(commentId));//动态评论主键id
        comment.setCommentType(1);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(3);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(userId);//当前对动态评论点赞的用户id
        //1.调用服务 取消点赞记录（评论表-删除记录 评论表-更新-1 查询点赞数量 ）
        int count = commentApi.dislike(comment);
        //2.将点赞记录从redis删除 key必须跟点赞的要一样
        String key = "comment_like_"+userId+"_"+commentId;
        redisTemplate.delete(key);//redis有记录说明当前用户对此动态已经点赞了
        //3.返回点赞数量
        return count;
    }
}
