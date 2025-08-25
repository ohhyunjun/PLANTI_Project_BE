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

    @PostMapping("/plants/{plantId}/diaries")
    public ResponseEntity<DiaryResponseDto> createDiary(
            @PathVariable Long plantId,
            @RequestBody DiaryRequestDto diaryRequestDto){
        DiaryResponseDto diaryResponseDto = diaryService.createDiary(plantId,diaryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryResponseDto);
    }

    @GetMapping("/plants/{plantId}/diaries")
    public ResponseEntity<List<DiaryResponseDto>> getDiariesByPlantId(
            @PathVariable Long plantId){
        List<DiaryResponseDto> diaryResponseDtoList = diaryService.getDiariesByPlantId(plantId);
        return ResponseEntity.ok(diaryResponseDtoList);
    }

    @GetMapping("/plants/{plantId}/diaries/{diaryId}")
    public ResponseEntity<DiaryResponseDto> getDiaryById(
            @PathVariable Long plantId,
            @PathVariable Long diaryId) {
        DiaryResponseDto diaryResponseDto = diaryService.getDiaryById(plantId,diaryId);
        return ResponseEntity.ok(diaryResponseDto);
    }

    @GetMapping("/diaries")
    public ResponseEntity<List<DiaryResponseDto>> getDiaries() {
        List<DiaryResponseDto> diaryResponseDtoList = diaryService.getDiaries();
        return ResponseEntity.ok(diaryResponseDtoList);
    }

    @PutMapping("/plants/{plantId}/diaries/{diaryId}")
    public ResponseEntity<DiaryResponseDto> updateDiary(
            @PathVariable Long plantId,
            @PathVariable Long diaryId,
            @RequestBody DiaryRequestDto diaryRequestDto) {
        DiaryResponseDto updatedDiary = diaryService.updateDiary(plantId, diaryId, diaryRequestDto);
        return ResponseEntity.ok(updatedDiary);
    }

    @DeleteMapping("/plants/{plantId}/diaries/{diaryId}")
    public ResponseEntity<Void> deleteDiary(
            @PathVariable Long plantId,
            @PathVariable Long diaryId) {
        diaryService.deleteDiary(plantId, diaryId);
        return ResponseEntity.noContent().build();
    }
}
