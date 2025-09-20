package com.metaverse.planti_be.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // "/**"는 모든 경로에 대해 CORS 설정을 적용한다는 의미입니다.
                .allowedOrigins("http://localhost:5173") // ◀◀ 자바스크립트 요청을 허용할 출처(프론트엔드 주소)를 명시합니다.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 허용할 HTTP 메서드를 지정합니다.
                .allowedHeaders("*") // 허용할 요청 헤더를 지정합니다. "*"는 모든 헤더를 허용합니다.
                .allowCredentials(true) // 쿠키와 같은 자격 증명 정보를 허용할지 여부를 설정합니다.
                .maxAge(3600); // 프리플라이트 요청의 결과를 캐시할 시간(초)을 설정합니다.
    }
}
