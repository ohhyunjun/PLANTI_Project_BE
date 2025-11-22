package com.metaverse.planti_be.notice.service.notification;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.notice.domain.Notice;
import com.metaverse.planti_be.notice.domain.NoticeType;
import com.metaverse.planti_be.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 🌱 잎식물(상추 등) 전용 알림 서비스
 * - 질병 발견 알림
 * - 수확 시기 알림
 * - 새싹 첫 발견 알림
 */
@Service
@RequiredArgsConstructor
public class LeafNotificationService {

    private final NoticeService noticeService;

    /**
     * 🌱 잎식물 AI 분석 결과를 기반으로 알림을 생성합니다.
     * - (질병, 상태, 수확 시기 등)
     *
     * @param device           알림을 받을 디바이스
     * @param detailedResponse AI 서버의 상세 분석 결과
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAndCreateNotifications(Device device, Map<String, Object> detailedResponse) {
        System.out.println("🌱 잎식물 전용 알림 체크 시작");

        String analysisStage = (String) detailedResponse.getOrDefault("analysis_stage", "");
        String bestResult = (String) detailedResponse.getOrDefault("bestResult", "no_detection");

        // 1. 질병 감지 알림 (AI가 'disease'로 판단 시)
        if ("disease".equalsIgnoreCase(analysisStage) && !"no_detection".equals(bestResult)) {
            createDiseaseDetectedNotice(
                    device.getUser(),
                    device,
                    bestResult // AI가 반환한 질병 이름
            );
        }

        // 2. 상추 수확 시기 알림 (growth stage이고 AI가 'MATURE'로 판단 시)
        if ("growth".equalsIgnoreCase(analysisStage) && "MATURE".equalsIgnoreCase(bestResult)) {
            createLeafHarvestReadyNotice(
                    device.getUser(),
                    device
            );
        }

        // 3. 새싹(발아) 알림은 updatePlantStageFromAnalysis에서 처리됩니다.

        System.out.println("🌱 잎식물 전용 알림 체크 완료\n");
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

    // ❗️ 질병 발견 알림 생성
    @Transactional
    public void createDiseaseDetectedNotice(User user, Device device, String diseaseName) {
        // 중복 방지: 이미 읽지 않은 동일 알림이 있으면 생성 안 함
        if (noticeService.hasUnreadNotice(user, device, NoticeType.DISEASE)) {
            System.out.println("이미 읽지 않은 질병 알림이 있어 생성하지 않습니다.");
            return;
        }

        Notice notice = new Notice(
                String.format("%s에서 질병(%s)이 발견되었습니다. 식물 상태를 확인해주세요!",
                        device.getDeviceNickname(), diseaseName),
                NoticeType.DISEASE,
                user,
                device,
                1  // 높은 우선순위
        );

        noticeService.saveNotice(notice);
        System.out.println("질병 발견 알림 생성 완료");
    }

    // ❗️ 잎식물 수확 시기 알림 생성 (단순 알림)
    @Transactional
    public void createLeafHarvestReadyNotice(User user, Device device) {
        // 중복 방지 (읽지 않은 수확 알림이 있으면 생성 안 함)
        if (noticeService.hasUnreadNotice(user, device, NoticeType.HARVEST)) {
            System.out.println("이미 읽지 않은 수확 시기 알림이 있어 생성하지 않습니다.");
            return;
        }

        Notice notice = new Notice(
                String.format("%s의 식물이 수확 시기에 도달했습니다! 지금 수확할 수 있습니다.",
                        device.getDeviceNickname()),
                NoticeType.HARVEST,
                user,
                device,
                2  // 보통 우선순위
        );

        // (잎식물은 별도 additionalData가 필요하지 않음)

        noticeService.saveNotice(notice);
        System.out.println("잎식물 수확 시기 알림 생성 완료");
    }
}