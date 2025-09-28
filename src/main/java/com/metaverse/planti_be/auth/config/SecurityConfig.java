package com.metaverse.planti_be.auth.config;

import com.metaverse.planti_be.auth.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 비밀번호 인코더 (BCrypt)를 Bean으로 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager를 수동 Bean으로 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // SecurityFilterChain을 수동 Bean으로 등록하여 HTTP 보안 규칙 정의
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 인가(Authorization, 엔드포인트의 접근 권한) 규칙 정의:
                .authorizeHttpRequests(authorize -> authorize
                        // 1. GET 요청 중 인증 없이 허용하는 엔드포인트들
                        .requestMatchers(HttpMethod.GET,
                                "/api/leds/*/status",           // 아두이노용 LED 상태 조회
                                "/api/aiArts",                  // AI Art 조회
                                "/api/posts",
                                "/api/posts/**",                // 특정 게시글 및 댓글 조회 허용
                                "/api/comments"                 // 전체 댓글 조회 허용
                        ).permitAll()

                        // 2. 인증없는 엔드포인트들
                        .requestMatchers(
                                "/api/auth/**",         // 회원가입
                                "/api/sensor_log/**",   // 센서 데이터 수집
                                "/api/photos/**"
                        ).permitAll()
                        // 3. 위에서 허용한 경로 외의 모든 요청은 반드시 인증이 필요함
                        .anyRequest().authenticated()
                )

                // 요청 헤더의 JWT를 검증하고 SecurityContext에 인증 정보를 설정하는 역할
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}