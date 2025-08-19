package com.metaverse.planti_be.comment.domain;

import com.metaverse.planti_be.common.TimeStamped;
import jakarta.persistence.*;
import lombok.Builder;
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

    @Builder
    public Comment(Long id, String content) {
        this.id = id;
        this.content = content;
    }

    public void update(String content) {
        this.content = content;
    }
}
