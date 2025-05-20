package shop.ink3.auth.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;
import shop.ink3.auth.dto.LoginResponse;

@Getter
public class OAuth2AuthenticationSuccessRedirectException extends AuthenticationException {
    private final LoginResponse tokens;

    public OAuth2AuthenticationSuccessRedirectException(LoginResponse tokens) {
        super("OAuth2 authentication successful, redirecting with token");
        this.tokens = tokens;
    }
}
