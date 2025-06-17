package shop.ink3.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ReactiveCodeRepositoryTest {
    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    ValueOperations<String, String> valueOps;

    @InjectMocks
    ReactiveCodeRepository reactiveCodeRepository;

    @Test
    void getReactiveCode() {
        String loginId = "testUser";
        String expectedCode = "abc123";
        String redisKey = "reactive_code:" + loginId;

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(redisKey)).thenReturn(expectedCode);

        String result = reactiveCodeRepository.getReactiveCode(loginId);

        assertThat(result).isEqualTo(expectedCode);
    }

    @Test
    void saveReactiveCode() {
        String loginId = "testUser";
        String code = "abc123";
        String redisKey = "reactive_code:" + loginId;

        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        reactiveCodeRepository.saveReactiveCode(loginId, code);

        verify(valueOps, times(1)).set(redisKey, code, 5, TimeUnit.MINUTES);
    }

    @Test
    void deleteReactiveCode() {
        String loginId = "testUser";
        String redisKey = "reactive_code:" + loginId;

        reactiveCodeRepository.deleteReactiveCode(loginId);

        verify(redisTemplate, times(1)).delete(redisKey);
    }
}
