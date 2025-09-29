package com.metaverse.planti_be.plant.controller;

import com.metaverse.planti_be.auth.domain.PrincipalDetails;
import com.metaverse.planti_be.plant.dto.PlantRequestDto;
import com.metaverse.planti_be.plant.dto.PlantResponseDto;
import com.metaverse.planti_be.plant.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlantController {

    private final PlantService plantService;

    // 식물 등록하기
    @PostMapping("/plants")
    public ResponseEntity<PlantResponseDto> createPlant(
            @RequestBody PlantRequestDto plantRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        PlantResponseDto plantResponseDto = plantService.createPlant(plantRequestDto, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(plantResponseDto);
    }

    // 전체 식물 불러오기
    @GetMapping("/plants")
    public ResponseEntity<List<PlantResponseDto>> getPlants(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<PlantResponseDto> plantResponseDtoList = plantService.getPlants(principalDetails.getUser());
        return ResponseEntity.ok(plantResponseDtoList);
    }

    // 특정 식물 불러오기
    @GetMapping("/plants/{plantId}")
    public ResponseEntity<PlantResponseDto> getPlantById(
            @PathVariable Long plantId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        PlantResponseDto plantResponseDto = plantService.getPlantById(plantId, principalDetails.getUser());
        return ResponseEntity.ok(plantResponseDto);
    }

    // 특정 식물 전체 수정하기
    @PutMapping("/plants/{plantId}")
    public ResponseEntity<PlantResponseDto> updatePlant(
            @PathVariable Long plantId,
            @RequestBody PlantRequestDto plantRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        PlantResponseDto updatePlant = plantService.updatePlant(plantId, plantRequestDto, principalDetails.getUser());
        return ResponseEntity.ok(updatePlant);
    }

    // 특정 식물 부분 수정하기
    @PatchMapping("/plants/{plantId}")
    public ResponseEntity<PlantResponseDto> patchPlant(
            @PathVariable Long plantId,
            @RequestBody PlantRequestDto plantRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        PlantResponseDto patchedPlant = plantService.patchPlant(plantId, plantRequestDto, principalDetails.getUser());
        return ResponseEntity.ok(patchedPlant);
    }


    // 특정 식물 삭제하기
    @DeleteMapping("/plants/{plantId}")
    public ResponseEntity<Void> deletePlant(
            @PathVariable Long plantId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        plantService.deletePlant(plantId, principalDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}