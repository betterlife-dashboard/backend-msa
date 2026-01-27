package com.betterlife.auth.integration;

import com.betterlife.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class RegisterIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void beforeEach() {
        userRepository.deleteAll();
    }

    @Test
    void register_invalidPassword_returns400() throws Exception {
        String email = "sample@test.com";
        String rawPasswordInvalid = "12345678";

        String reqJsonInvalid = """
                {
                    "name": "test",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, rawPasswordInvalid);

        long before = userRepository.count();
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJsonInvalid))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(containsString("숫자와 문자")));
        assertThat(before).isEqualTo(userRepository.count());
    }

    @Test
    void register_success_returns201_and_persists() throws Exception {
        String email = "sample@test.com";
        String rawPassword = "AbCd1234";

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
                        .content(reqJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string(containsString("회원가입")));
        assertThat(before + 1).isEqualTo(userRepository.count());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
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

        long before = userRepository.count();
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.content().string(containsString("이미 존재")));
        assertThat(before).isEqualTo(userRepository.count());
    }
}
