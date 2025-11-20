package com.betterlife.auth.service;

import com.betterlife.auth.client.TodoClient;
import com.betterlife.auth.domain.User;
import com.betterlife.auth.dto.*;
import com.betterlife.auth.exception.DuplicateUserException;
import com.betterlife.auth.exception.InvalidRequestException;
import com.betterlife.auth.repository.UserRepository;
import com.betterlife.auth.util.JwtProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final StringRedisTemplate redisTemplate;
    private final JwtProvider jwtProvider;
    private final TodoClient todoClient;

    public UserResponse me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));
        return UserResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public void register(RegisterRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new InvalidRequestException("아이디를 입력해주세요.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidRequestException("비밀번호를 입력해주세요.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("이미 존재하는 이메일입니다.");
        }
        if (!isValidPassword(request.getPassword())) {
            throw new InvalidRequestException("비밀번호는 8자 이상이며 숫자와 문자를 포함해야 합니다.");
        }

        String encodedPassword = encoder.encode(request.getPassword());
        User user = User.builder()
                .name(request.getName())
                .password(encodedPassword)
                .email(request.getEmail())
                .build();
        userRepository.save(user);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 사용자입니다."));
        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("패스워드가 일치하지 않습니다.");
        }
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        String key = "refresh:" + user.getId();
        redisTemplate.opsForValue().set(key, refreshToken, 30, TimeUnit.DAYS);
        return refreshToken;
    }

    public LoginResponse renew(String refreshToken) {
        Long userId = jwtProvider.getUserId(refreshToken);
        String token = jwtProvider.createAccessToken(userId);
        return LoginResponse.builder()
                .token(token)
                .build();
    }

    public void logout(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new InvalidRequestException("유효하지 않은 토큰입니다.");
        }

        Long userId = jwtProvider.getUserId(refreshToken);
        String key = "refresh:" + userId;

        String stored = redisTemplate.opsForValue().get(key);
        if (stored != null && stored.equals(refreshToken)) {
            redisTemplate.delete(key);
        }
    }

    public void withdraw(String refreshToken) {
        Long userId = jwtProvider.getUserId(refreshToken);

        this.logout(refreshToken);
        // 위험한 요청을 먼저 처리!
        todoClient.deleteAllTodo(userId);
        userRepository.deleteById(userId);
    }

    public String checkRefresh(String refreshToken) {
        if (refreshToken == null) {
            throw new InvalidRequestException("토큰이 존재하지 않습니다.");
        }

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new InvalidRequestException("유효하지 않은 토큰입니다.");
        }

        Long userId = jwtProvider.getUserId(refreshToken);
        String key = "refresh:" + userId;
        String storedToken = redisTemplate.opsForValue().get(key);

        if (storedToken == null) {
            throw new InvalidRequestException("토큰이 만료되었습니다.");
        }

        if (!storedToken.equals(refreshToken)) {
            redisTemplate.delete(key);
            throw new InvalidRequestException("잘못된 토큰입니다.");
        }

        String newRefresh = jwtProvider.createRefreshToken(userId);
        redisTemplate.opsForValue().set(key, newRefresh, 30, TimeUnit.DAYS);
        return newRefresh;
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Za-z].*") &&
                password.matches(".*[0-9].*");
    }
}
