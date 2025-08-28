package com.metaverse.planti_be.plant.controller;

import com.metaverse.planti_be.diary.dto.DiaryResponseDto;
import com.metaverse.planti_be.plant.dto.PlantRequestDto;
import com.metaverse.planti_be.plant.dto.PlantResponseDto;
import com.metaverse.planti_be.plant.service.PlantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PlantController {
    private final PlantService plantService;

    public PlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    @PostMapping("/plants")
    public ResponseEntity<PlantResponseDto> createPlant(@RequestBody PlantRequestDto plantRequestDto) {
        PlantResponseDto plantResponseDto = plantService.createPlant(plantRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(plantResponseDto);
    }

    @GetMapping("/plants")
    public ResponseEntity<List<PlantResponseDto>> getPlants() {
        List<PlantResponseDto> plantResponseDtoList = plantService.getPlants();
        return ResponseEntity.ok(plantResponseDtoList);
    }

    @PutMapping("/plants/{plantId}")
    public ResponseEntity<PlantResponseDto> updatePlant(@PathVariable Long plantId, PlantRequestDto plantRequestDto) {
        PlantResponseDto updatePlant = plantService.updatePlant(plantId, plantRequestDto);
        return ResponseEntity.ok(updatePlant);
    }

    @DeleteMapping("/plants/{plantId}")
    public ResponseEntity<Void> deletePlant(@PathVariable Long plantId) {
        plantService.deletePlant(plantId);
        return ResponseEntity.noContent().build();
    }
}
