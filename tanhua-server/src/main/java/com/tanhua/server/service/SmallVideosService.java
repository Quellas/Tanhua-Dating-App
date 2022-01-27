package com.tanhua.server.service;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.VideoApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 小视频业务逻辑处理层
 */
@Service
@Slf4j
public class SmallVideosService {

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Reference
    private VideoApi videoApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 发布小视频
     * videoThumbnail:视频封面文件
     * videoFile:视频文件
     */
    @CacheEvict(value = "VideoList_",allEntries = true)
    public void saveSmallVideos(MultipartFile videoThumbnail, MultipartFile videoFile) throws IOException {
        //1.调用阿里云oss将图片上传 并获取图片地址
        String filename = videoThumbnail.getOriginalFilename();
        String picUrl = ossTemplate.upload(filename, videoThumbnail.getInputStream());//图片封面地址
        //2.调用fastdfs将视频上传 并获取视频地址
        String videoFileName = videoFile.getOriginalFilename();
        String videoSuffix = videoFileName.substring(videoFileName.lastIndexOf(".") + 1);
        StorePath storePath = storageClient.uploadFile(videoFile.getInputStream(), videoFile.getSize(), videoSuffix, null);
        String videoUrl = fdfsWebServer.getWebServerUrl()+storePath.getFullPath();
        //3.调用服务 将小视频发布记录保存到视频表中
        Video video = new Video();
        video.setUserId(UserHolder.getUserId());//发布小视频的用户id
        video.setVid(1l);//随便设置下 没有用到
        video.setText("探花小视频");//文字
        video.setPicUrl(picUrl);//视频封面
        video.setVideoUrl(videoUrl);//视频地址
        videoApi.saveSmallVideos(video);
    }

    /**
     * 小视频分页列表数据
     * key:VideoList_1_10 value:PageResult<VideoVo>
     * key:VideoList_2_10 value:PageResult<VideoVo>
     */
    @Cacheable(value = "VideoList_",key = "#page +'_'+#pagesize")
    public PageResult<VideoVo> findPageBySmallVideos(long page, long pagesize) {
        log.debug("*************findPageBySmallVideos*******************查询小视频列表数据*******当前页码{}*****每页记录数{}*****",page,pagesize);
        //1.调用服务 分页查询小视频列表数据  查询表中最新10条记录
        PageResult<Video> videoPageResult = videoApi.findPageBySmallVideos(page,pagesize);
        if(videoPageResult == null || StringUtils.isEmpty(videoPageResult.getItems())){
            return new PageResult<>(0l, pagesize,0l, page,null);
        }
        //2,根据小视频发布用户id 查询用户信息
        //将List<Video> 转为List<VideoVo>
        List<VideoVo> videoVoList = new ArrayList<>();
        for (Video video : videoPageResult.getItems()) {
            VideoVo videoVo = new VideoVo();
            Long userId = video.getUserId();//发布小视频的用户id
            UserInfo userInfo = userInfoApi.findUserInfoById(userId);
            BeanUtils.copyProperties(userInfo,videoVo);//copy 头像 昵称
            BeanUtils.copyProperties(video,videoVo);//视频url 点赞数量  评论数量

            videoVo.setId(video.getId().toHexString());//设置视频主键id
            videoVo.setUserId(video.getUserId()); //设置视频发布用户的id
            videoVo.setCover(video.getPicUrl());//设置视频封面
            videoVo.setSignature(video.getText());//签名
            videoVo.setHasLiked(0);//是否已赞（1是，0否）
            //4.修改分页查询视频列表中返回的是否关注
            String key = "userFocus_"+UserHolder.getUserId()+"_"+userId;
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key))) {
                videoVo.setHasFocus(0);//是是否关注 （1是，0否）
            }else{
                videoVo.setHasFocus(1);//是是否关注 （1是，0否）
            }
            videoVoList.add(videoVo);
        }
        //3.将数据封装返回Vo
        PageResult<VideoVo> voPageResult = new PageResult<>();
        BeanUtils.copyProperties(videoPageResult,voPageResult);
        voPageResult.setItems(videoVoList);
        return voPageResult;
    }
    /**
     * 关注用户
     * followUserId:被关注的用户id
     */
    public void userFocus(Long followUserId) {
        //1.判断是否关注自己 （不要自己的登录 关注自己）

        Long userId = UserHolder.getUserId();
        //2.调用服务保存当前用户跟视频作者的关系
        FollowUser followUser  = new FollowUser();
        followUser.setUserId(userId);
        followUser.setFollowUserId(followUserId);
        videoApi.userFocus(followUser);
        //3.将关系记录到redis中
        String key = "userFocus_"+userId+"_"+followUserId;
        redisTemplate.opsForValue().set(key,"1");
    }

    /**
     * 取消关注用户
     * followUserId:被取消关注的用户id
     */
    public void userUnFocus(Long followUserId) {
        //1.调用服务删除当前用户跟视频作者的关系
        Long userId = UserHolder.getUserId();
        FollowUser followUser  = new FollowUser();
        followUser.setUserId(userId);
        followUser.setFollowUserId(followUserId);
        videoApi.userUnFocus(followUser);
        //2.将关系从redis中删除
        String key = "userFocus_"+userId+"_"+followUserId;
        redisTemplate.delete(key);
    }
}
