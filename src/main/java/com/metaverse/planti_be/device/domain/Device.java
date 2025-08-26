package com.metaverse.planti_be.device.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "Device")
@Entity
public class Device {

    @Id
    @Column(length = 100)
    private String serial_number;

}
