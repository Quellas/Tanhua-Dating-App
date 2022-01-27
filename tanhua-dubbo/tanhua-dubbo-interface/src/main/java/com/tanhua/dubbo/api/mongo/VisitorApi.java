package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Visitor;

import java.util.List;

/**
 * 访客服务接口
 */
public interface VisitorApi {
    /**
     * 根据当前用户id 和 上次登录时间 查询前5条访客记录
     * @param userId
     * @param redisTime
     * @return
     */
    List<Visitor> findVisitors(Long userId, String redisTime);

    /**
     * 根据当前用户id查询前5条访客记录
     * @param userId
     * @return
     */
    List<Visitor> findVisitors(Long userId);


    /**
     * 保存访客记录
     */
    void save(Visitor visitor);

}
