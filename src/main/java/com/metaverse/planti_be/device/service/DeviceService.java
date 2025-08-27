package com.metaverse.planti_be.device.service;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.device.dto.DeviceCreateRequestDto;
import com.metaverse.planti_be.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;

    @Transactional
    public void registerDevice(String serialNumber, User user){
        // 1. 시리얼 번호로 DB에서 기기를 찾습니다. 없으면 예외를 발생시킵니다.
        Device device = deviceRepository.findById(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시리얼 번호입니다."));
        // 2. 기기에 이미 다른 유저가 등록되어 있는지 확인합니다. (status보다 이 방법이 더 확실합니다)
        if (device.getUser() != null) {
            throw new IllegalStateException("이미 등록된 기기입니다.");
        }
        // 3. 기기의 유저 정보를 현재 로그인한 유저로 설정합니다.
        device.setUser(user);

        // 4. 기기의 상태를 변경합니다.
        device.setStatus(true);
    }
    // 관리자용 기기 생성 메서드
    @Transactional
    public void createDeviceByAdmin(DeviceCreateRequestDto requestDto) {
        if (deviceRepository.existsById(requestDto.getSerialNumber())) {
            throw new IllegalArgumentException("이미 존재하는 시리얼 번호입니다.");
        }

        // Device 생성자에 시리얼 번호와 닉네임을 전달
        // 생성자 내부에서 createLed()가 호출되어 Led도 함께 생성됩니다.
        Device newDevice = new Device(requestDto.getSerialNumber(), requestDto.getDeviceNickname());

        // Device만 저장하면 Cascade 옵션에 의해 Led도 함께 저장됩니다.
        deviceRepository.save(newDevice);
    }
}
