package shop.ink3.auth.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import shop.ink3.auth.client.UserClient;
import shop.ink3.auth.client.dto.AuthResponse;
import shop.ink3.auth.dto.LoginResponse;
import shop.ink3.auth.dto.UserRole;
import shop.ink3.auth.exception.OAuth2AuthenticationSuccessRedirectException;
import shop.ink3.auth.exception.OAuth2SignupRedirectException;
import shop.ink3.auth.exception.UserNotFoundException;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserClient userClient;
    private final TokenService tokenService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = new DefaultOAuth2UserService().loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oauthUser.getName();

        try {
            AuthResponse user = userClient.getSocialUser(provider, providerId).data();
            LoginResponse response = tokenService.issueTokens(user, UserRole.USER);
            throw new OAuth2AuthenticationSuccessRedirectException(response);
        } catch (UserNotFoundException e) {
            Map<String, Object> attributes = oauthUser.getAttributes();
            log.info("attributes: {}", attributes);
            throw new OAuth2SignupRedirectException(attributes);
        }
    }
}
