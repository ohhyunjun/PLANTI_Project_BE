package com.metaverse.planti_be.aiArt.repository;

import com.metaverse.planti_be.aiArt.domain.AiArt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiArtRepository extends JpaRepository<AiArt, Long> {
    List<AiArt> findAllByOrderByCreatedAtDesc();

    List<AiArt> findByPlantIdOrderByCreatedAtDesc(Long plantId);

    Optional<AiArt> findByIdAndPlantId(Long aiArtId, Long plantId);
}
