package shop.ink3.auth.dto;

public record ReissueRequest(
        long id,
        UserType type,
        String refreshToken
) {
}
