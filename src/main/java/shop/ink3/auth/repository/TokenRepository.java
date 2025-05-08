package shop.ink3.auth.repository;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import shop.ink3.auth.dto.UserType;

@RequiredArgsConstructor
@Repository
public class TokenRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    public String getRefreshToken(long id, UserType userType) {
        if (userType == UserType.ADMIN) {
            return redisTemplate.opsForValue().get("refresh:admin:" + id);
        }
        return redisTemplate.opsForValue().get("refresh:user:" + id);
    }

    public void saveRefreshToken(long id, UserType userType, String refreshToken) {
        if (userType == UserType.ADMIN) {
            redisTemplate.opsForValue().set("refresh:admin:" + id, refreshToken, refreshTokenValidity, TimeUnit.MILLISECONDS);
        } else {
            redisTemplate.opsForValue().set("refresh:user:" + id, refreshToken, refreshTokenValidity, TimeUnit.MILLISECONDS);
        }
    }

    public void saveAccessTokenToBlackList(String accessToken) {
        redisTemplate.opsForValue().set("blacklist:" + accessToken, "blocked", accessTokenValidity, TimeUnit.MILLISECONDS);
    }

    public void deleteRefreshToken(long id, UserType userType) {
        if (userType == UserType.ADMIN) {
            redisTemplate.delete("refresh:admin:" + id);
        } else {
            redisTemplate.delete("refresh:user:" + id);
        }
    }
}
