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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<PostResponseDto> getPosts(PrincipalDetails principalDetails, Pageable pageable) {
        Page<Post> postPage = postRepository.findAll(pageable);

        User currentUser = principalDetails != null ? principalDetails.getUser() : null;

        return postPage.map(post -> {
            int likesCount = (int) postLikeRepository.countByPost(post);

            boolean liked = false;
            if (currentUser != null) {
                liked = postLikeRepository.findByPostAndUser(post, currentUser).isPresent();
            }

            return new PostResponseDto(post, likesCount, liked);
        });
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PostResponseDto getPostById(PrincipalDetails principalDetails, Long postId) {
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

    public List<PostResponseDto> getHotPosts(PrincipalDetails principalDetails) {
        Page<Post> postPage = postRepository.findPostsWithAtLeastTenLikes(Pageable.unpaged());
        List<Post> posts = postPage.getContent();

        User currentUser = principalDetails != null ? principalDetails.getUser() : null;

        List<PostResponseDto> postResponseDtoList = posts.stream()
                .map(post -> {
                    int likesCount = (int) postLikeRepository.countByPost(post);

                    boolean liked = false;
                    if (currentUser != null) {
                        liked = postLikeRepository.findByPostAndUser(post, currentUser).isPresent();
                    }

                    return new PostResponseDto(post, likesCount, liked);
                })
                .toList();
        return postResponseDtoList;
    }

    @Transactional
    public PostResponseDto updatePost(
            PrincipalDetails principalDetails,
            Long postId,
            PostRequestDto postRequestDto,
            MultipartFile file,
            Boolean deleteFile) {
        Post post = findPost(postId);
        checkPostOwnership(post, principalDetails);

        post.update(
                postRequestDto.getTitle(),
                postRequestDto.getContent()
        );

        if (deleteFile != null && deleteFile) {
            fileService.deleteFilesByPost(post);
            System.out.println("📝 게시글 수정: 기존 파일 삭제 완료");
        }

        if (file != null && !file.isEmpty()) {
            if (!post.getFiles().isEmpty()) {
                fileService.deleteFilesByPost(post);
                System.out.println("📝 게시글 수정: 기존 파일 교체");
            }
            fileService.uploadFile(post, file);
            System.out.println("📝 게시글 수정: 새 파일 업로드 완료");
        }

        int likesCount = (int) postLikeRepository.countByPost(post);
        boolean liked = postLikeRepository.findByPostAndUser(post, principalDetails.getUser()).isPresent();

        return new PostResponseDto(post, likesCount, liked);
    }

    @Transactional
    public void deletePost(PrincipalDetails principalDetails, Long postId) {
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

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<PostResponseDto> getMyPosts(PrincipalDetails principalDetails, Pageable pageable) {
        User currentUser = principalDetails.getUser();
        Page<Post> myPosts = postRepository.findByUser(currentUser, pageable);

        return myPosts.map(post -> {
            int likesCount = (int) postLikeRepository.countByPost(post);
            boolean liked = postLikeRepository.findByPostAndUser(post, currentUser).isPresent();
            return new PostResponseDto(post, likesCount, liked);
        });
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<PostResponseDto> getLikedPosts(PrincipalDetails principalDetails, Pageable pageable) {
        User currentUser = principalDetails.getUser();
        Page<Post> likedPosts = postRepository.findPostsLikedByUser(currentUser, pageable);

        return likedPosts.map(post -> {
            int likesCount = (int) postLikeRepository.countByPost(post);
            boolean liked = true;
            return new PostResponseDto(post, likesCount, liked);
        });
    }
}