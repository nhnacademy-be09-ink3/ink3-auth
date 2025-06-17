package shop.ink3.auth.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import shop.ink3.auth.dto.JwtToken;

class CookieUtilTest {
    @Test
    void setTokenCookiesAddsAccessAndRefreshCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        long now = System.currentTimeMillis();
        JwtToken accessToken = new JwtToken("access", now + 60_000);
        JwtToken refreshToken = new JwtToken("refresh", now + 120_000);

        CookieUtil.setTokenCookies(response, accessToken, refreshToken);

        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).hasSize(2);
        String accessCookie = response.getHeaders(HttpHeaders.SET_COOKIE).get(0);
        String refreshCookie = response.getHeaders(HttpHeaders.SET_COOKIE).get(1);

        assertThat(accessCookie)
                .contains("accessToken=access")
                .contains("HttpOnly")
                .contains("Secure")
                .contains("Path=/")
                .contains("SameSite=Lax");

        assertThat(refreshCookie).contains("refreshToken=refresh");
    }
}
