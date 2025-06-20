package shop.ink3.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.ink3.auth.dto.JwtToken;
import shop.ink3.auth.dto.UserType;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {
    JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() throws Exception {
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA")
                .generateKeyPair();

        jwtTokenProvider = new JwtTokenProvider(
                keyPair.getPrivate(),
                keyPair.getPublic()
        );

        Field accessField = JwtTokenProvider.class.getDeclaredField("accessTokenValidity");
        accessField.setAccessible(true);
        accessField.set(jwtTokenProvider, 1000L * 60 * 15); // 15분

        Field refreshSessionField = JwtTokenProvider.class.getDeclaredField("refreshTokenSessionValidity");
        refreshSessionField.setAccessible(true);
        refreshSessionField.set(jwtTokenProvider, 1000L * 60 * 60 * 24); // 1일

        Field refreshField = JwtTokenProvider.class.getDeclaredField("refreshTokenValidity");
        refreshField.setAccessible(true);
        refreshField.set(jwtTokenProvider, 1000L * 60 * 60 * 24 * 7); // 7일
    }

    @Test
    void generateAccessToken() {
        JwtToken jwtToken = jwtTokenProvider.generateAccessToken(1L, "test", UserType.USER);
        Claims claims = jwtTokenProvider.parseToken(jwtToken.token());
        assertThat(claims.getSubject()).isEqualTo("test");
        assertThat(claims.get("id", Long.class)).isEqualTo(1L);
        assertThat(claims.get("userType", String.class)).isEqualTo("USER");
        assertThat(claims.get("tokenType", String.class)).isEqualTo("access");
        assertThat(claims.getExpiration().getTime()).isCloseTo(jwtToken.expiresAt(), within(5000L));
    }

    @Test
    void generateRefreshToken() {
        JwtToken jwtToken = jwtTokenProvider.generateRefreshToken(1L, "test", UserType.USER, false);
        Claims claims = jwtTokenProvider.parseToken(jwtToken.token());
        assertThat(claims.getSubject()).isEqualTo("test");
        assertThat(claims.get("id", Long.class)).isEqualTo(1L);
        assertThat(claims.get("userType", String.class)).isEqualTo("USER");
        assertThat(claims.get("tokenType", String.class)).isEqualTo("refresh");
        assertThat(claims.get("rememberMe", Boolean.class)).isFalse();
        assertThat(claims.getExpiration().getTime()).isCloseTo(jwtToken.expiresAt(), within(5000L));
    }

    @Test
    void parseTokenWithInvalidToken() {
        String invalidToken = "invalid.invalid.invalid";
        assertThatThrownBy(() -> jwtTokenProvider.parseToken(invalidToken)).isInstanceOf(JwtException.class);
    }
}
