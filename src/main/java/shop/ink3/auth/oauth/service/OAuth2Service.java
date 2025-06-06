package shop.ink3.auth.oauth.service;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import shop.ink3.auth.client.user.UserClient;
import shop.ink3.auth.dto.LoginResponse;
import shop.ink3.auth.dto.SocialUserResponse;
import shop.ink3.auth.dto.UserType;
import shop.ink3.auth.exception.UserNotFoundException;
import shop.ink3.auth.oauth.dto.OAuth2UserInfo;
import shop.ink3.auth.oauth.exception.OAuth2ProviderNotFoundException;
import shop.ink3.auth.oauth.exception.OAuth2UserNotFoundException;
import shop.ink3.auth.oauth.handler.OAuth2ClientHandler;
import shop.ink3.auth.service.TokenService;

@Service
public class OAuth2Service {
    private final Map<String, OAuth2ClientHandler> handlerMap = new ConcurrentHashMap<>();
    private final UserClient userClient;
    private final TokenService tokenService;

    public OAuth2Service(List<OAuth2ClientHandler> handlers, UserClient userClient, TokenService tokenService) {
        handlers.forEach(handler -> handlerMap.put(handler.getProvider(), handler));
        this.userClient = userClient;
        this.tokenService = tokenService;
    }

    public URI getAuthorizationUri(String provider) {
        if (!handlerMap.containsKey(provider)) {
            throw new OAuth2ProviderNotFoundException(provider);
        }
        return handlerMap.get(provider).getAuthorizationUri();
    }

    public LoginResponse processOAuth2Callback(String provider, HttpServletRequest request) {
        if (!handlerMap.containsKey(provider)) {
            throw new OAuth2ProviderNotFoundException(provider);
        }
        OAuth2ClientHandler handler = handlerMap.get(provider);
        OAuth2UserInfo userInfo = handler.handle(request);
        try {
            SocialUserResponse user = userClient.getSocialUser(userInfo.provider(), userInfo.providerId()).data();
            return tokenService.issueTokens(user.id(), user.username(), UserType.USER);
        } catch (UserNotFoundException e) {
            throw new OAuth2UserNotFoundException(userInfo);
        }
    }
}
