package com.metaverse.planti_be.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // 생성자를 통해 간단히 객체를 만들기 위함
public class ResponseDto {
    // AI가 생성한 조언 텍스트
    private String advice;
}