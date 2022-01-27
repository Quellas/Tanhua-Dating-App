package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.UserInfo;

/**
 * 用户信息服务接口
 */
public interface UserInfoApi {
    /**
     * 保存用户基础信息
     * @param userInfo
     */
    void saveUserInfo(UserInfo userInfo);

    /**
     * 根据用户id更新用户头像
     * @param userInfo
     */
    void editUserInfo(UserInfo userInfo);

    /**
     * 根据用户id查询用户基础信息
     * @param myCurrentUserId
     * @return
     */
    UserInfo findUserInfoById(Long myCurrentUserId);
}
