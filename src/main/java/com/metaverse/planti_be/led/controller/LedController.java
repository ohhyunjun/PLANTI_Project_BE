package com.metaverse.planti_be.led.controller;

import com.metaverse.planti_be.auth.domain.PrincipalDetails;
import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.led.dto.LedControlRequestDto;
import com.metaverse.planti_be.led.dto.LedIntensityResponseDto; // 새로 만든 DTO 임포트
import com.metaverse.planti_be.led.dto.LedStatusResponseDto;
import com.metaverse.planti_be.led.service.LedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leds")
@RequiredArgsConstructor
public class LedController {

    private final LedService ledService;

    // 사용자가 LED 설정을 변경하는 API (인증 필요)
    @PutMapping("/{serialNumber}")
    public ResponseEntity<String> controlLed(
            @PathVariable String serialNumber,
            @Valid @RequestBody LedControlRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        User user = principalDetails.getUser();
        ledService.updateLedSettings(serialNumber, requestDto, user);

        return ResponseEntity.ok("LED 설정이 성공적으로 변경되었습니다.");
    }

    // --- 아두이노가 현재 LED 밝기 값을 조회하는 API (인증 불필요) ---
    // GET /api/leds/{serialNumber}/status
    @GetMapping("/{serialNumber}/status")
    public ResponseEntity<LedIntensityResponseDto> getCurrentLedIntensity(@PathVariable String serialNumber) {
        LedIntensityResponseDto responseDto = ledService.getCurrentIntensity(serialNumber);
        return ResponseEntity.ok(responseDto);
    }

    // --- 사용자가 현재 LED 설정 값을 조회하는 API (인증 필요) ---
    // GET /api/leds/{serialNumber}
    @GetMapping("/{serialNumber}")
    public ResponseEntity<LedStatusResponseDto> getLedSettings(@PathVariable String serialNumber, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        // 본인 기기인지 확인하는 로직이 서비스 단에 필요할 수 있으나, 여기서는 일단 조회만 구현합니다.
        // LedService에 getLedSettings에 대한 권한 검사 로직을 추가하는 것을 권장합니다.
        LedStatusResponseDto responseDto = ledService.getLedSettings(serialNumber);
        return ResponseEntity.ok(responseDto);
    }
}