package com.metaverse.planti_be.aiArt.controller;

import com.metaverse.planti_be.aiArt.dto.AiArtRequestDto;
import com.metaverse.planti_be.aiArt.dto.AiArtResponseDto;
import com.metaverse.planti_be.aiArt.service.AiArtService;
import com.metaverse.planti_be.auth.domain.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AiArtController {

    private final AiArtService aiArtService;

    // 해당 식물의 아트 만들기
    @PostMapping("/plants/{plantId}/aiArts")
    public ResponseEntity<AiArtResponseDto> createAiArtForPlant(
            @PathVariable Long plantId,
            @RequestBody AiArtRequestDto aiArtRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        AiArtResponseDto aiArtResponseDto = aiArtService.createAiArt(plantId, userId, aiArtRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(aiArtResponseDto);
    }

    // 해당 식물의 아트 전체 조회
    @GetMapping("/plants/{plantId}/aiArts")
    public ResponseEntity<List<AiArtResponseDto>> getAiArtsByPlantId(
            @PathVariable Long plantId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        List<AiArtResponseDto> aiArtResponseDtoList = aiArtService.getAiArtsByPlantId(plantId, userId);
        return ResponseEntity.ok(aiArtResponseDtoList);
    }

    // 전체 아트 조회
    @GetMapping("/aiArts")
    public ResponseEntity<List<AiArtResponseDto>> getAiArts() {
        List<AiArtResponseDto> aiArtResponseDtoList = aiArtService.getAiArts();
        return ResponseEntity.ok(aiArtResponseDtoList);
    }

    // 해당 식물의 특정 아트 조회
    @GetMapping("/plants/{plantId}/aiArts/{aiArtId}")
    public ResponseEntity<AiArtResponseDto> getAiArtById(
            @PathVariable Long plantId,
            @PathVariable Long aiArtId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        AiArtResponseDto aiArtResponseDto = aiArtService.getAiArtById(plantId, aiArtId, userId);
        return ResponseEntity.ok(aiArtResponseDto);
    }

    // 해당 식물의 특정 아트 수정
    @PutMapping("/plants/{plantId}/aiArts/{aiArtId}")
    public ResponseEntity<AiArtResponseDto> updateAiArt(
            @PathVariable Long plantId,
            @PathVariable Long aiArtId,
            @RequestBody AiArtRequestDto aiArtRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        AiArtResponseDto updatedAiArt = aiArtService.updateAiArt(plantId, aiArtId, userId, aiArtRequestDto);
        return ResponseEntity.ok(updatedAiArt);
    }

    // 해당 식물의 특정 아트 삭제
    @DeleteMapping("/plants/{plantId}/aiArts/{aiArtId}")
    public ResponseEntity<Void> deleteAiArt(
            @PathVariable Long plantId,
            @PathVariable Long aiArtId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        aiArtService.deleteAiArt(plantId, aiArtId, userId);
        return ResponseEntity.noContent().build();
    }

}
