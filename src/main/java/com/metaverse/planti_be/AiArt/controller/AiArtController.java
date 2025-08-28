package com.metaverse.planti_be.AiArt.controller;

import com.metaverse.planti_be.AiArt.dto.AiArtRequestDto;
import com.metaverse.planti_be.AiArt.dto.AiArtResponseDto;
import com.metaverse.planti_be.AiArt.service.AiArtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AiArtController {
    private final AiArtService aiArtService;

    @PostMapping("/aiArts")
    public AiArtResponseDto createAiArt(@RequestBody AiArtRequestDto aiArtRequestDto) {
        return aiArtService.createAiArt(aiArtRequestDto);
    }

    @GetMapping("/aiArts")
    public List<AiArtResponseDto> getAiArts() {
        return aiArtService.getAiArts();
    }

    @PutMapping("/aiArts/{id}")
    public Long updateAiArt(@PathVariable Long id, @RequestBody AiArtRequestDto aiArtRequestDto) {
        return aiArtService.updateAiArt(id, aiArtRequestDto);
    }

    @DeleteMapping("/aiArts/{id}")
    public Long deleteAiArt(@PathVariable Long id) {
        return aiArtService.deleteAiArt(id);
    }

}
