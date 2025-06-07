package shop.ink3.auth.oauth.exception;

public class OAuth2ProviderNotFoundException extends RuntimeException {
    public OAuth2ProviderNotFoundException(String provider) {
        super("OAuth2 provider not found. Provider: " + provider);
    }
}
