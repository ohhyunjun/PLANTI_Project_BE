package com.metaverse.planti_be.notice.service;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.notice.domain.Notice;
import com.metaverse.planti_be.notice.dto.NoticeResponseDto;
import com.metaverse.planti_be.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;

    //사용자별 모든 알림 조회 (최신순)
    @Transactional(readOnly = true)
    public List<NoticeResponseDto> getUserNotices(User user) {
        return noticeRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(NoticeResponseDto::new)
                .toList();
    }

    //사용자별 읽지 않은 알림만 조회 (최신순)
    @Transactional(readOnly = true)
    public List<NoticeResponseDto> getUnreadNotices(User user) {
        return noticeRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user).stream()
                .map(NoticeResponseDto::new)
                .toList();
    }

    //사용자의 읽지 않은 알림 개수 조회
    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return noticeRepository.countByUserAndIsReadFalse(user);
    }

    //특정 알림을 읽음 처리
    @Transactional
    public void markAsRead(Long noticeId, User user) {
        Notice notice = findNotice(noticeId);

        // 해당 사용자의 알림인지 확인
        if (!notice.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 알림에 대한 권한이 없습니다.");
        }

        notice.setIsRead(true);
    }

    //사용자의 모든 알림을 읽음 처리
    @Transactional
    public void markAllAsRead(User user) {
        List<Notice> unreadNotices = noticeRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        unreadNotices.forEach(notice -> notice.setIsRead(true));
    }

    //사용자의 특정 알림 삭제
    @Transactional
    public Long deleteNotice(Long noticeId, User user) {
        Notice notice = findNotice(noticeId);

        // 해당 사용자의 알림인지 확인
        if (!notice.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 알림에 대한 권한이 없습니다.");
        }

        noticeRepository.delete(notice);
        return noticeId;
    }

    //알림 조회 헬퍼 메서드
    private Notice findNotice(Long noticeId) {
        return noticeRepository.findById(noticeId).orElseThrow(() ->
                new IllegalArgumentException("해당 알림은 존재하지 않습니다.")
        );
    }
}
