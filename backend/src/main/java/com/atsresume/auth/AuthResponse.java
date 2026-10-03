package com.atsresume.auth;

public record AuthResponse(String token, String fullName, String email, Role role) {
}
