package com.metaverse.planti_be.aiArt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AiArtRequestDto {
    @NotBlank
    private String originalImageUrl;
    @NotBlank
    private String style;
}
