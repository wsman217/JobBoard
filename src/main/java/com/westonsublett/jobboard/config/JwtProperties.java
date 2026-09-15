package com.westonsublett.jobboard.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;

    private String issuer = "jobboard";

    private String audience = "jobboard";

    private long accessTokenTtlSeconds = 3600;
}
