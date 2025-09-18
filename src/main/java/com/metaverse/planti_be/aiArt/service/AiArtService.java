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
    public AiArtResponseDto createAiArt(Long plantId, AiArtRequestDto aiArtRequestDto) {
        Plant plant = plantRepository.findById(plantId).orElseThrow(()->
                new IllegalArgumentException("해당 식물을 찾을 수 없습니다. Plant Id: " + plantId)
        );
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
    public List<AiArtResponseDto> getAiArtsByPlantId(Long plantId) {
        plantRepository.findById(plantId).orElseThrow(()->
                new IllegalArgumentException("해당 식물을 찾을 수 없습니다. Plant ID: " +  plantId)
        );
        return aiArtRepository.findByPlantIdOrderByCreatedAtDesc(plantId).stream()
                .map(AiArtResponseDto::new)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public AiArtResponseDto getAiArtById(Long plantId, Long aiArtId) {
        AiArt aiArt = findAiArtByPlantIdAndAiArtId(plantId, aiArtId);
        return new AiArtResponseDto(aiArt);
    }

    @Transactional
    public AiArtResponseDto updateAiArt(Long plantId, Long aiArtId, AiArtRequestDto aiArtRequestDto) {
        AiArt aiArt = findAiArtByPlantIdAndAiArtId(plantId, aiArtId);
        aiArt.update(
                aiArtRequestDto.getOriginalImageUrl(),
                aiArtRequestDto.getStyle()
        );
        return new AiArtResponseDto(aiArt);
    }

    @Transactional
    public void deleteAiArt(Long plantId, Long aiArtId) {
        AiArt aiArt = findAiArtByPlantIdAndAiArtId(plantId, aiArtId);
        aiArtRepository.delete(aiArt);
    }

    private AiArt findAiArtByPlantIdAndAiArtId(Long plantId, Long aiArtId) {
        plantRepository.findById(plantId).orElseThrow(() ->
                new IllegalArgumentException("해당 식물을 찾을 수 없습니다. Plant ID: " +  plantId)
        );

        return aiArtRepository.findByIdAndPlantId(aiArtId, plantId).orElseThrow(() ->
                new IllegalArgumentException("해당 식물(ID: " + plantId + ")에서 게시글(ID: " + aiArtId + ")을 찾을 수 없습니다.")
        );
    }

}
