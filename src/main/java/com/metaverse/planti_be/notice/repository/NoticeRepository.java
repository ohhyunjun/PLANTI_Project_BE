package com.metaverse.planti_be.notice.repository;

import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.notice.domain.Notice;
import com.metaverse.planti_be.notice.domain.NoticeType;
import org.springframework.data.jpa.repository.JpaRepository;
import com.metaverse.planti_be.auth.domain.User;

import java.util.List;

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
}
