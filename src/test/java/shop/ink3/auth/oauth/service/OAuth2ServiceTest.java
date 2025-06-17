package shop.ink3.auth.oauth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import shop.ink3.auth.client.dto.CommonResponse;
import shop.ink3.auth.client.user.UserClient;
import shop.ink3.auth.dto.JwtToken;
import shop.ink3.auth.dto.LoginResponse;
import shop.ink3.auth.dto.SocialUserResponse;
import shop.ink3.auth.dto.UserType;
import shop.ink3.auth.exception.UserNotFoundException;
import shop.ink3.auth.oauth.dto.OAuth2UserInfo;
import shop.ink3.auth.oauth.exception.OAuth2ProviderNotFoundException;
import shop.ink3.auth.oauth.exception.OAuth2UserNotFoundException;
import shop.ink3.auth.oauth.handler.OAuth2ClientHandler;
import shop.ink3.auth.service.TokenService;

@ExtendWith(MockitoExtension.class)
class OAuth2ServiceTest {
    @Mock
    UserClient userClient;
    @Mock
    TokenService tokenService;
    @Mock
    OAuth2ClientHandler handler;

    OAuth2Service oAuth2Service;

    @BeforeEach
    void setUp() {
        when(handler.getProvider()).thenReturn("payco");
        oAuth2Service = new OAuth2Service(List.of(handler), userClient, tokenService);
    }

    @Test
    void getAuthorizationUri() {
        URI uri = URI.create("http://auth");
        when(handler.getAuthorizationUri()).thenReturn(uri);

        URI result = oAuth2Service.getAuthorizationUri("payco");

        assertThat(result).isEqualTo(uri);
    }

    @Test
    void getAuthorizationUriWithUnknownProvider() {
        OAuth2Service service = new OAuth2Service(List.of(), userClient, tokenService);

        assertThatThrownBy(() -> service.getAuthorizationUri("unknown"))
                .isInstanceOf(OAuth2ProviderNotFoundException.class);
    }

    @Test
    void processOAuth2Callback() {
        HttpServletRequest request = new MockHttpServletRequest();
        OAuth2UserInfo info = new OAuth2UserInfo("payco", "123", "name", null, null, null, null);
        when(handler.handle(request)).thenReturn(info);
        SocialUserResponse user = new SocialUserResponse("payco", "123", 1L, "username");
        when(userClient.getSocialUser("payco", "123")).thenReturn(CommonResponse.success(user));
        LoginResponse tokens = new LoginResponse(new JwtToken("a", 1L), new JwtToken("r", 2L));
        when(tokenService.issueTokens(1L, "username", UserType.USER, true)).thenReturn(tokens);

        LoginResponse result = oAuth2Service.processOAuth2Callback("payco", request);

        assertThat(result).isEqualTo(tokens);
    }

    @Test
    void processOAuth2CallbackWithUnknownProvider() {
        HttpServletRequest request = new MockHttpServletRequest();
        OAuth2Service service = new OAuth2Service(List.of(handler), userClient, tokenService);
        assertThatThrownBy(() -> service.processOAuth2Callback("unknown", request))
                .isInstanceOf(OAuth2ProviderNotFoundException.class);
    }

    @Test
    void processOAuth2CallbackWithUserNotFound() {
        HttpServletRequest request = new MockHttpServletRequest();
        OAuth2UserInfo info = new OAuth2UserInfo("payco", "123", "name", null, null, null, null);
        when(handler.handle(request)).thenReturn(info);
        when(userClient.getSocialUser("payco", "123")).thenThrow(new UserNotFoundException());

        assertThatThrownBy(() -> oAuth2Service.processOAuth2Callback("payco", request))
                .isInstanceOf(OAuth2UserNotFoundException.class)
                .hasMessageContaining("123");
    }
}
