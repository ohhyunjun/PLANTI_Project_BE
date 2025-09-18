package com.metaverse.planti_be.aiArt.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.metaverse.planti_be.aiArt.domain.AiArt;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AiArtResponseDto {
    private Long id;
    private String originalImageUrl;
    private String artImageUrl;
    private String style;
    private String plantName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public AiArtResponseDto(AiArt aiArt) {
        this.id = aiArt.getId();
        this.originalImageUrl = aiArt.getOriginalImageUrl();
        this.artImageUrl = aiArt.getArtImageUrl();
        this.style = aiArt.getStyle();
        this.createdAt = aiArt.getCreatedAt();
        this.updatedAt = aiArt.getUpdatedAt();
        this.plantName = aiArt.getPlant().getName();
    }
}
