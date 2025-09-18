package com.metaverse.planti_be.notice.domain;

import com.metaverse.planti_be.common.TimeStamped;
import com.metaverse.planti_be.notice.dto.NoticeRequestDto;
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
    private Boolean is_Read;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoticeType noticeType;

    public Notice(NoticeRequestDto noticeRequestDto) {
        this.message = noticeRequestDto.getMessage();
        this.is_Read = noticeRequestDto.getIs_Read();
        this.noticeType = noticeRequestDto.getNoticeType();
    }
}
