package shop.ink3.auth.service;

import feign.FeignException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import shop.ink3.auth.client.UserClient;
import shop.ink3.auth.client.dto.AuthResponse;
import shop.ink3.auth.client.dto.CommonResponse;
import shop.ink3.auth.dto.UserType;
import shop.ink3.auth.jwt.JwtTokenProvider;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserClient userClient;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String socialUserId = (String) attributes.get("id");

        try {
            CommonResponse<AuthResponse> response = userClient.getSocialUser(provider, socialUserId);
            String accessToken = jwtTokenProvider.generateAccessToken(
                    response.data().id(),
                    response.data().username(),
                    UserType.USER.name()
            );
            String refreshToken = jwtTokenProvider.generateRefreshToken(response.data().username());
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("redirect_with_token"),
                    "redirect:https://ink3.shop/oauth-success?accessToken=%s&refreshToken=%s".formatted(accessToken, refreshToken)
            );
        } catch (FeignException.NotFound e) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("user_not_found"),
                    "redirect:/signup?provider=%s&socialUserId=%s".formatted(provider, socialUserId)
            );
        }
    }
}

