package com.metaverse.planti_be.notice.controller;

import com.metaverse.planti_be.notice.dto.NoticeRequestDto;
import com.metaverse.planti_be.notice.dto.NoticeResponseDto;
import com.metaverse.planti_be.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping("/notices")
    public NoticeResponseDto createNotice(
            @RequestBody NoticeRequestDto noticeRequestDto) {
        return noticeService.createNotice(noticeRequestDto);
    }

    @GetMapping("/notices")
    public List<NoticeResponseDto> getNotices() {
        return noticeService.getNotices();
    }

    @DeleteMapping("/notices/{noticeId}")
    public Long deleteNotice(@PathVariable Long noticeId) {
        return noticeService.deleteNotice(noticeId);
    }

}
