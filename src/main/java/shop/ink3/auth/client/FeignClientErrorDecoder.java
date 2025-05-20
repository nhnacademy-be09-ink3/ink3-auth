package shop.ink3.auth.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shop.ink3.auth.exception.DormantException;
import shop.ink3.auth.exception.UserNotFoundException;
import shop.ink3.auth.exception.WithdrawnException;

@RequiredArgsConstructor
@Component
public class FeignClientErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 410 -> throw new WithdrawnException();
            case 423 -> throw new DormantException();
            case 404 -> throw new UserNotFoundException();
            default -> {
                return defaultErrorDecoder.decode(methodKey, response);
            }
        }
    }
}
