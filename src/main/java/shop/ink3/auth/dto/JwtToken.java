package shop.ink3.auth.dto;

public record JwtToken(
        String token,
        long expiresAt
) {
}
