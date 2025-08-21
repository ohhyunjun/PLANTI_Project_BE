package com.metaverse.planti_be.plant.domain;

import com.metaverse.planti_be.common.TimeStamped;
import com.metaverse.planti_be.plant.dto.PlantRequestDto;
import com.metaverse.planti_be.plant.entity.Stage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @Column(name = "plantStage")
    @Enumerated(EnumType.STRING)
    private Stage stage;

    public Plant(PlantRequestDto plantRequestDto) {
        this.name = plantRequestDto.getName();
        this.species = plantRequestDto.getSpecies();
    }

    public void update(PlantRequestDto plantRequestDto) {
        this.name = plantRequestDto.getName();
        this.species = plantRequestDto.getSpecies();
    }

}
