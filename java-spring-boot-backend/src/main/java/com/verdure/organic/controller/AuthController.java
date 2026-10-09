package com.verdure.organic.controller;

import com.verdure.organic.model.User;
import com.verdure.organic.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody Map<String, String> payload) {

        String fullName = payload.getOrDefault("fullName", "").trim();
        String email = payload.getOrDefault("email", "").trim();
        String phone = payload.getOrDefault("phone", "").trim();
        String password = payload.getOrDefault("password", "");

        if (fullName.isBlank() || email.isBlank()
                || phone.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "All fields are required.")
            );
        }

        if (password.length() < 8) {
            return ResponseEntity.badRequest().body(
                Map.of("message",
                    "Password must be at least 8 characters.")
            );
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of("message", "Email is already registered.")
            );
        }

        if (userRepository.existsByPhone(phone)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of("message", "Phone number is already registered.")
            );
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("CUSTOMER");
        user.setResellerEarnings(BigDecimal.ZERO);

        try {
            User savedUser = userRepository.save(user);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                    "message", "Registration successful.",
                    "user", Map.of(
                        "id", savedUser.getId(),
                        "fullName", savedUser.getFullName(),
                        "email", savedUser.getEmail(),
                        "phone", savedUser.getPhone(),
                        "role", savedUser.getRole(),
                        "resellerEarnings",
                            savedUser.getResellerEarnings()
                    )
                )
            );
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of("message",
                    "Email or phone number is already registered.")
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> payload) {

        String identifier =
            payload.getOrDefault("identifier", "").trim();
        String password = payload.getOrDefault("password", "");

        if (identifier.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(
                Map.of("message",
                    "Email and password are required.")
            );
        }

        User user = userRepository
            .findByEmailIgnoreCase(identifier)
            .orElse(null);

        if (user == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(
                    password, user.getPasswordHash())) {
            return ResponseEntity.status(
                HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Invalid email or password.")
                );
        }

        return ResponseEntity.ok(Map.of(
            "message", "Login successful.",
            "user", Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "phone", user.getPhone(),
                "role", user.getRole(),
                "resellerEarnings", user.getResellerEarnings()
            )
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(
            Map.of("message", "Logout successful.")
        );
    }

    // ==========================================
    // NEW: FETCH ALL USERS FOR THE ADMIN VIEW
    // ==========================================
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> allUsers = userRepository.findAll();
        
        List<Map<String, Object>> responseList = allUsers.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("fullName", user.getFullName());
            map.put("email", user.getEmail());
            map.put("phone", user.getPhone());
            map.put("role", user.getRole());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
}
