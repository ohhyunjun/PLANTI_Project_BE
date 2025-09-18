package com.metaverse.planti_be.notice.dto;

import com.metaverse.planti_be.notice.domain.NoticeType;
import lombok.Getter;

@Getter
public class NoticeRequestDto {
    private String message;
    private Boolean is_Read;
    private NoticeType noticeType;
}
