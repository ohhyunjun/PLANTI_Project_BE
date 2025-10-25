package com.metaverse.planti_be.aiArt.controller;

import com.metaverse.planti_be.aiArt.dto.AiArtRequestDto;
import com.metaverse.planti_be.aiArt.dto.AiArtResponseDto;
import com.metaverse.planti_be.aiArt.service.AiArtService;
import com.metaverse.planti_be.auth.domain.PrincipalDetails;
import com.metaverse.planti_be.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AiArtController {

    private final AiArtService aiArtService;
    private final FileService fileService;

    // AI 아트용 이미지 업로드 (1단계)
    @PostMapping("/aiArts/upload")
    public ResponseEntity<Map<String, String>> uploadAiArtImage(
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        try {
            String imageUrl = fileService.uploadAiArtImage(image);
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 해당 식물의 아트 만들기 (2단계)
    @PostMapping("/aiArts")
    public ResponseEntity<AiArtResponseDto> createAiArt(
            @RequestBody AiArtRequestDto aiArtRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        AiArtResponseDto aiArtResponseDto = aiArtService.createAiArt(userId, aiArtRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(aiArtResponseDto);
    }

    // 유저의 아트 전체 조회
    @GetMapping("/aiArts/my")
    public ResponseEntity<List<AiArtResponseDto>> getMyAiArts(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        List<AiArtResponseDto> aiArtResponseDtoList = aiArtService.getAiArtsByUser(userId);
        return ResponseEntity.ok(aiArtResponseDtoList);
    }

    // 전체 아트 조회 (공개용)
    @GetMapping("/aiArts")
    public ResponseEntity<List<AiArtResponseDto>> getAllAiArts() {
        List<AiArtResponseDto> aiArtResponseDtoList = aiArtService.getAiArts();
        return ResponseEntity.ok(aiArtResponseDtoList);
    }

    // 해당 식물의 특정 아트 조회
    @GetMapping("/aiArts/{aiArtId}")
    public ResponseEntity<AiArtResponseDto> getAiArtById(
            @PathVariable Long aiArtId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        AiArtResponseDto aiArtResponseDto = aiArtService.getAiArtById(userId, aiArtId);
        return ResponseEntity.ok(aiArtResponseDto);
    }

    // 해당 식물의 특정 아트 수정
    @PutMapping("/aiArts/{aiArtId}")
    public ResponseEntity<AiArtResponseDto> updateAiArt(
            @PathVariable Long aiArtId,
            @RequestBody AiArtRequestDto aiArtRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        AiArtResponseDto updatedAiArt = aiArtService.updateAiArt(userId, aiArtId, aiArtRequestDto);
        return ResponseEntity.ok(updatedAiArt);
    }

    // 해당 식물의 특정 아트 삭제
    @DeleteMapping("/aiArts/{aiArtId}")
    public ResponseEntity<Void> deleteAiArt(
            @PathVariable Long aiArtId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        aiArtService.deleteAiArt(userId, aiArtId);
        return ResponseEntity.noContent().build();
    }
}