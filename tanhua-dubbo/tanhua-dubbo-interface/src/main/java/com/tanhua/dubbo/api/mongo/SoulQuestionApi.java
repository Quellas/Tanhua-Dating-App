package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.SoulQuestion;

import java.util.List;

/**
 * 测灵魂的服务接口
 */
public interface SoulQuestionApi {
    /**
     * 根据问卷id查询问题列表
     * @param questionnaireId
     * @return
     */
    List<SoulQuestion> findQuestions(Integer questionnaireId);
}
