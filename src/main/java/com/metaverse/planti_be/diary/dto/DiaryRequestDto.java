package com.metaverse.planti_be.diary.dto;

import lombok.Getter;

@Getter
public class DiaryRequestDto {
    private Long plantId;
    private String title;
    private String content;
}
