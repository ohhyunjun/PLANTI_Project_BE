package com.metaverse.planti_be.comment.controller;

import com.metaverse.planti_be.comment.domain.Comment;
import com.metaverse.planti_be.comment.dto.CommentRequestDto;
import com.metaverse.planti_be.comment.dto.CommentResponseDto;
import com.metaverse.planti_be.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성 API
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequestDto commentRequestDto) {
        CommentResponseDto commentResponseDto = commentService.createComment(postId,commentRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentResponseDto);
    }
    
    // 특정 게시글의 댓글 전체 불러오기
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPostId(
            @PathVariable Long postId) {
        List<CommentResponseDto> commentResponseDtoList = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(commentResponseDtoList);
    }


    // 특정 게시글의 댓글의 아이디 조회
    @GetMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> getCommentById(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        CommentResponseDto commentResponseDto = commentService.getCommentById(postId,commentId);
        return  ResponseEntity.ok(commentResponseDto);
    }

    // 댓글 수정 API
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto commentRequestDto){
        CommentResponseDto updatedComment = commentService.updateComment(postId, commentId, commentRequestDto);
        return ResponseEntity.ok(updatedComment);
    }

    // 댓글 전체 목록 읽어오기 API
    @GetMapping("/comments")
    public ResponseEntity<List<CommentResponseDto>> getComments() {
        List<CommentResponseDto> commentResponseDto = commentService.getComments();
        return ResponseEntity.ok(commentResponseDto);
    }

    // 댓글 삭제 API
    @DeleteMapping("posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId){
        commentService.deleteComment(postId, commentId);
        return ResponseEntity.noContent().build();
    }
}
