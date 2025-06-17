package shop.ink3.auth.oauth.handler.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import shop.ink3.auth.oauth.registration.OAuth2ClientRegistration;
import shop.ink3.auth.oauth.registration.OAuth2ClientRegistrationProperties;

class PaycoOAuth2ClientHandlerTest {
    @Test
    void authorizationUriContainsRequiredParameters() {
        OAuth2ClientRegistrationProperties props = new OAuth2ClientRegistrationProperties();
        OAuth2ClientRegistration reg = new OAuth2ClientRegistration(
                "client-id",
                "secret",
                "https://redirect",
                "authorization_code",
                "https://auth",
                "https://token",
                "https://info"
        );
        props.setRegistrations(Map.of("payco", reg));

        PaycoOAuth2ClientHandler handler = new PaycoOAuth2ClientHandler(props, WebClient.builder().build());

        URI uri = handler.getAuthorizationUri();

        assertThat(uri.toString()).contains("response_type=code")
                .contains("client_id=client-id")
                .contains("redirect_uri=https://redirect")
                .contains("serviceProviderCode=FRIENDS")
                .contains("userLocale=ko_KR");
        assertThat(handler.getProvider()).isEqualTo("payco");
    }
}
