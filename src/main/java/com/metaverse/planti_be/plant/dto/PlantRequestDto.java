package com.metaverse.planti_be.plant.dto;

import com.metaverse.planti_be.plant.domain.PlantStage;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PlantRequestDto {
    private String name;
    private String species;
    private LocalDateTime plantedAt;
    private PlantStage plantStage;
}
