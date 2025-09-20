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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public CommentResponseDto createComment(Long postId, CommentRequestDto commentRequestDto) {
        Post post = postRepository.findById(postId).orElseThrow(() ->
                new IllegalArgumentException("해당 글을 찾을 수 없습니다. Post ID: " + postId)
        );
        Comment comment = new Comment(
                commentRequestDto.getContent(),
                post
        );
        Comment savedComment = commentRepository.save(comment);
        return new CommentResponseDto(savedComment);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CommentResponseDto> getComments() {
        return commentRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        postRepository.findById(postId).orElseThrow(() ->
                new IllegalArgumentException("해당 글을 찾을 수 없습니다. Post ID: " + postId)
        );
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public CommentResponseDto getCommentById(Long postId, Long commentId) {
        Comment comment = findCommentByPostIdAndCommentId(postId, commentId);
        return new CommentResponseDto(comment);
    }

    @Transactional
    public CommentResponseDto updateComment(Long postId, Long commentId, CommentRequestDto commentRequestDto) {
        Comment comment = findCommentByPostIdAndCommentId(postId, commentId);
        comment.update(
                commentRequestDto.getContent()
        );
        return new CommentResponseDto(comment);
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId) {
        Comment comment = findCommentByPostIdAndCommentId(postId, commentId);
        commentRepository.delete(comment);
    }

    private Comment findCommentByPostIdAndCommentId(Long postId, Long commentId) {
        postRepository.findById(postId).orElseThrow(() ->
                new IllegalArgumentException("해당 글은 찾을 수 없습니다. Post ID: " + postId)
        );

        return commentRepository.findByIdAndPostId(commentId, postId).orElseThrow(() ->
                new IllegalArgumentException("해당 글(ID: " + postId + ")에서 댓글(ID: " + commentId + ")을 찾을 수 없습니다.")
        );
    }


    // 게시글을 작성한 유저인지 확인 구현해야함

}
