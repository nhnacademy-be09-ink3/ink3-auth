package shop.ink3.auth.dto;

public record SocialUserResponse(
        String provider,
        String providerId,
        Long id,
        String username
) {
}
