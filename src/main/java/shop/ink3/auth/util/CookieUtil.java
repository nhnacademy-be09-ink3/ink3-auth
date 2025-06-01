package shop.ink3.auth.util;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import shop.ink3.auth.dto.JwtToken;

public class CookieUtil {
    private CookieUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void setTokenCookies(
            HttpServletResponse response,
            JwtToken accessToken,
            JwtToken refreshToken
    ) {
        long now = System.currentTimeMillis();

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken.token())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMillis(accessToken.expiresAt() - now))
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken.token())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMillis(refreshToken.expiresAt() - now))
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}
