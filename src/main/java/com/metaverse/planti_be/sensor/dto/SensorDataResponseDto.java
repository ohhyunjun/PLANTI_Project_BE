package com.metaverse.planti_be.sensor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SensorDataResponseDto {
    private Double temperature; // 온도 평균
    private Double humidity; // 수분 평균
}
