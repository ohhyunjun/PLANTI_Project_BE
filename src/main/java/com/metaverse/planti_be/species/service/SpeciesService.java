package com.metaverse.planti_be.species.service;

import com.metaverse.planti_be.species.domain.Species;
import com.metaverse.planti_be.species.dto.SpeciesRequestDto;
import com.metaverse.planti_be.species.dto.SpeciesResponseDto;
import com.metaverse.planti_be.species.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpeciesService {
    private final SpeciesRepository speciesRepository;

    @Transactional
    public SpeciesResponseDto createSpecies(SpeciesRequestDto requestDto) {
        // 이름 중복 확인
        speciesRepository.findByName(requestDto.getName()).ifPresent(s -> {
            throw new IllegalArgumentException("이미 존재하는 품종입니다.");
        });

        Species species = new Species(
                requestDto.getName(),
                requestDto.getDaysToMature(),
                requestDto.getAiPromptGuideline()
        );
        Species savedSpecies = speciesRepository.save(species);
        return new SpeciesResponseDto(savedSpecies);
    }

    public List<SpeciesResponseDto> getAllSpecies() {
        return speciesRepository.findAll().stream()
                .map(SpeciesResponseDto::new)
                .collect(Collectors.toList());
    }
}
