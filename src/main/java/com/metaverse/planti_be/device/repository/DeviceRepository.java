package com.metaverse.planti_be.device.repository;

import com.metaverse.planti_be.device.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    // 사용자 ID로 기기 목록 조회 메서드 추가
    List<Device> findByUserId(Long userId);
}
