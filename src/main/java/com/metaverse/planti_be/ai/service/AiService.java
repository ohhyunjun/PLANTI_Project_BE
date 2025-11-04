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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatModel chatModel;
    private final OpenAiImageModel imageModel;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    /**
     * DALL-E를 이용해 텍스트 프롬프트 기반으로 이미지를 생성합니다.
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
     * Base64 이미지를 OpenAI Vision API로 직접 전송하여 분석
     */
    public String analyzeImage(String base64ImageData) {
        if (base64ImageData == null || base64ImageData.trim().isEmpty()) {
            throw new IllegalArgumentException("이미지 데이터는 비어있을 수 없습니다.");
        }

        try {
            log.info("이미지 분석 요청 - Base64 데이터를 OpenAI API로 직접 전송");

            if (!base64ImageData.startsWith("data:image/")) {
                throw new IllegalArgumentException("Base64 Data URL 형식이 올바르지 않습니다.");
            }

            String systemPrompt = "당신은 이미지를 매우 상세하게 분석하는 전문가입니다. " +
                    "이미지의 구도, 객체의 위치, 색상, 조명, 배경 등을 정확하고 구체적으로 설명해야 합니다.";

            String userPrompt = "이 이미지를 매우 상세하게 분석하여 설명해주세요. 다음 항목들을 포함해주세요:\n" +
                    "1. 주요 객체: 무엇이 있는지, 어디에 위치하는지\n" +
                    "2. 구도: 객체들의 배치와 공간 관계\n" +
                    "3. 색상: 주요 색상과 톤\n" +
                    "4. 조명: 빛의 방향과 분위기\n" +
                    "5. 배경: 배경의 특징\n" +
                    "6. 전체적인 분위기\n\n" +
                    "설명은 영어로 작성하고, 이 설명을 바탕으로 동일한 이미지를 재생성할 수 있을 정도로 구체적이어야 합니다.";

            String openAiUrl = "https://api.openai.com/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o");
            requestBody.put("max_tokens", 1000);

            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);

            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");

            List<Map<String, Object>> userContent = List.of(
                    Map.of("type", "text", "text", userPrompt),
                    Map.of("type", "image_url",
                            "image_url", Map.of("url", base64ImageData, "detail", "high"))
            );
            userMessage.put("content", userContent);

            requestBody.put("messages", List.of(systemMessage, userMessage));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(openAiUrl, entity, Map.class);

            if (response == null) {
                throw new RuntimeException("OpenAI API 응답이 null입니다.");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("OpenAI API 응답에 choices가 없습니다.");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String description = (String) message.get("content");

            if (description == null || description.trim().isEmpty()) {
                throw new RuntimeException("이미지 분석에 실패했습니다.");
            }

            log.info("이미지 분석 완료");
            return description;

        } catch (Exception e) {
            log.error("이미지 분석 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 분석에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 이미지 설명과 스타일을 기반으로 새로운 이미지를 생성합니다.
     */
    public String createImageWithDescription(String imageDescription, String style) {
        if (imageDescription == null || imageDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("이미지 설명은 비어있을 수 없습니다.");
        }
        if (style == null || style.trim().isEmpty()) {
            throw new IllegalArgumentException("스타일은 비어있을 수 없습니다.");
        }

        String styleInstruction = buildStyleInstruction(style);

        String finalPrompt = imageDescription + "\n\n" +
                styleInstruction + "\n\n" +
                "CRITICAL: Maintain the EXACT composition, object positions, and spatial relationships " +
                "described above. Only apply the style enhancement to this existing scene. " +
                "Do not add new objects or change the layout.";

        return createImage(finalPrompt);
    }

    /**
     * 스타일에 따른 프롬프트 지시문을 생성합니다.
     */
    private String buildStyleInstruction(String style) {
        String lowerStyle = style.toLowerCase();

        if (lowerStyle.contains("지브리") || lowerStyle.contains("ghibli")) {
            return "Apply this exact scene in Studio Ghibli animation style: " +
                    "soft watercolor textures, warm gentle lighting, hand-painted aesthetic, " +
                    "gentle brush strokes, and dreamy atmosphere.";
        } else if (lowerStyle.contains("수채화") || lowerStyle.contains("watercolor")) {
            return "Apply this exact scene in watercolor painting style: " +
                    "delicate color blending, transparent washes, paper texture, " +
                    "flowing and gentle aesthetic.";
        } else if (lowerStyle.contains("유화") || lowerStyle.contains("oil painting")) {
            return "Apply this exact scene in classical oil painting style: " +
                    "rich textures, visible brush strokes, deep colors, " +
                    "impasto technique, and luminous quality.";
        } else if (lowerStyle.contains("만화") || lowerStyle.contains("cartoon") || lowerStyle.contains("애니")) {
            return "Apply this exact scene in cartoon/anime style: " +
                    "bold outlines, vibrant colors, simplified but clear details, " +
                    "clean lines and bright aesthetic.";
        } else if (lowerStyle.contains("사실적") || lowerStyle.contains("realistic") || lowerStyle.contains("포토")) {
            return "Enhance this exact scene with photorealistic quality: " +
                    "improved lighting, better color grading, refined details, " +
                    "professional photography look.";
        } else if (lowerStyle.contains("빈티지") || lowerStyle.contains("vintage") || lowerStyle.contains("레트로")) {
            return "Apply this exact scene in vintage/retro style: " +
                    "warm nostalgic tones, slight film grain, aged photo look, " +
                    "classic photography aesthetic.";
        } else if (lowerStyle.contains("팝아트") || lowerStyle.contains("pop art")) {
            return "Apply this exact scene in pop art style: " +
                    "bold colors, high contrast, graphic quality, " +
                    "vibrant and bold aesthetic.";
        } else if (lowerStyle.contains("미니멀") || lowerStyle.contains("minimal")) {
            return "Apply this exact scene in minimalist style: " +
                    "clean lines, simple colors, reduced but essential details, " +
                    "minimalist aesthetic.";
        } else {
            return String.format("Apply this exact scene in %s style, " +
                    "maintaining all original elements and their positions.", style);
        }
    }

    /**
     * 식물 종류와 LED 설정값을 기반으로 AI에게 구체적인 조언을 구합니다.
     *
     * ✅ LED 밝기는 1-5 단계로만 표현 (0-255 변환 불필요)
     * ✅ AI도 1-5 단계로만 응답하도록 유도
     *
     * @param speciesName 식물 품종명 (예: "방울토마토")
     * @param aiPromptGuideline Species 테이블에 저장된 AI 프롬프트 가이드라인
     * @param userIntensity 사용자가 설정한 밝기 단계 (1-5)
     * @param startTimeStr 조명 시작 시간 ("HH:mm" 형식)
     * @param endTimeStr 조명 종료 시간 ("HH:mm" 형식)
     * @return AI가 생성한 '진단'과 '조언' 형식의 텍스트
     */
    public String getLedAdvice(String speciesName, String aiPromptGuideline,
                               int userIntensity, String startTimeStr, String endTimeStr) {
        // 파라미터 검증
        validateLedAdviceParameters(speciesName, userIntensity, startTimeStr, endTimeStr);

        LocalTime startTime = LocalTime.parse(startTimeStr);
        LocalTime endTime = LocalTime.parse(endTimeStr);
        Duration duration = Duration.between(startTime, endTime);
        if (duration.isNegative()) {
            duration = duration.plusDays(1);
        }
        long hours = duration.toHours();

        try {
            log.info("LED 조언 요청 - 품종: {}, 밝기: {}단계, 시간: {}~{} ({}시간)",
                    speciesName, userIntensity, startTimeStr, endTimeStr, hours);

            // ✅ System 메시지 - LED 단계 설명 포함
            String systemMessageText =
                    "당신은 '정밀 식물 재배 전문가'입니다. " +
                            "당신의 임무는 사용자의 설정을 '재배 가이드라인'과 비교하여, 부족한 점을 명확히 지적하고 개선 방안을 구체적인 숫자로 제시하는 것입니다.\n\n" +

                            "## 매우 중요한 규칙\n" +
                            "1. **밝기 표현**: 사용자에게 응답할 때, 0~255 범위의 '실제 밝기 값'이나 퍼센트(%)는 절대 언급하지 마세요. 모든 밝기 조언은 사용자가 이해하는 **1~5 '단계'**로만 표현해야 합니다.\n" +
                            "2. **부족한 점 지적**: 만약 설정이 최적 조건에 미치지 못한다면, '적절하다'는 표현 대신 부족한 점을 반드시 언급해야 합니다.\n" +
                            "3. **답변 형식**: 답변은 다음 형식에 맞춰 한국어로 2~3줄로 간결하게 요약해주세요:\n" +
                            "   **진단:** [현재 설정이 최적 조건과 비교했을 때 어떤지 명확한 평가. 부족한 점을 먼저 지적할 것.]\n" +
                            "   **조언:** [설정을 최적 조건에 맞추기 위한 구체적인 숫자(예: '2시간 더 늘리세요', '4단계로 조절하세요')를 제시.]";

            SystemMessage systemMessage = new SystemMessage(systemMessageText);

            // ✅ User 메시지 - 1-5 단계로만 표현
            String userMessageText = createLedAdvicePrompt(
                    speciesName, aiPromptGuideline, userIntensity, hours);

            UserMessage userMessage = new UserMessage(userMessageText);
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

            // AI 호출
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
    private void validateLedAdviceParameters(String speciesName, int intensity,
                                             String startTime, String endTime) {
        if (speciesName == null || speciesName.trim().isEmpty()) {
            throw new IllegalArgumentException("품종명은 비어있을 수 없습니다.");
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
        if (!startTime.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            throw new IllegalArgumentException("시작 시간 형식이 올바르지 않습니다. HH:mm 형식이어야 합니다.");
        }
        if (!endTime.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            throw new IllegalArgumentException("종료 시간 형식이 올바르지 않습니다. HH:mm 형식이어야 합니다.");
        }
    }

    /**
     * LED 조언을 위한 프롬프트 생성 - Species의 aiPromptGuideline 활용
     *
     * ✅ 1-5 단계로만 표현 (0-255 변환 불필요)
     */
    private String createLedAdvicePrompt(String speciesName, String aiPromptGuideline,
                                         int userIntensity, long hours) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("분석할 데이터는 다음과 같습니다:\n");
        prompt.append(String.format("- 식물 품종: %s\n", speciesName));

        // ✅ 1-5 단계로만 표현 (0-255 언급 안함)
        prompt.append(String.format("- LED 밝기 설정: %d단계 (1~5단계 중)\n", userIntensity));
        prompt.append(String.format("- 일일 조명 시간: 약 %d시간\n\n", hours));

        // Species 테이블의 aiPromptGuideline이 있으면 추가
        if (aiPromptGuideline != null && !aiPromptGuideline.trim().isEmpty()) {
            prompt.append("해당 품종에 대한 재배 가이드라인:\n");
            prompt.append(aiPromptGuideline);
            prompt.append("\n\n");
        }

        prompt.append("위 정보를 바탕으로:\n");
        prompt.append("1. 현재 LED 설정을 진단하고\n");
        prompt.append("2. 밝기 조정이 필요하다면 '몇 단계'로 조정할지 구체적으로 제안하며\n");
        prompt.append("3. 조명 시간 조정이 필요하다면 구체적인 시작/종료 시간을 제안하세요.\n\n");
        prompt.append("⚠️ 중요: 밝기는 반드시 '1단계', '2단계', ... '5단계' 형식으로만 표현하세요!");

        return prompt.toString();
    }
}