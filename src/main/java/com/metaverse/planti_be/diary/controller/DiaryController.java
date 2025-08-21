package com.metaverse.planti_be.diary.controller;

import com.metaverse.planti_be.diary.dto.DiaryRequestDto;
import com.metaverse.planti_be.diary.dto.DiaryResponseDto;
import com.metaverse.planti_be.diary.service.DiaryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DiaryController {
    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService){
        this.diaryService = diaryService;
    }

    @PostMapping("/diaries")
    public DiaryResponseDto createDiary(@RequestBody DiaryRequestDto diaryRequestDto){
        return diaryService.createDiary(diaryRequestDto);
    }

    @GetMapping("/diaries")
    public List<DiaryResponseDto> getDiaries() {
        return diaryService.getDiaries();
    }

    @PutMapping("/diaries/{diaryId}")
    public Long updateDiary(
            @PathVariable Long diaryId,
            @RequestBody DiaryRequestDto diaryRequestDto) {
        return diaryService.updateDiary(diaryId, diaryRequestDto);
    }

    public Long deleteDiary(@PathVariable Long diaryId) {
        return diaryService.deleteDiary(diaryId);
    }
}
