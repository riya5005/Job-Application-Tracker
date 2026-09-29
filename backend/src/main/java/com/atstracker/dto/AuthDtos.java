package com.atstracker.dto;

import com.atstracker.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// keeping the small auth-related DTOs in one file, didn't feel like they
// each deserved their own file for a project this size

public class AuthDtos {

    public static class RegisterRequest {
        @NotBlank
        public String fullName;

        @NotBlank @Email
        public String email;

        @NotBlank
        public String password;

        public Role role; // RECRUITER or CANDIDATE
    }

    public static class LoginRequest {
        @NotBlank @Email
        public String email;

        @NotBlank
        public String password;
    }

    public static class AuthResponse {
        public String token;
        public String fullName;
        public String email;
        public Role role;

        public AuthResponse(String token, String fullName, String email, Role role) {
            this.token = token;
            this.fullName = fullName;
            this.email = email;
            this.role = role;
        }
    }
}
