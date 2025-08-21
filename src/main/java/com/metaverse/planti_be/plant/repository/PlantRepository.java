package com.metaverse.planti_be.plant.repository;

import com.metaverse.planti_be.plant.domain.Plant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantRepository extends JpaRepository<Plant, Long> {
    List<Plant> findAllByOrderByPlantedAtAsc();
}
