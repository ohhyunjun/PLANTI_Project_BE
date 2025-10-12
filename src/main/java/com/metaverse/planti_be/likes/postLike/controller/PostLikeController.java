package com.metaverse.planti_be.likes.postLike.controller;

import com.metaverse.planti_be.auth.domain.PrincipalDetails;
import com.metaverse.planti_be.likes.postLike.dto.PostLikeResponseDto;
import com.metaverse.planti_be.likes.postLike.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<PostLikeResponseDto> toggleLikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        PostLikeResponseDto response = postLikeService.togglePostLike(principalDetails, postId);
        return ResponseEntity.ok(response);
    }
}
