package com.metaverse.planti_be.auth.service;

import com.metaverse.planti_be.auth.dto.JoinDTO;
import com.metaverse.planti_be.auth.entity.UserEntity;
import com.metaverse.planti_be.auth.entity.UserRole;
import com.metaverse.planti_be.auth.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public void joinProcess(JoinDTO joinDTO) {
        String name = joinDTO.getName();
        String password = joinDTO.getPassword();

        Boolean isExist = userRepository.existsByName(name);

        if (isExist) {
            return;
        }

        UserEntity data = UserEntity.builder()
                .name(name) // ✅ setUsername -> name
                .password(bCryptPasswordEncoder.encode(password)) // ✅ 비밀번호 암호화
                .role(UserRole.ADMIN) // ✅ String이 아닌 Enum 타입(UserRole.ADMIN)으로 역할 설정
                .build();

        userRepository.save(data);
    }
}
