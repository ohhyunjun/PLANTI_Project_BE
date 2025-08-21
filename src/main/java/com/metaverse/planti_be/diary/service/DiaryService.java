package com.metaverse.planti_be.diary.service;

import com.metaverse.planti_be.diary.domain.Diary;
import com.metaverse.planti_be.diary.dto.DiaryRequestDto;
import com.metaverse.planti_be.diary.dto.DiaryResponseDto;
import com.metaverse.planti_be.diary.repository.DiaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiaryService {
    private final DiaryRepository diaryRepository;

    public DiaryService(DiaryRepository diaryRepository){
        this.diaryRepository = diaryRepository;
    }

    public DiaryResponseDto createDiary(DiaryRequestDto diaryRequestDto) {
        Diary diary = new Diary(diaryRequestDto);
        Diary savedDiary = diaryRepository.save(diary);
        DiaryResponseDto diaryResponseDto = new DiaryResponseDto(savedDiary);
        return diaryResponseDto;
    }

    public List<DiaryResponseDto> getDiaries() {
        List<DiaryResponseDto> responseList = diaryRepository
                .findAllByOrderByCreatedAtAsc()
                .stream()
                .map(DiaryResponseDto::new)
                .toList();
        return responseList;
    }

    public Long updateDiary(Long diaryId, DiaryRequestDto diaryRequestDto) {
        Diary diary = findDiary(diaryId);
        diary.update(diaryRequestDto);
        return diaryId;
    }

    public Long deleteDiary(Long diaryId) {
        Diary diary = findDiary(diaryId);
        diaryRepository.delete(diary);
        return diaryId;
    }

    private Diary findDiary(Long diaryId) {
        return diaryRepository.findById(diaryId).orElseThrow(()->
                new IllegalArgumentException("해당 다이어리는 존재하지 않습니다.")
        );
    }
}
