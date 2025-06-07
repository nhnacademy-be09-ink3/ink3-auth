package shop.ink3.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record SendReactiveCodeRequest(
        @NotBlank String loginId
) {
}
