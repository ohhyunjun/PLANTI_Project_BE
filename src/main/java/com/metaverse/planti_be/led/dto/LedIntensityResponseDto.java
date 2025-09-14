package com.metaverse.planti_be.led.dto;

import lombok.Getter;

@Getter
public class LedIntensityResponseDto {
    private int intensity;

    public LedIntensityResponseDto(int intensity) {
        this.intensity = intensity;
    }
}