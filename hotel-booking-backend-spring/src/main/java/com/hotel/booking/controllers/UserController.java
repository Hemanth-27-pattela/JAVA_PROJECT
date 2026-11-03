package com.hotel.booking.controllers;

import com.hotel.booking.dtos.MessageResponse;
import com.hotel.booking.dtos.RegisterRequest;
import com.hotel.booking.dtos.UpdateRoleRequest;
import com.hotel.booking.dtos.UserDto;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.UserRepository;
import com.hotel.booking.security.JwtTokenProvider;
import com.hotel.booking.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User registration, profile, and administrative user management")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public UserController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Public user registration.
     * Always assigns the standard USER role for security.
     */
    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Register a new user with email and password")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request,
                                                    HttpServletResponse response) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("User already exists"));
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        // Security requirement: public registration must always assign the ordinary user role
        user.setRole(UserRole.USER);
        user.setIsActive(true);
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);

        // Issue token and set cookie
        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        ResponseCookie authCookie = ResponseCookie.from("auth_token", token)
                .httpOnly(true)
                .path("/")
                .maxAge(86400)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());

        return ResponseEntity.ok(new MessageResponse("User registered OK"));
    }

    /**
     * Get the authenticated user's profile.
     */
    @GetMapping("/me")
    @Operation(summary = "Current user profile", description = "Retrieve profile for the authenticated user")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userRepository.findById(userPrincipal.getId())
                .map(user -> ResponseEntity.ok(UserDto.fromEntity(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    /**
     * Admin: list all users.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List users (Admin only)", description = "Retrieve all users without password hashes")
    public ResponseEntity<List<UserDto>> listUsers() {
        List<UserDto> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /**
     * Admin: update user role.
     */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role (Admin only)", description = "Change a user's role")
    public ResponseEntity<?> updateUserRole(@PathVariable("id") String id,
                                            @Valid @RequestBody UpdateRoleRequest request,
                                            @AuthenticationPrincipal UserPrincipal currentUser) {
        // Prevent admin from demoting themselves
        if (currentUser != null && id.equals(currentUser.getId()) && request.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Cannot demote your own admin role"));
        }

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("User not found"));
        }

        User user = userOpt.get();
        user.setRole(request.getRole());
        user.setUpdatedAt(Instant.now());
        User updated = userRepository.save(user);
        return ResponseEntity.ok(UserDto.fromEntity(updated));
    }
}
