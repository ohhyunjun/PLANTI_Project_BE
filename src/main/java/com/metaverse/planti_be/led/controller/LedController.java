package com.metaverse.planti_be.led.controller;

import com.metaverse.planti_be.auth.domain.PrincipalDetails;
import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.led.dto.LedControlRequestDto;
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

    //사용자가 LED 설정을 변경하는 API (인증 필요)
    @PutMapping("/{serialNumber}")
    public ResponseEntity<String> controlLed(
            @PathVariable String serialNumber,
            @Valid @RequestBody LedControlRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        User user = principalDetails.getUser();
        ledService.updateLedSettings(serialNumber, requestDto, user);

        return ResponseEntity.ok("LED 설정이 성공적으로 변경되었습니다.");
    }

    //아두이노가 LED 설정을 조회하는 API (인증 불필요)
    @GetMapping("/{serialNumber}")
    public ResponseEntity<LedStatusResponseDto> getLedStatus(@PathVariable String serialNumber) {
        LedStatusResponseDto status = ledService.getLedStatus(serialNumber);
        return ResponseEntity.ok(status);
    }
}
