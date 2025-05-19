package shop.ink3.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import shop.ink3.auth.dto.LoginResponse;
import shop.ink3.auth.exception.OAuth2AuthenticationSuccessRedirectException;
import shop.ink3.auth.exception.OAuth2SignupRedirectException;

@Slf4j
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        if (exception instanceof OAuth2AuthenticationSuccessRedirectException) {
            LoginResponse tokens = ((OAuth2AuthenticationSuccessRedirectException) exception).getTokens();
            long now = System.currentTimeMillis();
            ResponseCookie accessCookie = ResponseCookie.from("accessToken", tokens.accessToken().token())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofMillis(tokens.accessToken().expiresAt() - now))
                    .sameSite("Strict")
                    .build();
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.refreshToken().token())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofMillis(tokens.refreshToken().expiresAt() - now))
                    .sameSite("Strict")
                    .build();
            response.addCookie(new Cookie(accessCookie.getName(), accessCookie.getValue()));
            response.addCookie(new Cookie(refreshCookie.getName(), refreshCookie.getValue()));
            response.sendRedirect("/");
            log.info("response: {}", response);
        } else if (exception instanceof OAuth2SignupRedirectException) {
            response.sendRedirect("/oauth/register");
        } else {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("OAuth2 authentication failed: " + exception.getMessage());
        }
    }
}
