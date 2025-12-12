package com.betterlife.focus;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@OpenAPIDefinition(
        info = @Info(
                title = "Focus API",
                version = "v1.0",
                description = "Focus API 문서"
        )
)
@EnableAspectJAutoProxy
public class FocusApplication {

    public static void main(String[] args) {
        SpringApplication.run(FocusApplication.class, args);
    }

}
