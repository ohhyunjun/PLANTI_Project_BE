package com.metaverse.planti_be.device.controller;

import com.metaverse.planti_be.auth.domain.PrincipalDetails;
import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.device.dto.DeviceRegistrationRequestDto;
import com.metaverse.planti_be.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @PostMapping("/register")
    public ResponseEntity<String> registerDevice(
            @RequestBody DeviceRegistrationRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        try {
            // @AuthenticationPrincipal 어노테이션으로 현재 로그인된 유저 정보를 가져옵니다.
            User currentUser = principalDetails.getUser();

            // 서비스 로직 호출
            deviceService.registerDevice(requestDto.getSerialNumber(), currentUser);

            return ResponseEntity.ok("기기가 성공적으로 등록되었습니다.");

        } catch (IllegalArgumentException e) { // DeviceService에서 기기를 못 찾은 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) { // DeviceService에서 이미 등록된 기기인 경우
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
