package com.tanhua.server.test;

import com.aliyuncs.exceptions.ClientException;
import com.tanhua.commons.templates.AudioTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class audioTest {

    @Autowired
    private AudioTemplate audioTemplate;

    @Test
    public void testAudio() throws UnsupportedEncodingException, ClientException {
        boolean b = audioTemplate.audioExamine("http://10.10.20.160:8888/group1/M00/00/00/CgoUoGFeZbyAQtjTAAATjhVOEZo064.m4a");
        System.out.println(b);
    }
}
