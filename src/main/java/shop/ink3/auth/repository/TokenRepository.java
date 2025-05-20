package shop.ink3.auth.repository;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import shop.ink3.auth.dto.UserRole;

@RequiredArgsConstructor
@Repository
public class TokenRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    private static final String USER_REFRESH_TOKEN_KEY = "refresh:user:";
    private static final String ADMIN_REFRESH_TOKEN_KEY = "refresh:admin:";
    private static final String BLACKLIST_KEY = "blacklist:";

    public String getRefreshToken(long id, UserRole userRole) {
        if (userRole == UserRole.ADMIN) {
            return redisTemplate.opsForValue().get(ADMIN_REFRESH_TOKEN_KEY + id);
        }
        return redisTemplate.opsForValue().get(USER_REFRESH_TOKEN_KEY + id);
    }

    public void saveRefreshToken(long id, UserRole userRole, String refreshToken) {
        if (userRole == UserRole.ADMIN) {
            redisTemplate.opsForValue().set(
                    ADMIN_REFRESH_TOKEN_KEY + id,
                    refreshToken,
                    refreshTokenValidity,
                    TimeUnit.MILLISECONDS
            );
        } else {
            redisTemplate.opsForValue().set(
                    USER_REFRESH_TOKEN_KEY + id,
                    refreshToken,
                    refreshTokenValidity,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public void saveAccessTokenToBlackList(String accessToken) {
        redisTemplate.opsForValue()
                .set(BLACKLIST_KEY + accessToken, "blocked", accessTokenValidity, TimeUnit.MILLISECONDS);
    }

    public void deleteRefreshToken(long id, UserRole userRole) {
        if (userRole == UserRole.ADMIN) {
            redisTemplate.delete(ADMIN_REFRESH_TOKEN_KEY + id);
        } else {
            redisTemplate.delete(USER_REFRESH_TOKEN_KEY + id);
        }
    }
}
