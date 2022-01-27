package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.SoulConclusion;
import com.tanhua.domain.mongo.SoulUserResult;

import java.util.List;

/**
 * 测灵魂的服务接口
 */
public interface SoulApi {

    /**
     * 查询结果
     * @param resultId
     * @return
     */
    SoulUserResult findResult(String resultId);

    /**
     * 查询类型相同的用户
     * @param conclusionType
     * @return
     */
    List<SoulUserResult> findUserByconclusionType(String conclusionType,String level);

    /**
     * 查询鉴定结果
     * @param conclusionType
     * @return
     */
    SoulConclusion findConclusion(String conclusionType);

}
