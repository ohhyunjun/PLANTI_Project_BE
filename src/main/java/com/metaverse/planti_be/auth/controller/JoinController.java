package com.metaverse.planti_be.auth.controller;

import com.metaverse.planti_be.auth.dto.JoinDTO;
import com.metaverse.planti_be.auth.service.JoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class JoinController {

    private final JoinService joinService;

    @PostMapping("/signup")
    // JSON 요청을 받기 위해 @RequestBody 추가
    public ResponseEntity<String> joinProcess(@RequestBody JoinDTO joinDTO) {
        try {
            joinService.joinProcess(joinDTO);
            // 성공 시 201 Created 상태와 메시지 반환
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 성공적으로 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            // JoinService에서 발생시킨 예외를 잡아 400 Bad Request로 응답
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}