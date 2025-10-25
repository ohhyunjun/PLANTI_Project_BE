package com.metaverse.planti_be.aiArt.service;

import com.metaverse.planti_be.ai.service.AiService;
import com.metaverse.planti_be.aiArt.domain.AiArt;
import com.metaverse.planti_be.aiArt.dto.AiArtRequestDto;
import com.metaverse.planti_be.aiArt.dto.AiArtResponseDto;
import com.metaverse.planti_be.aiArt.repository.AiArtRepository;
import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiArtService {

    private final AiArtRepository aiArtRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    /**
     * AI 아트 생성
     * @param userId 사용자 ID
     * @param aiArtRequestDto 아트 생성 요청 정보
     * @return 생성된 아트 정보
     */
    @Transactional
    public AiArtResponseDto createAiArt(Long userId, AiArtRequestDto aiArtRequestDto) {
        log.info("AI 아트 생성 요청 - 사용자 ID: {}, 스타일: {}", userId, aiArtRequestDto.getStyle());

        User user = findUserById(userId);

        // 1. AI 호출: 스타일 프롬프트를 전달하여 이미지 생성 요청
        String generatedArtUrl = aiService.createImage(aiArtRequestDto.getStyle());

        // 2. DB 저장: 원본 URL과 AI가 생성한 URL을 함께 DB에 저장
        AiArt aiArt = new AiArt(
                aiArtRequestDto.getOriginalImageUrl(),
                generatedArtUrl,
                aiArtRequestDto.getStyle()
        );
        aiArt.setUser(user);

        AiArt savedAiArt = aiArtRepository.save(aiArt);
        log.info("AI 아트 생성 완료 - ID: {}", savedAiArt.getId());

        return new AiArtResponseDto(savedAiArt);
    }

    /**
     * 모든 AI 아트 조회 (공개용)
     * @return 전체 아트 목록
     */
    @Transactional(readOnly = true)
    public List<AiArtResponseDto> getAiArts() {
        log.info("전체 AI 아트 조회 요청");
        return aiArtRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AiArtResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 모든 AI 아트 조회
     * @param userId 사용자 ID
     * @return 해당 사용자의 아트 목록
     */
    @Transactional(readOnly = true)
    public List<AiArtResponseDto> getAiArtsByUser(Long userId) {
        log.info("사용자 AI 아트 조회 요청 - 사용자 ID: {}", userId);
        return aiArtRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(AiArtResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 특정 AI 아트 상세 조회
     * @param userId 사용자 ID
     * @param aiArtId 아트 ID
     * @return 아트 상세 정보
     */
    @Transactional(readOnly = true)
    public AiArtResponseDto getAiArtById(Long userId, Long aiArtId) {
        log.info("AI 아트 상세 조회 - 사용자 ID: {}, 아트 ID: {}", userId, aiArtId);
        AiArt aiArt = findAiArtOwnedByUser(aiArtId, userId);
        return new AiArtResponseDto(aiArt);
    }

    /**
     * AI 아트 정보 수정
     * @param userId 사용자 ID
     * @param aiArtId 아트 ID
     * @param aiArtRequestDto 수정할 아트 정보
     * @return 수정된 아트 정보
     */
    @Transactional
    public AiArtResponseDto updateAiArt(Long userId, Long aiArtId, AiArtRequestDto aiArtRequestDto) {
        log.info("AI 아트 수정 요청 - 사용자 ID: {}, 아트 ID: {}, 새 스타일: {}",
                userId, aiArtId, aiArtRequestDto.getStyle());

        AiArt aiArt = findAiArtOwnedByUser(aiArtId, userId);

        // 1. AI 호출: 새로운 스타일 프롬프트로 이미지를 다시 생성
        String newArtImageUrl = aiService.createImage(aiArtRequestDto.getStyle());

        // 2. DB 업데이트: 도메인 객체의 update 메소드 호출
        aiArt.update(
                aiArtRequestDto.getOriginalImageUrl(),
                newArtImageUrl,
                aiArtRequestDto.getStyle()
        );

        // 변경된 내용은 @Transactional에 의해 자동 저장(dirty checking)
        log.info("AI 아트 수정 완료 - ID: {}", aiArtId);
        return new AiArtResponseDto(aiArt);
    }

    /**
     * AI 아트 삭제
     * @param userId 사용자 ID
     * @param aiArtId 아트 ID
     */
    @Transactional
    public void deleteAiArt(Long userId, Long aiArtId) {
        log.info("AI 아트 삭제 요청 - 사용자 ID: {}, 아트 ID: {}", userId, aiArtId);
        AiArt aiArt = findAiArtOwnedByUser(aiArtId, userId);
        aiArtRepository.delete(aiArt);
        log.info("AI 아트 삭제 완료 - ID: {}", aiArtId);
    }

    /**
     * 사용자 ID로 사용자를 찾는 헬퍼 메소드
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + userId));
    }

    /**
     * 특정 사용자가 소유한 AI 아트를 찾는 헬퍼 메소드
     */
    private AiArt findAiArtOwnedByUser(Long aiArtId, Long userId) {
        return aiArtRepository.findByIdAndUserId(aiArtId, userId)
                .orElseThrow(() -> new AccessDeniedException("해당 아트에 대한 권한이 없거나 존재하지 않는 아트입니다."));
    }
}