package com.metaverse.planti_be.comment.repository;

import com.metaverse.planti_be.comment.domain.Comment;
import com.metaverse.planti_be.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByOrderByCreatedAtAsc();

    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    Optional<Comment> findByIdAndPostId(Long commentId, Long postId);

    // 사용자가 작성한 댓글 조회
    List<Comment> findByUserOrderByCreatedAtDesc(User user);
}
