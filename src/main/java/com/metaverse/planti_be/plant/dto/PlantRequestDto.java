package com.metaverse.planti_be.plant.dto;

import com.metaverse.planti_be.plant.domain.PlantStage;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PlantRequestDto {
    private String name;

    // Species ID로 변경 (String species -> Long speciesId)
    private Long speciesId;

    private LocalDateTime plantedAt;
    private PlantStage stage;
    private String serialNumber;
}