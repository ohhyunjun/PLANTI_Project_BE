package com.metaverse.planti_be.aiArt.domain;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.common.TimeStamped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "aiArt")
@Entity
public class AiArt extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalImageUrl;

    @Column(nullable = false)
    private String artImageUrl;

    @Column(nullable = false)
    private String style;

    // User와 다대일 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    public AiArt(String originalImageUrl, String artImageUrl, String style) {
        this.originalImageUrl = originalImageUrl;
        this.artImageUrl = artImageUrl;
        this.style = style;
    }
    public void update(String originalImageUrl, String style) {
        this.originalImageUrl = originalImageUrl;
        this.style = style;
    }
}