package com.ptfmobile.vn.authservice.security;

import lombok.SneakyThrows;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository users) {
        this.userRepository = users;
    }

    @SneakyThrows
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.just(this.userRepository.findByUsername(username));
    }
}