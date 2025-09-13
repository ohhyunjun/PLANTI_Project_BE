package com.metaverse.planti_be.post.controller;

import com.metaverse.planti_be.post.dto.PostRequestDto;
import com.metaverse.planti_be.post.dto.PostResponseDto;
import com.metaverse.planti_be.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 글 만들기
    @PostMapping("/posts")
    public ResponseEntity<PostResponseDto> createPost(
            @RequestBody PostRequestDto postRequestDto){
        PostResponseDto postResponseDto = postService.createPost(postRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(postResponseDto);
    }

    // 전체 글 불러오기
    @GetMapping("/posts")
    public ResponseEntity<List<PostResponseDto>> getPosts(){
        List<PostResponseDto> postResponseDtoList = postService.getPosts();
        return ResponseEntity.ok(postResponseDtoList);
    }

    // 특정 글 불러오기
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponseDto> getPostById(
            @PathVariable Long postId){
        PostResponseDto postResponseDto = postService.getPostById(postId);
        return ResponseEntity.ok(postResponseDto);
    }

    // 특정 글 수정하기
    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @RequestBody PostRequestDto postRequestDto){
        PostResponseDto updatedPost = postService.updatePost(postId, postRequestDto);
        return ResponseEntity.ok(updatedPost);
    }

    // 특정 글 지우기
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId){
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
}
