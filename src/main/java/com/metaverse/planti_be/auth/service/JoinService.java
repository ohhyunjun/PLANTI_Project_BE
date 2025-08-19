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
                .name(name)
                .password(bCryptPasswordEncoder.encode(password))
                .email(joinDTO.getEmail())
                .role(UserRole.ADMIN)
                .build();

        userRepository.save(data);
    }
}
