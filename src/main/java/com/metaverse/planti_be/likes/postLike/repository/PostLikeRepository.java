package com.metaverse.planti_be.likes.postLike.repository;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.likes.postLike.domain.PostLike;
import com.metaverse.planti_be.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(Post post, User user);

    long countByPost(Post post);
}
