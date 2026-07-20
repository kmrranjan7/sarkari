package com.sarkari.auth.application.service.impl;

import com.sarkari.auth.application.service.AuthService;
import com.sarkari.auth.domain.entity.AppUser;
import com.sarkari.auth.domain.repository.AppUserRepository;
import com.sarkari.common.exception.BusinessException;
import com.sarkari.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AppUser register(String username, String rawPassword) {
        if (appUserRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username already exists.");
        }

        AppUser user = AppUser.builder()
                .username(username.trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .build();

        return appUserRepository.save(user);
    }

    @Override
    public Optional<AppUser> authenticate(String username, String rawPassword) {
        Optional<AppUser> user = appUserRepository.findByUsernameIgnoreCase(username.trim());

        if (user.isEmpty()) {
            return Optional.empty();
        }

        boolean passwordMatches = passwordEncoder.matches(rawPassword, user.get().getPasswordHash());
        return passwordMatches ? user : Optional.empty();
    }

    @Override
    public Optional<AppUser> getByUsername(String username) {
        return appUserRepository.findByUsernameIgnoreCase(username.trim());
    }

    @Override
    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll();
    }

    @Override
    public void deleteByUsername(String username) {
        AppUser existing = appUserRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        appUserRepository.delete(existing);
    }

    @Override
    public void updatePassword(String username, String newPassword) {
        AppUser existing = appUserRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (passwordEncoder.matches(newPassword, existing.getPasswordHash())) {
            throw new BusinessException("New password must be different from current password");
        }

        existing.setPasswordHash(passwordEncoder.encode(newPassword));
        appUserRepository.save(existing);
    }
}
