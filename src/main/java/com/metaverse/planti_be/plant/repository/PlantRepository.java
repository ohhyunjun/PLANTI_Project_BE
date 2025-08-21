package com.metaverse.planti_be.plant.repository;

import com.metaverse.planti_be.plant.domain.Plant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantRepository extends JpaRepository<Plant, Long> {
}
