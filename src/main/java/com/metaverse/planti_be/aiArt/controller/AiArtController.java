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
@RequestMapping("/api/aiArts") // 기본 경로 변경
@RequiredArgsConstructor
public class AiArtController {

    private final AiArtService aiArtService;

    // 현재 로그인된 유저의 AI 아트 만들기
    @PostMapping
    public ResponseEntity<AiArtResponseDto> createAiArt(
            @RequestBody AiArtRequestDto aiArtRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        AiArtResponseDto aiArtResponseDto = aiArtService.createAiArt(userId, aiArtRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(aiArtResponseDto);
    }

    // 현재 로그인된 유저의 AI 아트 전체 조회
    @GetMapping("/my")
    public ResponseEntity<List<AiArtResponseDto>> getMyAiArts(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        List<AiArtResponseDto> aiArtResponseDtoList = aiArtService.getAiArtsByUser(userId);
        return ResponseEntity.ok(aiArtResponseDtoList);
    }

    // 전체 아트 조회 (공개용)
    @GetMapping
    public ResponseEntity<List<AiArtResponseDto>> getAllAiArts() {
        List<AiArtResponseDto> aiArtResponseDtoList = aiArtService.getAiArts();
        return ResponseEntity.ok(aiArtResponseDtoList);
    }

    // 특정 아트 조회 (자신의 것만)
    @GetMapping("/{aiArtId}")
    public ResponseEntity<AiArtResponseDto> getAiArtById(
            @PathVariable Long aiArtId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        AiArtResponseDto aiArtResponseDto = aiArtService.getAiArtById(userId, aiArtId);
        return ResponseEntity.ok(aiArtResponseDto);
    }

    // 특정 아트 수정 (자신의 것만)
    @PutMapping("/{aiArtId}")
    public ResponseEntity<AiArtResponseDto> updateAiArt(
            @PathVariable Long aiArtId,
            @RequestBody AiArtRequestDto aiArtRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        AiArtResponseDto updatedAiArt = aiArtService.updateAiArt(userId, aiArtId, aiArtRequestDto);
        return ResponseEntity.ok(updatedAiArt);
    }

    // 특정 아트 삭제 (자신의 것만)
    @DeleteMapping("/{aiArtId}")
    public ResponseEntity<Void> deleteAiArt(
            @PathVariable Long aiArtId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        aiArtService.deleteAiArt(userId, aiArtId);
        return ResponseEntity.noContent().build();
    }
}