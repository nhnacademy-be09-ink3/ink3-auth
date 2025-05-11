package shop.ink3.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
