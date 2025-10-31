package com.metaverse.planti_be.notice.repository;

import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.notice.domain.Notice;
import com.metaverse.planti_be.notice.domain.NoticeType;
import org.springframework.data.jpa.repository.JpaRepository;
import com.metaverse.planti_be.auth.domain.User;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // 사용자별 알림 조회
    List<Notice> findByUserOrderByCreatedAtDesc(User user);

    // 사용자별 읽지 않은 알림 조회
    List<Notice> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    // 중복 알림 방지용 메서드들
    boolean existsByUserAndDeviceAndNoticeType(User user, Device device, NoticeType noticeType);
    boolean existsByUserAndDeviceAndNoticeTypeAndIsReadFalse(User user, Device device, NoticeType noticeType);

    // 읽지 않은 알림 개수 조회
    long countByUserAndIsReadFalse(User user);

    // 가장 최근의 특정 타입 알림을 조회하기 위한 메서드
    Optional<Notice> findTopByUserAndDeviceAndNoticeTypeOrderByCreatedAtDesc(User user, Device device, NoticeType noticeType);

    // 특정 사용자와 기기에 연관된 모든 알림 조회 (기기 삭제 시 사용)
    List<Notice> findByUserAndDevice(User user, Device device);
}
