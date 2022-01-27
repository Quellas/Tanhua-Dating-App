package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.AudioRecommendRecord;

import java.util.List;

/**
 * @Author：zhangben
 * @Date: 2021/10/6 19:51
 */
public interface AudioRecommendRecordApi {
    void insert(AudioRecommendRecord audioRecommendRecord);

    void updateById(AudioRecommendRecord audioRecommendRecord);

    /**
     * 寻找推荐表里面的所有的数据
     * @return
     */
    List<AudioRecommendRecord> findAll( Long userId,Long audioUserId);

    Boolean findById(Long userId, Long audioUserId);
}
