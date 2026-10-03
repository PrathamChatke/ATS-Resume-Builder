package com.atsresume.auth;

/**
 * The authentication principal placed on the security context by
 * {@link JwtAuthenticationFilter}. Carries just enough to authorize and
 * attribute requests without a database lookup on every call.
 */
public record AuthenticatedUser(Long id, String email, Role role) {
}
