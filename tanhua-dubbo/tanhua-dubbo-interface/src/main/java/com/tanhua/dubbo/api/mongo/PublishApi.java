package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;

/**
 * 圈子功能-服务接口
 */
public interface PublishApi {

    /**
     * 发布动态
     */
    String savePublish(PublishVo publishVo);

    /**
     * 好友动态分页查询
     */
    PageResult<Publish> findPagePublishByFriend(Long userId, int page, int pagesize);

    /**
     * 推荐动态分页查询（陌生人动态）
     */
    PageResult<Publish> findPagePublishByRecommend(Long userId, int page, int pagesize);

    /**
     * 用户动态分页查询（我的动态）
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult<Publish> findPagePublishByUserId(Long userId, int page, int pagesize);

    /**
     * 单条动态
     * publishId:动态发布id
     */
    Publish findPublishById(String publishId);

    /**
     * 根据动态发布id更新状态
     * @param message
     * @param state
     */
    void updateById(String message, Integer state);
}
