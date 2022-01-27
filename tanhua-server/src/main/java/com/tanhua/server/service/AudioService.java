package com.tanhua.server.service;


import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.domain.db.BlackList;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Audio;
import com.tanhua.domain.mongo.AudioRecommendRecord;
import com.tanhua.domain.vo.AudioVo;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.dubbo.api.db.BlackListApi;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.AudioApi;
import com.tanhua.dubbo.api.mongo.AudioRecommendRecordApi;
import com.tanhua.server.interceptor.UserHolder;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * @Author：zhangben
 * @Date: 2021/10/6 10:11
 */

/**
 * 语言业务逻辑处理层
 */
@Service
@Slf4j
public class AudioService {

    @Reference
    private BlackListApi blackListApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private AudioApi audioApi;

    @Reference
    private AudioRecommendRecordApi audioRecommendRecordApi;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 接受语音
     *
     * @return
     */
    public AudioVo acceptAudio() {
        // 1.获取当前用户的userID
        Long userId = UserHolder.getUserId();

        // 2.通过个人userID来查询用户的个人信息
        UserInfo userInfo = userInfoApi.findUserInfoById(userId);
        String gender = userInfo.getGender();// 当前用户的性别

        // 3.这里需要对性别来进行判断
        String aimGender;
        if (gender.equals("woman")) {
            aimGender = "man";
        } else {
            aimGender = "woman";
        }

        // 4.找到所有的黑明单中的人
        List<Audio> listAudio = new ArrayList<>();
        List<BlackList> list = blackListApi.findByUserId(userId);
        if (!CollectionUtils.isEmpty(list)) {
            for (BlackList blackList : list) {
                if (blackList != null) {
                    Long blackUserId = blackList.getBlackUserId();
                    Audio blackUserIdAudio = audioApi.findById(blackUserId);
                    if (blackUserIdAudio != null) {
                        listAudio.add(blackUserIdAudio);
                    }
                }
            }
        }

        // 5.找到所有的异性的人 遍历所有的audio表 选择里面的异性对象 异性中不包括自己 排除自己的和黑名单的集合
        List<Audio> audiolist02 = new ArrayList<>();
        List<Audio> audios = audioApi.findAll();
        if (!CollectionUtils.isEmpty(audios)) {
            for (Audio audio : audios) {
                // 对于结果来进行遍历
                Long audioUserId = audio.getUserId();

                /*// 5.5 audiolist02遍历集合遍历推荐记录表中所有的数据 将表中数据来进行遍历 找到已经推进过的数据
                List<String> stringList = new ArrayList<>();
                List<AudioRecommendRecord> audioRecommendRecordList = audioRecommendRecordApi.findAll(userId,audioUserId);
                if (!CollectionUtils.isEmpty(audioRecommendRecordList)) {
                    for (AudioRecommendRecord audioRecommendRecord : audioRecommendRecordList) {
                        // 这里是
                        String audioId = audioRecommendRecord.getAudioId();
                        if (audioId != null) {
                            stringList.add(audioId);
                        }
                    }
                }
                // 防止出现空指针异常
                if(stringList==null){
                    stringList.add("mm");
                }*/
                Boolean flag = audioRecommendRecordApi.findById(userId, audioUserId);
                if(!flag){
                    UserInfo userinfoAudio = userInfoApi.findUserInfoById(audioUserId);
                    // 拿到结果之后来找到性别
                    String audioGender = userinfoAudio.getGender();
                    if (audioGender.equals(aimGender)) {
                        // 这里是拿到符合性别的人 不包含在黑名单中的人 且推荐表里面没有的数据
                        if (!listAudio.contains(audio)) {
                            audiolist02.add(audio);
                        }
                    }
                }
            }
        }

        // 5.5假如audiolist02为空 给定默认值
        if (CollectionUtils.isEmpty(audiolist02)) {
            if (aimGender.equals("man")) {
                Audio audio = new Audio();
                audio.setId(new ObjectId());
                audio.setUserId((long) 3);
                audio.setAudioUrl("http://10.10.20.160:8888/group1/M00/00/00/CgoUoGFdYNyAeaDEAAAaKMiWeBE53..m4a");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = dateFormat.format(new Date());
                audio.setCreateTime(time);
                audiolist02.add(audio);
            } else {
                Audio audio = new Audio();
                audio.setId(new ObjectId());
                audio.setUserId((long) 4);
                audio.setAudioUrl("http://10.10.20.160:8888/group1/M00/00/00/CgoUoGFdYNyAeaDEAAAaKMiWeBE53..m4a");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = dateFormat.format(new Date());
                audio.setCreateTime(time);
            }
        }


        // 6.随机获得一个list集合中的数据 将数据封装到AudioVo中
        AudioVo audioVo = new AudioVo();
        Random random = new Random();
        Audio audio = new Audio();
        if (!CollectionUtils.isEmpty(audiolist02)) {
            audio = audiolist02.get(random.nextInt(audiolist02.size()));
            Long audioUserId = audio.getUserId();
            UserInfo userInfoById = userInfoApi.findUserInfoById(audioUserId);
            BeanUtils.copyProperties(userInfoById, audioVo);
            audioVo.setId(audioUserId.intValue());
            audioVo.setSoundUrl(audio.getAudioUrl());
        }

        // 7.调用redis实现对于数据存放
        Long timeOut = getNowToNextDaySeconds();
        String key = userId + "_acceptCount";
        // 7.1这里需要对数据进行判断
        String value = redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(value)) {
            int intvalue = Integer.parseInt(value);
            // 获取存入redis中的次数 如果超过三次提示用户
            if (intvalue >= 3) {
                // 抛出接受语音次数超过的异常
                throw new TanHuaException(ErrorResult.acceptError());
            }
            // 次数加一
            redisTemplate.opsForValue().set(key, String.valueOf(intvalue + 1), timeOut, TimeUnit.SECONDS);
        } else {
            // 保存到redis中 次数设置为一
            redisTemplate.opsForValue().set(key, "1", timeOut, TimeUnit.SECONDS);
        }

