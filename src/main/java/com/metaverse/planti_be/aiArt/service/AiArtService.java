package com.metaverse.planti_be.aiArt.service;

import com.metaverse.planti_be.aiArt.domain.AiArt;
import com.metaverse.planti_be.aiArt.dto.AiArtRequestDto;
import com.metaverse.planti_be.aiArt.dto.AiArtResponseDto;
import com.metaverse.planti_be.aiArt.repository.AiArtRepository;
import com.metaverse.planti_be.plant.domain.Plant;
import com.metaverse.planti_be.plant.repository.PlantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiArtService {

    private final AiArtRepository aiArtRepository;
    private final PlantRepository plantRepository;

    @Transactional
    public AiArtResponseDto createAiArt(Long userId, Long plantId, AiArtRequestDto aiArtRequestDto) {
        Plant plant = findPlantOwnedByUser(plantId, userId);

        AiArt aiArt = new AiArt(
                aiArtRequestDto.getOriginalImageUrl(),
                aiArtRequestDto.getArtImageUrl(),
                aiArtRequestDto.getStyle(),
                plant
        );
        AiArt savedAiArt = aiArtRepository.save(aiArt);
        return new AiArtResponseDto(savedAiArt);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<AiArtResponseDto> getAiArts() {
        return aiArtRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AiArtResponseDto::new)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<AiArtResponseDto> getAiArtsByPlantId(Long userId, Long plantId) {
        findPlantOwnedByUser(plantId, userId);
        return aiArtRepository.findByPlantIdOrderByCreatedAtDesc(plantId).stream()
                .map(AiArtResponseDto::new)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public AiArtResponseDto getAiArtById(Long userId, Long plantId, Long aiArtId) {
        Plant plant = findPlantOwnedByUser(plantId, userId);
        AiArt aiArt = findAiArtByPlant(aiArtId, plant.getId());
        return new AiArtResponseDto(aiArt);
    }

    @Transactional
    public AiArtResponseDto updateAiArt(Long userId, Long plantId, Long aiArtId, AiArtRequestDto aiArtRequestDto) {
        Plant plant = findPlantOwnedByUser(plantId, userId);
        AiArt aiArt = findAiArtByPlant(aiArtId, plant.getId());

        aiArt.update(
                aiArtRequestDto.getOriginalImageUrl(),
                aiArtRequestDto.getStyle()
        );
        return new AiArtResponseDto(aiArt);
    }

    @Transactional
    public void deleteAiArt(Long userId, Long plantId, Long aiArtId) {
        Plant plant = findPlantOwnedByUser(plantId, userId);
        AiArt aiArt = findAiArtByPlant(aiArtId, plant.getId());

        aiArtRepository.delete(aiArt);
    }

    private Plant findPlantOwnedByUser(Long plantId, Long userId) {
        return plantRepository.findByIdAndUserId(plantId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식물을 찾을 수 없거나 접근 권한이 없습니다."));
    }

    private AiArt findAiArtByPlant(Long aiArtId, Long plantId) {
        return aiArtRepository.findByIdAndPlantId(aiArtId, plantId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아트를 찾을 수 없습니다."));
    }

}
