package com.metaverse.planti_be.diary.service;

import com.metaverse.planti_be.diary.domain.Diary;
import com.metaverse.planti_be.diary.dto.DiaryRequestDto;
import com.metaverse.planti_be.diary.dto.DiaryResponseDto;
import com.metaverse.planti_be.diary.repository.DiaryRepository;
import com.metaverse.planti_be.plant.domain.Plant;
import com.metaverse.planti_be.plant.repository.PlantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final PlantRepository plantRepository;

    public DiaryService(DiaryRepository diaryRepository, PlantRepository plantRepository) {
        this.diaryRepository = diaryRepository;
        this.plantRepository = plantRepository;
    }

    @Transactional
    public DiaryResponseDto createDiary(DiaryRequestDto diaryRequestDto) {
        Plant plant = plantRepository.findById(diaryRequestDto.getPlantId()).orElseThrow(()->
                new IllegalArgumentException("해당 plant를 찾을 수 없습니다.")
        );
        Diary diary = new Diary(
                diaryRequestDto.getTitle(),
                diaryRequestDto.getContent(),
                plant);
        Diary savedDiary = diaryRepository.save(diary);
        DiaryResponseDto diaryResponseDto = new DiaryResponseDto(savedDiary);
        return diaryResponseDto;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<DiaryResponseDto> getDiaries() {
        List<DiaryResponseDto> diaryResponseDtoList = diaryRepository
                .findAllByOrderByCreatedAtAsc()
                .stream()
                .map(DiaryResponseDto::new)
                .toList();
        return diaryResponseDtoList;
    }

    @Transactional
    public DiaryResponseDto updateDiary(Long diaryId, DiaryRequestDto diaryRequestDto) {
        Diary diary = findDiary(diaryId);
        diary.update(
                diaryRequestDto.getTitle(),
                diaryRequestDto.getContent()
        );
        return new DiaryResponseDto(diary);
    }

    @Transactional
    public void deleteDiary(Long diaryId) {
        Diary diary = findDiary(diaryId);
        diaryRepository.delete(diary);
    }

    private Diary findDiary(Long diaryId) {
        return diaryRepository.findById(diaryId).orElseThrow(()->
                new IllegalArgumentException("해당 다이어리는 존재하지 않습니다.")
        );
    }
}
