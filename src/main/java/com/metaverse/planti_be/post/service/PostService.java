package com.metaverse.planti_be.post.service;

import com.metaverse.planti_be.auth.domain.PrincipalDetails;
import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.file.service.FileService;
import com.metaverse.planti_be.likes.postLike.repository.PostLikeRepository;
import com.metaverse.planti_be.post.domain.Post;
import com.metaverse.planti_be.post.dto.PostRequestDto;
import com.metaverse.planti_be.post.dto.PostResponseDto;
import com.metaverse.planti_be.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FileService fileService;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public PostResponseDto createPost(PrincipalDetails principalDetails, PostRequestDto postRequestDto, MultipartFile file) {
        User logginedUser = principalDetails.getUser();
        Post post = new Post(
                postRequestDto.getTitle(),
                postRequestDto.getContent(),
                logginedUser
        );
        Post savedPost = postRepository.save(post);

        if (file != null && !file.isEmpty()) {
            fileService.uploadFile(savedPost, file);
        }
        PostResponseDto postResponseDto = new PostResponseDto(savedPost, 0);
        return postResponseDto;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PostResponseDto> getPosts() {
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();

        List<PostResponseDto> PostResponseDtoList = posts.stream()
                .map(post -> {
                    // 1. 각 Post에 대한 좋아요 수를 조회합니다. (PostLikeRepository 필요)
                    int likesCount = (int) postLikeRepository.countByPost(post);

                    // 2. ⭐ Post 객체와 likesCount를 인자로 넘겨 새로운 DTO 생성자를 호출합니다.
                    return new PostResponseDto(post, likesCount);
                })
                .toList();

        return PostResponseDtoList;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PostResponseDto getPostById(PrincipalDetails principalDetails,Long postId) {
        Post post = findPost(postId);

        int currentLikesCount = (int) postLikeRepository.countByPost(post);
        boolean liked = false;
        System.out.println(principalDetails.getUser());
        if (principalDetails != null) {
            User currentUser = principalDetails.getUser();
            liked = postLikeRepository.findByPostAndUser(post, currentUser).isPresent();
        }

        return new PostResponseDto(post, currentLikesCount, liked);
    }

    @Transactional
    public PostResponseDto updatePost(PrincipalDetails principalDetails, Long postId, PostRequestDto postRequestDto){
        Post post = findPost(postId);
        checkPostOwnership(post, principalDetails);

        post.update(
                postRequestDto.getTitle(),
                postRequestDto.getContent()
        );

        int likesCount = (int) postLikeRepository.countByPost(post);
        boolean liked = postLikeRepository.findByPostAndUser(post, principalDetails.getUser()).isPresent();

        return new PostResponseDto(post, likesCount, liked);
    }

    @Transactional
    public void deletePost(PrincipalDetails principalDetails, Long postId){
        Post post = findPost(postId);
        checkPostOwnership(post, principalDetails);

        postRepository.delete(post);
    }

    public Post findPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(() ->
                new IllegalArgumentException("해당 게시글은 존재하지 않습니다.")
        );
    }

    private void checkPostOwnership(Post post, PrincipalDetails principalDetails) {
        if (!post.getUser().getId().equals(principalDetails.getUser().getId())) {
            throw new IllegalArgumentException("게시글은 작성자만 수정하거나 삭제할 수 있습니다.");
        }
    }
}
