package com.betterlife.auth.service;

import com.betterlife.auth.domain.UserEntity;
import com.betterlife.auth.dto.LoginRequest;
import com.betterlife.auth.dto.LoginResponse;
import com.betterlife.auth.dto.RegisterRequest;
import com.betterlife.auth.dto.UserResponse;
import com.betterlife.auth.exception.DuplicateUserException;
import com.betterlife.auth.exception.InvalidRequestException;
import com.betterlife.auth.exception.UnauthorizedException;
import com.betterlife.auth.repository.UserRepository;
import com.betterlife.auth.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    public UserResponse me(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 유저입니다."));
        return UserResponse.builder()
                .id(user.getId())
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
        UserEntity user = UserEntity.builder()
                .name(request.getName())
                .passwordHash(encodedPassword)
                .email(request.getEmail())
                .build();
        userRepository.save(user);
    }

    public String login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 사용자입니다."));
        if (!encoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidRequestException("패스워드가 일치하지 않습니다.");
        }
        String sessionId = UUID.randomUUID().toString();
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), sessionId);
        String key = "refresh:" + user.getId() + ":" + sessionId;
        redisTemplate.opsForValue().set(key, refreshToken, 30, TimeUnit.DAYS);
        return refreshToken;
    }

    public LoginResponse renew(String refreshToken) {
        Long userId = jwtProvider.getUserId(refreshToken);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 사용자입니다."));
        String token = jwtProvider.createAccessToken(userId);
        return LoginResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .token(token)
                .build();
    }

    public void logout(String refreshToken) {
        jwtProvider.validateToken(refreshToken);
        Long userId = jwtProvider.getUserId(refreshToken);
        String sessionId = jwtProvider.getSessionId(refreshToken);
        String key = "refresh:" + userId + ":" + sessionId;

        String stored = redisTemplate.opsForValue().get(key);
        if (stored != null && stored.equals(refreshToken)) {
            redisTemplate.delete(key);
        }
    }

    public void withdraw(String refreshToken) {
        Long userId = jwtProvider.getUserId(refreshToken);

        this.logout(refreshToken);
        // TODO : 메시지 전송을 통한 Todo 삭제로 데이터 정합성 확보 필요
        userRepository.deleteById(userId);
    }

    public String checkRefresh(String refreshToken) {
        jwtProvider.validateToken(refreshToken);

        Long userId = jwtProvider.getUserId(refreshToken);
        String sessionId = jwtProvider.getSessionId(refreshToken);
        String key = "refresh:" + userId + ":" + sessionId;
        String storedToken = redisTemplate.opsForValue().get(key);

        if (storedToken == null) {
            throw new UnauthorizedException("토큰이 만료되었습니다.");
        }

        if (!storedToken.equals(refreshToken)) {
            redisTemplate.delete(key);
            throw new UnauthorizedException("잘못된 토큰입니다.");
        }

        String newRefresh = jwtProvider.createRefreshToken(userId, sessionId);
        redisTemplate.opsForValue().set(key, newRefresh, 30, TimeUnit.DAYS);
        return newRefresh;
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Za-z].*") &&
                password.matches(".*[0-9].*");
    }
}