        // 7.2获取redis中的数据 将redis中的数据写到mongodb数据库中
        String UsedTimes = redisTemplate.opsForValue().get(key);
        int remainingTimes = 3 - Integer.parseInt(UsedTimes);
        // 7.3进行数据类型转换
        audioVo.setRemainingTimes(remainingTimes);

        // 8.向mongodb中插入推荐数据
        AudioRecommendRecord audioRecommendRecord = new AudioRecommendRecord();
        audioRecommendRecord.setId(new ObjectId());
        audioRecommendRecord.setToUserId(userId);
        if (audio != null) {
            audioRecommendRecord.setUserId(audio.getUserId());
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String recommendRecordTime = dateFormat.format(new Date());
        audioRecommendRecord.setRecommendRecordTime(recommendRecordTime);
        audioRecommendRecord.setAudioId(audio.getId().toHexString());// 这里是设置音频的主键的id值
        // 8.1调用接口保存到数据集中
        audioRecommendRecordApi.insert(audioRecommendRecord);

        // 9.得到返回值
        return audioVo;
    }


    /**
     * 发送语音
     *
     * @param soundFile
     */
    public void sendVoice(MultipartFile soundFile) throws IOException {
        //1.调用fastdfs 上传语音并获取语音地址
        String audioFilename = soundFile.getOriginalFilename();
        //1.1获取文件类型 例:mp3
        String audioSuffix = audioFilename.substring(audioFilename.lastIndexOf(".") + 1);
        byte[] fileBytes = soundFile.getBytes();
        //1.2 调用方法上传到fastdfs  方法传入参数:数据流  文件大小 文件类型
        StorePath storePath = storageClient.uploadFile(soundFile.getInputStream(), soundFile.getSize(), audioSuffix, null);
        //1.3获取语音路径
        String audioUrl = fdfsWebServer.getWebServerUrl() + storePath.getFullPath();

        //2.调用服务 将语言记录保存到mongoDB  的taohua_audio表中
        Audio audio = new Audio();
        audio.setAudioUrl(audioUrl);
        audio.setUserId(UserHolder.getUserId());
        String audioId = audioApi.sendVoice(audio);
        //3.保存当日接收次数
        //3.1 计算当前时间到24:00有几个小时并设置有效期
        Long timeOut = getNowToNextDaySeconds();
        String key = "audio_" + UserHolder.getUserId();
        //
        String val = (String) redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(val) && val.length() > 0) {
            int value = Integer.parseInt(val);
            //获取存入redis的次数
            //如果超过3次,就提示用户
            if (value >= 3) {
                //抛出发语音次数超过的异常
                throw new TanHuaException(ErrorResult.voiceFrequencyLimit());
            }
            //次数加一 ,保存到redis
            redisTemplate.opsForValue().set(key, String.valueOf(value + 1), timeOut, TimeUnit.SECONDS);
        } else {
            //保存到redis ,设置次数为1
            redisTemplate.opsForValue().set(key, "1", timeOut, TimeUnit.SECONDS);
        }
        //  Map<String,Object> map=new HashMap();
        //  map.put("fileBytes",fileBytes);
        //  map.put("audioSuffix",audioSuffix);
        // map.put("audioId",audioId);
        // String mapStr = JSON.toJSONString(map);
        // rocketMQTemplate.convertAndSend("tanhua-audio",mapStr);
        rocketMQTemplate.convertAndSend("tanhua-audio", audioId);
        log.debug("*************将音频id写入中间件成功了******************************");
    }


    /**
     * 获取当前时间到凌晨24:00的时间
     *
     * @return
     */
    //原文链接：https://blog.csdn.net/shy_1762538422/article/details/114702459
    public Long getNowToNextDaySeconds() {
        Calendar cal = Calendar.getInstance();
        //cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 24);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
    }
}
