package com.metaverse.planti_be.sensor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SensorLogRequestDto {
    private String sensor_type;
    private String value;
    @JsonProperty("serial_number")
    private String serialNumber;
}