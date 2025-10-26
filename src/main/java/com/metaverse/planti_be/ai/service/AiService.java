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

    public String createImage(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("이미지 생성 프롬프트는 비어있을 수 없습니다.");
        }

        try {
            log.info("AI 이미지 생성 요청");

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

            log.info("AI 이미지 생성 성공");
            return imageUrl;

        } catch (Exception e) {
            log.error("AI 이미지 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("AI 이미지 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * Base64 이미지를 OpenAI Vision API로 직접 전송하여 분석
     * Spring AI의 추상화를 우회하고 직접 HTTP 요청 사용
     */
    public String analyzeImage(String base64ImageData) {
        if (base64ImageData == null || base64ImageData.trim().isEmpty()) {
            throw new IllegalArgumentException("이미지 데이터는 비어있을 수 없습니다.");
        }

        try {
            log.info("이미지 분석 요청 - Base64 데이터를 OpenAI API로 직접 전송");

            // Base64 Data URL 형식 확인 (data:image/jpeg;base64,... 형태여야 함)
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

            // OpenAI Vision API 직접 호출
            String openAiUrl = "https://api.openai.com/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            // 요청 바디 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o"); // 또는 "gpt-4-vision-preview"
            requestBody.put("max_tokens", 1000);

            // 메시지 구성
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);

            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");

            // 사용자 메시지 콘텐츠 (텍스트 + 이미지)
            List<Map<String, Object>> userContent = List.of(
                    Map.of("type", "text", "text", userPrompt),
                    Map.of("type", "image_url",
                            "image_url", Map.of("url", base64ImageData, "detail", "high"))
            );
            userMessage.put("content", userContent);

            requestBody.put("messages", List.of(systemMessage, userMessage));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // API 호출
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(openAiUrl, entity, Map.class);

            if (response == null) {
                throw new RuntimeException("OpenAI API 응답이 null입니다.");
            }

            // 응답에서 텍스트 추출
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

    public String getLedAdvice(String plantSpecies, int userIntensity, String startTimeStr, String endTimeStr) {
        validateLedAdviceParameters(plantSpecies, userIntensity, startTimeStr, endTimeStr);

        LocalTime startTime = LocalTime.parse(startTimeStr);
        LocalTime endTime = LocalTime.parse(endTimeStr);

        Duration duration = Duration.between(startTime, endTime);
        if (duration.isNegative()) {
            duration = duration.plusDays(1);
        }
        long hours = duration.toHours();

        try {
            String systemMessageText = "당신은 '정밀 식물 재배 전문가'입니다. " +
                    "사용자의 설정을 최적 조건과 비교하여 개선 방안을 제시합니다. " +
                    "밝기는 1~5 단계로만 표현하세요.";
            SystemMessage systemMessage = new SystemMessage(systemMessageText);

            String userMessageText = String.format(
                    "분석할 데이터:\n- 식물: %s\n- LED: %d단계\n- 조명 시간: %d시간",
                    plantSpecies, userIntensity, hours
            );
            UserMessage userMessage = new UserMessage(userMessageText);

            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
            String advice = chatModel.call(prompt).getResult().getOutput().getContent();

            if (advice == null || advice.trim().isEmpty()) {
                throw new RuntimeException("AI가 조언을 생성하지 못했습니다.");
            }

            return advice;

        } catch (Exception e) {
            log.error("LED 조언 생성 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("AI 조언 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

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
    }
}