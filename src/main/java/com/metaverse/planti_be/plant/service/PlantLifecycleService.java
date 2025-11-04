package com.metaverse.planti_be.plant.service;

import com.metaverse.planti_be.plant.domain.Plant;
import com.metaverse.planti_be.plant.domain.PlantStage;
import com.metaverse.planti_be.plant.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlantLifecycleService {

    private final PlantRepository plantRepository;

    /**
     * 매일 새벽 2시에 실행되어 발아(GERMINATION) 상태의 식물들을 성체(MATURE)로 업데이트합니다.
     */
    @Scheduled(cron = "0 0 2 * * *") // 매일 02:00:00에 실행
    @Transactional
    public void updateMaturePlants() {
        log.info("식물 성장 단계 업데이트 스케줄러 시작");

        // 1. 발아 단계에 있는 모든 식물을 조회합니다.
        List<Plant> germinatedPlants = plantRepository.findByPlantStage(PlantStage.GERMINATION);

        log.info("- 업데이트 대상 식물 {}개 발견", germinatedPlants.size());

        for (Plant plant : germinatedPlants) {
            // 2. 발아 시간이 기록되지 않았거나 품종 정보가 없으면 건너뜁니다.
            if (plant.getGerminatedAt() == null || plant.getSpecies() == null) {
                continue;
            }

            // 3. 발아 후 경과 시간을 계산합니다.
            long daysSinceGerminated = ChronoUnit.DAYS.between(plant.getGerminatedAt(), LocalDateTime.now());
            int daysToMature = plant.getSpecies().getDaysToMature();

            // 4. 경과 시간이 성체까지 걸리는 기간을 넘었으면 상태를 변경합니다.
            if (daysSinceGerminated >= daysToMature) {
                plant.setPlantStage(PlantStage.MATURE);
                log.info("  - 식물 ID: {} -> 성체(MATURE)로 변경됨 (발아 후 {}일 경과)", plant.getId(), daysSinceGerminated);
            }
        }
        log.info("식물 성장 단계 업데이트 스케줄러 종료");
    }
}