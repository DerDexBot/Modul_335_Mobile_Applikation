package com.workforce.auth.controller;

import com.workforce.auth.config.JwtUtil;
import com.workforce.auth.dto.LoginRequest;
import com.workforce.auth.model.User;
import com.workforce.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request.username(), request.password());
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().getName());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "role", user.getRole().getName(),
                "username", user.getUsername(),
                "userId", user.getId()
        ));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, String>> validate(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isValid(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
        return ResponseEntity.ok(Map.of(
                "username", jwtUtil.extractUsername(token),
                "role", jwtUtil.extractRole(token)
        ));
    }
}
