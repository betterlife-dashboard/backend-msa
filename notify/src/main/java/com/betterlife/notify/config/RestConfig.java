package com.betterlife.notify.config;

import com.betterlife.notify.exception.FirebaseCredentialLoadException;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class RestConfig {

    private final String firebaseKey;

    public RestConfig(@Value("${GOOGLE_APPLICATION_CREDENTIALS}") String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    @Bean
    public GoogleCredentials googleCredentials() {
        try {
            return GoogleCredentials
                    .fromStream(new FileInputStream(firebaseKey))
                    .createScoped(List.of("https://www.googleapis.com/auth/firebase.messaging"));
        } catch (IOException e) {
            throw new FirebaseCredentialLoadException("Firebase credential 파일을 로드하지 못했습니다. 배포 환경 설정을 확인하세요.", e);
        }
    }
}
