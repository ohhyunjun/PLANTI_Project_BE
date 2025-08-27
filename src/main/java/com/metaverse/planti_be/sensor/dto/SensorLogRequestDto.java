package com.metaverse.planti_be.sensor.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SensorLogRequestDto {
    private String sensor_type;
    private String value;
    private String serialNumber;
}