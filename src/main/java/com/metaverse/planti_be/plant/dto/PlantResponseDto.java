package com.metaverse.planti_be.plant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.metaverse.planti_be.aiArt.dto.AiArtResponseDto;
import com.metaverse.planti_be.diary.dto.DiaryResponseDto;
import com.metaverse.planti_be.plant.domain.Plant;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class PlantResponseDto {
    private Long id;
    private String name;
    private String species;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime plantedAt;

    private List<DiaryResponseDto> diaries;
    private List<AiArtResponseDto> aiArts;

    public PlantResponseDto(Plant plant) {
        this.id = plant.getId();
        this.name = plant.getName();
        this.species = plant.getSpecies();
        this.plantedAt = plant.getPlantedAt();
        this.diaries = plant.getDiaries()
                .stream()
                .map(DiaryResponseDto::new)
                .collect(Collectors.toList()
                );
        this.aiArts = plant.getAiArts()
                .stream()
                .map(AiArtResponseDto::new)
                .collect(Collectors.toList()
                );
    }
}
