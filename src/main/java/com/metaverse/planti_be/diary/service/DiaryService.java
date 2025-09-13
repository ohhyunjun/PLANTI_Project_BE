package com.metaverse.planti_be.diary.service;

import com.metaverse.planti_be.diary.domain.Diary;
import com.metaverse.planti_be.diary.dto.DiaryRequestDto;
import com.metaverse.planti_be.diary.dto.DiaryResponseDto;
import com.metaverse.planti_be.diary.repository.DiaryRepository;
import com.metaverse.planti_be.plant.domain.Plant;
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
        Plant plant = plantRepository.findById(plantId).orElseThrow(() ->
                new IllegalArgumentException("해당 식물을 찾을 수 없습니다. Plant ID: " +  plantId)
        );
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
        plantRepository.findById(plantId).orElseThrow(() ->
                new IllegalArgumentException("해당 식물을 찾을 수 없습니다. Plant ID: " + plantId)
        );
        return diaryRepository.findByPlantIdOrderByCreatedAtAsc(plantId).stream()
                .map(DiaryResponseDto::new)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public DiaryResponseDto getDiaryById(Long plantId, Long diaryId) {
        Diary diary = findDiariesByPlantIdAndDiaryId(plantId, diaryId);
        return new DiaryResponseDto(diary);
    }

    @Transactional
    public DiaryResponseDto updateDiary(Long plantId, Long diaryId, DiaryRequestDto diaryRequestDto) {
        Diary diary = findDiariesByPlantIdAndDiaryId(plantId, diaryId);
        diary.update(
                diaryRequestDto.getTitle(),
                diaryRequestDto.getContent()
        );
        return new DiaryResponseDto(diary);
    }

    @Transactional
    public void deleteDiary(Long plantId, Long diaryId) {
        Diary diary = findDiariesByPlantIdAndDiaryId(plantId, diaryId);
        diaryRepository.delete(diary);
    }

    private Diary findDiariesByPlantIdAndDiaryId(Long plantId, Long diaryId) {
        plantRepository.findById(plantId).orElseThrow(() ->
                new IllegalArgumentException("해당 식물을 찾을 수 없습니다. Plant Id: " + plantId)
        );

        return diaryRepository.findByIdAndPlantId(diaryId, plantId).orElseThrow(() ->
                new IllegalArgumentException("해당 식물(ID: " + plantId + ")에서 다이어리(ID: " +  diaryId + ")를 찾을 수 없습니다.")
        );
    }
}
