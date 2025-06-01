package shop.ink3.auth.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuth2TokenResponse(
        @JsonProperty("access_token")
        String accessToken,
        
        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        String expiresIn,

        @JsonProperty("refresh_token")
        String refreshToken
) {
}
