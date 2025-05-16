package shop.ink3.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
