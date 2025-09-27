package com.metaverse.planti_be.notice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.metaverse.planti_be.notice.domain.Notice;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NoticeResponseDto {
    private Long id;
    private String message;
    private Boolean isRead;
    private String noticeType;
    private String noticeDescription;
    private String deviceNickname;
    private String deviceSerial;
    private Integer priority;
    private String additionalData;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public NoticeResponseDto(Notice notice) {
        this.id = notice.getId();
        this.message = notice.getMessage();
        this.isRead = notice.getIsRead();
        this.noticeType = notice.getNoticeType().name();
        this.noticeDescription = notice.getNoticeType().getDescription();
        this.deviceNickname = notice.getDevice() != null ? notice.getDevice().getDeviceNickname() : null;
        this.deviceSerial = notice.getDevice() != null ? notice.getDevice().getId() : null;
        this.priority = notice.getPriority();
        this.additionalData = notice.getAdditionalData();
        this.createdAt = notice.getCreatedAt();
        this.updatedAt = notice.getUpdatedAt();
    }
}
