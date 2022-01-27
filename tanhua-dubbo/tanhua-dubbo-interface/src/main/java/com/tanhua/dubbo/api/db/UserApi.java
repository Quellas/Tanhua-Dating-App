package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.User;

/**
 * 用户服务提供者接口
 */
public interface UserApi {
    /**
     * 完成用户保存返回用户id
     */
    public Long saveUser(User user);

    /**
     * 根据手机号码查询用户功能
     */
    public User findByMobile(String mobile);
}
