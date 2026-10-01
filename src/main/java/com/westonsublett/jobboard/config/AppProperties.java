package com.westonsublett.jobboard.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();

    private Frontend frontend = new Frontend();

    @Getter
    @Setter
    public static class Frontend {

        private String redirectUri = "http://localhost:3000";
    }

    @Getter
    @Setter
    public static class Jwt {

        private String secret;

        private String issuer = "jobboard";

        private String audience = "jobboard";

        private long accessTokenTtlSeconds = 3600;
    }
}
