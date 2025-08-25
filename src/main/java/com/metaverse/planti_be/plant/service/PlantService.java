package com.metaverse.planti_be.plant.service;

import com.metaverse.planti_be.plant.domain.Plant;
import com.metaverse.planti_be.plant.dto.PlantRequestDto;
import com.metaverse.planti_be.plant.dto.PlantResponseDto;
import com.metaverse.planti_be.plant.repository.PlantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
public class PlantService {
    private final PlantRepository plantRepository;

    public PlantService(PlantRepository plantRepository) {
        this.plantRepository = plantRepository;
    }

    @Transactional
    public PlantResponseDto createPlant(PlantRequestDto plantRequestDto) {
        Plant plant = new Plant(
                plantRequestDto.getName(),
                plantRequestDto.getSpecies(),
                plantRequestDto.getPlantedAt()
        );
        Plant savedPlant = plantRepository.save(plant);
        PlantResponseDto plantResponseDto = new PlantResponseDto(savedPlant);
        return  plantResponseDto;
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

    @Transactional
    public PlantResponseDto updatePlant(Long plantId, PlantRequestDto plantRequestDto) {
        Plant plant = findPlant(plantId);
        plant.update(
                plantRequestDto.getName(),
                plantRequestDto.getSpecies()
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


    // enum 상태에서 초기상태를 설정해줘야한다.

}
