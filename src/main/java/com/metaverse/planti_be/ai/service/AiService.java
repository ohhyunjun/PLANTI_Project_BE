package com.metaverse.planti_be.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatModel chatModel;
    private final OpenAiImageModel imageModel;

    /**
     * DALL-E를 이용해 텍스트 프롬프트 기반으로 이미지를 생성합니다.
     * @param prompt 이미지 생성 프롬프트
     * @return 생성된 이미지 URL
     * @throws IllegalArgumentException 프롬프트가 비어있거나 null인 경우
     * @throws RuntimeException AI 이미지 생성 실패 시
     */
    public String createImage(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("이미지 생성 프롬프트는 비어있을 수 없습니다.");
        }

        try {
            log.info("AI 이미지 생성 요청 - 프롬프트: {}", prompt);

            OpenAiImageOptions options = OpenAiImageOptions.builder()
                    .withQuality("hd")
                    .withN(1)
                    .withHeight(1024)
                    .withWidth(1024)
                    .withResponseFormat("url")
                    .build();

            ImagePrompt imagePrompt = new ImagePrompt(prompt, options);
            ImageResponse response = imageModel.call(imagePrompt);

            String imageUrl = response.getResult().getOutput().getUrl();

            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                throw new RuntimeException("AI가 이미지 URL을 생성하지 못했습니다.");
            }

            log.info("AI 이미지 생성 성공 - URL: {}", imageUrl);
            return imageUrl;

        } catch (Exception e) {
            log.error("AI 이미지 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("AI 이미지 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 식물 종류와 LED 설정값을 기반으로 AI에게 구체적인 조언을 구합니다.
     * @param plantSpecies 식물 종류 (예: "토마토")
     * @param userIntensity 사용자가 설정한 밝기 단계 (1-5)
     * @param startTimeStr 조명 시작 시간 ("HH:mm" 형식)
     * @param endTimeStr 조명 종료 시간 ("HH:mm" 형식)
     * @return AI가 생성한 '진단'과 '조언' 형식의 텍스트
     */
    public String getLedAdvice(String plantSpecies, int userIntensity, String startTimeStr, String endTimeStr) {
        validateLedAdviceParameters(plantSpecies, userIntensity, startTimeStr, endTimeStr);

        // 1. AI에 전달할 데이터 가공 (지속 시간 계산, 실제 밝기 값 변환)
        LocalTime startTime = LocalTime.parse(startTimeStr);
        LocalTime endTime = LocalTime.parse(endTimeStr);

        Duration duration = Duration.between(startTime, endTime);
        if (duration.isNegative()) { // 자정을 넘어가는 시간 처리
            duration = duration.plusDays(1);
        }
        long hours = duration.toHours();
        int actualIntensity = mapIntensity(userIntensity);

        try {
            log.info("LED 조언 요청 - 식물: {}, 설정: {}단계({}), 시간: {}~{} ({}시간)",
                    plantSpecies, userIntensity, actualIntensity, startTimeStr, endTimeStr, hours);

            // 2. AI의 역할과 답변 형식을 구체적으로 지시하는 시스템 프롬프트
            String systemMessageText = "당신은 '정밀 식물 재배 전문가'입니다. " +
                    "당신의 임무는 사용자의 설정을 '참고: 최적 성장 조건'과 비교하여, 부족한 점을 명확히 지적하고 개선 방안을 구체적인 숫자로 제시하는 것입니다. " +
                    "**매우 중요한 규칙: 사용자에게 응답할 때, 0~255 범위의 '실제 밝기 값'은 절대 언급하지 마세요. 모든 밝기 조언은 사용자가 이해하는 1~5 '단계'로만 표현해야 합니다.** " +
                    "만약 설정이 최적 조건에 미치지 못한다면, '적절하다'는 표현 대신 부족한 점을 반드시 언급해야 합니다. " +
                    "답변은 다음 형식에 맞춰 한국어로 2~3줄로 요약해주세요:\n" +
                    "**진단:** [현재 설정이 최적 조건과 비교했을 때 어떤지 명확한 평가. 부족한 점을 먼저 지적할 것.]\n" +
                    "**조언:** [설정을 최적 조건에 맞추기 위한 구체적인 숫자(예: '2시간 더 늘리세요', '4단계로 조절하세요')를 제시.]";
            SystemMessage systemMessage = new SystemMessage(systemMessageText);

            // 3. 가공된 데이터를 명확하게 전달하는 사용자 프롬프트
            String userMessageText = createLedAdvicePrompt(plantSpecies, userIntensity, actualIntensity, hours);
            UserMessage userMessage = new UserMessage(userMessageText);

            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
            String advice = chatModel.call(prompt).getResult().getOutput().getContent();

            if (advice == null || advice.trim().isEmpty()) {
                throw new RuntimeException("AI가 조언을 생성하지 못했습니다.");
            }

            log.info("LED 조언 생성 성공");
            return advice;

        } catch (Exception e) {
            log.error("LED 조언 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("AI 조언 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * LED 조언 요청 파라미터 검증
     */
    private void validateLedAdviceParameters(String plantSpecies, int intensity,
                                             String startTime, String endTime) {
        if (plantSpecies == null || plantSpecies.trim().isEmpty()) {
            throw new IllegalArgumentException("식물 종류는 비어있을 수 없습니다.");
        }
        if (intensity < 0 || intensity > 5) {
            throw new IllegalArgumentException("LED 강도는 0에서 5 사이여야 합니다.");
        }
        if (startTime == null || startTime.trim().isEmpty()) {
            throw new IllegalArgumentException("시작 시간은 필수입니다.");
        }
        if (endTime == null || endTime.trim().isEmpty()) {
            throw new IllegalArgumentException("종료 시간은 필수입니다.");
        }
        // 시간 형식 검증 (HH:mm)
        if (!startTime.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            throw new IllegalArgumentException("시작 시간 형식이 올바르지 않습니다. HH:mm 형식이어야 합니다.");
        }
        if (!endTime.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            throw new IllegalArgumentException("종료 시간 형식이 올바르지 않습니다. HH:mm 형식이어야 합니다.");
        }
    }

    /**
     * LED 조언을 위한 프롬프트 생성
     */
    private String createLedAdvicePrompt(String plantSpecies, int userIntensity, int actualIntensity, long hours) {
        /*
        // 향후 DB나 설정 파일에서 식물별 최적 조건을 가져오도록 확장할 수 있습니다.
        String guidelines = "";
        if ("토마토".equalsIgnoreCase(plantSpecies)) {
            guidelines = "참고: 토마토의 최적 성장 조건은 일일 조명 시간 14-16시간이며, 빛의 세기는 최대치(5단계, 255)에 가깝게 설정하는 것입니다.";
        }
        // else if ("상추".equalsIgnoreCase(plantSpecies)) { ... } 와 같이 확장 가능*/

        return String.format(
                "분석할 데이터는 다음과 같습니다:\n" +
                        "- 식물 종류: %s\n" +
                        "- LED 설정: %d단계 (실제 밝기 값: %d, 최대: 255)\n" +
                        "- 일일 조명 시간: 약 %d시간",
                plantSpecies, userIntensity, actualIntensity, hours
        );
    }
    /**
     * 사용자의 1-5단계 입력을 실제 하드웨어 값(0-255)으로 변환합니다.
     * 이 로직은 LedService의 로직과 동일하게 유지되어야 합니다.
     */
    private int mapIntensity(int userIntensity) {
        switch (userIntensity) {
            case 1: return 31;
            case 2: return 82;
            case 3: return 143;
            case 4: return 200;
            case 5: return 255;
            default: return 0;
        }
    }
}