package shop.ink3.auth.client.dto;


public record AuthResponse(
        Long id,
        String username,
        String password
) {
}
