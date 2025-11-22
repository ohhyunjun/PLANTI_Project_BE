package com.metaverse.planti_be.notice.domain;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.common.TimeStamped;
import com.metaverse.planti_be.device.domain.Device;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "notice")
@Entity
public class Notice extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NoticeType noticeType;

    // 알림을 받을 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 알림과 관련된 디바이스 (선택적)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_serial")
    private Device device;

    // 우선순위 (1: 높음, 2: 보통, 3: 낮음)
    @Column(nullable = false)
    private Integer priority = 2;

    // 관련 센서 값 또는 추가 데이터 (JSON 형태)
    @Column(length = 1000)
    private String additionalData;

    // 자동 알림 생성용 생성자
    public Notice(String message, NoticeType noticeType, User user, Device device, Integer priority) {
        this.message = message;
        this.noticeType = noticeType;
        this.user = user;
        this.device = device;
        this.priority = priority != null ? priority : 2;
        this.isRead = false;
    }
}
