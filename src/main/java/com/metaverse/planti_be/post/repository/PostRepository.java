package com.metaverse.planti_be.post.repository;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findById(Long postId);

    @Query("SELECT p From Post p LEFT JOIN p.postLikes pl GROUP BY p.id HAVING COUNT(pl) >= 10 ORDER BY COUNT(pl) DESC, p.createdAt DESC")
    Page<Post> findPostsWithAtLeastTenLikes(Pageable pageable);

    // 페이지 전체 포스트 글 조회
    Page<Post> findAll(Pageable pageable);

    // 사용자가 작성한 게시글 조회
    Page<Post> findByUser(User user, Pageable pageable);

    // 사용자가 좋아요한 게시글 조회
    @Query("SELECT p FROM Post p JOIN p.postLikes pl WHERE pl.user = :user ORDER BY pl.createdAt DESC")
    Page<Post> findPostsLikedByUser(@Param("user") User user, Pageable pageable);
}

