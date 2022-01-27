package com.tanhua.commons;

import com.tanhua.commons.properties.*;
import com.tanhua.commons.templates.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 配置类
 */
@Configuration
@EnableConfigurationProperties({
        SmsProperties.class,
        OssProperties.class,
        FaceProperties.class,
        HuanXinProperties.class,
        HuaWeiUGCProperties.class,
        AudioProperties.class,
        VoiceProperties.class
})
public class CommonsAutoConfiguration {

    /**
     * 短信组件
     * @param smsProperties
     * @return
     */
    @Bean
    public SmsTemplate smsTemplate(SmsProperties smsProperties){
        SmsTemplate smsTemplate = new SmsTemplate(smsProperties);
        smsTemplate.init();
        return smsTemplate;
    }
    /**
     * OSS组件
     */
    @Bean
    public OssTemplate ossTemplate(OssProperties ossProperties){
        return new OssTemplate(ossProperties);
    }

    /**
     * 人脸识别组件
     */
    @Bean
    public FaceTemplate faceTemplate(FaceProperties faceProperties){
        return new FaceTemplate(faceProperties);
    }

    @Bean
    public HuanXinTemplate huanXinTemplate(HuanXinProperties huanXinProperties){
        return new HuanXinTemplate(huanXinProperties);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.build();
    }

    @Bean
    public HuaWeiUGCTemplate huaWeiUGCTemplate(HuaWeiUGCProperties properties) {
        return new HuaWeiUGCTemplate(properties);
    }
    @Bean
    public AudioTemplate audioTemplate(AudioProperties properties) {
        return new AudioTemplate(properties);
    }

    @Bean
    public VoiceTemplate voiceTemplate(VoiceProperties properties) {
        return new VoiceTemplate(properties);
    }

}