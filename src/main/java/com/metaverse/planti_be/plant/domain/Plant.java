package com.metaverse.planti_be.plant.domain;

import com.metaverse.planti_be.AiArt.domain.AiArt;
import com.metaverse.planti_be.common.TimeStamped;
import com.metaverse.planti_be.diary.domain.Diary;
import com.metaverse.planti_be.plant.entity.Stage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "plant")
@Entity
public class Plant extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String species;

    @Column(updatable = false) // 처음 심은 날짜 수정하고싶으면 true
    private LocalDateTime plantedAt;

    // enum 미구현
    @Column(name = "stage")
    @Enumerated(EnumType.STRING)
    private Stage stage;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Diary> diaries = new ArrayList<>();

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiArt> aiArts = new ArrayList<>();


    public Plant(String name, String species, LocalDateTime plantedAt) {
        this.name = name;
        this.species = species;
        this.plantedAt = plantedAt;
    }

    public void update(String name, String species) {
        this.name = name;
        this.species = species;
    }

}
