package com.metaverse.planti_be.AiArt.domain;

import com.metaverse.planti_be.AiArt.dto.AiArtRequestDto;
import com.metaverse.planti_be.common.TimeStamped;
import com.metaverse.planti_be.plant.domain.Plant;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    public AiArt(AiArtRequestDto aiArtRequestDto) {
        this.originalImageUrl = aiArtRequestDto.getOriginalImageUrl();
        this.artImageUrl = aiArtRequestDto.getArtImageUrl();
        this.style = aiArtRequestDto.getStyle();
    }
    public void update(AiArtRequestDto aiArtRequestDto) {
        this.originalImageUrl = aiArtRequestDto.getOriginalImageUrl();
        this.artImageUrl = aiArtRequestDto.getArtImageUrl();
        this.style = aiArtRequestDto.getStyle();
    }
}
