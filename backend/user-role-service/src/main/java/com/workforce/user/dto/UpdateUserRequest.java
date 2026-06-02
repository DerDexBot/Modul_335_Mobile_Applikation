package com.workforce.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO für das Bearbeiten eines bestehenden Benutzers (US-HR-02).
 * Alle Felder sind optional – nur nicht-null Werte werden aktualisiert.
 *
 * @param firstName Neuer Vorname (optional)
 * @param lastName  Neuer Nachname (optional)
 * @param email     Neue E-Mail-Adresse (optional)
 * @param active    Aktivierungsstatus – {@code false} deaktiviert den Benutzer
 * @param roleName  Neuer Rollenname (optional, z.B. "ADMIN", "HR", "SHIFT_LEAD", "EMPLOYEE")
 */
public record UpdateUserRequest(
        @Size(max = 100, message = "First name is too long")
        String firstName,

        @Size(max = 100, message = "Last name is too long")
        String lastName,

        @Email(message = "Email must be valid")
        @Size(max = 150, message = "Email is too long")
        String email,

        Boolean active,

        @Pattern(regexp = "^(ADMIN|HR|SHIFT_LEAD|EMPLOYEE)$", message = "Role is invalid")
        String roleName
) {}
