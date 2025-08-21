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

    // 게시글 작성 API
    @PostMapping("/posts")
    public ResponseEntity<PostResponseDto> createPost(@RequestBody PostRequestDto postRequestDto){
        PostResponseDto postResponseDto = postService.createPost(postRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(postResponseDto);
    }

    // 게시글 목록 조회 API
    @GetMapping("/posts")
    public ResponseEntity<List<PostResponseDto>> getPosts(){
        List<PostResponseDto> postResponseDtoList = postService.getPosts();
        return ResponseEntity.ok(postResponseDtoList);
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
