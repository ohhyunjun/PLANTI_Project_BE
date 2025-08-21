package com.metaverse.planti_be.auth.config;

import com.metaverse.planti_be.auth.jwt.JWTFilter;
import com.metaverse.planti_be.auth.jwt.JWTUtil;
import com.metaverse.planti_be.auth.jwt.LoginFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // Spring Security를 활성화하는 어노테이션
@RequiredArgsConstructor
public class SecurityConfig {
    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    // JWTFilter가 의존하는 UserDetailsService를 주입받음
    private final UserDetailsService userDetailsService;

    // 비밀번호 인코더 (BCrypt)를 Bean으로 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // SecurityFilterChain을 수동 Bean으로 등록하여 HTTP 보안 규칙 정의
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        //csrf disable
        http
                .csrf((auth) -> auth.disable());

        //From 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());

        //http basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());

        //경로별 인가 작업
        http
                // 인가(Authorization, 엔드포인트의 접근 권한) 규칙 정의:
                .authorizeHttpRequests(authorize -> authorize
                        // 회원가입 및 로그인 API는 인증 없이 접근을 허용
                        .requestMatchers("/api/auth/**").permitAll()
                        // 그 외의 모든 요청은 인증을 요구(==로그인상태 == jwt토큰여부)
                        // (향후 JWT 필터를 통해 인증될 예정)
                        .anyRequest().authenticated()
                );
        // JWT 검증 필터(JWTFilter)를 등록
        http
                .addFilterBefore(new JWTFilter(jwtUtil, userDetailsService), LoginFilter.class);

        //세션 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
