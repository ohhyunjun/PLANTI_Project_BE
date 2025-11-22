package com.metaverse.planti_be.notice.domain;

public enum NoticeType {
    WATER_SHORTAGE("물 부족"),
    DISEASE("질병 발견"),           // DISEASE_DETECTED → DISEASE
    FRUIT_APPEARED("열매 첫 발견"), // FRUIT_FIRST_APPEARED → FRUIT_APPEARED
    HARVEST("수확 시기"),           // HARVEST_READY → HARVEST
    GROWTH_CHANGE("성장 변화"),     // GROWTH_STAGE_CHANGED → GROWTH_CHANGE
    SYSTEM("시스템 알림");

    private final String description;

    NoticeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
