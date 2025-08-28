package com.metaverse.planti_be.AiArt.repository;

import com.metaverse.planti_be.AiArt.domain.AiArt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiArtRepository extends JpaRepository<AiArt, Long> {
    List<AiArt> findAllByOrderByCreatedAtDesc();
}
