package com.metaverse.planti_be.auth.service;

import com.metaverse.planti_be.auth.domain.User;
import com.metaverse.planti_be.auth.domain.UserRole;
import com.metaverse.planti_be.auth.dto.SignUpRequestDto;
import com.metaverse.planti_be.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(SignUpRequestDto signUpRequestDto) {
        if (userRepository.existsByUsername(signUpRequestDto.getUsername())) {
            throw new IllegalArgumentException("Username 사용자 계정이 사용중입니다.");
        }

        if (userRepository.existsByEmail(signUpRequestDto.getEmail())) {
            throw new IllegalArgumentException("Email 사용자 이메일이 사용중입니다.");
        }

        User user = new User(
                signUpRequestDto.getUsername(),
                passwordEncoder.encode(signUpRequestDto.getPassword()),
                signUpRequestDto.getEmail(),
                UserRole.ADMIN
        );

        userRepository.save(user);
    }

    @Transactional(readOnly = true) // 데이터 변경이 없는 읽기 전용 트랜잭션
    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }
}