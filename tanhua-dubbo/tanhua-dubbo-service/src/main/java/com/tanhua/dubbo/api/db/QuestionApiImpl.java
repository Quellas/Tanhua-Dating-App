package com.tanhua.dubbo.api.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.Settings;
import com.tanhua.dubbo.mapper.QuestionMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 问题服务接口实现类
 */
@Service
public class QuestionApiImpl implements QuestionApi {

    @Autowired
    private QuestionMapper questionMapper;


    @Override
    public Question findByUserId(Long userId) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return questionMapper.selectOne(queryWrapper);
    }
    /**
     *保存问题表
     * @param question
     */
    @Override
    public void saveQuestions(Question question) {
        questionMapper.insert(question);
    }
    /**
     * 更新问题表
     * @param question
     */
    @Override
    public void editQuestions(Question question) {
        questionMapper.updateById(question);
    }
}
