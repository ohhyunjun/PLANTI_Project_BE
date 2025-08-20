package com.metaverse.planti_be.post.controller;

import com.metaverse.planti_be.post.dto.PostRequestDto;
import com.metaverse.planti_be.post.dto.PostResponseDto;
import com.metaverse.planti_be.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 작성 API
    @PostMapping("/posts")
    public PostResponseDto createPost(@RequestBody PostRequestDto postRequestDto){
        return postService.createPost(postRequestDto);
    }

    // 게시글 목록 조회 API
    @GetMapping("/posts")
    public List<PostResponseDto> getAllPosts(){
        return postService.getPost();
    }

    // 게시글 수정 API
    @PutMapping("/posts/{id}")
    public Long updatePost(@PathVariable Long id, @RequestBody PostRequestDto postRequestDto){
        return postService.updatePost(id, postRequestDto);
    }

    // 게시글 삭제 API
    @DeleteMapping("/posts/{id}")
    public Long deletePost(@PathVariable Long id){
        return postService.deletePost(id);
    }
}
