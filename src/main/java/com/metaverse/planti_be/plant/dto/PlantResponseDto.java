package com.metaverse.planti_be.plant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.metaverse.planti_be.diary.dto.DiaryResponseDto;
import com.metaverse.planti_be.plant.domain.Plant;
import com.metaverse.planti_be.plant.domain.PlantStage;
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
    private PlantStage plantStage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime plantedAt;

    // 디바이스 정보
    private String deviceSerial;
    private String deviceNickname;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 다이어리만 포함 (aiArts 제거)
    private List<DiaryResponseDto> diaries;

    public PlantResponseDto(Plant plant) {
        this.id = plant.getId();
        this.name = plant.getName();
        this.species = plant.getSpecies();
        this.plantStage = plant.getPlantStage();
        this.plantedAt = plant.getPlantedAt();
        this.createdAt = plant.getCreatedAt();
        this.updatedAt = plant.getUpdatedAt();

        // 디바이스 정보
        this.deviceSerial = plant.getDevice().getId();
        this.deviceNickname = plant.getDevice().getDeviceNickname();

        // 다이어리만 매핑
        this.diaries = plant.getDiaries()
                .stream()
                .map(DiaryResponseDto::new)
                .collect(Collectors.toList());
    }
}
