package com.tanhua.server.controller;

/**
 * @Author：zhangben
 * @Date: 2021/10/6 9:57
 */

import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.domain.vo.AudioVo;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.server.service.AudioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 传音功能控制层
 */
@RestController
@RequestMapping
public class AudioController {

    @Autowired
    private AudioService audioService;

    /**
     * 接受语音
     *
     * @return
     */
    @RequestMapping(value = "/peachblossom", method = RequestMethod.GET)
    public ResponseEntity acceptAudio() {
        AudioVo audioVo = audioService.acceptAudio();
        return ResponseEntity.ok(audioVo);
    }

    /**
     * 发送语音
     */
    @RequestMapping(value = "/peachblossom",method = RequestMethod.POST)
    public ResponseEntity sendVoice(MultipartFile soundFile) throws IOException {
        audioService.sendVoice(soundFile);
        return ResponseEntity.ok(null);
    }
}
