package com.metaverse.planti_be.led.repository;

import com.metaverse.planti_be.led.domain.Led;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedRepository extends JpaRepository<Led, String> {
}
