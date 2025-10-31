package com.metaverse.planti_be.notice.service;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.notice.domain.Notice;
import com.metaverse.planti_be.notice.domain.NoticeType;
import com.metaverse.planti_be.notice.dto.NoticeResponseDto;
import com.metaverse.planti_be.notice.repository.NoticeRepository;
import com.metaverse.planti_be.plant.domain.PlantStage;
import com.metaverse.planti_be.plant.repository.PlantRepository;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;

    // JSON 처리를 위한 ObjectMapper와 임계값 상수
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final double SIGNIFICANT_CHANGE_THRESHOLD = 0.2; // 20%

    private final PlantRepository plantRepository;

    // 수확 알림의 추가 데이터(additionalData)를 관리하기 위한 내부 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class HarvestData {
        private Integer totalFruits;
        private Integer matureFruits;
    }

    //사용자별 모든 알림 조회 (최신순)
    @Transactional(readOnly = true)
    public List<NoticeResponseDto> getUserNotices(User user) {
        return noticeRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(NoticeResponseDto::new)
                .toList();
    }

    //사용자별 읽지 않은 알림만 조회 (최신순)
    @Transactional(readOnly = true)
    public List<NoticeResponseDto> getUnreadNotices(User user) {
        return noticeRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user).stream()
                .map(NoticeResponseDto::new)
                .toList();
    }

    //사용자의 읽지 않은 알림 개수 조회
    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return noticeRepository.countByUserAndIsReadFalse(user);
    }

    //특정 알림을 읽음 처리
    @Transactional
    public void markAsRead(Long noticeId, User user) {
        Notice notice = findNotice(noticeId);

        // 해당 사용자의 알림인지 확인
        if (!notice.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 알림에 대한 권한이 없습니다.");
        }

        notice.setIsRead(true);
    }

    //사용자의 모든 알림을 읽음 처리
    @Transactional
    public void markAllAsRead(User user) {
        List<Notice> unreadNotices = noticeRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        unreadNotices.forEach(notice -> notice.setIsRead(true));
    }

    //사용자의 특정 알림 삭제
    @Transactional
    public Long deleteNotice(Long noticeId, User user) {
        Notice notice = findNotice(noticeId);

        // 해당 사용자의 알림인지 확인
        if (!notice.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 알림에 대한 권한이 없습니다.");
        }

        noticeRepository.delete(notice);
        return noticeId;
    }

    //알림 조회 헬퍼 메서드
    private Notice findNotice(Long noticeId) {
        return noticeRepository.findById(noticeId).orElseThrow(() ->
                new IllegalArgumentException("해당 알림은 존재하지 않습니다.")
        );
    }

    // 물통 수위 부족 알림 생성
    @Transactional
    public void createWaterShortageNotice(User user, Device device, Double waterLevel) {
        // 중복 방지: 이미 읽지 않은 동일 알림이 있으면 생성 안 함
        if (noticeRepository.existsByUserAndDeviceAndNoticeTypeAndIsReadFalse(
                user, device, NoticeType.WATER_SHORTAGE)) {
            return;
        }

        Notice notice = new Notice(
                String.format("%s의 물통 수위가 낮습니다. 물을 채워주세요!",
                        device.getDeviceNickname()),
                NoticeType.WATER_SHORTAGE,
                user,
                device,
                1  // 높은 우선순위
        );

        noticeRepository.save(notice);
        System.out.println("물통 수위 부족 알림 생성 완료");
    }

    // 새싹 첫 발견 알림 생성
    @Transactional
    public void createSproutFirstAppearedNotice(User user, Device device) {
        // 새싹 첫 발견은 한 번만 알림 (영구 중복 방지)
        if (noticeRepository.existsByUserAndDeviceAndNoticeType(
                user, device, NoticeType.GROWTH_STAGE_CHANGED)) {
            System.out.println("이미 새싹 발견 알림이 생성되었습니다.");
            return;
        }

        //Plant 상태 업데이트
        plantRepository.findByDeviceId(device.getId())
                .ifPresent(plant -> {
                    if (plant.getPlantStage() == PlantStage.SEED) {
                        plant.setPlantStage(PlantStage.GERMINATION);
                        plant.setGerminatedAt(LocalDateTime.now());
                        System.out.println("식물 상태가 SEED → GERMINATION으로 변경되었습니다.");
                    }
                });

        Notice notice = new Notice(
                String.format("%s에서 새싹이 처음 발견되었습니다! 식물이 자라기 시작했어요!",
                        device.getDeviceNickname()),
                NoticeType.GROWTH_STAGE_CHANGED,
                user,
                device,
                2  // 보통 우선순위
        );

        noticeRepository.save(notice);
        System.out.println("새싹 첫 발견 알림 생성 완료");
    }

    // 열매 첫 발견 알림 생성
    @Transactional
    public void createFruitFirstAppearedNotice(User user, Device device, Integer fruitCount) {
        // 열매 첫 발견은 한 번만 알림 (영구 중복 방지)
        if (noticeRepository.existsByUserAndDeviceAndNoticeType(
                user, device, NoticeType.FRUIT_FIRST_APPEARED)) {
            System.out.println("이미 열매 첫 발견 알림이 생성되었습니다.");
            return;
        }

        plantRepository.findByDeviceId(device.getId())
                .ifPresent(plant -> {
                    if (plant.getPlantStage() != PlantStage.FRUIT) {
                        plant.setPlantStage(PlantStage.FRUIT);
                        System.out.println("식물 상태가 FRUIT로 변경되었습니다.");
                    }
                });

        Notice notice = new Notice(
                String.format("%s에서 열매가 처음 발견되었습니다! (개수: %d개) 축하드립니다!",
                        device.getDeviceNickname(), fruitCount),
                NoticeType.FRUIT_FIRST_APPEARED,
                user,
                device,
                2  // 보통 우선순위
        );

        noticeRepository.save(notice);
        System.out.println("열매 첫 발견 알림 생성 완료");
    }

    // 수확 시기 알림 생성(의미 있는 변화 감지)
    @Transactional
    public void createHarvestReadyNotice(User user, Device device, Integer totalFruits, Integer matureFruits) {
        // 중복 방지 (읽지 않은 수확 알림이 있으면 생성 안 함)
        if (noticeRepository.existsByUserAndDeviceAndNoticeTypeAndIsReadFalse(
                user, device, NoticeType.HARVEST_READY)) {
            System.out.println("이미 읽지 않은 수확 시기 알림이 있어 생성하지 않습니다.");
            return;
        }

        // 마지막 수확 알림 조회
        Optional<Notice> lastHarvestNotice = noticeRepository
                .findTopByUserAndDeviceAndNoticeTypeOrderByCreatedAtDesc(
                        user, device, NoticeType.HARVEST_READY);

        // ✅ 3. 이전 알림이 있다면 의미 있는 변화 확인
        if (lastHarvestNotice.isPresent()) {
            boolean hasSignificantChange = checkSignificantChange(
                    lastHarvestNotice.get(),
                    totalFruits,
                    matureFruits
            );

            if (!hasSignificantChange) {
                System.out.println("⚠️ 의미 있는 변화가 없어 수확 알림을 생성하지 않습니다.");
                System.out.println("   (현재: 전체 " + totalFruits + "개, 성숙 " + matureFruits + "개)");
                return;
            }
        }

        Notice notice = new Notice(
                String.format("%s의 열매가 수확 시기에 도달했습니다! (전체: %d개, 성숙: %d개)",
                        device.getDeviceNickname(), totalFruits, matureFruits),
                NoticeType.HARVEST_READY,
                user,
                device,
                2  // 보통 우선순위
        );

        // 4. additionalData에 현재 상태를 JSON으로 저장
        try {
            HarvestData data = new HarvestData(totalFruits, matureFruits);
            notice.setAdditionalData(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            System.err.println("HarvestData JSON 변환 오류: " + e.getMessage());
            notice.setAdditionalData("{}"); // 오류 발생 시 빈 객체 저장
        }

        noticeRepository.save(notice);
        System.out.println("수확 시기 알림 생성 완료");
    }

    // 이전 알림과 현재 상태를 비교하여 의미 있는 변화가 있는지 확인하는 헬퍼 메서드
    private boolean checkSignificantChange(Notice lastNotice, int currentTotal, int currentMature) {
        try {
            String additionalData = lastNotice.getAdditionalData();
            if (additionalData == null || additionalData.isEmpty()) {
                return true; // 이전 데이터가 없으면 변화가 있는 것으로 간주
            }

            HarvestData previousData = objectMapper.readValue(additionalData, HarvestData.class);
            int previousTotal = previousData.getTotalFruits();
            int previousMature = previousData.getMatureFruits();

            System.out.println("🔍 수확 알림 변화 감지:");
            System.out.printf("   - 이전 상태: 전체 %d개, 성숙 %d개\n", previousTotal, previousMature);
            System.out.printf("   - 현재 상태: 전체 %d개, 성숙 %d개\n", currentTotal, currentMature);

            // 전체 열매 개수 변화율 계산 (0으로 나누기 방지)
            double totalChangeRate = (previousTotal == 0) ? 1.0 :
                    Math.abs((double) (currentTotal - previousTotal) / previousTotal);

            // 성숙한 열매 개수 변화율 계산 (0으로 나누기 방지)
            double matureChangeRate = (previousMature == 0 && currentMature > 0) ? 1.0 :
                    (previousMature == 0) ? 0.0 : Math.abs((double) (currentMature - previousMature) / previousMature);

            System.out.printf("   - 변화율: 전체 %.1f%%, 성숙 %.1f%%\n", totalChangeRate * 100, matureChangeRate * 100);

            // 둘 중 하나라도 임계값을 넘으면 '의미 있는 변화'로 판단
            return totalChangeRate >= SIGNIFICANT_CHANGE_THRESHOLD || matureChangeRate >= SIGNIFICANT_CHANGE_THRESHOLD;

        } catch (Exception e) {
            System.err.println("이전 알림 데이터 파싱 중 오류 발생: " + e.getMessage());
            return true; // 데이터 파싱에 실패하면 안전하게 변화가 있는 것으로 간주
        }
    }

}