package com.tanhua.server.controller;

import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.domain.vo.VisitorVo;
import com.tanhua.server.service.MovementsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 消费者-圈子功能控制层
 */
@RestController
@RequestMapping("/movements")
public class MovementsController {

    @Autowired
    private MovementsService movementsService;

    /**
     * 发布动态
     * imageContent:多张图片
     * publishVo：文本数据
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity savePublish(PublishVo publishVo, MultipartFile[] imageContent) throws IOException {
        movementsService.savePublish(publishVo,imageContent);
        return ResponseEntity.ok(null);
    }



    /**
     * 好友动态分页查询
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity findPagePublishByFriend(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize){
        PageResult<MomentVo> pageResult = movementsService.findPagePublishByFriend(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 推荐动态分页查询（陌生人动态数据）
     */
    @RequestMapping(value = "/recommend",method = RequestMethod.GET)
    public ResponseEntity findPagePublishByRecommend(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize){
        PageResult<MomentVo> pageResult = movementsService.findPagePublishByRecommend(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 用户动态（我的动态）  userId:用户id
     */
    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public ResponseEntity findPagePublishByUserId(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize,Long userId){
        PageResult<MomentVo> pageResult = movementsService.findPagePublishByUserId(page,pagesize,userId);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 动态点赞
     * publishId:动态发布id
     */
    @RequestMapping(value = "/{id}/like",method = RequestMethod.GET)
    public ResponseEntity like(@PathVariable("id") String publishId) {
        int count = movementsService.like(publishId);
        return ResponseEntity.ok(count);
    }

    /**
     * 动态取消点赞
     * publishId:动态发布id
     */
    @RequestMapping(value = "/{id}/dislike",method = RequestMethod.GET)
    public ResponseEntity dislike(@PathVariable("id") String publishId) {
        int count = movementsService.dislike(publishId);
        return ResponseEntity.ok(count);
    }


    /**
     * 动态喜欢
     * publishId:动态发布id
     */
    @RequestMapping(value = "/{id}/love",method = RequestMethod.GET)
    public ResponseEntity love(@PathVariable("id") String publishId) {
        int count = movementsService.love(publishId);
        return ResponseEntity.ok(count);
    }

    /**
     * 动态取消喜欢
     * publishId:动态发布id
     */
    @RequestMapping(value = "/{id}/unlove",method = RequestMethod.GET)
    public ResponseEntity unlove(@PathVariable("id") String publishId) {
        int count = movementsService.unlove(publishId);
        return ResponseEntity.ok(count);
    }


    /**
     * 单条动态
     * publishId:动态发布id
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public ResponseEntity findPublishById(@PathVariable("id") String publishId) {
        MomentVo momentVo = movementsService.findPublishById(publishId);
        return ResponseEntity.ok(momentVo);
    }

    /**
     * 谁看过我
     */
    @RequestMapping(value = "/visitors",method = RequestMethod.GET)
    public ResponseEntity findVisitors() {
        List<VisitorVo> list = movementsService.findVisitors();
        return ResponseEntity.ok(list);
    }
}
