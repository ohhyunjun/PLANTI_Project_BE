package com.metaverse.planti_be.board;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class BoardController {
    @GetMapping("some-test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getAdminData(){
        return ResponseEntity.ok("축하합니다! ADMIN 권한으로 보호된 API에 성공적으로 접근했습니다.");
    }
}
