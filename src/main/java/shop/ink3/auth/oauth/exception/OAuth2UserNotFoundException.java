package shop.ink3.auth.oauth.exception;

import lombok.Getter;
import shop.ink3.auth.oauth.dto.OAuth2UserInfo;

@Getter
public class OAuth2UserNotFoundException extends RuntimeException {
    private final OAuth2UserInfo userInfo;

    public OAuth2UserNotFoundException(OAuth2UserInfo userInfo) {
        super("OAuth2 user not found. Provider Id: " + userInfo.providerId());
        this.userInfo = userInfo;
    }
}
