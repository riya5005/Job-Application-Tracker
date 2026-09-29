package com.atstracker.controller;

import com.atstracker.model.User;
import com.atstracker.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

// Spring Security gives us the email (via the JWT) on the Authentication object,
// but most of the service methods want the actual User entity - this just bridges that gap
// so I'm not repeating the same lookup in every controller method.
@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User get(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB, this shouldn't happen"));
    }
}
