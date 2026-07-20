package com.sarkari.auth.interfaces.rest;

import com.sarkari.auth.application.service.AuthService;
import com.sarkari.auth.domain.entity.AppUser;
import com.sarkari.common.exception.ResourceNotFoundException;
import com.sarkari.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Validated
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

        private static final String USERNAME_KEY = "username";
        private final AuthService authService;

        @PostMapping("/register")
        @Operation(summary = "Register", description = "Create a new user record in database")
        public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequest request) {
                try {
                        AppUser createdUser = authService.register(request.username(), request.password());

                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.<Map<String, Object>>builder()
                                                        .success(true)
                                                        .message("User registered successfully")
                                                        .data(Map.of(USERNAME_KEY, createdUser.getUsername()))
                                                        .build());
                } catch (IllegalArgumentException exception) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(ApiResponse.<Map<String, Object>>builder()
                                                        .success(false)
                                                        .message(exception.getMessage())
                                                        .data(null)
                                                        .build());
                }
        }

    @PostMapping("/login")
        @Operation(summary = "Login", description = "Validate username and password from database")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) {
                Optional<AppUser> user = authService.authenticate(request.username(), request.password());

                if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("Invalid credentials. Please try again.")
                            .data(null)
                            .build());
        }

        log.info("Login successful for username={}", user.get().getUsername());

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Login successful")
                .data(Map.of(USERNAME_KEY, user.get().getUsername()))
                .build());
    }

        @GetMapping("/users")
        @Operation(summary = "Get all users", description = "Fetch all users")
        public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsers() {
                List<Map<String, Object>> users = authService.getAllUsers()
                                .stream()
                                .map(user -> Map.<String, Object>of(
                                                "id", user.getId(),
                                                USERNAME_KEY, user.getUsername(),
                                                "createdAt", user.getCreatedAt(),
                                                "updatedAt", user.getUpdatedAt()
                                ))
                                .toList();

                return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                                .success(true)
                                .message("Users fetched successfully")
                                .data(users)
                                .build());
        }

        @GetMapping("/users/{username}")
        @Operation(summary = "Get user details", description = "Fetch user details by username")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getUserByUsername(
                        @PathVariable
                        @NotBlank(message = "Username is required")
                        @Size(min = 3, max = 80, message = "Username must be between 3 and 80 characters")
                        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username contains invalid characters")
                        String username
        ) {
                AppUser user = authService.getByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                                .success(true)
                                .message("User fetched successfully")
                                .data(Map.of(
                                                "id", user.getId(),
                                                USERNAME_KEY, user.getUsername(),
                                                "createdAt", user.getCreatedAt(),
                                                "updatedAt", user.getUpdatedAt()
                                ))
                                .build());
        }

        @DeleteMapping("/users/{username}")
        @Operation(summary = "Delete user", description = "Delete user by username")
        public ResponseEntity<ApiResponse<Void>> deleteUserByUsername(
                        @PathVariable
                        @NotBlank(message = "Username is required")
                        @Size(min = 3, max = 80, message = "Username must be between 3 and 80 characters")
                        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username contains invalid characters")
                        String username
        ) {
                authService.deleteByUsername(username);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("User deleted successfully")
                                .data(null)
                                .build());
        }

        @PutMapping("/users/{username}/password")
        @Operation(summary = "Update user password", description = "Update user password by username")
        public ResponseEntity<ApiResponse<Void>> updatePassword(
                        @PathVariable
                        @NotBlank(message = "Username is required")
                        @Size(min = 3, max = 80, message = "Username must be between 3 and 80 characters")
                        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username contains invalid characters")
                        String username,
                        @Valid @RequestBody UpdatePasswordRequest request
        ) {
                authService.updatePassword(username, request.newPassword());
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Password updated successfully")
                                .data(null)
                                .build());
        }

    public record RegisterRequest(
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 80, message = "Username must be between 3 and 80 characters")
            String username,
            @NotBlank(message = "Password is required")
            @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
            String password
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 80, message = "Username must be between 3 and 80 characters")
            String username,
            @NotBlank(message = "Password is required")
            @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
            String password
    ) {
    }

    public record UpdatePasswordRequest(
            @NotBlank(message = "New password is required")
            @Size(min = 8, max = 72, message = "New password must be between 8 and 72 characters")
            String newPassword
    ) {
    }
}
