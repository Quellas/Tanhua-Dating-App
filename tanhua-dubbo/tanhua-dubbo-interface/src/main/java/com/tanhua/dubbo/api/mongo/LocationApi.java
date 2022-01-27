package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.vo.UserLocationVo;

import java.util.List;

/**
 * 搜附近服务接口
 */
public interface LocationApi {
    /**
     * 上报地址位置
     * @param latitude
     * @param longitude
     * @param addrStr
     * @param userId
     */
    void saveLocation(Double latitude, Double longitude, String addrStr, Long userId);

    /**
     * 根据当前用户位置 与 需要搜索距离 查询附近的用户数据
     * @param distance
     * @param userId
     * @return
     */
    List<UserLocationVo> searchNearUser(Long distance, Long userId);
}
