package shop.ink3.auth.repository;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ReactiveCodeRepository {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REACTIVE_CODE_KEY = "reactive_code:";

    public String getReactiveCode(String loginId) {
        return redisTemplate.opsForValue().get(REACTIVE_CODE_KEY + loginId);
    }

    public void saveReactiveCode(String loginId, String reactiveCode) {
        redisTemplate.opsForValue().set(REACTIVE_CODE_KEY + loginId, reactiveCode, 5, TimeUnit.MINUTES);
    }

    public void deleteReactiveCode(String loginId) {
        redisTemplate.delete(REACTIVE_CODE_KEY + loginId);
    }
}
