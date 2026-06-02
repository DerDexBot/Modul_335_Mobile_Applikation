package com.workforce.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO für das Anlegen eines neuen Benutzers (US-HR-01).
 *
 * @param username  Gewünschter Benutzername (muss eindeutig sein)
 * @param email     E-Mail-Adresse (muss eindeutig sein)
 * @param password  Initiales Passwort (wird BCrypt-gehasht gespeichert)
 * @param firstName Vorname
 * @param lastName  Nachname
 * @param roleName  Name der zuzuweisenden Rolle (z.B. "SHIFT_LEAD")
 */
public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 100, message = "Username is too long")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Username contains invalid characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 150, message = "Email is too long")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
        String password,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name is too long")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name is too long")
        String lastName,

        @NotBlank(message = "Role is required")
        @Pattern(regexp = "^(ADMIN|HR|SHIFT_LEAD|EMPLOYEE)$", message = "Role is invalid")
        String roleName
) {}
