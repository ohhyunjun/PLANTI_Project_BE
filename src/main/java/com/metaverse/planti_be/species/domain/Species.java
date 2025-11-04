package com.metaverse.planti_be.species.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Table(name = "species")
@Entity
public class Species {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // 예: "방울토마토", "적상추"

    // 발아 후 성체까지 걸리는 평균 일수
    @Column(nullable = false)
    private int daysToMature;

    // AI LED 조언 생성 시 참고할 가이드라인 텍스트
    @Column(length = 500)
    private String aiPromptGuideline;

    // 빌더 패턴이나 생성자를 통해 초기 데이터를 설정할 수 있습니다.
    public Species(String name, int daysToMature, String aiPromptGuideline) {
        this.name = name;
        this.daysToMature = daysToMature;
        this.aiPromptGuideline = aiPromptGuideline;
    }

    // 수정 메서드
    public void update(String name, int daysToMature, String aiPromptGuideline) {
        this.name = name;
        this.daysToMature = daysToMature;
        this.aiPromptGuideline = aiPromptGuideline;
    }
}
