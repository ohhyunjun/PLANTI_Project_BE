package com.metaverse.planti_be.sensor.service;

import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.device.repository.DeviceRepository;
import com.metaverse.planti_be.sensor.domain.SensorLog;
import com.metaverse.planti_be.sensor.domain.SensorType;
import com.metaverse.planti_be.sensor.dto.SensorLogRequestDto;
import com.metaverse.planti_be.sensor.repository.SensorLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorLogService {

    private final SensorLogRepository sensorLogRepository;
    private final DeviceRepository deviceRepository; // Device 엔티티를 찾기 위해 필요

    // DTO를 받아 로그를 생성하고 저장하는 메서드
    @Transactional
    public void createSensorLog(SensorLogRequestDto sensorLogDto) {
        // 1. DTO의 serial_number로 Device 엔티티를 찾습니다.
        //    (해당 장치가 없으면 예외 발생)
        Device device = deviceRepository.findById(sensorLogDto.getSerialNumber())
                .orElseThrow(() -> new IllegalArgumentException("해당 시리얼 넘버를 가진 장치가 존재하지 않습니다."));

        // 2. 새로운 SensorLog 엔티티를 생성합니다.
        SensorLog newLog = new SensorLog(sensorLogDto);

        // 3. 찾은 Device 엔티티를 SensorLog에 연결합니다.
        newLog.setDevice(device);

        // 4. SensorLog를 데이터베이스에 저장합니다.
        sensorLogRepository.save(newLog);
    }

    public Double getAverageSensorValue(String serialNumber, SensorType sensorType) {
        List<SensorLog> logs = sensorLogRepository.findTop10ByDeviceAndSensorType(
                serialNumber,
                sensorType,
                PageRequest.of(0, 10)
        );

        if (logs.isEmpty()) {
            return null; // 데이터 없음
        }

        // 평균 계산
        double sum = logs.stream()
                .mapToDouble(log -> Double.parseDouble(log.getValue()))
                .sum();

        return sum / logs.size();
    }
}
