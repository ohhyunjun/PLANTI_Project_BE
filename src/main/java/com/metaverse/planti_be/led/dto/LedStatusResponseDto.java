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
        // DB의 0~255 값을 1~5로 역변환
        this.intensity = reverseMapIntensity(led.getIntensity());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        this.startTime = led.getStartTime().format(formatter);
        this.endTime = led.getEndTime().format(formatter);
    }

    // 255 범위를 1~5로 역변환
    private int reverseMapIntensity(int dbIntensity) {
        if (dbIntensity == 0) return 0;
        if (dbIntensity <= 31) return 1;
        if (dbIntensity <= 82) return 2;
        if (dbIntensity <= 143) return 3;
        if (dbIntensity <= 200) return 4;
        return 5;
    }
}
