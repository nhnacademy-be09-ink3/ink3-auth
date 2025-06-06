package shop.ink3.auth.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;
import shop.ink3.auth.exception.DormantException;
import shop.ink3.auth.exception.UserNotFoundException;
import shop.ink3.auth.exception.WithdrawnException;

@Component
public class FeignClientErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 410 -> new WithdrawnException();
            case 423 -> new DormantException();
            case 404 -> new UserNotFoundException();
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }
}
