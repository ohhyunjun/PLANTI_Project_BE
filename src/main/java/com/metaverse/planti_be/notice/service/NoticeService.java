package com.metaverse.planti_be.notice.service;

import com.metaverse.planti_be.notice.domain.Notice;
import com.metaverse.planti_be.notice.dto.NoticeRequestDto;
import com.metaverse.planti_be.notice.dto.NoticeResponseDto;
import com.metaverse.planti_be.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeResponseDto createNotice(NoticeRequestDto noticeRequestDto) {
        Notice notice = new Notice(noticeRequestDto);
        Notice savedNotice = noticeRepository.save(notice);
        NoticeResponseDto noticeResponseDto = new NoticeResponseDto(savedNotice);
        return noticeResponseDto;
    }

    public List<NoticeResponseDto> getNotices() {
        List<NoticeResponseDto> responseList = noticeRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(NoticeResponseDto::new)
                .toList();
        return responseList;
    }

    public Long deleteNotice(Long noticeId) {
        Notice notice = findNotice(noticeId);
        noticeRepository.delete(notice);
        return noticeId;
    }

    private Notice findNotice(Long noticeId) {
        return noticeRepository.findById(noticeId).orElseThrow(() ->
                new IllegalArgumentException("해당 알림은 존재하지 않습니다.")
        );
    }
}
