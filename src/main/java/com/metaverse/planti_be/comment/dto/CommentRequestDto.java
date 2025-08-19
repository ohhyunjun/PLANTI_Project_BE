package com.metaverse.planti_be.comment.dto;

import com.metaverse.planti_be.comment.domain.Comment;
import lombok.Getter;

@Getter
public class CommentRequestDto {
    private String content;

    public Comment toEntity() {
        return Comment.builder()
                .content(content)
                .build();
    }
}
