package com.metaverse.planti_be.photo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.device.repository.DeviceRepository;
import com.metaverse.planti_be.photo.domain.Photo;
import com.metaverse.planti_be.photo.dto.PhotoRequestDto;
import com.metaverse.planti_be.photo.dto.PhotoResponseDto;
import com.metaverse.planti_be.photo.repository.PhotoRepository;
import com.metaverse.planti_be.plant.domain.Plant;
import com.metaverse.planti_be.plant.domain.PlantStage;
import com.metaverse.planti_be.plant.repository.PlantRepository;
import com.metaverse.planti_be.notice.service.notification.LeafNotificationService;
import com.metaverse.planti_be.notice.service.notification.FruitNotificationService;
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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final DeviceRepository deviceRepository;
    private final PlantRepository plantRepository;

    // ✅ 식물별 알림 서비스 주입
    private final LeafNotificationService leafNotificationService;
    private final FruitNotificationService fruitNotificationService;

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

            // 🌱 Device의 Plant 정보를 기반으로 crop_type 자동 결정
            String cropType = determineCropType(device);
            System.out.println("ℹ️ 자동 결정된 작물 타입: " + cropType + " (Device: " + serialNumber + ")");

            body.add("crop_type", cropType);

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

                // AI 분석 결과 기반 알림 생성 및 식물 상태 업데이트
                if (device.getUser() != null) {
                    System.out.println("📢 알림 생성 및 식물 상태 업데이트 프로세스 시작!");

                    try {
                        // "lettuce"는 잎식물(Leaf)로 간주
                        if ("lettuce".equalsIgnoreCase(cropType)) {
                            // 🌱 잎식물 로직 (상추 등)
                            // 1. 식물 상태 업데이트 (내부에서 새싹 알림 자동 호출)
                            updatePlantStageFromAnalysis(device, detailedResponse);

                            // 2. 잎식물 전용 알림 (질병, 수확 시기)
                            leafNotificationService.checkAndCreateNotifications(device, detailedResponse);

                        } else {
                            // 🍅 열매식물 로직 (토마토 등)
                            // 1. 식물 상태 업데이트 (내부에서 새싹/열매 알림 자동 호출)
                            updateFruitPlantStage(device, detailedResponse);

                            // 2. 열매식물 전용 알림 (수확 시기)
                            fruitNotificationService.checkAndCreateNotifications(device, detailedResponse);
                        }
                    } catch (Exception e) {
                        // 알림 생성 실패는 로그만 기록하고 전체 프로세스는 계속 진행
                        System.err.println("⚠️ 알림 생성 중 오류 발생 (사진 저장 및 AI 분석은 정상 완료): " + e.getMessage());
                        e.printStackTrace();
                    }

                } else {
                    System.out.println("⚠️ 디바이스에 사용자가 연결되어 있지 않습니다!");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ AI 상세 분석 서버 호출 실패: " + e.getMessage());
            e.printStackTrace();
            // 오류 발생 시 기본값으로 설정
            savedPhoto.updateDetailedAnalysis("analysis_failed", 0.0, 0, "{}");
        }

        return new PhotoResponseDto(savedPhoto);
    }

    /**
     * 🌱 잎식물(상추) AI 분석 결과를 기반으로 식물 상태를 자동 업데이트합니다.
     *
     * 분석 단계 매핑:
     * - analysis_stage: "disease" → PlantStage 변경 없음 (질병 감지)
     * - analysis_stage: "growth" → bestResult에 따라 GERMINATION 또는 MATURE로 업데이트
     */
    private void updatePlantStageFromAnalysis(Device device, Map<String, Object> detailedResponse) {
        try {
            // 1. 해당 디바이스에 연결된 식물 찾기
            Optional<Plant> plantOpt = plantRepository.findByDeviceId(device.getId());

            if (plantOpt.isEmpty()) {
                System.out.println("   ℹ️ 디바이스에 등록된 식물이 없습니다. 상태 업데이트 생략.");
                return;
            }

            Plant plant = plantOpt.get();
            PlantStage currentStage = plant.getPlantStage();

            // 2. 분석 단계(analysis_stage) 확인
            String analysisStage = (String) detailedResponse.getOrDefault("analysis_stage", "");
            String bestResult = (String) detailedResponse.getOrDefault("bestResult", "no_detection");

            System.out.println("🌱 식물 상태 업데이트 체크:");
            System.out.println("   - 현재 식물 상태: " + currentStage);
            System.out.println("   - AI 분석 단계: " + analysisStage);
            System.out.println("   - AI 최고 검출: " + bestResult);

            PlantStage newStage = null;
            boolean shouldRecordGermination = false;

            // 3. analysis_stage에 따른 상태 변경 로직
            switch (analysisStage) {
                case "disease":
                    // 질병 감지 시 - 상태 변경 없음
                    System.out.println("   ⚠️ 질병이 감지되었습니다. 식물 상태 유지: " + currentStage);
                    break;

                case "growth":
                    // 성장 단계 감지 - bestResult를 기반으로 판단
                    if (bestResult != null && !bestResult.equals("no_detection")) {
                        // "level 1", "level 2", "level 3" 등으로 오는 경우 (초기 단계)
                        if (bestResult.toLowerCase().contains("level")) {
                            // SEED인 경우 GERMINATION으로
                            if (currentStage == PlantStage.SEED) {
                                newStage = PlantStage.GERMINATION;
                                shouldRecordGermination = true;
                            }
                        }
                        // "GERMINATION"로 명시적으로 오는 경우
                        else if ("GERMINATION".equalsIgnoreCase(bestResult)) {
                            if (currentStage == PlantStage.SEED) {
                                newStage = PlantStage.GERMINATION;
                                shouldRecordGermination = true;
                            }
                        }
                        // "MATURE" (성숙/수확 준비)로 오는 경우
                        else if ("MATURE".equalsIgnoreCase(bestResult)) {
                            if (currentStage != PlantStage.MATURE) {
                                newStage = PlantStage.MATURE;
                            }
                        }
                    }
                    break;

                default:
                    System.out.println("   ℹ️ 알 수 없는 분석 단계: " + analysisStage);
                    break;
            }

            // 4. 발아 시점 기록
            if (shouldRecordGermination && plant.getGerminatedAt() == null) {
                plant.setGerminatedAt(LocalDateTime.now());
                System.out.println("   🌱 발아 시점 기록: " + plant.getGerminatedAt());
            }

            // 5. 상태 변경 적용
            if (newStage != null && newStage != currentStage) {
                plant.setPlantStage(newStage);
                plantRepository.save(plant);
                System.out.println("   ✅ 식물 상태 업데이트 완료: " + currentStage + " → " + newStage);

                // ❗️ 새싹(발아) 상태로 변경 시, 알림 생성
                if (newStage == PlantStage.GERMINATION) {
                    leafNotificationService.createSproutFirstAppearedNotice(
                            device.getUser(),
                            device
                    );
                }
            } else {
                System.out.println("   ℹ️ 식물 상태 변경 없음 (조건 미충족 또는 이미 적절한 상태)");
            }

        } catch (Exception e) {
            System.err.println("   ❌ 식물 상태 업데이트 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🍅 열매식물(토마토) AI 분석 결과를 기반으로 식물 상태를 자동 업데이트합니다.
     */
    private void updateFruitPlantStage(Device device, Map<String, Object> detailedResponse) {
        try {
            Optional<Plant> plantOpt = plantRepository.findByDeviceId(device.getId());

            if (plantOpt.isEmpty()) {
                System.out.println("   ℹ️ 디바이스에 등록된 식물이 없습니다. 상태 업데이트 생략.");
                return;
            }

            Plant plant = plantOpt.get();
            PlantStage currentStage = plant.getPlantStage();

            @SuppressWarnings("unchecked")
            Map<String, Integer> classSummary = (Map<String, Integer>)
                    detailedResponse.getOrDefault("classSummary", Map.of());

            System.out.println("🍅 식물 상태 업데이트 체크:");
            System.out.println("   - 현재 식물 상태: " + currentStage);

            // 1. 새싹 발견 시 GERMINATION으로 변경
            int sproutCount = classSummary.getOrDefault("sprout", 0);
            if (sproutCount > 0 && currentStage == PlantStage.SEED) {
                plant.setPlantStage(PlantStage.GERMINATION);
                if (plant.getGerminatedAt() == null) {
                    plant.setGerminatedAt(LocalDateTime.now());
                }
                plantRepository.save(plant);
                System.out.println("   ✅ 식물 상태 업데이트 완료: SEED → GERMINATION");

                // 새싹 알림 생성
                fruitNotificationService.createSproutFirstAppearedNotice(
                        device.getUser(),
                        device
                );
            }

            // 2. 열매 발견 시 FRUIT로 변경
            int fruitCount = 0;
            for (int i = 1; i <= 6; i++) {
                fruitCount += classSummary.getOrDefault("level " + i, 0);
            }

            if (fruitCount > 0 && currentStage != PlantStage.FRUIT) {
                plant.setPlantStage(PlantStage.FRUIT);
                plantRepository.save(plant);
                System.out.println("   ✅ 식물 상태 업데이트 완료: " + currentStage + " → FRUIT");

                // 열매 첫 발견 알림 생성
                fruitNotificationService.createFruitFirstAppearedNotice(
                        device.getUser(),
                        device,
                        fruitCount
                );
            }

        } catch (Exception e) {
            System.err.println("   ❌ 식물 상태 업데이트 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🔍 Device에 연결된 Plant의 Species 정보를 기반으로 crop_type을 자동 결정합니다.
     *
     * @param device 분석할 디바이스
     * @return "tomato" 또는 "lettuce" (기본값: "tomato")
     */
    private String determineCropType(Device device) {
        try {
            // 1. Device에 연결된 Plant 찾기
            Optional<Plant> plantOpt = plantRepository.findByDeviceId(device.getId());

            if (plantOpt.isEmpty()) {
                System.out.println("   ⚠️ 디바이스에 등록된 식물이 없습니다. 기본값 'tomato' 사용");
                return "tomato";
            }

            Plant plant = plantOpt.get();
            String speciesName = plant.getSpecies().getName();

            System.out.println("   📋 등록된 식물 품종: " + speciesName);

            // 2. Species 이름을 기반으로 crop_type 결정
            if (speciesName.toLowerCase().contains("상추") ||
                    speciesName.toLowerCase().contains("lettuce")) {
                return "lettuce";
            } else if (speciesName.toLowerCase().contains("토마토") ||
                    speciesName.toLowerCase().contains("tomato")) {
                return "tomato";
            }

            // 3. 기본값: tomato
            System.out.println("   ℹ️ 품종명에서 작물 타입을 특정할 수 없습니다. 기본값 'tomato' 사용");
            return "tomato";

        } catch (Exception e) {
            System.err.println("   ❌ crop_type 결정 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return "tomato";
        }
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
            System.err.println("❌ AI 상세 분석 서버 호출 실패: " + e.getMessage());
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
            System.err.println("❌ JSON 변환 오류: " + e.getMessage());
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