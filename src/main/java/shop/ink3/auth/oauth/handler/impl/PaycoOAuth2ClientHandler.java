package shop.ink3.auth.oauth.handler.impl;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Objects;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import shop.ink3.auth.oauth.dto.OAuth2TokenResponse;
import shop.ink3.auth.oauth.dto.OAuth2UserInfo;
import shop.ink3.auth.oauth.exception.OAuth2AuthenticationException;
import shop.ink3.auth.oauth.handler.OAuth2ClientHandler;
import shop.ink3.auth.oauth.registration.OAuth2ClientRegistration;
import shop.ink3.auth.oauth.registration.OAuth2ClientRegistrationProperties;

@Component
@ConditionalOnProperty(prefix = "oauth2.client.registrations", name = "payco.client-id")
public class PaycoOAuth2ClientHandler implements OAuth2ClientHandler {
    private static final String PROVIDER = "payco";

    private static final String RESPONSE_TYPE = "response_type";
    private static final String CLIENT_ID = "client_id";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String SERVICE_PROVIDER_CODE = "serviceProviderCode";
    private static final String USER_LOCALE = "userLocale";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String CODE = "code";
    private static final String ACCESS_TOKEN = "access_token";

    private final OAuth2ClientRegistration registration;
    private final URI authorizationUri;
    private final WebClient webClient;

    public PaycoOAuth2ClientHandler(OAuth2ClientRegistrationProperties properties, WebClient webClient) {
        this.registration = properties.getRegistrations().get(PROVIDER);
        this.authorizationUri = UriComponentsBuilder.fromUriString(registration.authorizationUri())
                .queryParam(RESPONSE_TYPE, "code")
                .queryParam(CLIENT_ID, registration.clientId())
                .queryParam(REDIRECT_URI, registration.redirectUri())
                .queryParam(SERVICE_PROVIDER_CODE, "FRIENDS")
                .queryParam(USER_LOCALE, "ko_KR")
                .build().toUri();
        this.webClient = webClient;
    }

    @Override
    public String getProvider() {
        return PROVIDER;
    }

    @Override
    public URI getAuthorizationUri() {
        return authorizationUri;
    }

    @Override
    public OAuth2UserInfo handle(HttpServletRequest request) {
        String code = request.getParameter(CODE);
        OAuth2TokenResponse token = getAccessToken(code);
        if (Objects.isNull(token)) {
            throw new OAuth2AuthenticationException();
        }
        return getUserInfo(token.accessToken());
    }

    private OAuth2TokenResponse getAccessToken(String code) {
        URI uri = UriComponentsBuilder.fromUriString(registration.tokenUri())
                .queryParam(GRANT_TYPE, registration.authorizationGrantType())
                .queryParam(CLIENT_ID, registration.clientId())
                .queryParam(CLIENT_SECRET, registration.clientSecret())
                .queryParam(CODE, code)
                .build().toUri();
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(OAuth2TokenResponse.class)
                .block();
    }

    private OAuth2UserInfo getUserInfo(String accessToken) {
        JsonNode response = webClient.post()
                .uri(registration.userInfoUri())
                .header(CLIENT_ID, registration.clientId())
                .header(ACCESS_TOKEN, accessToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (Objects.isNull(response)) {
            return null;
        }

        JsonNode memberNode = response.path("data").path("member");

        return new OAuth2UserInfo(
                PROVIDER,
                memberNode.path("idNo").asText(null),
                memberNode.path("name").asText(null),
                memberNode.path("genderCode").asText(null),
                memberNode.path("email").asText(null),
                memberNode.path("mobile").asText(null),
                memberNode.path("birthday").asText(null)
        );
    }
}
