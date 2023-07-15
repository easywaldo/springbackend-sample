package com.jwt;

import root.Application;
import root.domain.User;
import root.config.jwt.JwtProperties;
import root.config.jwt.TokenProvider;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import root.repository.UserRepository;

import java.time.Duration;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class)
public class TokenProviderTest {
    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProperties jwtProperties;

    @DisplayName("유저 토큰이 정상적으로 만들어진다.")
    @Test
    void generateToken() {
        // given
        User testUser = userRepository.save(User.builder()
                .email("acetios@gmail.com")
                .password("1111")
                .build());

        // when
        String userToken= tokenProvider.generateToken(testUser, Duration.ofDays(14));

        // then
        Long userId = Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(userToken)
                .getBody()
                .get("id", Long.class);
        assertThat(userId).isEqualTo(testUser.getId());
    }
}
