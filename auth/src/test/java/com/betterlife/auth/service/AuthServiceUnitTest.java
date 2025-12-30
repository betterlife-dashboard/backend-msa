package com.betterlife.auth.service;

import com.betterlife.auth.client.TodoClient;
import com.betterlife.auth.domain.User;
import com.betterlife.auth.dto.LoginRequest;
import com.betterlife.auth.dto.RegisterRequest;
import com.betterlife.auth.exception.DuplicateUserException;
import com.betterlife.auth.repository.UserRepository;
import com.betterlife.auth.util.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TodoClient todoClient;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_duplicateEmail_throws() {
        RegisterRequest request = new RegisterRequest();
        request.setName("tester");
        request.setEmail("test@example.com");
        request.setPassword("Password1");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateUserException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_storesRefreshToken() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User user = User.builder()
                .name("tester")
                .email("test@example.com")
                .password(encoder.encode("Password1"))
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password1");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtProvider.createRefreshToken(1L)).thenReturn("refresh-token");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String token = authService.login(request);

        assertThat(token).isEqualTo("refresh-token");
        verify(valueOperations).set(eq("refresh:1"), eq("refresh-token"), eq(30L), eq(TimeUnit.DAYS));
    }
}
