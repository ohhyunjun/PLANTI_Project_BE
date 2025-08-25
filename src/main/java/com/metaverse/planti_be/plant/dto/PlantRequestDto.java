package com.metaverse.planti_be.plant.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PlantRequestDto {
    private String name;
    private String species;
    private LocalDateTime plantedAt;
}
