package com.metaverse.planti_be.comment.service;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.auth.repository.UserRepository;
import com.metaverse.planti_be.comment.domain.Comment;
import com.metaverse.planti_be.comment.dto.CommentRequestDto;
import com.metaverse.planti_be.comment.dto.CommentResponseDto;
import com.metaverse.planti_be.comment.repository.CommentRepository;
import com.metaverse.planti_be.post.domain.Post;
import com.metaverse.planti_be.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring의 Transactional로 통일

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 클래스 레벨에 readOnly 기본값 설정
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponseDto createComment(Long userId, Long postId, CommentRequestDto commentRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 글을 찾을 수 없습니다. Post ID: " + postId));

        Comment comment = new Comment(
                commentRequestDto.getContent(),
                post,
                user
        );
        Comment savedComment = commentRepository.save(comment);
        return new CommentResponseDto(savedComment);
    }

    public List<CommentResponseDto> getComments() {
        return commentRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        // 댓글을 조회하기 전에 게시글이 존재하는지 먼저 확인합니다.
        postRepository.findById(postId).orElseThrow(() ->
                new IllegalArgumentException("해당 글을 찾을 수 없습니다. Post ID: " + postId)
        );
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    public CommentResponseDto getCommentById(Long postId, Long commentId) {
        // 특정 게시글에 속한 댓글이 맞는지 확인합니다.
        Comment comment = findCommentByPostIdAndCommentId(postId, commentId);
        return new CommentResponseDto(comment);
    }

    @Transactional // 쓰기(Write) 작업
    public CommentResponseDto updateComment(Long userId, Long commentId, CommentRequestDto commentRequestDto) {
        Comment comment = findCommentOwnedByUser(commentId, userId);
        comment.update(
                commentRequestDto.getContent()
        );
        return new CommentResponseDto(comment);
    }

    @Transactional // 쓰기(Write) 작업
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = findCommentOwnedByUser(commentId, userId);
        commentRepository.delete(comment);
    }

    //사용자가 소유한 Comment 엔티티를 조회하고 반환합니다. (수정/삭제용)
    private Comment findCommentOwnedByUser(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다. ID: " + commentId));
        if (!comment.getUser().getId().equals(userId)) {
            throw new SecurityException("해당 댓글에 대한 수정/삭제 권한이 없습니다.");
        }
        return comment;
    }

    //특정 게시글에 속한 Comment 엔티티를 조회하고 반환합니다. (단순 조회용)
    private Comment findCommentByPostIdAndCommentId(Long postId, Long commentId) {
        return commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글에서 댓글을 찾을 수 없습니다."));
    }
}