package com.betterlife.auth.client;

import com.betterlife.auth.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Client
@RequiredArgsConstructor
public class TodoClient {

    private final RestTemplate restTemplate;

    @Value("${todo.service.url}")
    private String todoServiceUrl;

    public Boolean deleteAllTodo(Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId.toString());
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    todoServiceUrl + "/todo/delete_user",
                    HttpMethod.DELETE,
                    entity,
                    Boolean.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new InvalidRequestException("잘못된 요청입니다.");
            }
            throw new IllegalStateException("todo 서비스 호출 실패: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            throw new IllegalStateException("todo 서비스 연결에 실패했습니다.", e);
        }
    }
}
