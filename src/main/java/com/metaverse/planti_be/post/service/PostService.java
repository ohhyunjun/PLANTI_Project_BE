package com.metaverse.planti_be.post.service;

import com.metaverse.planti_be.post.domain.Post;
import com.metaverse.planti_be.post.dto.PostRequestDto;
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

    public Post save(PostRequestDto postRequestDto){
        return postRepository.save(postRequestDto.toEntity());
    }
    // 게시글 전체 조회
    public List<Post> findAll(){
        return postRepository.findAll();
    }
    // 게시글 삭제
    public void delete(Long id){
        Post post = postRepository.findById(id).orElseThrow(()->new RuntimeException("Post not found" + id));
        postRepository.delete(post);
    }
    // 게시글 수정
    public Post update(Long id, PostRequestDto postRequestDto){
        Post post = postRepository.findById(id).orElseThrow(()->new RuntimeException("Post not found" + id));
        post.update(postRequestDto.getTitle(),postRequestDto.getContent());
        return post;
    }
}
