package com.ccat.api.security;

import com.ccat.api.model.entity.User;
import com.ccat.api.model.enums.UserStatus;
import com.ccat.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByStrEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getStrEmail())
                .password(user.getStrPasswordHash() != null ? user.getStrPasswordHash() : "")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getEmRole().name())))
                .accountLocked(user.getEmStatus() == UserStatus.SUSPENDED)
                .disabled(user.getEmStatus() == UserStatus.DELETED)
                .build();
    }
}
