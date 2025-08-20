package com.metaverse.planti_be.auth.repository;

import com.metaverse.planti_be.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // 사용자 이름(name)으로 User 객체를 조회하는 메서드
    // Spring Security의 UserDetailsService에서 사용
    Optional<UserEntity> findByUsername(String name);

    // 사용자 이름(username)이 존재하는지 확인하는 메서드 (회원가입 시 중복 체크 등)
    Boolean existsByName(String name);

    // 이메일(email)이 존재하는지 확인하는 메서드 (회원가입 시 중복 체크 등)
    boolean existsByEmail(String email);
}
