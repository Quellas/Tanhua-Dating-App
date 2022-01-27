package com.tanhua.server.controller;

import com.tanhua.domain.vo.NearUserVo;
import com.tanhua.server.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 搜附近控制层
 */
@RestController
public class LocationController {

    @Autowired
    private LocationService locationService;

    /**
     * 上报地址位置
     */
    @RequestMapping(value = "/baidu/location",method = RequestMethod.GET)
    public ResponseEntity saveLocation(@RequestBody Map params){
        Double latitude = (Double)params.get("latitude");//纬度
        Double longitude = (Double)params.get("longitude");//经度
        String addrStr =(String)params.get("addrStr");//位置描述
        locationService.saveLocation(latitude,longitude,addrStr);
        return ResponseEntity.ok(null);
    }

    /**
     * 搜附近
     * gender：性别
     * distance：距离
     */
    @RequestMapping(value = "/tanhua/search",method = RequestMethod.GET)
    public ResponseEntity searchNearUser(@RequestParam(required=false) String gender,
                                         @RequestParam(defaultValue = "2000") String distance){
        List<NearUserVo> list = locationService.searchNearUser(gender,distance);
        return ResponseEntity.ok(list);
    }
}
