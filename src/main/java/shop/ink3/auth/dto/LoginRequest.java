package shop.ink3.auth.dto;

public record LoginRequest(
        String username,
        String password,
        UserType userType
) {
}
