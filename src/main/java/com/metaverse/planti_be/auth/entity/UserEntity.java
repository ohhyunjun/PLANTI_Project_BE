package com.metaverse.planti_be.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity // ✅ 이 클래스가 데이터베이스 테이블과 연결되는 것임을 선언
@Table(name = "user") // ✅ 연결될 테이블의 이름을 "user"로 지정
@EntityListeners(AuditingEntityListener.class) // ✅ 생성/수정 시간 자동 기록을 위한 리스너 추가
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "name",unique = true, nullable = false, updatable = false)
    private String name;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role;

    @CreatedDate // 엔티티 생성 시 시간 자동 저장
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserEntity(String name, String password, String email, UserRole role) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.role = role;
    }
}
