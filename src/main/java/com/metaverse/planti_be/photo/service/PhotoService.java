package com.metaverse.planti_be.photo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.device.repository.DeviceRepository;
import com.metaverse.planti_be.photo.domain.Photo;
import com.metaverse.planti_be.photo.dto.PhotoRequestDto;
import com.metaverse.planti_be.photo.dto.PhotoResponseDto;
import com.metaverse.planti_be.photo.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final DeviceRepository deviceRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${ai.server.detailed.url}")
    private String aiDetailedServerUrl;

    @Transactional
    public PhotoResponseDto savePhoto(PhotoRequestDto requestDto) throws IOException {
        MultipartFile imageFile = requestDto.getImageFile();
        String serialNumber = requestDto.getSerialNumber();

        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 필요합니다.");
        }

        Device device = deviceRepository.findById(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 기기입니다: " + serialNumber));

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String extension = getFileExtension(imageFile.getOriginalFilename());
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + "_" + UUID.randomUUID().toString() + "." + extension;

        String filePath = Paths.get(uploadDir, fileName).toString();
        imageFile.transferTo(new File(filePath));

        Photo photo = new Photo(device, filePath, fileName);
        Photo savedPhoto = photoRepository.save(photo);

        // AI 상세 분석 요청 (모든 객체 검출)
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(new File(filePath)));

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            // Python API 서버에 상세 분석 요청 - Map으로 받음
            @SuppressWarnings("unchecked")
            Map<String, Object> detailedResponse = restTemplate.postForObject(
                    aiDetailedServerUrl, entity, Map.class);

            if (detailedResponse != null) {
                // 가장 신뢰도 높은 객체 찾기
                String bestResult = "no_detection";
                Double bestConfidence = 0.0;

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> detections = (List<Map<String, Object>>) detailedResponse.get("detections");

                if (detections != null && !detections.isEmpty()) {
                    for (Map<String, Object> detection : detections) {
                        Object confidenceObj = detection.get("confidence");
                        if (confidenceObj != null) {
                            Double confidence = Double.valueOf(confidenceObj.toString());
                            if (confidence > bestConfidence) {
                                bestConfidence = confidence;
                                bestResult = (String) detection.get("className");
                            }
                        }
                    }
                }

                // 상세 결과를 JSON으로 변환하여 저장
                String detailedResultsJson = convertToJson(detailedResponse);

                // 총 검출 개수 추출
                Integer totalDetected = 0;
                Object totalObj = detailedResponse.get("totalDetected");
                if (totalObj != null) {
                    totalDetected = Integer.valueOf(totalObj.toString());
                }

                // DB 업데이트
                savedPhoto.updateDetailedAnalysis(
                        bestResult,
                        bestConfidence,
                        totalDetected,
                        detailedResultsJson
                );
            }

        } catch (Exception e) {
            System.err.println("AI 상세 분석 서버 호출 실패: " + e.getMessage());
            e.printStackTrace();
            // 오류 발생 시 기본값으로 설정
            savedPhoto.updateDetailedAnalysis("analysis_failed", 0.0, 0, "{}");
        }

        return new PhotoResponseDto(savedPhoto);
    }

    @Transactional
    public PhotoResponseDto analyzePhotoDetailed(PhotoRequestDto requestDto) throws IOException {
        MultipartFile imageFile = requestDto.getImageFile();

        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 필요합니다.");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 임시 파일로 저장
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String extension = getFileExtension(imageFile.getOriginalFilename());
            String tempFileName = "temp_" + System.currentTimeMillis() + "." + extension;
            String tempFilePath = Paths.get(uploadDir, tempFileName).toString();
            imageFile.transferTo(new File(tempFilePath));

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(new File(tempFilePath)));

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            // Python API 서버에 상세 분석 요청 - Map으로 받음
            @SuppressWarnings("unchecked")
            Map<String, Object> detailedResponse = restTemplate.postForObject(
                    aiDetailedServerUrl, entity, Map.class);

            // 임시 파일 삭제
            new File(tempFilePath).delete();

            // PhotoResponseDto로 변환하여 반환 (DB 저장 없이)
            if (detailedResponse != null) {
                // 가장 신뢰도 높은 객체 찾기
                String bestResult = "no_detection";
                Double bestConfidence = 0.0;

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> detections = (List<Map<String, Object>>) detailedResponse.get("detections");

                if (detections != null && !detections.isEmpty()) {
                    for (Map<String, Object> detection : detections) {
                        Object confidenceObj = detection.get("confidence");
                        if (confidenceObj != null) {
                            Double confidence = Double.valueOf(confidenceObj.toString());
                            if (confidence > bestConfidence) {
                                bestConfidence = confidence;
                                bestResult = (String) detection.get("className");
                            }
                        }
                    }
                }

                // 총 검출 개수 추출
                Integer totalDetected = 0;
                Object totalObj = detailedResponse.get("totalDetected");
                if (totalObj != null) {
                    totalDetected = Integer.valueOf(totalObj.toString());
                }

                // 더미 디바이스 생성 (DB 저장 안함)
                Device dummyDevice = new Device();
                Photo tempPhoto = new Photo(dummyDevice, "", "temp_analysis");
                tempPhoto.updateDetailedAnalysis(
                        bestResult,
                        bestConfidence,
                        totalDetected,
                        convertToJson(detailedResponse)
                );

                return new PhotoResponseDto(tempPhoto);
            }

            throw new RuntimeException("AI 서버로부터 응답을 받지 못했습니다.");

        } catch (Exception e) {
            System.err.println("AI 상세 분석 서버 호출 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("상세 분석 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public PhotoResponseDto findLatestPhoto() {
        return photoRepository.findTopByOrderByIdDesc()
                .map(PhotoResponseDto::new)
                .orElseThrow(() -> new IllegalArgumentException("저장된 사진이 없습니다."));
    }

    // JSON 변환 헬퍼 메소드
    private String convertToJson(Map<String, Object> response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(Map.of(
                    "classSummary", response.get("classSummary") != null ? response.get("classSummary") : Map.of(),
                    "detections", response.get("detections") != null ? response.get("detections") : List.of()
            ));
        } catch (Exception e) {
            System.err.println("JSON 변환 오류: " + e.getMessage());
            return "{}";
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        try {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }
}