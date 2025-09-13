package com.metaverse.planti_be.led.service;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.device.repository.DeviceRepository;
import com.metaverse.planti_be.led.domain.Led;
import com.metaverse.planti_be.led.dto.LedControlRequestDto;
import com.metaverse.planti_be.led.dto.LedStatusResponseDto;
import com.metaverse.planti_be.led.repository.LedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LedService {
    private final LedRepository ledRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public void updateLedSettings(String serialNumber, LedControlRequestDto dto, User user){
        // 기기가 사용자의 소유인지 확인
        Device device = deviceRepository.findById(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기기입니다."));
        if (!device.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 기기에 대한 권한이 없습니다.");
        }

        // 기기와 연결된 Led 정보 조회
        Led led = ledRepository.findById(serialNumber)
                .orElseThrow(() -> new IllegalStateException("해당 기기의 LED 정보를 찾을 수 없습니다."));

        // 사용자 입력(1~5)을 아두이노 PWM 값(0~255)으로 변환
        int mappedIntensity = mapIntensity(dto.getIntensity());

        // Led 엔티티에 새로운 설정값 저장
        led.setIntensity(mappedIntensity);
        led.setStartTime(dto.getStartTime());
        led.setEndTime(dto.getEndTime());
    }

    @Transactional(readOnly = true)
    public LedStatusResponseDto getLedStatus(String serialNumber) {
        Led led = ledRepository.findById(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 기기의 LED 정보를 찾을 수 없습니다."));

        return new LedStatusResponseDto(led);
    }

    // 강도 매핑 로직 (1-5단계 -> 0-255 값)
    private int mapIntensity(int userIntensity) {
        if (userIntensity < 1 || userIntensity > 5) {
            return 0; // 범위를 벗어나면 끔
        }
        // 예: 1->51, 2->102, 3->153, 4->204, 5->255
        return (int) Math.round((userIntensity / 5.0) * 255.0);
    }
}
