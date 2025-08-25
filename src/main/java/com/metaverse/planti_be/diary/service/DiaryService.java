package com.metaverse.planti_be.diary.service;

import com.metaverse.planti_be.diary.domain.Diary;
import com.metaverse.planti_be.diary.dto.DiaryRequestDto;
import com.metaverse.planti_be.diary.dto.DiaryResponseDto;
import com.metaverse.planti_be.diary.repository.DiaryRepository;
import com.metaverse.planti_be.plant.domain.Plant;
import com.metaverse.planti_be.plant.dto.PlantResponseDto;
import com.metaverse.planti_be.plant.repository.PlantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final PlantRepository plantRepository;

    @Transactional
    public DiaryResponseDto createDiary(Long plantId, DiaryRequestDto diaryRequestDto) {
        Plant plant = getValidPlant(plantId);

        Diary diary = new Diary(
                diaryRequestDto.getTitle(),
                diaryRequestDto.getContent(),
                plant
        );
        Diary savedDiary = diaryRepository.save(diary);
        return new DiaryResponseDto(savedDiary);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<DiaryResponseDto> getDiaries() {
        return diaryRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(DiaryResponseDto::new)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<DiaryResponseDto> getDiariesByPlantId(Long plantId) {
        getValidPlant(plantId);

        return diaryRepository.findByPlantIdOrderByCreatedAtAsc(plantId).stream()
                .map(DiaryResponseDto::new)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public DiaryResponseDto getDiaryById(Long plantId, Long diaryId) {
        Diary diary = getValidDiary(plantId, diaryId);
        return new DiaryResponseDto(diary);
    }

    @Transactional
    public DiaryResponseDto updateDiary(Long plantId, Long diaryId, DiaryRequestDto diaryRequestDto) {
        Diary diary = getValidDiary(plantId, diaryId);
        diary.update(
                diaryRequestDto.getTitle(),
                diaryRequestDto.getContent()
        );
        return new DiaryResponseDto(diary);
    }

    @Transactional
    public void deleteDiary(Long plantId, Long diaryId) {
        Diary diary = getValidDiary(plantId, diaryId);
        diaryRepository.delete(diary);
    }

    private Plant getValidPlant(Long plantId) {
        return plantRepository.findById(plantId).orElseThrow(()->
                new IllegalArgumentException("해당 식물을 찾을 수 없습니다. Plant ID:" + plantId)
        );
    }

    private Diary getValidDiary(Long plantId, Long diaryId) {
        Plant plant = getValidPlant(plantId);

        return diaryRepository.findByIdAndPlantId(diaryId, plant.getId()).orElseThrow(()->
                new IllegalArgumentException("식물(ID: " + plantId + ")에서 다이어리(ID: "+ diaryId + ")을 찾을 수 없습니다.")
        );
    }
}
