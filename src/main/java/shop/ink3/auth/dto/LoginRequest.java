package shop.ink3.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotNull UserType userType,
        @NotNull Boolean rememberMe
) {
}
