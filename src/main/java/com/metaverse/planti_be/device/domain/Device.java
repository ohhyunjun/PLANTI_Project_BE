package com.metaverse.planti_be.device.domain;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.common.TimeStamped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "device")
@Getter
@Setter
@NoArgsConstructor
public class Device extends TimeStamped {
    @Id
    @Column(name = "serial_number")
    private String id;

    @Column(name="device_nickname")
    private String deviceNickname;

    @Column(name="status")
    private Boolean status;

    @ManyToOne(fetch = FetchType.LAZY) // N:1 관계 명시, 성능을 위해 LAZY 로딩을 권장
    @JoinColumn(name = "user_id")      // DB에 생성될 외래 키 컬럼 이름을 'user_id'로 지정
    private User user;


}
