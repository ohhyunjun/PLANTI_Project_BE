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
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // 댓글 추가
    public CommentResponseDto createComment(Long postId, CommentRequestDto commentRequestDto) {
        Post post = getValidPost(postId);

        Comment comment = new Comment(
                commentRequestDto.getContent(),
                post
        );
        Comment savedComment = commentRepository.save(comment);
        return new CommentResponseDto(savedComment);
    }
    // 댓글 전체 조회
    public List<CommentResponseDto> getComments() {
        return commentRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    //특정 글의 댓글 조회
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        getValidPost(postId);

        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    // 댓글의 아이디 조회
    public CommentResponseDto getCommentById(Long postId, Long commentId) {
        Comment comment = getValidComment(postId,commentId);
        return new CommentResponseDto(comment);
    }

    // 댓글 수정
    public CommentResponseDto updateComment(Long postId, Long commentId, CommentRequestDto commentRequestDto) {
        Comment comment = getValidComment(postId, commentId);
        comment.update(
                commentRequestDto.getContent()
        );
        return new CommentResponseDto(comment);
    }

    public void deleteComment(Long postId, Long commentId) {
        Comment comment = getValidComment(postId, commentId);
        commentRepository.delete(comment);
    }

    private Post getValidPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(()->
                new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. Post ID:" + postId)
        );
    }

    private Comment getValidComment(Long postId, Long commentId) {
        Post post = getValidPost(postId);

        return commentRepository.findByIdAndPostId(commentId, post.getId()).orElseThrow(() ->
                new IllegalArgumentException("게시글(ID: " + postId + ")에서 댓글(ID: "+ commentId + ")을 찾을 수 없습니다.")
        );
    }

    // 게시글을 작성한 유저인지 확인 구현해야함

}
