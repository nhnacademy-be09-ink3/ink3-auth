package shop.ink3.auth.oauth.handler.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import shop.ink3.auth.oauth.dto.OAuth2UserInfo;
import shop.ink3.auth.oauth.exception.OAuth2AuthenticationException;
import shop.ink3.auth.oauth.registration.OAuth2ClientRegistration;
import shop.ink3.auth.oauth.registration.OAuth2ClientRegistrationProperties;

@ExtendWith(MockitoExtension.class)
class PaycoOAuth2ClientHandlerTest {

    private MockWebServer mockServer;
    private PaycoOAuth2ClientHandler handler;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        String baseUrl = mockServer.url("/").toString();

        OAuth2ClientRegistration registration = new OAuth2ClientRegistration(
                "client-id",
                "client-secret",
                "http://localhost/callback",
                "authorization_code",
                baseUrl + "authorize",
                baseUrl + "token",
                baseUrl + "userinfo"
        );

        OAuth2ClientRegistrationProperties properties = new OAuth2ClientRegistrationProperties();
        Map<String, OAuth2ClientRegistration> map = new HashMap<>();
        map.put("payco", registration);
        properties.setRegistrations(map);

        WebClient webClient = WebClient.builder().build();
        handler = new PaycoOAuth2ClientHandler(properties, webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void getProvider_returnsPayco() {
        assertThat(handler.getProvider()).isEqualTo("payco");
    }

    @Test
    void getAuthorizationUri_containsExpectedQueryParams() {
        URI uri = handler.getAuthorizationUri();
        String query = uri.getQuery();

        assertThat(query).contains("response_type=code");
        assertThat(query).contains("client_id=client-id");
        assertThat(query).contains("redirect_uri=http://localhost/callback");
        assertThat(query).contains("serviceProviderCode=FRIENDS");
        assertThat(query).contains("userLocale=ko_KR");
    }

    @Test
    void handle_successfullyFetchesUserInfo() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("code")).thenReturn("test-code");

        mockServer.enqueue(new MockResponse()
                .setBody("""
                        {
                          "accessToken": "access-token",
                          "tokenType": "bearer",
                          "expiresIn": "3600",
                          "refreshToken": "refresh-token"
                        }
                        """)
                .addHeader("Content-Type", "application/json"));

        mockServer.enqueue(new MockResponse()
                .setBody("""
                        {
                          "data": {
                            "member": {
                              "idNo": "id-123",
                              "name": "테스트",
                              "genderCode": "M",
                              "email": "test@test.com",
                              "mobile": "01012345678",
                              "birthday": "19900101"
                            }
                          }
                        }
                        """)
                .addHeader("Content-Type", "application/json"));

        OAuth2UserInfo userInfo = handler.handle(request);

        assertThat(userInfo.provider()).isEqualTo("payco");
        assertThat(userInfo.providerId()).isEqualTo("id-123");
        assertThat(userInfo.name()).isEqualTo("테스트");
        assertThat(userInfo.email()).isEqualTo("test@test.com");
    }

    @Test
    void handle_tokenEndpointReturnsNull_throwsException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("code")).thenReturn("test-code");

        mockServer.enqueue(new MockResponse()
                .setBody("")
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> handler.handle(request))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }
}
