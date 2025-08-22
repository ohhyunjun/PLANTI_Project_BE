package com.metaverse.planti_be.diary.domain;

import com.metaverse.planti_be.common.TimeStamped;
import com.metaverse.planti_be.diary.dto.DiaryRequestDto;
import com.metaverse.planti_be.plant.domain.Plant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "diary")
@Entity
public class Diary extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000, nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    public Diary(DiaryRequestDto diaryRequestDto) {
        this.title = diaryRequestDto.getTitle();
        this.content = diaryRequestDto.getContent();
    }

    public void update(DiaryRequestDto diaryRequestDto) {
        this.title = diaryRequestDto.getTitle();
        this.content = diaryRequestDto.getContent();
    }

}
