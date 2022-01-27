package com.tanhua.server.controller;

import com.tanhua.domain.vo.AnswerVo;
import com.tanhua.domain.vo.QuestionnaireVo;
import com.tanhua.domain.vo.ReportVo;
import com.tanhua.domain.vo.SoulQuestionVo;
import com.tanhua.server.service.SoulService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 测灵魂的控制层
 */
@RestController
@RequestMapping("/testSoul")
public class SoulController {

    @Autowired
    private SoulService soulService;

    /**
     * 测灵魂-问卷列表
     */
    @GetMapping
    public ResponseEntity questionList(){
        List<QuestionnaireVo> questionnaireVos =  soulService.questionList();
        return ResponseEntity.ok(questionnaireVos);
    }

    /**
     * 测灵魂-提交问卷
     */
    @PostMapping
    public ResponseEntity submitQuestionnaire(@RequestBody Map<String,List<AnswerVo>> map){
        List<AnswerVo> answers = map.get("answers");
        String reportId = soulService.submitQuestionnaire(answers);
        return ResponseEntity.ok(reportId);
    }


    /**
     * 查看结果
     * @param resultId
     * @return
     */
    @GetMapping("/report/{id}")
    public ResponseEntity soulReport(@PathVariable("id") String resultId){
        ReportVo reportVo = soulService.soulReport(resultId);
        return ResponseEntity.ok(reportVo);
    }



}
