package shop.ink3.auth.repository;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import shop.ink3.auth.dto.UserRole;

@ExtendWith(SpringExtension.class)
class TokenRepositoryTest {

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    ValueOperations<String, String> valueOps;

    @InjectMocks
    TokenRepository tokenRepository;

    long accessTokenValidity = 1000L * 60 * 30;
    long refreshTokenValidity = 1000L * 60 * 60 * 24 * 7;

    @BeforeEach
    void setUp() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        Field accessField = TokenRepository.class.getDeclaredField("accessTokenValidity");
        Field refreshField = TokenRepository.class.getDeclaredField("refreshTokenValidity");
        accessField.setAccessible(true);
        refreshField.setAccessible(true);
        accessField.set(tokenRepository, accessTokenValidity);
        refreshField.set(tokenRepository, refreshTokenValidity);
    }

    @Test
    void getRefreshTokenForAdmin() {
        when(valueOps.get("refresh:admin:1")).thenReturn("adminToken");

        String result = tokenRepository.getRefreshToken(1L, UserRole.ADMIN);

        Assertions.assertEquals("adminToken", result);
    }

    @Test
    void getRefreshTokenForUser() {
        when(valueOps.get("refresh:user:1")).thenReturn("userToken");

        String result = tokenRepository.getRefreshToken(1L, UserRole.USER);

        Assertions.assertEquals("userToken", result);
    }

    @Test
    void saveAdminRefreshToken() {
        tokenRepository.saveRefreshToken(1L, UserRole.ADMIN, "adminToken");
        verify(valueOps).set("refresh:admin:1", "adminToken", refreshTokenValidity, TimeUnit.MILLISECONDS);
    }

    @Test
    void saveUserRefreshToken() {
        tokenRepository.saveRefreshToken(1L, UserRole.USER, "userToken");
        verify(valueOps).set("refresh:user:1", "userToken", refreshTokenValidity, TimeUnit.MILLISECONDS);
    }

    @Test
    void saveAccessTokenToBlackList() {
        tokenRepository.saveAccessTokenToBlackList("accessToken");
        verify(valueOps).set("blacklist:accessToken", "blocked", accessTokenValidity, TimeUnit.MILLISECONDS);
    }

    @Test
    void deleteAdminRefreshToken() {
        tokenRepository.deleteRefreshToken(1L, UserRole.ADMIN);
        verify(redisTemplate).delete("refresh:admin:1");
    }

    @Test
    void deleteUserRefreshToken() {
        tokenRepository.deleteRefreshToken(1L, UserRole.USER);
        verify(redisTemplate).delete("refresh:user:1");
    }
}
