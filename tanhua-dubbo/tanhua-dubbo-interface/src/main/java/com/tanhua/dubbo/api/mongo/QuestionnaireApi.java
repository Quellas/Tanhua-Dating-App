package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Questionnaire;

import java.util.List;

public interface QuestionnaireApi {

    /**
     * 查询问卷列表
     * @return
     */
    List<Questionnaire> findQuestionnaires();

    /**
     * 查询问卷等级
     * @param questionId
     * @return
     */
    Questionnaire findLevel(String questionId);
}