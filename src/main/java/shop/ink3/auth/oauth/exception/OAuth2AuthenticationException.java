package shop.ink3.auth.oauth.exception;

public class OAuth2AuthenticationException extends RuntimeException {
    public OAuth2AuthenticationException() {
        super("OAuth2 token exchange failed: access token is null or missing.");
    }
}
