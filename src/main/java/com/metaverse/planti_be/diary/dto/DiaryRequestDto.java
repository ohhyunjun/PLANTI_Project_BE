package com.metaverse.planti_be.diary.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DiaryRequestDto {
    private String title;
    private String content;
    private LocalDate targetDate;
}
