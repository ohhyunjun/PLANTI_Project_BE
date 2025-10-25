package com.metaverse.planti_be.post.repository;

import com.metaverse.planti_be.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();

    Optional<Post> findById(Long postId);

    @Query("SELECT p From Post p LEFT JOIN p.postLikes pl GROUP BY p.id HAVING COUNT(pl) >= 10 ORDER BY COUNT(pl) DESC, p.createdAt DESC")
    List<Post> findPostsWithAtLeastTenLikes();
}
