package com.metaverse.planti_be.post.controller;

import com.metaverse.planti_be.post.domain.Post;
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
        Post post = postService.save(postRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PostResponseDto(post));
    }

    // 게시글 목록 조회 API
    @GetMapping("/posts")
    public ResponseEntity<List<PostResponseDto>> getAllPosts(){
        List<PostResponseDto> postListDto = postService.findAll()
                .stream()
                .map(PostResponseDto::new)
                .toList();
        return ResponseEntity.ok().body(postListDto);
    }

    // 게시글 삭제 API
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePostById(@PathVariable Long id){
        postService.delete(id);
        return ResponseEntity.ok().build();
    }

    // 게시글 수정 API
    @PutMapping("/posts/{id}")
    public ResponseEntity<PostResponseDto> updatePostById(@PathVariable Long id, @RequestBody PostRequestDto postRequestDto){
        Post post = postService.update(id, postRequestDto);
        return ResponseEntity.ok().body(new PostResponseDto(post));
    }
}
