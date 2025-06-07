package shop.ink3.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReissueRequest(
        @NotNull Long id,
        @NotNull UserType userType,
        @NotBlank String refreshToken
) {
}
