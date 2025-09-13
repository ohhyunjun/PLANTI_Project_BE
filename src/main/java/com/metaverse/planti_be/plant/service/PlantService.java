package com.metaverse.planti_be.plant.service;

import com.metaverse.planti_be.plant.domain.Plant;
import com.metaverse.planti_be.plant.dto.PlantRequestDto;
import com.metaverse.planti_be.plant.dto.PlantResponseDto;
import com.metaverse.planti_be.plant.repository.PlantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantService {

    private final PlantRepository plantRepository;

    @Transactional
    public PlantResponseDto createPlant(PlantRequestDto plantRequestDto) {
        Plant plant = new Plant(
                plantRequestDto.getName(),
                plantRequestDto.getSpecies(),
                plantRequestDto.getPlantedAt(),
                plantRequestDto.getPlantStage()
        );
        Plant savedPlant = plantRepository.save(plant);
        PlantResponseDto plantResponseDto = new PlantResponseDto(savedPlant);
        return plantResponseDto;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PlantResponseDto> getPlants() {
        List<PlantResponseDto> plantResponseDtoList = plantRepository
                .findAllByOrderByPlantedAtAsc()
                .stream()
                .map(PlantResponseDto::new)
                .toList();
        return plantResponseDtoList;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PlantResponseDto getPlantById(Long plantId) {
        Plant plant = findPlant(plantId);
        return new PlantResponseDto(plant);
    }

    @Transactional
    public PlantResponseDto updatePlant(Long plantId, PlantRequestDto plantRequestDto) {
        Plant plant = findPlant(plantId);
        plant.update(
                plantRequestDto.getName(),
                plantRequestDto.getSpecies(),
                plantRequestDto.getPlantStage()
        );
        return new PlantResponseDto(plant);
    }

    @Transactional
    public void deletePlant(Long plantId) {
        Plant plant = findPlant(plantId);
        plantRepository.delete(plant);
    }

    private Plant findPlant(Long plantId) {
        return plantRepository.findById(plantId).orElseThrow(()->
                new IllegalArgumentException("해당 식물은 존재하지 않습니다.")
        );
    }

}

