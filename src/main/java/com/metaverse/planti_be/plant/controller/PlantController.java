package com.metaverse.planti_be.plant.controller;

import com.metaverse.planti_be.plant.dto.PlantRequestDto;
import com.metaverse.planti_be.plant.dto.PlantResponseDto;
import com.metaverse.planti_be.plant.service.PlantService;
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
    public PlantResponseDto createPlant(PlantRequestDto plantRequestDto) {
        return plantService.createPlant(plantRequestDto);
    }

    @GetMapping("/plants")
    public List<PlantResponseDto> getPlants() {
        return plantService.getPlants();
    }

    @PutMapping("/plants/{plantId}")
    public Long updatePlant(@PathVariable Long plantId, PlantRequestDto plantRequestDto) {
        return plantService.updatePlant(plantId, plantRequestDto);
    }

    @DeleteMapping("/plants/{plantId}")
    public Long deletePlant(@PathVariable Long plantId) {
        return plantService.deletePlant(plantId);
    }
}
