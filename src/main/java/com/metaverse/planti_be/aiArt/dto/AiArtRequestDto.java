package com.metaverse.planti_be.aiArt.dto;

import lombok.Getter;

@Getter
public class AiArtRequestDto {
    private Long plantId;
    private String originalImageUrl;
    private String artImageUrl;
    private String style;
}
