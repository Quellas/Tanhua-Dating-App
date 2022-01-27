package com.tanhua.server.controller;

import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.CommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论管理控制层
 */
@RestController
@RequestMapping("/comments")
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    /**
     * 评论列表分页查询
     * movementId:动态id
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity findPageCommentById(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize, String movementId){
        PageResult<CommentVo> pageResult = commentsService.findPageCommentById(page,pagesize,movementId);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 动态发布评论
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity saveComment(@RequestBody Map<String,String> params) {
        String publishId = params.get("movementId");//动态发布id
        String content = params.get("comment");//评论内容
        int count = commentsService.saveComment(publishId,content);
        //动态评论数量
        return ResponseEntity.ok(count);
    }


    /**
     * 动态评论点赞
     * commentId:评论id（评论表的主键id）
     */
    @RequestMapping(value = "/{id}/like",method = RequestMethod.GET)
    public ResponseEntity like(@PathVariable("id") String commentId) {
        int count = commentsService.like(commentId);
        return ResponseEntity.ok(count);
    }

    /**
     * 动态评论取消点赞
     * commentId:评论id
     */
    @RequestMapping(value = "/{id}/dislike",method = RequestMethod.GET)
    public ResponseEntity dislike(@PathVariable("id") String commentId) {
        int count = commentsService.dislike(commentId);
        return ResponseEntity.ok(count);
    }
}
