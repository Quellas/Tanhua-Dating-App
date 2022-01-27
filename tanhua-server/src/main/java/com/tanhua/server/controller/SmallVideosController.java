package com.tanhua.server.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.RecommendUserQueryParam;
import com.tanhua.domain.vo.TodayBestVo;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.server.service.SmallVideosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 小视频管理控制层
 */
@RestController
@RequestMapping("/smallVideos")
public class SmallVideosController {

    @Autowired
    private SmallVideosService smallVideosService;

    /**
     * 发布小视频
     * videoThumbnail:视频封面文件
     * videoFile:视频文件
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity saveSmallVideos(MultipartFile videoThumbnail,MultipartFile videoFile) throws IOException {
        smallVideosService.saveSmallVideos(videoThumbnail,videoFile);
        return ResponseEntity.ok(null);
    }


    /**
     * 小视频分页列表数据
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity findPageBySmallVideos(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize){
        page = page < 1 ? 1:page;
        PageResult<VideoVo> pageResult = smallVideosService.findPageBySmallVideos(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 关注用户
     * followUserId:被关注的用户id
     */
    @RequestMapping(value = "/{uid}/userFocus",method = RequestMethod.POST)
    public ResponseEntity userFocus(@PathVariable("uid") Long followUserId) {
        smallVideosService.userFocus(followUserId);
        return ResponseEntity.ok(null);
    }

    /**
     * 取消关注用户
     * followUserId:被取消关注的用户id
     */
    @RequestMapping(value = "/{uid}/userUnFocus",method = RequestMethod.POST)
    public ResponseEntity userUnFocus(@PathVariable("uid") Long followUserId) {
        smallVideosService.userUnFocus(followUserId);
        return ResponseEntity.ok(null);
    }

}
