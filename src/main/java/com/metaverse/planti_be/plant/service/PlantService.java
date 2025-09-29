package com.metaverse.planti_be.plant.service;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.device.repository.DeviceRepository;
import com.metaverse.planti_be.plant.domain.Plant;
import com.metaverse.planti_be.plant.dto.PlantRequestDto;
import com.metaverse.planti_be.plant.dto.PlantResponseDto;
import com.metaverse.planti_be.plant.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlantService {

    private final PlantRepository plantRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public PlantResponseDto createPlant(PlantRequestDto plantRequestDto, User user) {
        // 1. 디바이스 존재 및 소유권 확인
        Device device = findDeviceOwnedByUser(plantRequestDto.getSerialNumber(), user);

        // 2. 해당 디바이스에 이미 식물이 등록되어 있는지 확인
        if (plantRepository.existsByDeviceId(device.getId())) {
            throw new IllegalArgumentException("해당 디바이스에는 이미 식물이 등록되어 있습니다.");
        }

        Plant plant = new Plant(
                plantRequestDto.getName(),
                plantRequestDto.getSpecies(),
                plantRequestDto.getPlantedAt(),
                plantRequestDto.getStage(),
                device
        );
        Plant savedPlant = plantRepository.save(plant);
        return new PlantResponseDto(savedPlant);
    }

    public List<PlantResponseDto> getPlants(User user) {
        return plantRepository.findByUserIdOrderByPlantedAtAsc(user.getId())
                .stream()
                .map(PlantResponseDto::new)
                .toList();
    }

    public PlantResponseDto getPlantById(Long plantId, User user) {
        Plant plant = findPlantOwnedByUser(plantId, user);
        return new PlantResponseDto(plant);
    }

    @Transactional
    public PlantResponseDto updatePlant(Long plantId, PlantRequestDto plantRequestDto, User user) {
        Plant plant = findPlantOwnedByUser(plantId, user);

        plant.update(
                plantRequestDto.getName(),
                plantRequestDto.getSpecies(),
                plantRequestDto.getStage()
        );
        return new PlantResponseDto(plant);
    }

    @Transactional
    public PlantResponseDto patchPlant(Long plantId, PlantRequestDto plantRequestDto, User user) {
        Plant plant = findPlantOwnedByUser(plantId, user);

        // 이름이 null이 아니면 업데이트
        if (plantRequestDto.getName() != null) {
            plant.setName(plantRequestDto.getName());
        }
        // 종류가 null이 아니면 업데이트
        if (plantRequestDto.getSpecies() != null) {
            plant.setSpecies(plantRequestDto.getSpecies());
        }
        // 단계가 null이 아니면 업데이트
        if (plantRequestDto.getStage() != null) {
            plant.setPlantStage(plantRequestDto.getStage());
        }
        if (plantRequestDto.getPlantedAt() != null) {
            plant.setPlantStage(plantRequestDto.getStage());
        }
        return new PlantResponseDto(plant);
    }

    @Transactional
    public void deletePlant(Long plantId, User user) {
        Plant plant = findPlantOwnedByUser(plantId, user);
        plantRepository.delete(plant);
    }

    // 사용자 소유의 식물 찾기
    private Plant findPlantOwnedByUser(Long plantId, User user) {
        return plantRepository.findByIdAndUserId(plantId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 식물을 찾을 수 없거나 접근 권한이 없습니다."));
    }

    // 사용자 소유의 디바이스 찾기
    private Device findDeviceOwnedByUser(String deviceSerial, User user) {
        Device device = deviceRepository.findById(deviceSerial)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 디바이스입니다: " + deviceSerial));

        if (!device.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 디바이스에 대한 권한이 없습니다.");
        }

        return device;
    }
}