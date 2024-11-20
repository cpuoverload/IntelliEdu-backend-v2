package com.cpuoverload.intelliedu.config;

import dev.ai4j.openai4j.OpenAiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "openai")
@Data
public class OpenAiConfig {
    private String token;

    @Bean
    public OpenAiClient openAiClient() {
        return OpenAiClient.builder().openAiApiKey(token).build();
    }
}
