package com.school.sis.auth.security;

import com.school.sis.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SisUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public SisUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(username, username)
                .map(SisUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
