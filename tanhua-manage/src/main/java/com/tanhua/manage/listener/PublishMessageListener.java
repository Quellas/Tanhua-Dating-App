package com.tanhua.manage.listener;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.templates.HuaWeiUGCTemplate;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.dubbo.api.mongo.PublishApi;
import com.tanhua.manage.domain.Log;
import com.tanhua.manage.mapper.LogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 监听动态消息类 更改动态发布表状态
 */
@Component
@RocketMQMessageListener(
        topic="tanhua-publish",consumerGroup="tanhuapublish"
)
@Slf4j
public class PublishMessageListener implements RocketMQListener<String> {

    @Reference
    private PublishApi publishApi;

    @Autowired
    private HuaWeiUGCTemplate huaWeiUGCTemplate;
    
    /**
     *更改动态发布表状态
     * @param message
     */
    @Override
    public void onMessage(String message) {
        Integer state=2; //定义默认值已驳回    状态0：待审核，1：已审核，2：已驳回
        log.debug("*****************动态审核开始，数据{}**************************",message);
        //1.调用服务根据动态发布id 查询动态对象
        Publish publish = publishApi.findPublishById(message);
        String textContent = publish.getTextContent();//文本内容
        List<String> medias = publish.getMedias();//图片地址集合
        //2.调用华为云组件审核 参数：动态对象中 文本内容 图片地址
        boolean flag1 = huaWeiUGCTemplate.imageContentCheck(medias.toArray(new String[]{}));
        boolean flag2 = huaWeiUGCTemplate.textContentCheck(textContent);
        if(flag1 && flag2){
            state = 1;
        }
        //3.根据审核结果 更新审核状态
        publishApi.updateById(message,state);
        log.debug("*****************动态审核结束了**************************");
    }
}
