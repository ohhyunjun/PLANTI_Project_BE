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
    public ResponseEntity<CommentResponseDto> createComment(@PathVariable Long postId, @RequestBody CommentRequestDto commentRequestDto){
        Comment comment = commentService.save(commentRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CommentResponseDto(comment));
    }

    // 댓글 목록 읽어오기 API
    @GetMapping("/posts/{postId}/comments")
    public List<Comment> read(@PathVariable Long postId) {
        return commentService.findAll(postId);
    }

    // 댓글 삭제 API
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long postId,@PathVariable Long commentId){
        commentService.delete(postId,commentId);
        return ResponseEntity.ok().build();
    }

    // 댓글 수정 API
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(@PathVariable Long postId, @PathVariable Long commentId, @RequestBody CommentRequestDto commentRequestDto){
        Comment comment = commentService.update(postId, commentId, commentRequestDto);
        return ResponseEntity.ok().body(new CommentResponseDto(comment));
    }
}
