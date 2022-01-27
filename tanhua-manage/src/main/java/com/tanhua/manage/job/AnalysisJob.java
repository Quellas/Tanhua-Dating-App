package com.tanhua.manage.job;

import com.tanhua.manage.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 定时任务处理
 */
@Component
@Slf4j
public class AnalysisJob {

    @Autowired
    private AnalysisService analysisService;

    /**
     * 定时5秒钟执行一次
     */
    /*@Scheduled(cron = "0/5 * * ? * * ")
    public void job(){
        log.debug("********"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }*/

    @Scheduled(cron = "0/10 * * ? * * ")
    public void analysis(){
        log.debug("*******开始执行定时任务*******************************");
        analysisService.analysis();
        log.debug("*******结束执行定时任务*******************************");
    }
}
