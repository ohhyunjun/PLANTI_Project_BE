package com.metaverse.planti_be.auth.repository;

import com.metaverse.planti_be.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Boolean existsByName(String name);
}
