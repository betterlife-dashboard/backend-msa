package com.betterlife.gateway.swagger;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "openapi")
public class ApiServicesProperties {

    private List<ServiceInfo> services;

    @Data
    public static class ServiceInfo {
        private String name;
        private String url;
    }
}
