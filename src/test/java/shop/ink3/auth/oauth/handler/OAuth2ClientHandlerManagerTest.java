package shop.ink3.auth.oauth.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OAuth2ClientHandlerManagerTest {
    @Test
    void getHandlerReturnsMatchingProvider() {
        OAuth2ClientHandler handler1 = Mockito.mock(OAuth2ClientHandler.class);
        OAuth2ClientHandler handler2 = Mockito.mock(OAuth2ClientHandler.class);
        Mockito.when(handler1.getProvider()).thenReturn("payco");
        Mockito.when(handler2.getProvider()).thenReturn("kakao");

        OAuth2ClientHandlerManager manager = new OAuth2ClientHandlerManager(List.of(handler1, handler2));

        assertThat(manager.getHandler("payco")).isEqualTo(handler1);
        assertThat(manager.getHandler("kakao")).isEqualTo(handler2);
        assertThat(manager.getHandler("naver")).isNull();
    }
}
