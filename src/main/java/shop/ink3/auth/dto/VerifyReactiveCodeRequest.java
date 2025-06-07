package shop.ink3.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyReactiveCodeRequest(
        @NotBlank String loginId,
        @NotBlank String code
) {
}
