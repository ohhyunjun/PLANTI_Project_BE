package com.metaverse.planti_be.likes.postLike.service;

import com.metaverse.planti_be.auth.domain.PrincipalDetails;
import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.likes.postLike.domain.PostLike;
import com.metaverse.planti_be.likes.postLike.dto.PostLikeResponseDto;
import com.metaverse.planti_be.likes.postLike.repository.PostLikeRepository;
import com.metaverse.planti_be.post.domain.Post;
import com.metaverse.planti_be.post.repository.PostRepository;
import com.metaverse.planti_be.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final PostService postService;
    private final PostRepository postRepository;

    @Transactional
    public PostLikeResponseDto togglePostLike(PrincipalDetails principalDetails, Long postId) {

        Post post = postRepository.findById(postId).orElseThrow(() ->
                new IllegalArgumentException("해당 게시글은 존재하지 않습니다.")
        );

        User currentUser = principalDetails.getUser();

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, currentUser);

        boolean liked;
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            liked = false;
        } else {
            PostLike newLike = new PostLike(post, currentUser);
            postLikeRepository.save(newLike);
            liked = true;
        }

        int currentLikeCount = (int) postLikeRepository.countByPost(post);

        return new PostLikeResponseDto(post.getId(), currentUser.getId(), liked, currentLikeCount);
    }
}
