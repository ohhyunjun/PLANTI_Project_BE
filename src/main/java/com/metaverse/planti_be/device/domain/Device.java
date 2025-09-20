package com.metaverse.planti_be.device.domain;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.common.TimeStamped;
import com.metaverse.planti_be.led.domain.Led;
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

    @OneToOne(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private Led led;

    // Led를 함께 생성하기 위한 편의 메서드
    public void createLed() {
        this.led = new Led(this);
    }

    // 관리자가 기기를 생성할 때 사용할 생성자
    public Device(String id, String deviceNickname) {
        this.id = id;
        this.deviceNickname = deviceNickname;
        this.status = false;
        createLed(); // 생성 시점에 Led도 함께 생성
    }

}
