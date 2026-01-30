package com.betterlife.auth.integration;

import com.betterlife.auth.event.UserDeletedEvent;
import com.betterlife.auth.repository.UserRepository;
import com.betterlife.auth.util.JwtProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class LoginIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private DirectExchange deletedUserExchange;

    @BeforeEach
    void beforeEach() throws Exception {
        userRepository.deleteAll();

        String email = "sample@test.com";
        String rawPassword = "AbCd1234";

        String reqJson = """
                {
                    "name": "test",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, rawPassword);
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson));
    }

    @Test
    void login_success_returns200_setsRefreshCookies_andStoresRefreshInRedis() throws Exception {
        String email = "sample@test.com";
        String rawPassword = "AbCd1234";

        String reqJsonLogin = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, rawPassword);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJsonLogin))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("test"))
                .andExpect(MockMvcResultMatchers.header().string("Set-Cookie", containsString("refresh_token=")))
                .andReturn();

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        String refreshToken = setCookie.split("refresh_token=")[1].split(";")[0];

        Long userId = jwtProvider.getUserId(refreshToken);
        String sessionId = jwtProvider.getSessionId(refreshToken);
        String redisKey = "refresh:" + userId + ":" + sessionId;
        assertThat(redisTemplate.hasKey(redisKey)).isTrue();
    }

    @Test
    void login_invalidPassword_return400() throws Exception {
        String email = "sample@test.com";
        String rawPasswordInvalid = "12345678";

        String reqJsonLoginInvalid = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, rawPasswordInvalid);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJsonLoginInvalid))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(containsString("패스워드")));

    }

    @Test
    void renew_success_returns200() throws Exception {
        String email = "sample@test.com";
        String rawPassword = "AbCd1234";

        String reqJsonLogin = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, rawPassword);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJsonLogin))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("test"))
                .andExpect(MockMvcResultMatchers.header().string("Set-Cookie", containsString("refresh_token=")))
                .andReturn();

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        String refreshToken = setCookie.split("refresh_token=")[1].split(";")[0];

        // 성공 케이스
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/renew")
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("test"));
    }

    @Test
    void renew_invalidRefreshToken_returns401() throws Exception {
        String email = "sample@test.com";
        String rawPassword = "AbCd1234";

        String reqJsonLogin = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, rawPassword);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJsonLogin))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("test"))
                .andExpect(MockMvcResultMatchers.header().string("Set-Cookie", containsString("refresh_token=")))
                .andReturn();

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        String refreshToken = setCookie.split("refresh_token=")[1].split(";")[0];

        // 실패 -> 잘못된 리프레시 토큰
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/renew")
                        .cookie(new Cookie("refresh_token", refreshToken + "a")))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string(containsString("토큰")));

        Long userId = jwtProvider.getUserId(refreshToken);
        String sessionId = jwtProvider.getSessionId(refreshToken);
        String redisKey = "refresh:" + userId + ":" + sessionId;
        redisTemplate.delete(redisKey);

        // 실패 -> redis에서 삭제된 key
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/renew")
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string(containsString("토큰")));
    }

    @Test
    void withdraw_success_returns204_publishMessage() throws Exception {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        Queue queue = new Queue("event.user.deleted.queue");
        Binding binding = BindingBuilder.bind(queue).to(deletedUserExchange).with("event.user.deleted.key");
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);

        String email = "sample@test.com";
        String rawPassword = "AbCd1234";

        String reqJsonLogin = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, rawPassword);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJsonLogin))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("test"))
                .andExpect(MockMvcResultMatchers.header().string("Set-Cookie", containsString("refresh_token=")))
                .andReturn();

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        String refreshToken = setCookie.split("refresh_token=")[1].split(";")[0];

        // 성공
        mockMvc.perform(MockMvcRequestBuilders.delete("/auth/withdraw")
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Long userId = jwtProvider.getUserId(refreshToken);
        String sessionId = jwtProvider.getSessionId(refreshToken);
        String redisKey = "refresh:" + userId + ":" + sessionId;

        // 레디스 토큰 삭제 확인
        String str = redisTemplate.opsForValue().get(redisKey);
        assertThat(str).isNull();

        // 메시지 전송 확인
        rabbitTemplate.setReceiveTimeout(3000);
        UserDeletedEvent event = (UserDeletedEvent) rabbitTemplate.receiveAndConvert("event.user.deleted.queue");
        assertThat(event.getId()).isEqualTo(userId);
    }

    @Test
    void withdraw_bindingFail_returnCallback() throws Exception {
        String email = "sample@test.com";
        String rawPassword = "AbCd1234";

        String reqJsonLogin = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, rawPassword);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJsonLogin))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("test"))
                .andExpect(MockMvcResultMatchers.header().string("Set-Cookie", containsString("refresh_token=")))
                .andReturn();

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        String refreshToken = setCookie.split("refresh_token=")[1].split(";")[0];

        // 성공
        mockMvc.perform(MockMvcRequestBuilders.delete("/auth/withdraw")
                        .cookie(new Cookie("refresh_token", refreshToken)));

        Thread.sleep(2000);
    }
}
