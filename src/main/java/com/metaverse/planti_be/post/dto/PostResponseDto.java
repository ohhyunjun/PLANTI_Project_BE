package com.metaverse.planti_be.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.metaverse.planti_be.comment.dto.CommentResponseDto;
import com.metaverse.planti_be.file.dto.FileResponseDto;
import com.metaverse.planti_be.post.domain.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private Long authorId;  // 추가: 작성자 ID
    private String authorUsername;
    private String authorEmail;
    private boolean liked;
    private int likesCount;
    private int commentCount;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private List<CommentResponseDto> comments;
    private List<FileResponseDto> files;

    public PostResponseDto(Post post, int likesCount, boolean liked) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();

        if (post.getUser() != null) {
            this.authorId = post.getUser().getId();  // 작성자 ID 설정
            this.authorUsername = post.getUser().getUsername();
            this.authorEmail = post.getUser().getEmail();
        }

        this.likesCount = likesCount;
        this.liked = liked;

        if (post.getComments() != null) {
            this.comments = post.getComments().stream()
                    .map(CommentResponseDto::new)
                    .collect(Collectors.toList());
            this.commentCount = post.getComments().size();//댓글 개수 설정
        } else {
            this.comments = List.of();
        }

        if (post.getFiles() != null) {
            this.files = post.getFiles().stream()
                    .map(FileResponseDto::new)
                    .collect(Collectors.toList());
        } else {
            this.files = List.of();
        }
    }

    public PostResponseDto(Post post, int likesCount) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();

        if (post.getUser() != null) {
            this.authorId = post.getUser().getId();  // 작성자 ID 설정
            this.authorUsername = post.getUser().getUsername();
        }

        this.likesCount = likesCount;
        this.liked = false;

        this.comments = List.of();
        this.commentCount = 0;
        this.files = List.of();
    }

}