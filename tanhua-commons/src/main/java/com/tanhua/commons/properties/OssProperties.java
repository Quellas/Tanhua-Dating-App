package com.tanhua.commons.properties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tanhua.oss")
public class OssProperties {
    private String endpoint; //服务器域名
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;//存储空间名称
    private String url;//sztanhua.oss-cn-shenzhen.aliyuncs.com 访问图片域名地址
}