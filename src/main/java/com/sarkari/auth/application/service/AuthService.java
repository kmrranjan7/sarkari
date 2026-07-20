package com.sarkari.auth.application.service;

import com.sarkari.auth.domain.entity.AppUser;
import java.util.List;
import java.util.Optional;

public interface AuthService {

    AppUser register(String username, String rawPassword);

    Optional<AppUser> authenticate(String username, String rawPassword);

    Optional<AppUser> getByUsername(String username);

    List<AppUser> getAllUsers();

    void deleteByUsername(String username);

    void updatePassword(String username, String newPassword);
}
