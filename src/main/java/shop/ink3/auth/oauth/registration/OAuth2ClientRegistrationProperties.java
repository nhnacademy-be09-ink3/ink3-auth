package shop.ink3.auth.oauth.registration;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oauth2.client")
public class OAuth2ClientRegistrationProperties {
    @Getter
    @Setter
    private Map<String, OAuth2ClientRegistration> registrations;
}
