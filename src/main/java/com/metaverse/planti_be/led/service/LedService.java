package com.metaverse.planti_be.led.service;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.device.repository.DeviceRepository;
import com.metaverse.planti_be.led.domain.Led;
import com.metaverse.planti_be.led.dto.LedControlRequestDto;
import com.metaverse.planti_be.led.dto.LedIntensityResponseDto;
import com.metaverse.planti_be.led.dto.LedStatusResponseDto;
import com.metaverse.planti_be.led.repository.LedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class LedService {
    private final LedRepository ledRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public void updateLedSettings(String serialNumber, LedControlRequestDto dto, User user){
        Device device = deviceRepository.findById(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기기입니다."));
        if (!device.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 기기에 대한 권한이 없습니다.");
        }

        Led led = ledRepository.findById(serialNumber)
                .orElseThrow(() -> new IllegalStateException("해당 기기의 LED 정보를 찾을 수 없습니다."));

        int mappedIntensity = mapIntensity(dto.getIntensity());

        led.setIntensity(mappedIntensity);
        led.setStartTime(dto.getStartTime());
        led.setEndTime(dto.getEndTime());
    }

    // --- 아두이노가 호출하는 메소드 ---
    // 이름 변경: getLedSettings -> getCurrentIntensity
    @Transactional(readOnly = true)
    public LedIntensityResponseDto getCurrentIntensity(String serialNumber) {
        Led led = ledRepository.findById(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 기기의 LED 정보를 찾을 수 없습니다."));

        LocalTime now = LocalTime.now();
        LocalTime startTime = led.getStartTime();
        LocalTime endTime = led.getEndTime();

        int currentIntensity = 0;

        if (!startTime.equals(endTime)) {
            if (startTime.isBefore(endTime)) {
                if (now.isAfter(startTime) && now.isBefore(endTime)) {
                    currentIntensity = led.getIntensity();
                }
            }
            else {
                if (now.isAfter(startTime) || now.isBefore(endTime)) {
                    currentIntensity = led.getIntensity();
                }
            }
        }

        return new LedIntensityResponseDto(currentIntensity);
    }

    // --- 사용자가 호출하는 메소드 ---
    // 이름 변경: getLedStatus -> getLedSettings
    @Transactional(readOnly = true)
    public LedStatusResponseDto getLedSettings(String serialNumber, User user) {
        // 1. 기기 존재 및 소유권 확인
        Device device = deviceRepository.findById(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기기입니다."));

        if (!device.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 기기에 대한 권한이 없습니다.");
        }

        // 2. LED 정보 조회
        Led led = ledRepository.findById(serialNumber)
                .orElseThrow(() -> new IllegalStateException("해당 기기의 LED 정보를 찾을 수 없습니다."));

        return new LedStatusResponseDto(led);
    }

    private int mapIntensity(int userIntensity) {
        switch (userIntensity) {
            case 1: return 31;
            case 2: return 82;
            case 3: return 143;
            case 4: return 200;
            case 5: return 255;
            default: return 0;
        }
    }
}