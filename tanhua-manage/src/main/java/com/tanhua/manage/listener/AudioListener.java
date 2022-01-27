package com.tanhua.manage.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.commons.templates.AudioTemplate;
import com.tanhua.commons.templates.HuaWeiUGCTemplate;
import com.tanhua.commons.templates.VoiceTemplate;
import com.tanhua.domain.mongo.Audio;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.dubbo.api.mongo.AudioApi;
import com.tanhua.dubbo.api.mongo.PublishApi;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 监听动态消息类 更改动态发布表状态
 */
@Component
@RocketMQMessageListener(
        topic="tanhua-audio",consumerGroup="tanhuaaudio"
)
@Slf4j
public class AudioListener implements RocketMQListener<String> {

    @Reference
    private AudioApi audioApi;

    @Autowired
    private AudioTemplate audioTemplate;

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    
    /**
     *更改语音发布表状态
     * @param message
     */
    //@SneakyThrows
    @SneakyThrows
    @Override
    public void onMessage(String message) {
        Integer state=2; //定义默认值已驳回    状态0：待审核，1：已审核，2：已驳回
        log.debug("*****************动态审核开始，数据{}**************************",message);
        //1.调用服务根据音频id 查询动态对象
        //Map<String,Object> map = JSON.parseObject(message, Map.class);
        //byte[] fileBytes = (byte[]) map.get("fileBytes");
        //String audioSuffix = (String) map.get("audioSuffix");
        //String audioId =(String) map.get("audioId");
        Audio audio = audioApi.findAudioById(message);
        //2.调用语音检测云组件审核 参数：音频地址
        if (false) {
            boolean flag = audioTemplate.audioExamine(audio.getAudioUrl());
            if (flag) {
                state = 1;
            }
        }
        state = 1;  //默认审核通过
        //3.根据审核结果 更新审核状态
        audioApi.updateById(message,state);
        log.debug("*****************动态审核结束了**************************");
    }
}
