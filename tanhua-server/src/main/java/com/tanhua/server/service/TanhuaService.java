package com.tanhua.server.service;


import com.tanhua.domain.mongo.AudioRecommendRecord;
import com.tanhua.dubbo.api.db.BlackListApi;
import com.tanhua.dubbo.api.mongo.AudioRecommendRecordApi;
import com.tanhua.dubbo.api.mongo.UserLikeApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TanhuaService {

    @Reference
    private BlackListApi blackListApi;

    @Reference
    private AudioRecommendRecordApi audioRecommendRecordApi;

    @Reference
    private UserLikeApi userLikeApi;
    /*
     * 探花-喜欢
     * publishId:id
     * */
    public void love(Long userId) {
        Long toUserId = UserHolder.getUserId();//当前用户id
        AudioRecommendRecord audioRecommendRecord = new AudioRecommendRecord();
        audioRecommendRecord.setStatus("1");
        audioRecommendRecord.setUserId(userId);
        audioRecommendRecord.setToUserId(toUserId);
        audioRecommendRecordApi.updateById(audioRecommendRecord);

        userLikeApi.insertLoveUserId(audioRecommendRecord);
    }

    /*
     * 探花-取消喜欢
     * publishId:id
     * */
    public void unlove(Long userId) {
        Long toUserId = UserHolder.getUserId();//当前用户id
        AudioRecommendRecord audioRecommendRecord = new AudioRecommendRecord();
        audioRecommendRecord.setStatus("0");
        audioRecommendRecord.setUserId(userId);
        audioRecommendRecord.setToUserId(toUserId);
        audioRecommendRecordApi.updateById(audioRecommendRecord);


        blackListApi.insertUnloveUserId(audioRecommendRecord);

    }
}
