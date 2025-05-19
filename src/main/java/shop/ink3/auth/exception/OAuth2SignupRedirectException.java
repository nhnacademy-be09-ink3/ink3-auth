package shop.ink3.auth.exception;

import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;


@Getter
public class OAuth2SignupRedirectException extends AuthenticationException {
    private final Map<String, Object> attributes;

    public OAuth2SignupRedirectException(Map<String, Object> attributes) {
        super("Redirecting to signup page with OAuth2 attributes");
        this.attributes = attributes;
    }
}
