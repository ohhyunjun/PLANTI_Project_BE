package com.metaverse.planti_be.AiArt.dto;

import lombok.Getter;

@Getter
public class AiArtRequestDto {
    private String originalImageUrl;
    private String artImageUrl;
    private String style;
}
