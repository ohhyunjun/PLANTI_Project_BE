package com.metaverse.planti_be.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // JSON 데이터를 객체로 변환하기 위해 기본 생성자가 필요합니다.
public class UsernameCheckRequestDto {
    private String username;
}
