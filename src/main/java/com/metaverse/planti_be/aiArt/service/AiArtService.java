package com.metaverse.planti_be.aiArt.service;

import com.metaverse.planti_be.ai.service.AiService;
import com.metaverse.planti_be.aiArt.domain.AiArt;
import com.metaverse.planti_be.aiArt.dto.AiArtRequestDto;
import com.metaverse.planti_be.aiArt.dto.AiArtResponseDto;
import com.metaverse.planti_be.aiArt.repository.AiArtRepository;
import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.auth.repository.UserRepository;
import com.metaverse.planti_be.file.service.FileService;
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
    private final FileService fileService;

    @Transactional
    public AiArtResponseDto createAiArt(Long userId, AiArtRequestDto aiArtRequestDto) {
        log.info("AI 아트 생성 요청 - 사용자 ID: {}, 스타일: {}", userId, aiArtRequestDto.getStyle());

        User user = findUserById(userId);

        // 1. 로컬 이미지를 Base64 Data URL로 변환
        String base64ImageData = convertImageToBase64(aiArtRequestDto.getOriginalImageUrl());
        log.info("이미지 Base64 변환 완료");

        // 2. OpenAI Vision API로 이미지 분석 (Base64 직접 전송)
        String imageDescription = aiService.analyzeImage(base64ImageData);
        log.info("원본 이미지 분석 완료");

        // 3. 분석된 설명 + 스타일로 새 이미지 생성
        String generatedArtUrl = aiService.createImageWithDescription(imageDescription, aiArtRequestDto.getStyle());
        log.info("스타일 적용 이미지 생성 완료");

        // 4. DB 저장
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

    @Transactional(readOnly = true)
    public List<AiArtResponseDto> getAiArts() {
        log.info("전체 AI 아트 조회 요청");
        return aiArtRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AiArtResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AiArtResponseDto> getAiArtsByUser(Long userId) {
        log.info("사용자 AI 아트 조회 요청 - 사용자 ID: {}", userId);
        return aiArtRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(AiArtResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AiArtResponseDto getAiArtById(Long userId, Long aiArtId) {
        log.info("AI 아트 상세 조회 - 사용자 ID: {}, 아트 ID: {}", userId, aiArtId);
        AiArt aiArt = findAiArtOwnedByUser(aiArtId, userId);
        return new AiArtResponseDto(aiArt);
    }

    @Transactional
    public AiArtResponseDto updateAiArt(Long userId, Long aiArtId, AiArtRequestDto aiArtRequestDto) {
        log.info("AI 아트 수정 요청 - 사용자 ID: {}, 아트 ID: {}, 새 스타일: {}",
                userId, aiArtId, aiArtRequestDto.getStyle());

        AiArt aiArt = findAiArtOwnedByUser(aiArtId, userId);

        String base64ImageData = convertImageToBase64(aiArtRequestDto.getOriginalImageUrl());

        String imageDescription = aiService.analyzeImage(base64ImageData);
        log.info("원본 이미지 분석 완료");

        String newArtImageUrl = aiService.createImageWithDescription(imageDescription, aiArtRequestDto.getStyle());
        log.info("새 스타일 적용 이미지 생성 완료");

        aiArt.update(
                aiArtRequestDto.getOriginalImageUrl(),
                newArtImageUrl,
                aiArtRequestDto.getStyle()
        );

        log.info("AI 아트 수정 완료 - ID: {}", aiArtId);
        return new AiArtResponseDto(aiArt);
    }

    @Transactional
    public void deleteAiArt(Long userId, Long aiArtId) {
        log.info("AI 아트 삭제 요청 - 사용자 ID: {}, 아트 ID: {}", userId, aiArtId);
        AiArt aiArt = findAiArtOwnedByUser(aiArtId, userId);
        aiArtRepository.delete(aiArt);
        log.info("AI 아트 삭제 완료 - ID: {}", aiArtId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + userId));
    }

    private AiArt findAiArtOwnedByUser(Long aiArtId, Long userId) {
        return aiArtRepository.findByIdAndUserId(aiArtId, userId)
                .orElseThrow(() -> new AccessDeniedException("해당 아트에 대한 권한이 없거나 존재하지 않는 아트입니다."));
    }

    /**
     * 이미지 URL을 Base64 Data URL로 변환
     * OpenAI Vision API에 직접 전송 가능한 형식
     */
    private String convertImageToBase64(String imageUrl) {
        try {
            log.info("이미지 URL: {}", imageUrl);

            // URL에서 파일명 추출
            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            log.info("추출된 파일명: {}", fileName);

            // FileService를 통해 Base64 Data URL로 변환
            String base64DataUrl = fileService.getAiArtImageAsBase64(fileName);

            log.info("Base64 Data URL 생성 성공 - 길이: {} chars", base64DataUrl.length());

            return base64DataUrl;

        } catch (Exception e) {
            log.error("이미지 Base64 변환 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지를 Base64로 변환하는데 실패했습니다: " + e.getMessage(), e);
        }
    }
}