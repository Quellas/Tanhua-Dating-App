package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.Question;

/**
 * 问题服务接口
 */
public interface QuestionApi {
    /**
     * 获取陌生人问题
     * @param userId
     * @return
     */
    Question findByUserId(Long userId);

    /**
     *保存问题表
     * @param question
     */
    void saveQuestions(Question question);

    /**
     * 更新问题表
     * @param question
     */
    void editQuestions(Question question);
}
