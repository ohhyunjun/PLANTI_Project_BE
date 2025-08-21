package com.metaverse.planti_be.auth.service;

import com.metaverse.planti_be.auth.dto.JoinDTO;
import com.metaverse.planti_be.auth.entity.UserEntity;
import com.metaverse.planti_be.auth.entity.UserRole;
import com.metaverse.planti_be.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor //final 필드에 대한 생성자를 자동으로 생성
public class JoinService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void joinProcess(JoinDTO joinDTO) {
        String name = joinDTO.getName();
        String password = joinDTO.getPassword();
        String email = joinDTO.getEmail();

        // 아이디(name) 중복 확인
        if (userRepository.existsByName(name)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 이메일 중복 확인 (UserRepository에 existsByEmail이 정의되어 있어야 함)
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // UserEntity.builder() 대신 생성자를 사용하여 객체 생성
        UserEntity user = new UserEntity(
                name,
                passwordEncoder.encode(password), // 비밀번호 암호화
                email,
                UserRole.ROLE_USER // 기본 역할을 ADMIN이 아닌 USER로 설정
        );

        userRepository.save(user);
    }
}
