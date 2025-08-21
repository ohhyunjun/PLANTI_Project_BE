package com.metaverse.planti_be.diary.controller;

import com.metaverse.planti_be.diary.service.DiaryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DiaryController {
    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService){
        this.diaryService = diaryService;
    }

}
