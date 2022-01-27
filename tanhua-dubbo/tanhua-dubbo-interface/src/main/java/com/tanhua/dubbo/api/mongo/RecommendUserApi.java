package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;

/**
 * 今日佳人服务接口
 */
public interface RecommendUserApi {
    /**
     * 根据当前登录用户id查询今日佳人用户
     * @param currentUserId
     * @return
     */
    RecommendUser findByUserId(Long currentUserId);

    /**
     * 推荐佳人分页列表数据
     */
    PageResult<RecommendUser> recommendation(Integer page, Integer pagesize, Long currentUserId);

    /**
     * 获取缘分值
     * @param personUserId
     * @param userId
     * @return
     */
    Double findByUserIdAndPersonId(Long personUserId, Long userId);
}
