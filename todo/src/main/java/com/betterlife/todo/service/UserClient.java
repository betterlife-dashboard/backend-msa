package com.betterlife.todo.service;

import com.betterlife.todo.dto.UserDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public UserDto getUser(Long userId) {
        try {
            return restTemplate.getForObject(authServiceUrl + "/auth/" + userId, UserDto.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new EntityNotFoundException("존재하지 않는 유저입니다.");
            }
            throw new IllegalStateException("유저 서비스 호출 실패: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) { // 네트워크/타임아웃
            throw new IllegalStateException("유저 서비스에 연결할 수 없습니다.", e);
        }
    }
}
