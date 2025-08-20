package com.metaverse.planti_be.comment.domain;

import com.metaverse.planti_be.comment.dto.CommentRequestDto;
import com.metaverse.planti_be.common.TimeStamped;
import com.metaverse.planti_be.post.domain.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Comment")
@Getter
@Setter
@NoArgsConstructor
public class Comment extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content")
    private String content;


    public Comment(CommentRequestDto  commentRequestDto) {
        this.content = commentRequestDto.getContent();
    }

    public void update(CommentRequestDto commentRequestDto) {
        this.content = commentRequestDto.getContent();
    }
}
