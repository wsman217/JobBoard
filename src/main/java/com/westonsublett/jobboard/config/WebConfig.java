package com.westonsublett.jobboard.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig  implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                // TODO bring this out into a configuration so it can be changed on deployment.
                .allowedOrigins("https://int-test.com", "https://api.int-test.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                // TODO will want to figure out all the required headers and add them here instead of just allowing everything.
                //  Will also probably want this as a config
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
