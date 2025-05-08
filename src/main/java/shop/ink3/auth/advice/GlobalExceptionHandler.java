package shop.ink3.auth.advice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shop.ink3.auth.client.dto.CommonResponse;
import shop.ink3.auth.exception.DormantException;
import shop.ink3.auth.exception.InvalidPasswordException;
import shop.ink3.auth.exception.InvalidRefreshTokenException;
import shop.ink3.auth.exception.UserNotFoundException;
import shop.ink3.auth.exception.WithdrawnException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<CommonResponse<Void>> handleExpiredJwtException(ExpiredJwtException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.error(HttpStatus.UNAUTHORIZED, "JWT token has expired."));
    }

    @ExceptionHandler({MalformedJwtException.class, SignatureException.class, IllegalArgumentException.class})
    public ResponseEntity<CommonResponse<Void>> handleInvalidJwtException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.error(HttpStatus.BAD_REQUEST, "Invalid JWT token."));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleUsernameNotFound(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CommonResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<CommonResponse<Void>> handleInvalidPassword(InvalidPasswordException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.error(HttpStatus.UNAUTHORIZED, e.getMessage()));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<CommonResponse<Void>> handleInvalidRefresh(InvalidRefreshTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.error(HttpStatus.UNAUTHORIZED, "Invalid refresh token."));
    }

    @ExceptionHandler(DormantException.class)
    public ResponseEntity<CommonResponse<Void>> handleDormantException(DormantException e) {
        return ResponseEntity.status(HttpStatus.LOCKED).body(CommonResponse.error(HttpStatus.LOCKED, e.getMessage()));
    }

    @ExceptionHandler(WithdrawnException.class)
    public ResponseEntity<CommonResponse<Void>> handleWithdrawnException(WithdrawnException e) {
        return ResponseEntity.status(HttpStatus.GONE).body(CommonResponse.error(HttpStatus.GONE, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred."));
    }
}

