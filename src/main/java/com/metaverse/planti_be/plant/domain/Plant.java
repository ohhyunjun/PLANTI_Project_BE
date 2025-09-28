package com.metaverse.planti_be.plant.domain;

import com.metaverse.planti_be.common.TimeStamped;
import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.diary.domain.Diary;
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

    @Column(updatable = false)
    private LocalDateTime plantedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlantStage plantStage;

    // Device와의 1:1 연관관계만 유지
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_serial", unique = true, nullable = false)
    private Device device;

    // Diary와의 1:M 관계만 유지 (AiArt 관계 제거)
    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Diary> diaries = new ArrayList<>();

    public Plant(String name, String species, LocalDateTime plantedAt, PlantStage plantStage, Device device) {
        this.name = name;
        this.species = species;
        this.plantedAt = plantedAt;
        this.plantStage = plantStage;
        this.device = device;
    }

    public void update(String name, String species, PlantStage plantStage) {
        this.name = name;
        this.species = species;
        this.plantStage = plantStage;
    }
}
