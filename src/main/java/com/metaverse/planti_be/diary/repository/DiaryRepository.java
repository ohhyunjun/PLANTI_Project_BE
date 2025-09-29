package com.metaverse.planti_be.diary.repository;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.diary.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    // 특정 사용자가 작성한 모든 일기를 조회
    List<Diary> findAllByUserOrderByCreatedAtAsc(User user);

    // 특정 사용자가 특정 식물에 대해 작성한 모든 일기를 조회
    List<Diary> findByUserAndPlantIdOrderByCreatedAtAsc(User user, Long plantId);

    // 특정 게시글에 속한 특정 다이어리를 조회 (공개 조회용)
    Optional<Diary> findByIdAndPlantId(Long diaryId, Long plantId);

    // 특정 사용자가 작성한 특정 다이어리를 조회 (수정/삭제 시 소유권 확인용)
    Optional<Diary> findByIdAndUser(Long diaryId, User user);

    // 특정 사용자가 특정 날짜(targetDate)에 작성한 모든 다이어리를 조회
    List<Diary> findByUserAndTargetDate(User user, LocalDate targetDate);
}