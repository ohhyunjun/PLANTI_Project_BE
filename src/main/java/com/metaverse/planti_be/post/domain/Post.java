package com.metaverse.planti_be.post.domain;

import com.metaverse.planti_be.common.TimeStamped;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Post")
@Getter
@Setter
@NoArgsConstructor
public class Post extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Builder
    public Post(Long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public void update(String title, String content){
        this.title = title;
        this.content = content;
    }
}
