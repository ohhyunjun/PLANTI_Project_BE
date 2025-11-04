package com.metaverse.planti_be.species.repository;

import com.metaverse.planti_be.plant.domain.Plant;
import com.metaverse.planti_be.species.domain.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpeciesRepository extends JpaRepository<Species, Long> {
    // 품종 이름으로 조회 (중복 등록 방지용)
    Optional<Species> findByName(String name);
}
