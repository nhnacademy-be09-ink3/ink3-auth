package shop.ink3.auth.oauth.handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class OAuth2ClientHandlerManager {
    private final Map<String, OAuth2ClientHandler> handlerMap = new ConcurrentHashMap<>();

    public OAuth2ClientHandlerManager(List<OAuth2ClientHandler> handlers) {
        handlers.forEach(handler -> handlerMap.put(handler.getProvider(), handler));
    }

    public OAuth2ClientHandler getHandler(String provider) {
        return handlerMap.get(provider);
    }
}
