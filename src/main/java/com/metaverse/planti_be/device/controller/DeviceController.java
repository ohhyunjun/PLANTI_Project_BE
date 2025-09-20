package com.metaverse.planti_be.device.controller;

import com.metaverse.planti_be.auth.domain.PrincipalDetails;
import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.device.dto.DeviceCreateRequestDto;
import com.metaverse.planti_be.device.dto.DeviceRegistrationRequestDto;
import com.metaverse.planti_be.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
            @RequestBody DeviceCreateRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        try {
            User currentUser = principalDetails.getUser();

            // 서비스 로직 호출 (닉네임 추가)
            deviceService.registerDevice(requestDto.getSerialNumber(), requestDto.getDeviceNickname(), currentUser);

            return ResponseEntity.ok("기기가 성공적으로 등록되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    // 관리자용 기기 생성 API
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createDevice(@RequestBody DeviceRegistrationRequestDto requestDto) {
        try {
            // 서비스 로직 호출 (시리얼 번호만 전달)
            deviceService.createDeviceByAdmin(requestDto.getSerialNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body("기기가 성공적으로 생성되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
