package shop.ink3.auth.oauth.registration;

public record OAuth2ClientRegistration(
        String clientId,
        String clientSecret,
        String redirectUri,
        String authorizationGrantType,
        String authorizationUri,
        String tokenUri,
        String userInfoUri
) {
}
