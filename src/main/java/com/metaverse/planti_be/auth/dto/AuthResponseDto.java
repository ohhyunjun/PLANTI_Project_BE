package com.metaverse.planti_be.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponseDto {
    private String username;
    private String accessToken;

    public AuthResponseDto(String username, String accessToken) {
        this.username = username;
        this.accessToken = accessToken;
    }
}