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
    @PostMapping("/comments")
    public CommentResponseDto createComment(@RequestBody CommentRequestDto commentRequestDto){
        return commentService.createComment(commentRequestDto);
    }

    // 댓글 목록 읽어오기 API
    @GetMapping("/comments")
    public List<CommentResponseDto> getComments() {
        return commentService.getComments();
    }

    // 댓글 수정 API
    @PutMapping("/comments/{commentId}")
    public Long updateComment(@PathVariable Long commentId, @RequestBody CommentRequestDto commentRequestDto){
        return commentService.updateComment(commentId, commentRequestDto);
    }

    // 댓글 삭제 API
    @DeleteMapping("/comments/{commentId}")
    public Long deleteComment(@PathVariable Long commentId){
        return commentService.deleteComment(commentId);
    }
}
