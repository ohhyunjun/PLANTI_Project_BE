package com.metaverse.planti_be.device.domain;

import com.metaverse.planti_be.sensor.domain.SensorType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "Device")
@Entity
public class Device {

    @Id
    @Column(length = 100)
    private String serial_number;

    @Column(length = 20)
    private String ip;

    @Column(length = 20)
    private String status;

    private LocalDateTime connect_time;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type")
    private SensorType sensor_type;

}
