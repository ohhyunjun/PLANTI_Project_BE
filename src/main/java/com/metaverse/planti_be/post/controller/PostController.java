package com.metaverse.planti_be.post.controller;

import com.metaverse.planti_be.auth.domain.PrincipalDetails;
import com.metaverse.planti_be.post.dto.PostRequestDto;
import com.metaverse.planti_be.post.dto.PostResponseDto;
import com.metaverse.planti_be.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 글 만들기
    @PostMapping("/posts")
    public ResponseEntity<PostResponseDto> createPost(
            @RequestPart("postData") PostRequestDto postRequestDto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        PostResponseDto postResponseDto = postService.createPost(principalDetails, postRequestDto, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(postResponseDto);
    }

    // 전체 글 불러오기
    @GetMapping("/posts")
    public ResponseEntity<Page<PostResponseDto>> getPosts(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponseDto> postResponseDtoPage = postService.getPosts(principalDetails, pageable);
        return ResponseEntity.ok(postResponseDtoPage);
    }

    // ⚠️ 중요: /posts/hot, /posts/my, /posts/liked는 /posts/{postId}보다 위에 있어야 함
    @GetMapping("/posts/hot")
    public ResponseEntity<List<PostResponseDto>> getHotPosts(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<PostResponseDto> postResponseDtoList = postService.getHotPosts(principalDetails);
        return ResponseEntity.ok(postResponseDtoList);
    }

    // 내가 작성한 글 조회 API
    @GetMapping("/posts/my")
    public ResponseEntity<Page<PostResponseDto>> getMyPosts(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponseDto> myPosts = postService.getMyPosts(principalDetails, pageable);
        return ResponseEntity.ok(myPosts);
    }

    // 좋아요한 글 조회 API
    @GetMapping("/posts/liked")
    public ResponseEntity<Page<PostResponseDto>> getLikedPosts(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponseDto> likedPosts = postService.getLikedPosts(principalDetails, pageable);
        return ResponseEntity.ok(likedPosts);
    }

    // 특정 글 불러오기
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponseDto> getPostById(
            @PathVariable Long postId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        PostResponseDto postResponseDto = postService.getPostById(principalDetails, postId);
        return ResponseEntity.ok(postResponseDto);
    }

    // 특정 글 수정하기
    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @RequestPart("postData") PostRequestDto postRequestDto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "deleteFile", required = false, defaultValue = "false") Boolean deleteFile,
            @AuthenticationPrincipal PrincipalDetails principalDetails){
        PostResponseDto updatedPost = postService.updatePost(principalDetails, postId, postRequestDto, file, deleteFile);
        return ResponseEntity.ok(updatedPost);
    }

    // 특정 글 지우기
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal PrincipalDetails principalDetails){
        postService.deletePost(principalDetails, postId);
        return ResponseEntity.noContent().build();
    }
}