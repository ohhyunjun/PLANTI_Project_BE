package com.metaverse.planti_be.device.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.metaverse.planti_be.device.domain.Device;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DeviceResponseDto {

    private String serialNumber;
    private String deviceNickname;
    private Boolean status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 식물 정보 (null일 수 있음)
    private PlantSummaryDto plant;

    public DeviceResponseDto(Device device, PlantSummaryDto plant) {
        this.serialNumber = device.getId();
        this.deviceNickname = device.getDeviceNickname();
        this.status = device.getStatus();
        this.createdAt = device.getCreatedAt();
        this.updatedAt = device.getUpdatedAt();
        this.plant = plant;
    }

    // 식물 요약 정보만을 담는 내부 클래스
    @Getter
    @AllArgsConstructor
    public static class PlantSummaryDto {
        private Long id;
        private String name;
        private String species;
    }
}