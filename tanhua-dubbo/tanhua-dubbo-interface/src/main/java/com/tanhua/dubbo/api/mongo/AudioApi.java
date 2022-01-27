package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Audio;
import com.tanhua.domain.vo.AudioVo;

import java.util.List;

import com.tanhua.domain.mongo.Audio;

/**
 * @Author：zhangben
 * @Date: 2021/10/6 10:17
 */
public interface AudioApi {

    /**
     * 保存语音记录到 mongoDB 的taohua_audio表
     * @param audio
     */
    String sendVoice(Audio audio);


    Audio findById(Long blackUserId);

    List<Audio> findAll();

    /**
     * 感觉id查询audio
     * @param message
     * @return
     */
    Audio findAudioById(String message);

    /**
     * 更新audio
     * @param message
     * @param state
     */
    void updateById(String message, Integer state);
}
