package com.metaverse.planti_be.auth.service;

import com.metaverse.planti_be.auth.dto.CustomUserDetails;
import com.metaverse.planti_be.auth.entity.UserEntity;
import com.metaverse.planti_be.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Spring Security의 UserDetailsService 인터페이스의 필수 구현 메서드
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // userRepository를 사용하여 데이터베이스에서 username에 해당하는 사용자 정보를 조회
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 조회된 UserEntity를 CustomUserDetails 객체로 감싸서 반환
        return new CustomUserDetails(userEntity);
    }
}
