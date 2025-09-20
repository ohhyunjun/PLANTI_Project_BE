package com.metaverse.planti_be.led.domain;

import com.metaverse.planti_be.common.TimeStamped;
import com.metaverse.planti_be.device.domain.Device;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Led extends TimeStamped{
    @Id
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Device의 ID를 Led의 ID로 매핑
    @JoinColumn(name = "led_id") //DB에 led_id이름으로 저장
    private Device device;

    private int intensity;
    private LocalTime startTime;
    private LocalTime endTime;

    // Device가 생성될 때 Led를 만들기 위한 생성자
    public Led(Device device) {
        this.device = device;
        this.intensity = 0;
        this.startTime = LocalTime.of(0, 0);
        this.endTime = LocalTime.of(0, 0);
    }
}
