package com.metaverse.planti_be.notice.service.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.notice.domain.Notice;
import com.metaverse.planti_be.notice.domain.NoticeType;
import com.metaverse.planti_be.notice.repository.NoticeRepository;
import com.metaverse.planti_be.notice.service.NoticeService;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * 🍅 열매식물(토마토 등) 전용 알림 서비스
 * - 새싹 첫 발견 알림
 * - 열매 첫 발견 알림
 * - 수확 시기 알림 (의미 있는 변화 감지)
 */
@Service
@RequiredArgsConstructor
public class FruitNotificationService {

    private final NoticeService noticeService;
    private final NoticeRepository noticeRepository;

    // JSON 처리를 위한 ObjectMapper와 임계값 상수
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final double SIGNIFICANT_CHANGE_THRESHOLD = 0.2; // 20%

    // 수확 알림의 추가 데이터(additionalData)를 관리하기 위한 내부 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class HarvestData {
        private Integer totalFruits;
        private Integer matureFruits;
    }

    /**
     * 🍅 열매식물(토마토 등) AI 분석 결과를 기반으로 알림을 생성합니다.
     * - (새싹, 열매 개수, 수확 시기 등)
     *
     * @param device           알림을 받을 디바이스
     * @param detailedResponse AI 서버의 상세 분석 결과
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAndCreateNotifications(Device device, Map<String, Object> detailedResponse) {

        System.out.println("📢 열매식물 알림 체크 시작");

        @SuppressWarnings("unchecked")
        Map<String, Integer> classSummary = (Map<String, Integer>)
                detailedResponse.getOrDefault("classSummary", Map.of());

        System.out.println("   - classSummary: " + classSummary);

        // 1. 새싹 발견 시, 알림 생성을 '시도'
        int sproutCount = classSummary.getOrDefault("sprout", 0);
        System.out.println("   - sproutCount: " + sproutCount);

        if (sproutCount > 0) {
            // NoticeService가 내부적으로 중복을 확인하므로, 우리는 그냥 호출만 하면 됩니다.
            createSproutFirstAppearedNotice(
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
            createFruitFirstAppearedNotice(
                    device.getUser(),
                    device,
                    fruitCount
            );
        }

        // 4. 수확 시기 체크
        if (fruitCount >= 5) {
            int level5Count = classSummary.getOrDefault("level 5", 0);
            int level6Count = classSummary.getOrDefault("level 6", 0);
            int matureFruitCount = level5Count + level6Count;

            double matureRatio = (fruitCount > 0) ? (double) matureFruitCount / fruitCount : 0;

            System.out.println("   - 성숙한 열매 (level 5+6): " + matureFruitCount);
            System.out.println("   - 성숙 비율: " + matureRatio);

            if (matureRatio >= 0.7) {
                // 수확 시기 알림은 '읽지 않은' 알림이 있는지 체크하므로, 기존 로직도 좋습니다.
                createHarvestReadyNotice(
                        device.getUser(),
                        device,
                        fruitCount,
                        matureFruitCount
                );
            }
        }
        System.out.println("📢 열매식물 알림 체크 완료\n");
    }

    // 새싹 첫 발견 알림 생성
    @Transactional
    public void createSproutFirstAppearedNotice(User user, Device device) {
        // 새싹 첫 발견은 한 번만 알림 (영구 중복 방지)
        if (noticeService.hasNotice(user, device, NoticeType.GROWTH_CHANGE)) {
            System.out.println("이미 새싹 발견 알림이 생성되었습니다.");
            return;
        }

        Notice notice = new Notice(
                String.format("%s에서 새싹이 처음 발견되었습니다! 식물이 자라기 시작했어요!",
                        device.getDeviceNickname()),
                NoticeType.GROWTH_CHANGE,
                user,
                device,
                2  // 보통 우선순위
        );

        noticeService.saveNotice(notice);
        System.out.println("새싹 첫 발견 알림 생성 완료");
    }

    // 열매 첫 발견 알림 생성
    @Transactional
    public void createFruitFirstAppearedNotice(User user, Device device, Integer fruitCount) {
        // 열매 첫 발견은 한 번만 알림 (영구 중복 방지)
        if (noticeService.hasNotice(user, device, NoticeType.FRUIT_APPEARED)) {
            System.out.println("이미 열매 첫 발견 알림이 생성되었습니다.");
            return;
        }

        Notice notice = new Notice(
                String.format("%s에서 열매가 처음 발견되었습니다! (개수: %d개) 축하드립니다!",
                        device.getDeviceNickname(), fruitCount),
                NoticeType.FRUIT_APPEARED,
                user,
                device,
                2  // 보통 우선순위
        );

        noticeService.saveNotice(notice);
        System.out.println("열매 첫 발견 알림 생성 완료");
    }

    // (열매) 수확 시기 알림 생성(의미 있는 변화 감지)
    @Transactional
    public void createHarvestReadyNotice(User user, Device device, Integer totalFruits, Integer matureFruits) {
        // 중복 방지 (읽지 않은 수확 알림이 있으면 생성 안 함)
        if (noticeService.hasUnreadNotice(user, device, NoticeType.HARVEST)) {
            System.out.println("이미 읽지 않은 수확 시기 알림이 있어 생성하지 않습니다.");
            return;
        }

        // 마지막 수확 알림 조회
        Optional<Notice> lastHarvestNotice = noticeRepository
                .findTopByUserAndDeviceAndNoticeTypeOrderByCreatedAtDesc(
                        user, device, NoticeType.HARVEST);

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
                NoticeType.HARVEST,
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

        noticeService.saveNotice(notice);
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