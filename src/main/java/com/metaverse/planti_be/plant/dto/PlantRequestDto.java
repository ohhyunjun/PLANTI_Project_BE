package com.metaverse.planti_be.plant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metaverse.planti_be.plant.domain.PlantStage;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PlantRequestDto {
    private String name;
    private String species;
    @JsonProperty("planted_at")
    private LocalDateTime plantedAt;

    private PlantStage stage;

    @JsonProperty("serial_number")
    private String serialNumber;
}