package com.metaverse.planti_be.post.service;

import com.metaverse.planti_be.file.service.FileService;
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

    @Transactional
    public PostResponseDto createPost(PostRequestDto postRequestDto, MultipartFile file) {
        Post post = new Post(
                postRequestDto.getTitle(),
                postRequestDto.getContent()
        );
        Post savedPost = postRepository.save(post);

        if (file != null && !file.isEmpty()) {
            fileService.uploadFile(savedPost.getId(), file);
        }
        PostResponseDto postResponseDto = new PostResponseDto(savedPost);
        return postResponseDto;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PostResponseDto> getPosts() {
        List<PostResponseDto> PostResponseDtoList = postRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(PostResponseDto::new)
                .toList();
        return PostResponseDtoList;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PostResponseDto getPostById(Long postId) {
        Post post = findPost(postId);
        return new PostResponseDto(post);
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, PostRequestDto postRequestDto){
        Post post = findPost(postId);
        post.update(
                postRequestDto.getTitle(),
                postRequestDto.getContent()
        );
        return new PostResponseDto(post);
    }

    @Transactional
    public void deletePost(Long postId){
        Post post = findPost(postId);
        postRepository.delete(post);
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(() ->
                new IllegalArgumentException("해당 게시글은 존재하지 않습니다.")
        );
    }
}
