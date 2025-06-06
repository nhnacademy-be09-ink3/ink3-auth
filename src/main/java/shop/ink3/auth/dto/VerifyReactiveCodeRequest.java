package shop.ink3.auth.dto;

public record VerifyReactiveCodeRequest(
        String loginId,
        String code
) {
}
