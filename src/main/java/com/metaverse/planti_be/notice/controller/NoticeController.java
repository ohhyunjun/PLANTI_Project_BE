package com.metaverse.planti_be.notice.controller;

import com.metaverse.planti_be.auth.domain.PrincipalDetails;
import com.metaverse.planti_be.notice.dto.NoticeResponseDto;
import com.metaverse.planti_be.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 사용자의 모든 알림 조회
    @GetMapping("/notices")
    public ResponseEntity<List<NoticeResponseDto>> getUserNotices(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<NoticeResponseDto> notices = noticeService.getUserNotices(principalDetails.getUser());
        return ResponseEntity.ok(notices);
    }

    // 사용자의 읽지 않은 알림 조회
    @GetMapping("/notices/unread")
    public ResponseEntity<List<NoticeResponseDto>> getUnreadNotices(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<NoticeResponseDto> unreadNotices = noticeService.getUnreadNotices(principalDetails.getUser());
        return ResponseEntity.ok(unreadNotices);
    }

    // 읽지 않은 알림 개수 조회
    @GetMapping("/notices/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        long count = noticeService.getUnreadCount(principalDetails.getUser());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // 특정 알림 읽음 처리
    @PutMapping("/notices/{noticeId}/read")
    public ResponseEntity<String> markAsRead(
            @PathVariable Long noticeId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        try {
            noticeService.markAsRead(noticeId, principalDetails.getUser());
            return ResponseEntity.ok("알림이 읽음 처리되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 모든 알림 읽음 처리
    @PutMapping("/notices/read-all")
    public ResponseEntity<String> markAllAsRead(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        noticeService.markAllAsRead(principalDetails.getUser());
        return ResponseEntity.ok("모든 알림이 읽음 처리되었습니다.");
    }

    // 특정 알림 삭제
    @DeleteMapping("/notices/{noticeId}")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable Long noticeId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        try {
            noticeService.deleteNotice(noticeId, principalDetails.getUser());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
