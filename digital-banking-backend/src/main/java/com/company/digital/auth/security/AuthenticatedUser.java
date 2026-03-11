package com.company.digital.auth.security;

public record AuthenticatedUser(Long userId, String email, String role) {
}
