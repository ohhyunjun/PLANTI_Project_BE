package com.metaverse.planti_be.diary.service;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.auth.repository.UserRepository;
import com.metaverse.planti_be.diary.domain.Diary;
import com.metaverse.planti_be.diary.dto.DiaryRequestDto;
import com.metaverse.planti_be.diary.dto.DiaryResponseDto;
import com.metaverse.planti_be.diary.repository.DiaryRepository;
import com.metaverse.planti_be.plant.domain.Plant;
import com.metaverse.planti_be.plant.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring의 Transactional로 통일

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 클래스 레벨에 readOnly 기본값 설정
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final PlantRepository plantRepository;
    private final UserRepository userRepository; // User 정보 조회를 위해 주입

    @Transactional
    public DiaryResponseDto createDiary(Long userId, Long plantId, DiaryRequestDto diaryRequestDto) {
        User user = findUserById(userId);
        // 다이어리를 작성하려는 식물이 현재 로그인한 유저의 소유인지 먼저 확인합니다.
        Plant plant = findPlantOwnedByUser(plantId, userId);

        Diary diary = new Diary(
                diaryRequestDto.getTitle(),
                diaryRequestDto.getContent(),
                plant,
                user // 다이어리에 작성자(User) 정보를 함께 저장합니다.
        );
        Diary savedDiary = diaryRepository.save(diary);
        return new DiaryResponseDto(savedDiary);
    }

    public List<DiaryResponseDto> getDiaries(Long userId) {
        User user = findUserById(userId);
        // 현재 로그인한 유저가 작성한 모든 다이어리를 조회합니다.
        return diaryRepository.findAllByUserOrderByCreatedAtAsc(user).stream()
                .map(DiaryResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<DiaryResponseDto> getDiariesByPlantId(Long userId, Long plantId) {
        User user = findUserById(userId);
        // 조회하려는 식물이 현재 로그인한 유저의 소유인지 먼저 확인합니다.
        findPlantOwnedByUser(plantId, userId);

        return diaryRepository.findByUserAndPlantIdOrderByCreatedAtAsc(user, plantId).stream()
                .map(DiaryResponseDto::new)
                .collect(Collectors.toList());
    }

    public DiaryResponseDto getDiaryById(Long userId, Long plantId, Long diaryId) {
        // 조회하려는 식물이 현재 로그인한 유저의 소유인지 먼저 확인합니다.
        findPlantOwnedByUser(plantId, userId);

        // 해당 식물에 속한 다이어리가 맞는지 확인합니다.
        Diary diary = diaryRepository.findByIdAndPlantId(diaryId, plantId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식물에서 다이어리를 찾을 수 없습니다."));
        return new DiaryResponseDto(diary);
    }

    @Transactional
    public DiaryResponseDto updateDiary(Long userId, Long diaryId, DiaryRequestDto diaryRequestDto) {
        // 수정하려는 다이어리가 현재 로그인한 유저의 소유인지 먼저 확인합니다.
        Diary diary = findDiaryOwnedByUser(diaryId, userId);

        diary.update(
                diaryRequestDto.getTitle(),
                diaryRequestDto.getContent()
        );
        return new DiaryResponseDto(diary);
    }

    @Transactional
    public void deleteDiary(Long userId, Long diaryId) {
        // 삭제하려는 다이어리가 현재 로그인한 유저의 소유인지 먼저 확인합니다.
        Diary diary = findDiaryOwnedByUser(diaryId, userId);

        diaryRepository.delete(diary);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private Plant findPlantOwnedByUser(Long plantId, Long userId) {
        return plantRepository.findByIdAndUserId(plantId, userId)
                .orElseThrow(() -> new SecurityException("해당 식물에 접근할 권한이 없습니다."));
    }

    private Diary findDiaryOwnedByUser(Long diaryId, Long userId) {
        User user = findUserById(userId);
        return diaryRepository.findByIdAndUser(diaryId, user)
                .orElseThrow(() -> new SecurityException("해당 다이어리를 찾을 수 없거나 접근 권한이 없습니다."));
    }
}