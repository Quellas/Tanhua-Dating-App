package com.tanhua.server.controller;

import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.IMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息模块控制层
 */
@RestController
@RequestMapping("/messages")
public class ContactsController {

    @Autowired
    private IMService imService;

    /**
     * 联系人添加
     */
    @RequestMapping(value = "/contacts",method = RequestMethod.POST)
    public ResponseEntity saveContacts(@RequestBody Map params){
        Long personUserId = Long.parseLong(params.get("userId").toString());//陌生人的用户id
        imService.saveContacts(personUserId);
        return ResponseEntity.ok(null);
    }


    /**
     * 联系人列表分页列表数据
     */
    @RequestMapping(value = "/contacts",method = RequestMethod.GET)
    public ResponseEntity findPageByFriend(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize,String keyword){
        PageResult<ContactVo> pageResult = imService.findPageByFriend(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 喜欢列表分页列表数据
     *  //评论类型，1-点赞，2-评论，3-喜欢
     */
    @RequestMapping(value = "/loves",method = RequestMethod.GET)
    public ResponseEntity loves(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize){
        PageResult<MessageVo> pageResult = imService.messageCommentList(3,page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 点赞列表分页列表数据
     * //评论类型，1-点赞，2-评论，3-喜欢
     */
    @RequestMapping(value = "/likes",method = RequestMethod.GET)
    public ResponseEntity likes(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize){
        PageResult<MessageVo> pageResult = imService.messageCommentList(1,page,pagesize);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 评论列表分页列表数据
     * //评论类型，1-点赞，2-评论，3-喜欢
     */
    @RequestMapping(value = "/comments",method = RequestMethod.GET)
    public ResponseEntity comments(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize){
        PageResult<MessageVo> pageResult = imService.messageCommentList(2,page,pagesize);
        return ResponseEntity.ok(pageResult);
    }
}
