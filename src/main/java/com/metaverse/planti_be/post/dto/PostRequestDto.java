package com.metaverse.planti_be.post.dto;

import com.metaverse.planti_be.post.domain.Post;
import lombok.Getter;

@Getter
public class PostRequestDto {
    private String title;
    private String content;
}
