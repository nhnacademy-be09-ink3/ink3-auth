package shop.ink3.auth.dto;

public record ReissueRequest(
        long id,
        UserRole role,
        String refreshToken
) {
}
