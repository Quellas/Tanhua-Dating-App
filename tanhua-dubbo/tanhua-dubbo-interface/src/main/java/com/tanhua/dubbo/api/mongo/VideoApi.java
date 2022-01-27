package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;

/**
 * 小视频服务接口
 */
public interface VideoApi {
    /**
     * 发布小视频
     * videoThumbnail:视频封面文件
     * videoFile:视频文件
     */
    void saveSmallVideos(Video video);

    /**
     * 小视频分页列表数据
     */
    PageResult<Video> findPageBySmallVideos(long page, long pagesize);

    /**
     * 关注用户
     * followUserId:被关注的用户id
     */
    void userFocus(FollowUser followUser);

    /**
     * 取消关注用户
     * followUserId:被取消关注的用户id
     */
    void userUnFocus(FollowUser followUser);
}
