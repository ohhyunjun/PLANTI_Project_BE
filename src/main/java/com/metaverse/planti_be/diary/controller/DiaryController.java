package com.metaverse.planti_be.diary.controller;

import com.metaverse.planti_be.auth.domain.PrincipalDetails;
import com.metaverse.planti_be.diary.dto.DiaryRequestDto;
import com.metaverse.planti_be.diary.dto.DiaryResponseDto;
import com.metaverse.planti_be.diary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    // 특정 식물의 다이어리 만들기
    @PostMapping("/plants/{plantId}/diaries")
    public ResponseEntity<DiaryResponseDto> createDiary(
            @PathVariable Long plantId,
            @RequestBody DiaryRequestDto diaryRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long userId = principalDetails.getUser().getId();
        DiaryResponseDto diaryResponseDto = diaryService.createDiary(userId, plantId, diaryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryResponseDto);
    }

    // 특정 식물의 다이어리 내용 불러오기
    @GetMapping("/plants/{plantId}/diaries")
    public ResponseEntity<List<DiaryResponseDto>> getDiariesByPlantId(
            @PathVariable Long plantId,
            @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long userId = principalDetails.getUser().getId();
        List<DiaryResponseDto> diaryResponseDtoList = diaryService.getDiariesByPlantId(userId, plantId);
        return ResponseEntity.ok(diaryResponseDtoList);
    }

    // 특정 식물의 다이어리의 특정 내용 불러오기
    @GetMapping("/plants/{plantId}/diaries/{diaryId}")
    public ResponseEntity<DiaryResponseDto> getDiaryById(
            @PathVariable Long plantId,
            @PathVariable Long diaryId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        DiaryResponseDto diaryResponseDto = diaryService.getDiaryById(userId, plantId, diaryId);
        return ResponseEntity.ok(diaryResponseDto);
    }

    // 전체 다이어리 불러오기
    @GetMapping("/diaries")
    public ResponseEntity<List<DiaryResponseDto>> getDiaries(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        List<DiaryResponseDto> diaryResponseDtoList = diaryService.getDiaries(userId);
        return ResponseEntity.ok(diaryResponseDtoList);
    }

    // 특정 날짜의 다이어리 목록 불러오기
    @GetMapping("/diaries/by-date")
    public ResponseEntity<List<DiaryResponseDto>> getDiariesByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Long userId = principalDetails.getUser().getId();
        List<DiaryResponseDto> diaryResponseDtoList = diaryService.getDiariesByDate(userId, date);
        return ResponseEntity.ok(diaryResponseDtoList);
    }

    // 특정 식물의 특정 다이어리 수정하기 - 계층적 URL로 변경
    @PutMapping("/plants/{plantId}/diaries/{diaryId}")
    public ResponseEntity<DiaryResponseDto> updateDiary(
            @PathVariable Long plantId,
            @PathVariable Long diaryId,
            @RequestBody DiaryRequestDto diaryRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        DiaryResponseDto updatedDiary = diaryService.updateDiary(userId, plantId, diaryId, diaryRequestDto);
        return ResponseEntity.ok(updatedDiary);
    }

    // 특정 식물의 특정 다이어리 삭제하기 - 계층적 URL로 변경
    @DeleteMapping("/plants/{plantId}/diaries/{diaryId}")
    public ResponseEntity<Void> deleteDiary(
            @PathVariable Long plantId,
            @PathVariable Long diaryId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        diaryService.deleteDiary(userId, plantId, diaryId);
        return ResponseEntity.noContent().build();
    }
}