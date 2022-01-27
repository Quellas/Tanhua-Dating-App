package com.tanhua.server.test;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.server.TanhuaServerApplication;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

/**
 * fastdfs入门案例
 *  上传图片
 *  下载图片查看（浏览器访问即可）
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TanhuaServerApplication.class)
public class FastDFSTest {

    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    /**
     * 文件上传
     */
    @Test
    public void uploadFile() throws IOException {
        String filenameUrl = "C:\\Users\\Administrator\\Desktop\\1.jpg";
        File file = new File(filenameUrl);
        //InputStream inputStream:文件输入流, long fileSize 文件大小, String fileExtName 文件后缀,  Set<MetaData> metaDataSet：null
        StorePath storePath = client.uploadFile(FileUtils.openInputStream(file), file.length(), "jpg", null);
        //获取返回的地址
        System.out.println("****getGroup****"+storePath.getGroup()+"****getFullPath****"+storePath.getFullPath()+"****getPath****"+storePath.getPath());
        System.out.println("getWebServerUrl***********"+fdfsWebServer.getWebServerUrl()+storePath.getFullPath());
    }

    public void uploadAudio(){
        String fileNameUrl="";
    }
}
