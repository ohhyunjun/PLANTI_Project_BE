package com.metaverse.planti_be.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaverse.planti_be.auth.dto.AuthResponseDto;
import com.metaverse.planti_be.auth.dto.CustomUserDetails;
import com.metaverse.planti_be.auth.dto.LoginRequestDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    //JWTUtil 주입
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/auth/login");
    }

    //인증 시도 메소드: JSON 요청을 처리하도록 오버라이드
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        LoginRequestDto loginRequestDto;

        try {
            // 요청 본문(body)의 JSON 데이터를 LoginRequestDto 객체로 변환
            String requestBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            loginRequestDto = objectMapper.readValue(requestBody, LoginRequestDto.class);
        } catch (IOException e) {
            // JSON 파싱 실패 시 예외 처리
            throw new RuntimeException("JSON 파싱 오류: " + e.getMessage());
        }

        String username = loginRequestDto.getUsername();
        String password = loginRequestDto.getPassword();

        // 3. Spring Security 인증 토큰 생성
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        // 4. AuthenticationManager에 인증을 위임
        return authenticationManager.authenticate(authToken);
    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException{
        //UserDetails 객체 추출
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        // 사용자의 name 추출
        String name = customUserDetails.getUsername();

        // 사용자의 Role 추출
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        //JWT 제작 및 만료 시간
        String token = jwtUtil.createJwt(name, role, 60*60*10L);
        // AuthResponseDto 객체 생성
        AuthResponseDto authResponse = new AuthResponseDto(name, token);

        // 응답 설정
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // 응답 본문에 DTO를 JSON으로 변환하여 작성
        response.getWriter().write(objectMapper.writeValueAsString(authResponse));
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(403);
    }
}