package com.metaverse.planti_be.device.service;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.device.dto.DeviceResponseDto;
import com.metaverse.planti_be.device.repository.DeviceRepository;
import com.metaverse.planti_be.notice.domain.Notice;
import com.metaverse.planti_be.notice.repository.NoticeRepository;
import com.metaverse.planti_be.plant.repository.PlantRepository;
import com.metaverse.planti_be.sensor.domain.SensorType;
import com.metaverse.planti_be.sensor.dto.SensorDataResponseDto;
import com.metaverse.planti_be.sensor.service.SensorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final PlantRepository plantRepository;
    private final SensorLogService sensorLogService;
    private final NoticeRepository noticeRepository;

    @Transactional
    public void registerDevice(String serialNumber, String deviceNickname, User user){
        // 1. 시리얼 번호로 DB에서 기기를 찾습니다. 없으면 예외를 발생시킵니다.
        Device device = deviceRepository.findById(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시리얼 번호입니다."));
        // 2. 기기에 이미 다른 유저가 등록되어 있는지 확인합니다. (status보다 이 방법이 더 확실합니다)
        if (device.getUser() != null) {
            throw new IllegalStateException("이미 등록된 기기입니다.");
        }
        // 3. 기기의 유저 정보를 현재 로그인한 유저로 설정합니다.
        device.setUser(user);

        // 4. 기기 닉네임을 설정하는 로직
        device.setDeviceNickname(deviceNickname);

        // 5. 기기의 상태를 변경합니다.
        device.setStatus(true);
    }

    // 사용자의 모든 기기 조회 로직 추가
    @Transactional(readOnly = true)
    public List<DeviceResponseDto> getUserDevices(User user) {
        List<Device> devices = deviceRepository.findByUserId(user.getId());

        return devices.stream().map(device -> {
            // 해당 기기에 연결된 식물이 있는지 확인
            DeviceResponseDto.PlantSummaryDto plantSummary = plantRepository.findByDeviceId(device.getId())
                    .map(plant -> new DeviceResponseDto.PlantSummaryDto(
                            plant.getId(),
                            plant.getName(),
                            plant.getSpecies().getName(),
                            plant.getPlantStage()
                    ))
                    .orElse(null);

            return new DeviceResponseDto(device, plantSummary);
        }).collect(Collectors.toList());
    }

    // 관리자용 기기 생성 메서드
    @Transactional
    public void createDeviceByAdmin(String serialNumber) {
        if (deviceRepository.existsById(serialNumber)) {
            throw new IllegalArgumentException("이미 존재하는 시리얼 번호입니다.");
        }

        // Device 생성 시 deviceNickname은 null로 전달
        Device newDevice = new Device(serialNumber, null);

        // Device만 저장하면 Cascade 옵션에 의해 Led도 함께 저장됩니다.
        deviceRepository.save(newDevice);
    }

    @Transactional
    public void deleteDevice(String serialNumber, User user) {
        Device device = deviceRepository.findById(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기기입니다."));

        // 소유권 확인
        if (device.getUser() == null || !device.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 기기에 대한 권한이 없습니다.");
        }

        // 해당 기기와 연관된 모든 알림을 먼저 삭제
        List<Notice> deviceNotices = noticeRepository.findByUserAndDevice(user, device);
        if (!deviceNotices.isEmpty()) {
            noticeRepository.deleteAll(deviceNotices);
            System.out.println("기기 삭제: " + deviceNotices.size() + "개의 알림이 삭제되었습니다.");
        }

        // 기기를 삭제하는 대신 소유권만 해제 (관리자가 처음 등록한 상태로 되돌림)
        device.setUser(null);               // 소유자 제거
        device.setDeviceNickname(null);     // 닉네임 제거
        device.setStatus(false);            // 상태를 미사용으로 변경

        // 연결된 식물이 있다면 같이 제거
        plantRepository.findByDeviceId(device.getId()).ifPresent(plant -> {
            plantRepository.delete(plant);
        });

        // device 자체는 삭제하지 않고 업데이트만 함
        deviceRepository.save(device);
    }

    @Transactional(readOnly = true)
    public DeviceResponseDto getDevice(String serialNumber, User user) {
        Device device = deviceRepository.findById(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기기입니다."));

        if (!device.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 기기에 대한 권한이 없습니다.");
        }

        DeviceResponseDto.PlantSummaryDto plantSummary = plantRepository.findByDeviceId(device.getId())
                .map(plant -> new DeviceResponseDto.PlantSummaryDto(
                        plant.getId(),
                        plant.getName(),
                        plant.getSpecies().getName(),
                        plant.getPlantStage()
                ))
                .orElse(null);

        return new DeviceResponseDto(device, plantSummary);
    }

    @Transactional(readOnly = true)
    public SensorDataResponseDto getSensorData(String serialNumber, User user) {
        // 소유권 확인
        Device device = deviceRepository.findById(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기기입니다."));

        if (!device.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 기기에 대한 권한이 없습니다.");
        }

        // 센서 데이터 평균 계산
        Double avgTemp = sensorLogService.getAverageSensorValue(serialNumber, SensorType.TEMPERATURE);
        Double avgHumidity = sensorLogService.getAverageSensorValue(serialNumber, SensorType.HUMIDITY);

        return new SensorDataResponseDto(avgTemp, avgHumidity);
    }



}
