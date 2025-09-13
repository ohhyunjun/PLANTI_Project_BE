package com.metaverse.planti_be.AiArt.service;

import com.metaverse.planti_be.AiArt.domain.AiArt;
import com.metaverse.planti_be.AiArt.dto.AiArtRequestDto;
import com.metaverse.planti_be.AiArt.dto.AiArtResponseDto;
import com.metaverse.planti_be.AiArt.repository.AiArtRepository;
import com.metaverse.planti_be.plant.domain.Plant;
import com.metaverse.planti_be.plant.repository.PlantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiArtService {
    private final AiArtRepository aiArtRepository;
    private final PlantRepository plantRepository;

    @Transactional
    public AiArtResponseDto createAiArt(AiArtRequestDto aiArtRequestDto) {
        Plant plant = plantRepository.findById(aiArtRequestDto.getPlantId()).orElseThrow(()->
                new IllegalArgumentException("해당 식물을 찾을 수 없습니다.")
        );
        AiArt aiArt = new AiArt(
                aiArtRequestDto.getOriginalImageUrl(),
                aiArtRequestDto.getArtImageUrl(),
                aiArtRequestDto.getStyle(),
                plant
        );
        AiArt savedAiArt = aiArtRepository.save(aiArt);
        AiArtResponseDto aiArtResponseDto = new AiArtResponseDto(savedAiArt);
        return aiArtResponseDto;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<AiArtResponseDto> getAiArts() {
        List<AiArtResponseDto> aiArtResponseDtoList = aiArtRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AiArtResponseDto::new)
                .toList();
        return aiArtResponseDtoList;
    }

    @Transactional
    public AiArtResponseDto updateAiArt(Long aiArtId, AiArtRequestDto aiArtRequestDto) {
        AiArt aiArt = findAiArt(aiArtId);
        aiArt.update(
                aiArtRequestDto.getOriginalImageUrl(),
                aiArtRequestDto.getStyle()
        );
        return new AiArtResponseDto(aiArt);
    }

    @Transactional
    public void deleteAiArt(Long aiArtId) {
        AiArt aiArt = findAiArt(aiArtId);
        aiArtRepository.delete(aiArt);
    }

    private AiArt findAiArt(Long aiArtId) {
        return aiArtRepository.findById(aiArtId).orElseThrow(() ->
                new IllegalArgumentException("해당 AI 아트는 존재하지 않습니다.")
        );
    }

}
