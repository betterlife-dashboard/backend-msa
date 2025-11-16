package com.betterlife.auth.repository;

import com.betterlife.auth.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void addUser() {
        User save = userRepository.save(User.builder().name("hyeongsh").email("test@test.com").password("abcd1234").build());
        assertThat(save.getEmail()).isEqualTo("test@test.com");
    }
}