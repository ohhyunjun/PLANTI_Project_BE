package com.metaverse.planti_be.species.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class SpeciesRequestDto {
    @NotBlank
    private String name;

    @Positive // 양수여야 함
    private int daysToMature;

    private String aiPromptGuideline;
}