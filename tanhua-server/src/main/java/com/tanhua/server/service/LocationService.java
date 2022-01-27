package com.tanhua.server.service;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.domain.vo.NearUserVo;
import com.tanhua.domain.vo.UserLocationVo;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.LocationApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜附近业务处理层
 */
@Service
public class LocationService {

    @Reference
    private LocationApi locationApi;

    @Reference
    private UserInfoApi userInfoApi;

    /**
     * 上报地址位置
     * UserLocation:对象中GeoJsonPoint 序列化异常
     */
    public void saveLocation(Double latitude, Double longitude, String addrStr) {
        Long userId = UserHolder.getUserId();
        locationApi.saveLocation(latitude,longitude,addrStr, userId);
    }

    /**
     * 搜附近
     * gender：性别
     * distance：距离
     */
    public List<NearUserVo> searchNearUser(String gender, String distance) {
        Long userId = UserHolder.getUserId();
        //1.根据当前用户距离 和 搜索距离 查询附近用户数据
        List<UserLocationVo> userLocationVoList =locationApi.searchNearUser(Long.parseLong(distance),userId);
        if(CollectionUtils.isEmpty(userLocationVoList)){
            return null;
        }
        //2.根据搜索的附近用户id 查询UserInfo
        List<NearUserVo> nearUserVoList = new ArrayList<>();
        for (UserLocationVo userLocationVo : userLocationVoList) {
            NearUserVo nearUserVo = new NearUserVo();
            Long nearUserId = userLocationVo.getUserId();
            //2.1 过滤当前用户
            if(userId.equals(nearUserId)){
                continue;
            }
            UserInfo userInfo = userInfoApi.findUserInfoById(nearUserId);
            //2.2 过滤不符合选择的性别的用户
            if(!StringUtils.isEmpty(gender) && !userInfo.getGender().equals(gender)){
                continue;
            }
            nearUserVo.setUserId(nearUserId);//附近用户id
            nearUserVo.setAvatar(userInfo.getAvatar());//附近用户的头像
            nearUserVo.setNickname(userInfo.getNickname());//附近用户的昵称
            nearUserVoList.add(nearUserVo);
        }
        //3.数据封装返回Vo
        return nearUserVoList;
    }
}
