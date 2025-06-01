package shop.ink3.auth.oauth.handler;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import shop.ink3.auth.oauth.dto.OAuth2UserInfo;

public interface OAuth2ClientHandler {
    String getProvider();

    URI getAuthorizationUri();

    OAuth2UserInfo handle(HttpServletRequest request);
}
