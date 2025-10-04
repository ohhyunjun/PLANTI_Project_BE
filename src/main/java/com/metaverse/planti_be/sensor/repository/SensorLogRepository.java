package com.metaverse.planti_be.sensor.repository;

import com.metaverse.planti_be.sensor.domain.SensorLog;
import com.metaverse.planti_be.sensor.domain.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {
    @Query("SELECT s FROM SensorLog s WHERE s.device.id = :serialNumber AND s.sensor_type = :sensorType ORDER BY s.createdAt DESC")
    List<SensorLog> findTop10ByDeviceAndSensorType(
            @Param("serialNumber") String serialNumber,
            @Param("sensorType") SensorType sensorType,
            org.springframework.data.domain.Pageable pageable
    );
}
