package com.metaverse.planti_be.aiArt.repository;

import com.metaverse.planti_be.aiArt.domain.AiArt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiArtRepository extends JpaRepository<AiArt, Long> {
    // 유저 ID로 생성일 내림차순 조회
    List<AiArt> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 아트 ID와 유저 ID로 조회
    Optional<AiArt> findByIdAndUserId(Long aiArtId, Long userId);

    // 전체 아트 생성일 내림차순 조회
    List<AiArt> findAllByOrderByCreatedAtDesc();
}

