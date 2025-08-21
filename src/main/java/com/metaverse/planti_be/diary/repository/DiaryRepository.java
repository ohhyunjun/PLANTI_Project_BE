package com.metaverse.planti_be.diary.repository;

import com.metaverse.planti_be.diary.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
}
