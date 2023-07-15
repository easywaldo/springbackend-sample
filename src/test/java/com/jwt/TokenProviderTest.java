package com.jwt;

import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
import java.util.Date;
import java.util.Map;


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

    @DisplayName("validToken(): 만료된 토큰인 경우 유효성 검증에 실패")
    @Test
    void validToken_invalidToken() {
        // given
        String token = JwtFactory.builder()
                .expiration(new Date(new Date().getTime() - Duration.ofDays(7).toMillis()))
                .build()
                .createToken(jwtProperties);

        // when
        boolean result = tokenProvider.validToken(token);

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("getAuthentication 을 이용하여 토큰으로 뷰터 유저 정보를 가져올 수 있음")
    @Test
    void getAuthentication() {
        // given
        String userEmail = "acetious@gmail.com";
        String token = JwtFactory.builder()
                .subject(userEmail)
                .build()
                .createToken(jwtProperties);
        // when
        Authentication authentication = tokenProvider.getAuthentication(token);

        // then
        val userDetails = (UserDetails) authentication.getPrincipal();
        assertThat(userDetails.getUsername()).isEqualTo(userEmail);
    }

    @DisplayName("getUserId 를 이용하여 토큰으로부터 유저 아이디 정보를 가져올 수 있음")
    @Test
    void  getUserId() {
        // given
        Long userId = 1L;
        String token = JwtFactory.builder()
                .claims(Map.of("id", userId))
                .build()
                .createToken(jwtProperties);
        // when
        Long userIdByToken = tokenProvider.getUserId(token);

        // then
        assertThat(userIdByToken).isEqualTo(userId);
    }
}
