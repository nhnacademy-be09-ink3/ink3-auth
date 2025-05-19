package shop.ink3.auth.dto;

public record LoginResponse(
        JwtToken accessToken,
        JwtToken refreshToken
) {
}
