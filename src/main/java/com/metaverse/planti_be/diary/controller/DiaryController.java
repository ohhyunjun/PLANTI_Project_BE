package com.metaverse.planti_be.diary.controller;

import com.metaverse.planti_be.diary.dto.DiaryRequestDto;
import com.metaverse.planti_be.diary.dto.DiaryResponseDto;
import com.metaverse.planti_be.diary.service.DiaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<DiaryResponseDto> createDiary(@RequestBody DiaryRequestDto diaryRequestDto){
        DiaryResponseDto diaryResponseDto = diaryService.createDiary(diaryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryResponseDto);
    }

    @GetMapping("/diaries")
    public ResponseEntity<List<DiaryResponseDto>> getDiaries() {
        List<DiaryResponseDto> diaryResponseDtoList = diaryService.getDiaries();
        return ResponseEntity.ok(diaryResponseDtoList);
    }

    @PutMapping("/diaries/{diaryId}")
    public ResponseEntity<DiaryResponseDto> updateDiary(
            @PathVariable Long diaryId,
            @RequestBody DiaryRequestDto diaryRequestDto) {
        DiaryResponseDto updatedDiary = diaryService.updateDiary(diaryId, diaryRequestDto);
        return ResponseEntity.ok(updatedDiary);
    }

    @DeleteMapping("/diaries/{diaryId}")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.noContent().build();
    }
}
