package com.metaverse.planti_be.diary.repository;

import com.metaverse.planti_be.diary.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findAllByOrderByCreatedAtAsc();
}
