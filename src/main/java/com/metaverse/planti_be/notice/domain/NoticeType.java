package com.metaverse.planti_be.notice.domain;

public enum NoticeType {
    WATER_SHORTAGE("물 부족"),
    PEST_DETECTED("해충 발견"), // 나중에 사용 예정
    FRUIT_FIRST_APPEARED("열매 첫 발견"),
    HARVEST_READY("수확 시기"),
    GROWTH_STAGE_CHANGED("성장 단계 변화"),
    SYSTEM_ALERT("시스템 알림");

    private final String description;

    NoticeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
