package com.metaverse.planti_be.auth.jwt;


import com.metaverse.planti_be.auth.dto.CustomUserDetails;
import com.metaverse.planti_be.auth.entity.UserEntity;
import com.metaverse.planti_be.auth.entity.UserRole;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Spring의 Bean으로 등록
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동 생성
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserDetailsService userDetailsService; // DB에서 사용자 정보를 가져오기 위한 의존성 추가

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //request에서 Authorization 헤더를 찾음
        final String authorization= request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("token null"); // 토큰이 없으면 다음 필터로 진행
            filterChain.doFilter(request, response);
            return;
        }

        //Bearer 부분 제거 후 순수 토큰만 획득
        final String token = authorization.substring(7);
        final String username;
        try {
            // 3. JWT에서 사용자 이름(username)을 추출합니다.
            // 🚨 여기서 만료, 서명 오류 등 발생 시 JwtException을 catch합니다.
            username = jwtUtil.extractUsername(token); // 이 메소드는 JWTUtil에 구현되어 있어야 합니다.
        } catch (JwtException e) {
            // 4. 예외 발생 시, 즉시 401 Unauthorized 응답을 보내고 필터 체인을 중단합니다.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"유효하지 않거나 만료된 토큰입니다.\"}");
            return; // 메소드 종료
        }

        // 5. 사용자 이름이 존재하고, 아직 SecurityContext에 인증 정보가 없는 경우에만 인증을 진행합니다.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. UserDetailsService를 통해 DB에서 사용자 정보를 가져옵니다.
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 7. 토큰이 유효한지 최종 검증합니다. (사용자 정보 일치, 만료 여부 등)
            // 이 메소드는 JWTUtil에 구현되어 있어야 합니다.
            if (jwtUtil.validateToken(token, userDetails)) {

                // 8. 유효한 토큰이면, Spring Security가 사용할 인증 토큰(Authentication)을 생성합니다.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // JWT 방식에서는 비밀번호(Credentials)를 사용하지 않으므로 null입니다.
                        userDetails.getAuthorities()
                );

                // 9. 요청에 대한 세부 정보를 인증 토큰에 추가합니다.
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 10. SecurityContextHolder에 인증 정보를 설정하여, 현재 요청을 '인증된 상태'로 만듭니다.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }


        filterChain.doFilter(request, response);
    }
}
