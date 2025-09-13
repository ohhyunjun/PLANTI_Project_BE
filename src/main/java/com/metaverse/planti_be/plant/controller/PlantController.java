package com.metaverse.planti_be.plant.controller;

import com.metaverse.planti_be.plant.dto.PlantRequestDto;
import com.metaverse.planti_be.plant.dto.PlantResponseDto;
import com.metaverse.planti_be.plant.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlantController {

    private final PlantService plantService;

    // 식물 등록하기
    @PostMapping("/plants")
    public ResponseEntity<PlantResponseDto> createPlant(
            @RequestBody PlantRequestDto plantRequestDto) {
        PlantResponseDto plantResponseDto = plantService.createPlant(plantRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(plantResponseDto);
    }

    // 전체 식물 불러오기
    @GetMapping("/plants")
    public ResponseEntity<List<PlantResponseDto>> getPlants() {
        List<PlantResponseDto> plantResponseDtoList = plantService.getPlants();
        return ResponseEntity.ok(plantResponseDtoList);
    }

    // 특정 식물 불러오기
    @GetMapping("/plants/{plantId}")
    public ResponseEntity<PlantResponseDto> getPlantById(
            @PathVariable Long plantId) {
        PlantResponseDto plantResponseDto = plantService.getPlantById(plantId);
        return ResponseEntity.ok(plantResponseDto);
    }

    // 특정 식물 수정하기
    @PutMapping("/plants/{plantId}")
    public ResponseEntity<PlantResponseDto> updatePlant(
            @PathVariable Long plantId, PlantRequestDto plantRequestDto) {
        PlantResponseDto updatePlant = plantService.updatePlant(plantId, plantRequestDto);
        return ResponseEntity.ok(updatePlant);
    }

    // 특정 식물 삭제하기
    @DeleteMapping("/plants/{plantId}")
    public ResponseEntity<Void> deletePlant(
            @PathVariable Long plantId) {
        plantService.deletePlant(plantId);
        return ResponseEntity.noContent().build();
    }
}
