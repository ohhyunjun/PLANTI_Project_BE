package com.metaverse.planti_be.led.dto;

import com.metaverse.planti_be.led.domain.Led;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class LedStatusResponseDto {
    private int intensity;
    private String startTime;
    private String endTime;

    public LedStatusResponseDto(Led led){
        this.intensity = led.getIntensity();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        this.startTime = led.getStartTime().format(formatter);
        this.endTime = led.getEndTime().format(formatter);
    }
}
