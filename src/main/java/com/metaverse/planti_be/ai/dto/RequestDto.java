package com.metaverse.planti_be.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // JSON 처리를 위한 기본 생성자
public class RequestDto {
    @NotBlank// 현재 설정을 조회할 기기의 시리얼 번호
    private String serialNumber;
}