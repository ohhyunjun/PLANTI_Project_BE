package com.metaverse.planti_be.diary.service;

import com.metaverse.planti_be.diary.repository.DiaryRepository;
import org.springframework.stereotype.Service;

@Service
public class DiaryService {
    private final DiaryRepository diaryRepository;

    public DiaryService(DiaryRepository diaryRepository){
        this.diaryRepository = diaryRepository;
    }
}
