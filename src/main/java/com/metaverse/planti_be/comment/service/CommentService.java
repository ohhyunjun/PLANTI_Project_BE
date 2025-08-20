package com.metaverse.planti_be.comment.service;

import com.metaverse.planti_be.comment.domain.Comment;
import com.metaverse.planti_be.comment.dto.CommentRequestDto;
import com.metaverse.planti_be.comment.dto.CommentResponseDto;
import com.metaverse.planti_be.comment.repository.CommentRepository;
import com.metaverse.planti_be.post.domain.Post;
import com.metaverse.planti_be.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // 댓글 추가
    public CommentResponseDto createComment(CommentRequestDto commentRequestDto) {
        Post post = postRepository.findById(commentRequestDto.getPostId()).orElseThrow(() ->
                new IllegalArgumentException("해당 글을 찾을 수 없습니다.")
        );

        Comment comment = new Comment(
                commentRequestDto.getContent(),
                post
        );
        Comment savedComment = commentRepository.save(comment);
        CommentResponseDto commentResponseDto = new CommentResponseDto(savedComment);
        return commentResponseDto;
    }
    // 댓글 전체 조회
    public List<CommentResponseDto> getComments() {
        List<CommentResponseDto> commentResponseDtoList = commentRepository
                .findAllByOrderByCreatedAtAsc()
                .stream()
                .map(CommentResponseDto::new)
                .toList();
        return commentResponseDtoList;
    }

    // 댓글 수정
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto commentRequestDto) {
        Comment comment = findComment(commentId);
        comment.update(
                commentRequestDto.getContent()
        );
        return new CommentResponseDto(comment);
    }

    public void deleteComment(Long commentId) {
        Comment comment = findComment(commentId);
        commentRepository.delete(comment);
    }

    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new IllegalArgumentException("해당 댓글은 존재하지 않습니다.")
        );
    }

    // 게시글을 작성한 유저인지 확인 구현해야함

}
