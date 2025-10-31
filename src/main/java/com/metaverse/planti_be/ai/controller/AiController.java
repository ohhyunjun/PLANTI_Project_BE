package com.metaverse.planti_be.ai.controller;

import com.metaverse.planti_be.ai.dto.RequestDto;
import com.metaverse.planti_be.ai.dto.ResponseDto;
import com.metaverse.planti_be.ai.service.AiService;
import com.metaverse.planti_be.auth.domain.PrincipalDetails;
import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.led.dto.LedStatusResponseDto;
import com.metaverse.planti_be.led.service.LedService;
import com.metaverse.planti_be.plant.domain.Plant;
import com.metaverse.planti_be.plant.repository.PlantRepository;
import com.metaverse.planti_be.species.domain.Species;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final LedService ledService;
    private final PlantRepository plantRepository;

    @PostMapping("/led-advice")
    public ResponseEntity<ResponseDto> getLedAdvice(
            @Valid @RequestBody RequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        User user = principalDetails.getUser();
        String serialNumber = requestDto.getSerialNumber();

        // 1. 시리얼 번호로 식물 정보를 조회
        Plant plant = plantRepository.findByDeviceId(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 기기에 등록된 식물이 없습니다."));

        // 2. Plant에서 Species 정보 가져오기
        Species species = plant.getSpecies();

        // 3. LedService를 호출하여 현재 LED 설정값 조회
        LedStatusResponseDto currentLedStatus = ledService.getLedSettings(serialNumber, user);

        // 4. AiService를 호출하여 AI 조언 요청
        // Species의 name과 aiPromptGuideline을 함께 전달
        String advice = aiService.getLedAdvice(
                species.getName(),
                species.getAiPromptGuideline(),
                currentLedStatus.getIntensity(),
                currentLedStatus.getStartTime(),
                currentLedStatus.getEndTime()
        );

        ResponseDto responseDto = new ResponseDto(advice);

        return ResponseEntity.ok(responseDto);
    }
}