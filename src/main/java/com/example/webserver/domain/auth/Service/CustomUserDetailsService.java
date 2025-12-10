package com.example.webserver.domain.auth.Service;

import com.example.webserver.domain.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    @Override
    public UserDetails loadUserByUsername(String loginUserId) throws UsernameNotFoundException {

        logger.info(String.valueOf(5));
        // DB에서 사용자 ID를 찾아 UserDetails(User 엔티티) 객체를 반환
        return userRepository.findByLoginUserId(loginUserId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginUserId));
    }
}