package com.metaverse.planti_be.species.controller;

import com.metaverse.planti_be.species.dto.SpeciesRequestDto;
import com.metaverse.planti_be.species.dto.SpeciesResponseDto;
import com.metaverse.planti_be.species.service.SpeciesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/species")
@RequiredArgsConstructor
public class SpeciesController {
    private final SpeciesService speciesService;

    // (관리자용) 새 품종 등록
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpeciesResponseDto> createSpecies(@Valid @RequestBody SpeciesRequestDto requestDto) {
        SpeciesResponseDto responseDto = speciesService.createSpecies(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 전체 품종 목록 조회
    @GetMapping
    public ResponseEntity<List<SpeciesResponseDto>> getAllSpecies() {
        List<SpeciesResponseDto> speciesList = speciesService.getAllSpecies();
        return ResponseEntity.ok(speciesList);
    }
}
