package com.metaverse.planti_be.AiArt.service;

import com.metaverse.planti_be.AiArt.domain.AiArt;
import com.metaverse.planti_be.AiArt.dto.AiArtRequestDto;
import com.metaverse.planti_be.AiArt.dto.AiArtResponseDto;
import com.metaverse.planti_be.AiArt.repository.AiArtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiArtService {
    private final AiArtRepository aiArtRepository;

    public AiArtResponseDto createAiArt(AiArtRequestDto aiArtRequestDto) {
        AiArt aiArt = new AiArt(aiArtRequestDto);
        AiArt savedAiArt = aiArtRepository.save(aiArt);
        AiArtResponseDto aiArtResponseDto = new AiArtResponseDto(savedAiArt);
        return aiArtResponseDto;
    }

    public List<AiArtResponseDto> getAiArts() {
        List<AiArtResponseDto> responseList = aiArtRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AiArtResponseDto::new)
                .toList();
        return responseList;
    }


    public Long updateAiArt(Long aiArtId, AiArtRequestDto aiArtRequestDto) {
        AiArt aiArt = findAiArt(aiArtId);
        aiArt.update(aiArtRequestDto);
        return aiArtId;
    }

    public Long deleteAiArt(Long aiArtId) {
        AiArt aiArt = findAiArt(aiArtId);
        aiArtRepository.delete(aiArt);
        return aiArtId;
    }

    private AiArt findAiArt(Long aiArtId) {
        return aiArtRepository.findById(aiArtId).orElseThrow(() ->
                new IllegalArgumentException("해당 AI 아트는 존재하지 않습니다.")
        );
    }

}
