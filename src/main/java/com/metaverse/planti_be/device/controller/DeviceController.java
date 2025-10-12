package com.metaverse.planti_be.device.controller;

import com.metaverse.planti_be.auth.domain.PrincipalDetails;
import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.device.dto.DeviceCreateRequestDto;
import com.metaverse.planti_be.device.dto.DeviceRegistrationRequestDto;
import com.metaverse.planti_be.device.dto.DeviceResponseDto;
import com.metaverse.planti_be.device.service.DeviceService;
import com.metaverse.planti_be.sensor.dto.SensorDataResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    // 사용자의 모든 기기 조회 엔드포인트 추가
    @GetMapping
    public ResponseEntity<List<DeviceResponseDto>> getUserDevices(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        User currentUser = principalDetails.getUser();
        List<DeviceResponseDto> devices = deviceService.getUserDevices(currentUser);
        return ResponseEntity.ok(devices);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerDevice(
            @Valid @RequestBody DeviceCreateRequestDto requestDto,
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
    @PostMapping
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
    @DeleteMapping("/{serialNumber}")
    public ResponseEntity<String> deleteDevice(
            @PathVariable String serialNumber,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        try {
            User currentUser = principalDetails.getUser();
            deviceService.deleteDevice(serialNumber, currentUser);
            return ResponseEntity.ok("기기 연결이 성공적으로 해제되었습니다."); // 이 부분만 수정
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // 특정 기기 조회
    @GetMapping("/{serialNumber}")
    public ResponseEntity<DeviceResponseDto> getDevice(
            @PathVariable String serialNumber,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        try {
            User currentUser = principalDetails.getUser();
            DeviceResponseDto device = deviceService.getDevice(serialNumber, currentUser);
            return ResponseEntity.ok(device);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    // 센서 데이터 조회
    @GetMapping("/{serialNumber}/sensors")
    public ResponseEntity<SensorDataResponseDto> getSensorData(
            @PathVariable String serialNumber,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        try {
            User currentUser = principalDetails.getUser();
            SensorDataResponseDto sensorData = deviceService.getSensorData(serialNumber, currentUser);
            return ResponseEntity.ok(sensorData);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }
}
