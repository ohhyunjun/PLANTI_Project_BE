package com.metaverse.planti_be.auth.repository;

import com.metaverse.planti_be.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Boolean existsByName(String name);

    //username을 받아 DB 테이블에서 회원을 조회하는 메소드 작성
    UserEntity findByName(String name);
}
