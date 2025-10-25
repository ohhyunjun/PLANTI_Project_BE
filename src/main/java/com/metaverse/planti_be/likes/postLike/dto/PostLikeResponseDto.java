package com.metaverse.planti_be.likes.postLike.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostLikeResponseDto {
    private Long postId;
    private Long userId;
    private boolean liked;
    private int likesCount;

    public PostLikeResponseDto(Long postId, Long userId, boolean liked, int likesCount) {
        this.postId = postId;
        this.userId = userId;
        this.liked = liked;
        this.likesCount = likesCount;
    }
}
