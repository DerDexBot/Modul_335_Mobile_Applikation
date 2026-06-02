package com.workforce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 100, message = "Username is too long")
        String username,

        @NotBlank(message = "Password is required")
        @Size(max = 255, message = "Password is too long")
        String password
) {}
