package com.metaverse.planti_be.sensor.domain;

import com.metaverse.planti_be.common.TimeStamped;
import com.metaverse.planti_be.device.domain.Device;
import com.metaverse.planti_be.sensor.dto.SensorLogRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "Sensor_Log")
@Entity
public class SensorLog extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long log_id;

    // SensorType 필드를 올바르게 선언했습니다.
    @Enumerated(EnumType.STRING)
    private SensorType sensor_type;

    @Column(nullable = false)
    private String value;

    // Device 엔티티와의 관계를 설정했습니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serial_number")
    private Device device;

    // DTO를 사용하여 엔티티를 생성하는 생성자입니다.
    public SensorLog(SensorLogRequestDto requestDto) {
        // 이 부분의 오타를 수정했습니다.
        this.sensor_type = SensorType.valueOf(requestDto.getSensor_type());
        this.value = requestDto.getValue();
    }
}