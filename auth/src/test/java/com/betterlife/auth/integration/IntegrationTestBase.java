package com.betterlife.auth.integration;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@Tag("integration")
public abstract class IntegrationTestBase {

    static final MySQLContainer<?> mySql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    static final RabbitMQContainer rabbitMQ = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-alpine"));

    static {
        // 한 번만 start
        mySql.start();
        redis.start();
        rabbitMQ.start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySql::getJdbcUrl);
        registry.add("spring.datasource.username", mySql::getUsername);
        registry.add("spring.datasource.password", mySql::getPassword);
        registry.add("spring.datasource.driver-class-name", mySql::getDriverClassName);

        registry.add("spring.flyway.url", mySql::getJdbcUrl);
        registry.add("spring.flyway.user", mySql::getUsername);
        registry.add("spring.flyway.password", mySql::getPassword);

        // Redis
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        // RabbitMQ
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQ::getAdminPassword);
        registry.add("spring.rabbitmq.publisher-confirm-type", () -> "correlated");
        registry.add("spring.rabbitmq.publisher-returns", () -> "true");
        registry.add("spring.rabbitmq.template.mandatory", () -> "true");

        // 권장: 스키마는 Flyway가 책임지고, JPA는 검증만
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> true);
    }
}
