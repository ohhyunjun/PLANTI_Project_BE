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
    private String authorUsername;
    private String authorEmail;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private List<CommentResponseDto> comments;
    private List<FileResponseDto> files;

    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();

        if (post.getUser() != null) {
            this.authorUsername = post.getUser().getUsername();
            this.authorEmail = post.getUser().getEmail();
        } else {
            this.authorUsername = null;
            this.authorEmail = null;
        }

        if (post.getComments() != null) {
            this.comments = post.getComments().stream()
                    .map(CommentResponseDto::new)
                    .collect(Collectors.toList());
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

}
