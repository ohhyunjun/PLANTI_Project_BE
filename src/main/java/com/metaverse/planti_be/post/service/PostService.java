package com.metaverse.planti_be.post.service;

import com.metaverse.planti_be.post.domain.Post;
import com.metaverse.planti_be.post.dto.PostRequestDto;
import com.metaverse.planti_be.post.dto.PostResponseDto;
import com.metaverse.planti_be.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;

    public PostResponseDto createPost(PostRequestDto postRequestDto) {
        Post post = new Post(postRequestDto);
        Post savedPost = postRepository.save(post);
        PostResponseDto postResponseDto = new PostResponseDto(savedPost);
        return postResponseDto;
    }

    // 게시글 전체 조회
    public List<PostResponseDto> getPost() {
        List<PostResponseDto> responseList = postRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(PostResponseDto::new)
                .toList();
        return responseList;
    }

    // 게시글 수정
    public Long updatePost(Long id, PostRequestDto postRequestDto){
        Post post = findPost(id);
        post.update(postRequestDto);
        return id;
    }

    // 게시글 삭제
    public Long deletePost(Long id){
        Post post = findPost(id);
        postRepository.delete(post);
        return id;
    }

    private Post findPost(Long id) {
        return postRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 게시글은 존재하지 않습니다.")
        );
    }
}
