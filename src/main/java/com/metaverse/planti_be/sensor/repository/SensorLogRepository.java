package com.metaverse.planti_be.sensor.repository;

import com.metaverse.planti_be.sensor.domain.SensorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {
}
