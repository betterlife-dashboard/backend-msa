package com.betterlife.auth.integration;

import com.betterlife.auth.repository.UserRepository;
import com.betterlife.auth.util.JwtProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.hamcrest.Matchers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AuthIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @BeforeEach
    void beforeEach() {
        userRepository.deleteAll();
    }

    @Test
    void register() throws Exception {
        String email = "sample@test.com";
        String rawPassword = "AbCd1234";
        String rawPasswordInvalid = "12345678";

        String reqJsonInvalid = """
                {
                    "name": "test",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, rawPasswordInvalid);

        String reqJson = """
                {
                    "name": "test",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, rawPassword);

        long before = userRepository.count();
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJsonInvalid))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(containsString("숫자와 문자")));
        assertThat(before).isEqualTo(userRepository.count());

        before = userRepository.count();
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string(containsString("회원가입")));
        assertThat(before + 1).isEqualTo(userRepository.count());

        before = userRepository.count();
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.content().string(containsString("이미 존재")));
        assertThat(before).isEqualTo(userRepository.count());
    }

    @Test
    void login() throws Exception {
        String email = "sample@test.com";
        String rawPassword = "AbCd1234";
        String rawPasswordInvalid = "12345678";

        String reqJson = """
                {
                    "name": "test",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, rawPassword);

        String reqJsonLogin = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, rawPassword);

        String reqJsonLoginInvalid = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, rawPasswordInvalid);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJsonLoginInvalid))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(containsString("패스워드")));

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
    void renew() throws Exception {
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
        result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/renew")
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("test"))
                .andReturn();

        setCookie = result.getResponse().getHeader("Set-Cookie");
        refreshToken = setCookie.split("refresh_token=")[1].split(";")[0];

        // 실패 -> 잘못된 리프레시 토큰
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/renew")
                        .cookie(new Cookie("refresh_token", refreshToken + "a")))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string(containsString("토큰")));

        Long userId = jwtProvider.getUserId(refreshToken);
        String sessionId = jwtProvider.getSessionId(refreshToken);
        String redisKey = "refresh:" + userId + ":" + sessionId;
        redisTemplate.delete(redisKey);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/renew")
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string(containsString("토큰")));
    }
}
