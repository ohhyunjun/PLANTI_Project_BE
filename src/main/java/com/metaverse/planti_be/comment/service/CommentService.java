package com.metaverse.planti_be.comment.service;

import com.metaverse.planti_be.comment.domain.Comment;
import com.metaverse.planti_be.comment.dto.CommentRequestDto;
import com.metaverse.planti_be.comment.repository.CommentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;

    // 댓글 추가
    public Comment save(CommentRequestDto commentRequestDto){
        return commentRequestDto.save(commentRequestDto.toEntity());
    }
    // 댓글 전체 조회
    public List<Comment> findAll(){
        return commentRepository.findAll();
    }
    // 댓글 삭제
    public void delete(Long postId, Long id){
        Comment comment = commentRepository.findByPostIdAndId(postId, id).orElseThrow(()->
                new IllegalArgumentException("해당 댓글이 존재하지 않습니다. " + id));

        commentRepository.delete(comment);
    }
    // 댓글 수정
    public Comment update(Long postId, Long id, CommentRequestDto commentRequestDto){
        Comment comment = commentRepository.findByPostIdAndId(postId, id).orElseThrow(()->
                new IllegalArgumentException("해당 댓글이 존재하지 않습니다. " + id));

        comment.update(commentRequestDto.getContent());
        return commentRepository.save(comment);
    }

    // 게시글을 작성한 유저인지 확인 구현해야함

}
