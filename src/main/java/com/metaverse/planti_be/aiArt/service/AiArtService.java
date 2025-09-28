package com.metaverse.planti_be.aiArt.service;

import com.metaverse.planti_be.aiArt.domain.AiArt;
import com.metaverse.planti_be.aiArt.dto.AiArtRequestDto;
import com.metaverse.planti_be.aiArt.dto.AiArtResponseDto;
import com.metaverse.planti_be.aiArt.repository.AiArtRepository;
import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.auth.repository.UserRepository;
import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.device.repository.DeviceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiArtService {

    private final AiArtRepository aiArtRepository;
    private final UserRepository userRepository; // UserRepository 주입

    // AI 아트 생성
    @Transactional
    public AiArtResponseDto createAiArt(Long userId, AiArtRequestDto aiArtRequestDto) {
        User user = findUserById(userId);

        AiArt aiArt = new AiArt(
                aiArtRequestDto.getOriginalImageUrl(),
                aiArtRequestDto.getArtImageUrl(),
                aiArtRequestDto.getStyle()
        );
        aiArt.setUser(user); // User 설정

        AiArt savedAiArt = aiArtRepository.save(aiArt);
        return new AiArtResponseDto(savedAiArt);
    }

    // 모든 AI 아트 조회 (공개용)
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<AiArtResponseDto> getAiArts() {
        return aiArtRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AiArtResponseDto::new)
                .collect(Collectors.toList());
    }

    // 특정 유저의 모든 AI 아트 조회
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<AiArtResponseDto> getAiArtsByUser(Long userId) {
        return aiArtRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(AiArtResponseDto::new)
                .collect(Collectors.toList());
    }

    // 특정 AI 아트 상세 조회
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public AiArtResponseDto getAiArtById(Long userId, Long aiArtId) {
        AiArt aiArt = findAiArtOwnedByUser(aiArtId, userId);
        return new AiArtResponseDto(aiArt);
    }

    // AI 아트 정보 수정
    @Transactional
    public AiArtResponseDto updateAiArt(Long userId, Long aiArtId, AiArtRequestDto aiArtRequestDto) {
        AiArt aiArt = findAiArtOwnedByUser(aiArtId, userId);

        aiArt.update(
                aiArtRequestDto.getOriginalImageUrl(),
                aiArtRequestDto.getStyle()
        );
        return new AiArtResponseDto(aiArt);
    }

    // AI 아트 삭제
    @Transactional
    public void deleteAiArt(Long userId, Long aiArtId) {
        AiArt aiArt = findAiArtOwnedByUser(aiArtId, userId);
        aiArtRepository.delete(aiArt);
    }

    // 유저 ID로 유저를 찾는 헬퍼 메소드
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다: " + userId));
    }

    // 특정 유저가 소유한 AI 아트를 찾는 헬퍼 메소드
    private AiArt findAiArtOwnedByUser(Long aiArtId, Long userId) {
        return aiArtRepository.findByIdAndUserId(aiArtId, userId)
                .orElseThrow(() -> new AccessDeniedException("해당 아트에 대한 권한이 없거나 존재하지 않는 아트입니다."));
    }
}