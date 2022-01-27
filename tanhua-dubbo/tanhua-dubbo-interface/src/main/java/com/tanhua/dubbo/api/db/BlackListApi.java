package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.BlackList;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.AudioRecommendRecord;
import com.tanhua.domain.vo.PageResult;

import java.util.List;

/**
 * 黑名单管理服务接口
 */
public interface BlackListApi {
    /**
     *黑名单分页查询
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult<UserInfo> findPageBlackList(int page, int pagesize, Long userId);

    /**
     * 移除黑名单
     * @param userId
     * @param blackUserId
     */
    void deleteBlackUser(Long userId, Long blackUserId);

    List<BlackList> findByUserId(Long userId);

    /*
    * 探花-不喜欢(添加黑名单)
    * */
    void insertUnloveUserId(AudioRecommendRecord audioRecommendRecord);
}
