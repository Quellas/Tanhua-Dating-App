package com.tanhua.server.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.RecommendUserQueryParam;
import com.tanhua.domain.vo.TodayBestVo;
import com.tanhua.server.service.IMService;
import com.tanhua.server.service.TodayBestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 今日佳人控制层
 */
@RestController
@RequestMapping("/tanhua")
public class TodayBestController {

    @Autowired
    private TodayBestService todayBestService;

    //即时通讯业务逻辑处理层
    @Autowired
    private IMService imService;

    /**
     * 今日佳人
     */
    @RequestMapping(value = "/todayBest",method = RequestMethod.GET)
    public ResponseEntity todayBest(){
        TodayBestVo todayBestVo = todayBestService.todayBest();
        return ResponseEntity.ok(todayBestVo);
    }

    /**
     * 推荐佳人分页列表数据
     */
    @RequestMapping(value = "/recommendation",method = RequestMethod.GET)
    public ResponseEntity recommendation(RecommendUserQueryParam recommendUserQueryParam){
        PageResult<TodayBestVo> pageResult = todayBestService.recommendation(recommendUserQueryParam);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 查看用户详情
     * personUserId:佳人的用户id
     */
    @RequestMapping(value = "/{id}/personalInfo",method = RequestMethod.GET)
    public ResponseEntity personalInfo(@PathVariable("id") Long personUserId){
        TodayBestVo todayBestVo = todayBestService.personalInfo(personUserId);
        return ResponseEntity.ok(todayBestVo);
    }



    /**
     * 查询陌生人问题
     */
    @RequestMapping(value = "/strangerQuestions",method = RequestMethod.GET)
    public ResponseEntity strangerQuestions(Long userId){
        String content = todayBestService.strangerQuestions(userId);
        return ResponseEntity.ok(content);
    }


    /**
     * 回复陌生人问题
     */
    @RequestMapping(value = "/strangerQuestions",method = RequestMethod.POST)
    public ResponseEntity replyStrangerQuestions(@RequestBody Map params){
        Long personUserId = Long.parseLong(params.get("userId").toString());//佳人用户id
        String reply = (String)params.get("reply");
        imService.replyStrangerQuestions(personUserId,reply);
        return ResponseEntity.ok(null);
    }
}
