package com.tanhua.manage.test;

import com.tanhua.commons.templates.HuaWeiUGCTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HuaWeiTest {

    @Autowired
    private HuaWeiUGCTemplate template;

    @Test
    public void testToken() {
        System.out.println(template.getToken());
    }

    @Test
    public void testText() {
        boolean check = template.textContentCheck("好好先生");
        System.out.println("***********1***********"+check);

        boolean check1 = template.textContentCheck("草泥马");
        System.out.println("***********2***********"+check1);


        boolean check2 = template.textContentCheck("毒品");
        System.out.println("***********3***********"+check2);


        boolean check3 = template.textContentCheck("习近平");
        System.out.println("***********4***********"+check3);

        String[] urls = new String[]{
                "http://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/logo/9.jpg",
                "http://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/logo/10.jpg"
        };
        boolean check4 = template.imageContentCheck(urls);
        System.out.println("***********5***********"+check4);
    }

    @Test
    public void testImages() {
        String[] urls = new String[]{
                "http://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/logo/9.jpg",
                "http://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/logo/10.jpg"
        };
        boolean check = template.imageContentCheck(urls);
        System.out.println(check);
    }
}