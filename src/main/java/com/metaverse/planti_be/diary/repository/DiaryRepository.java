package com.metaverse.planti_be.diary.repository;

import com.metaverse.planti_be.diary.domain.Diary;
import com.metaverse.planti_be.plant.domain.Plant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findAllByOrderByCreatedAtAsc();

    List<Diary> findByPlantIdOrderByCreatedAtAsc(Long plantId);

    Optional<Diary> findByIdAndPlantId(Long diaryId, Long plantId);
}
