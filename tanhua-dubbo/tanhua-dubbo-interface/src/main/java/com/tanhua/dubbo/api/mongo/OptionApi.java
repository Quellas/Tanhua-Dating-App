package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Option;

import java.util.List;

public interface OptionApi {
    /**
     * 根据问题id查询选项列表
     * @param questionId
     * @return
     */
    List<Option> findOptions(Long questionId);

    /**
     * 查询问题选项
     * @param optionId
     * @return
     */
    Option findOption(String optionId);
}
