package com.metaverse.planti_be.led.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

import java.time.LocalTime;

@Getter
public class LedControlRequestDto {
    @Min(value = 0)
    @Max(value = 5)
    private int intensity;

    //"HH:mm"형식으로 시간을 받음
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
}
