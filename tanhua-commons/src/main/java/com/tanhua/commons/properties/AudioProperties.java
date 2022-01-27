package com.tanhua.commons.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tanhua.audio")
public class AudioProperties {
private String accessKeyID;
private String accessKeyecret;
//private String audioUrl;//音文件的地址。
}
