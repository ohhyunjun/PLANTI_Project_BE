package com.metaverse.planti_be.post.controller;

import com.metaverse.planti_be.post.domain.Post;
import com.metaverse.planti_be.post.dto.PostRequestDto;
import com.metaverse.planti_be.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 작성 API
    public ResponseEntity<Post> createPost(@RequestBody PostRequestDto postRequestDto){
        Post post = postService.save(postRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }
}
