package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.SoulUserResult;

public interface SoulUserResultApi {
    /**
     * 根据用户id和问卷级别查询结果
     * @param userId
     * @param level
     * @return
     */
    SoulUserResult findByUserIdAndLevel(Long userId, String level);

    /**
     * 保存用户测试结果
     * @param soulUserResult
     * @return
     */
    String saveSoulUserResult(SoulUserResult soulUserResult);

    /**
     * 更新测试结果
     * @param soulUserResult
     * @return
     */
    String updateSoulUserResult(SoulUserResult soulUserResult);
}
