package com.aichainid.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiServiceConfig {

    @Value("${ai.service.base-url:http://localhost:8000}")
    private String aiServiceBaseUrl;

    @Bean
    public WebClient aiServiceWebClient() {
        return WebClient.builder()
                .baseUrl(aiServiceBaseUrl)
                .build();
    }
}
