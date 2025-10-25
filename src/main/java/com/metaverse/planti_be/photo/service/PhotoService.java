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
import com.metaverse.planti_be.notice.service.NoticeService;

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
    private final NoticeService noticeService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${file.upload-dir.camera}")
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
                // 응답에서 필요한 정보 추출
                String bestResult = (String) detailedResponse.getOrDefault("bestResult", "no_detection");
                Integer totalDetected = Integer.valueOf(detailedResponse.getOrDefault("totalDetected", 0).toString());

                // 평균 신뢰도 사용
                Double avgConfidence = Double.valueOf(detailedResponse.getOrDefault("avgConfidence", 0.0).toString());

                // 상세 결과를 JSON으로 변환하여 저장 (x,y 좌표 제외된 상태)
                String detailedResultsJson = convertToJson(detailedResponse);

                // DB 업데이트 - 평균 신뢰도 저장
                savedPhoto.updateDetailedAnalysis(
                        bestResult,
                        avgConfidence,
                        totalDetected,
                        detailedResultsJson
                );

                System.out.println("🎯 AI 분석 결과:");
                System.out.println("   - 최고 검출: " + bestResult);
                System.out.println("   - 평균 신뢰도: " + avgConfidence);
                System.out.println("   - 총 검출 수: " + totalDetected);
                System.out.println("   - 전체 응답: " + detailedResponse);

                // 디바이스 사용자 확인
                System.out.println("🔍 디바이스 정보:");
                System.out.println("   - Device ID: " + device.getId());
                System.out.println("   - Device Nickname: " + device.getDeviceNickname());
                System.out.println("   - User: " + (device.getUser() != null ? device.getUser().getUsername() : "NULL"));

                // AI 분석 결과 기반 알림 생성
                if (device.getUser() != null) {
                    System.out.println("알림 생성 프로세스 시작!");
                    checkAndCreateNotifications(device, detailedResponse);
                } else {
                    System.out.println("디바이스에 사용자가 연결되어 있지 않습니다!");
                }
            }

        } catch (Exception e) {
            System.err.println("AI 상세 분석 서버 호출 실패: " + e.getMessage());
            e.printStackTrace();
            // 오류 발생 시 기본값으로 설정
            savedPhoto.updateDetailedAnalysis("analysis_failed", 0.0, 0, "{}");
        }

        return new PhotoResponseDto(savedPhoto);
    }

    private void checkAndCreateNotifications(Device device, Map<String, Object> detailedResponse) {

        System.out.println("알림 체크 시작");

        @SuppressWarnings("unchecked")
        Map<String, Integer> classSummary = (Map<String, Integer>)
                detailedResponse.getOrDefault("classSummary", Map.of());

        System.out.println("   - classSummary: " + classSummary);

        // 1. 새싹 발견 시, 알림 생성을 '시도'
        int sproutCount = classSummary.getOrDefault("sprout", 0);
        System.out.println("   - sproutCount: " + sproutCount);

        if (sproutCount > 0) {
            // NoticeService가 내부적으로 중복을 확인하므로, 우리는 그냥 호출만 하면 됩니다.
            noticeService.createSproutFirstAppearedNotice(
                    device.getUser(),
                    device
            );
        }

        // 2. 열매 개수 계산
        int fruitCount = 0;
        for (int i = 1; i <= 6; i++) {
            fruitCount += classSummary.getOrDefault("level " + i, 0);
        }
        System.out.println("   - fruitCount: " + fruitCount);


        // 3. 열매 발견 시, 알림 생성을 '시도' (1개 이상)
        if (fruitCount > 0) {
            // 여기도 마찬가지로 NoticeService가 중복을 확인합니다.
            noticeService.createFruitFirstAppearedNotice(
                    device.getUser(),
                    device,
                    fruitCount
            );
        }

        // 4. 수확 시기 체크 (기존 로직 유지)
        if (fruitCount >= 5) {
            int level5Count = classSummary.getOrDefault("level 5", 0);
            int level6Count = classSummary.getOrDefault("level 6", 0);
            int matureFruitCount = level5Count + level6Count;

            double matureRatio = (fruitCount > 0) ? (double) matureFruitCount / fruitCount : 0;

            System.out.println("   - 성숙한 열매 (level 5+6): " + matureFruitCount);
            System.out.println("   - 성숙 비율: " + matureRatio);

            if (matureRatio >= 0.7) {
                // 수확 시기 알림은 '읽지 않은' 알림이 있는지 체크하므로, 기존 로직도 좋습니다.
                noticeService.createHarvestReadyNotice(
                        device.getUser(),
                        device,
                        fruitCount,
                        matureFruitCount
                );
            }
        }
        System.out.println("알림 체크 완료\n");
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

            // 임시 파일 경로 생성
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
                String bestResult = (String) detailedResponse.getOrDefault("bestResult", "no_detection");
                Double avgConfidence = Double.valueOf(detailedResponse.getOrDefault("avgConfidence", 0.0).toString());
                Integer totalDetected = Integer.valueOf(detailedResponse.getOrDefault("totalDetected", 0).toString());

                // 더미 디바이스 생성 (DB 저장 안함)
                Device dummyDevice = new Device();
                Photo tempPhoto = new Photo(dummyDevice, "", "temp_analysis");
                tempPhoto.updateDetailedAnalysis(
                        bestResult,
                        avgConfidence,
                        totalDetected,
                        convertToJson(detailedResponse)
                );

                System.out.println("🔍 임시 분석 결과:");
                System.out.println("   - 최고 검출: " + bestResult);
                System.out.println("   - 평균 신뢰도: " + avgConfidence);
                System.out.println("   - 총 검출 수: " + totalDetected);

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

    // JSON 변환 헬퍼 메소드 - x,y 좌표는 이미 제외됨
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