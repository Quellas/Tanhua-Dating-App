package com.tanhua.commons.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tanhua.voice")
public class VoiceProperties {
    private String appId;
    private String apiKey;
    private String secretKey;
}
