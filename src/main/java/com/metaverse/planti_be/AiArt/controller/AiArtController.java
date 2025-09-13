package com.metaverse.planti_be.AiArt.controller;

import com.metaverse.planti_be.AiArt.dto.AiArtRequestDto;
import com.metaverse.planti_be.AiArt.dto.AiArtResponseDto;
import com.metaverse.planti_be.AiArt.service.AiArtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AiArtController {
    private final AiArtService aiArtService;

    @PostMapping("/aiArts")
    public ResponseEntity<AiArtResponseDto> createAiArt(
            @RequestBody AiArtRequestDto aiArtRequestDto) {
        AiArtResponseDto aiArtResponseDto = aiArtService.createAiArt(aiArtRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(aiArtResponseDto);
    }

    @GetMapping("/aiArts")
    public ResponseEntity<List<AiArtResponseDto>> getAiArts() {
        List<AiArtResponseDto> aiArtResponseDtoList = aiArtService.getAiArts();
        return ResponseEntity.ok(aiArtResponseDtoList);
    }

    @PutMapping("/aiArts/{aiArtId}")
    public ResponseEntity<AiArtResponseDto> updateAiArt(
            @PathVariable Long aiArtId,
            @RequestBody AiArtRequestDto aiArtRequestDto) {
        AiArtResponseDto updatedAiArt = aiArtService.updateAiArt(aiArtId, aiArtRequestDto);
        return ResponseEntity.ok(updatedAiArt);
    }

    @DeleteMapping("/aiArts/{aiArtId}")
    public ResponseEntity<Void> deleteAiArt(
            @PathVariable Long aiArtId) {
        aiArtService.deleteAiArt(aiArtId);
        return ResponseEntity.noContent().build();
    }

}
