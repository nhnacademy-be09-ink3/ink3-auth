package shop.ink3.auth.client.user.dto;


public record AuthResponse(
        Long id,
        String username,
        String password
) {
}
