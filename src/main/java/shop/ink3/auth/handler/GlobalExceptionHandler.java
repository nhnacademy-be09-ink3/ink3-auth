package shop.ink3.auth.handler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.UriComponentsBuilder;
import shop.ink3.auth.client.dto.CommonResponse;
import shop.ink3.auth.exception.DormantException;
import shop.ink3.auth.exception.InvalidPasswordException;
import shop.ink3.auth.exception.InvalidReactiveCodeException;
import shop.ink3.auth.exception.InvalidRefreshTokenException;
import shop.ink3.auth.exception.InvalidUserStateException;
import shop.ink3.auth.exception.UserNotFoundException;
import shop.ink3.auth.exception.WithdrawnException;
import shop.ink3.auth.oauth.exception.OAuth2AuthenticationException;
import shop.ink3.auth.oauth.exception.OAuth2ProviderNotFoundException;
import shop.ink3.auth.oauth.exception.OAuth2UserNotFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @Value("${front.url}")
    private String frontUrl;

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<CommonResponse<Void>> handleExpiredJwtException(ExpiredJwtException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.error(HttpStatus.UNAUTHORIZED, "JWT token has expired."));
    }

    @ExceptionHandler({JwtException.class, IllegalArgumentException.class})
    public ResponseEntity<CommonResponse<Void>> handleInvalidJwtException(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.error(HttpStatus.UNAUTHORIZED, "Invalid JWT token."));
    }

    @ExceptionHandler({
            InvalidPasswordException.class,
            InvalidRefreshTokenException.class,
            OAuth2AuthenticationException.class
    })
    public ResponseEntity<CommonResponse<Void>> handleUnauthorizedException(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.error(HttpStatus.UNAUTHORIZED, e.getMessage()));
    }

    @ExceptionHandler({UserNotFoundException.class, OAuth2ProviderNotFoundException.class})
    public ResponseEntity<CommonResponse<Void>> handleUsernameNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CommonResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(OAuth2UserNotFoundException.class)
    public ResponseEntity<Void> handleOAuth2UserNotFoundException(OAuth2UserNotFoundException e) {
        URI uri = UriComponentsBuilder.fromUriString(frontUrl + "/register")
                .queryParam("provider", e.getUserInfo().provider())
                .queryParam("providerId", e.getUserInfo().providerId())
                .queryParam("name", e.getUserInfo().name())
                .queryParam("gender", e.getUserInfo().gender())
                .queryParam("email", e.getUserInfo().email())
                .queryParam("mobile", e.getUserInfo().mobile())
                .queryParam("birthday", e.getUserInfo().birthday())
                .build().toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(uri).build();
    }

    @ExceptionHandler(DormantException.class)
    public ResponseEntity<CommonResponse<Void>> handleDormantException(DormantException e) {
        return ResponseEntity.status(HttpStatus.LOCKED).body(CommonResponse.error(HttpStatus.LOCKED, e.getMessage()));
    }

    @ExceptionHandler(WithdrawnException.class)
    public ResponseEntity<CommonResponse<Void>> handleWithdrawnException(WithdrawnException e) {
        return ResponseEntity.status(HttpStatus.GONE).body(CommonResponse.error(HttpStatus.GONE, e.getMessage()));
    }

    @ExceptionHandler({InvalidUserStateException.class, InvalidReactiveCodeException.class})
    public ResponseEntity<CommonResponse<Void>> handleBadRequestException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.error(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Map<String, String>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.error(HttpStatus.BAD_REQUEST, "Invalid input values.", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        log.error("Unexpected server error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred."));
    }
}
