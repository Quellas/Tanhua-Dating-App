package com.tanhua.manage.listener;

import com.alibaba.fastjson.JSON;
import com.tanhua.manage.domain.Log;
import com.tanhua.manage.mapper.LogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 监听日志消息类 往日志表写日志记录
 */
@Component
@RocketMQMessageListener(
        topic="tanhua-logs",consumerGroup="tanhualogs"
)
@Slf4j
public class LogMessageListener implements RocketMQListener<String> {

    @Autowired
    private LogMapper logMapper;

    /**
     * 从消息队列监听到日志消息
     * 往日志表写入数据
     * @param message
     */
    @Override
    public void onMessage(String message) {
        log.debug("*****************日志数据{}**************************",message);
        //message就是消息队列中的数据
        Map<String,String> map = JSON.parseObject(message, Map.class);
        String type = map.get("type");
        String userId = map.get("userId");
        String logTime = map.get("logTime");
        Log tanhuaLog = new Log();
        tanhuaLog.setUserId(Long.parseLong(userId));//操作用户id
        tanhuaLog.setLogTime(logTime);//日志操作时间
        tanhuaLog.setPlace("深圳");
        tanhuaLog.setEquipment("iphone13");//操作设备
        tanhuaLog.setType(type);//日志操作类型
        tanhuaLog.setCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));//日志创建时间
        logMapper.insert(tanhuaLog);
        log.debug("*****************日志保存成功了**************************");
    }
}
