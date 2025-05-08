package shop.ink3.auth.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("The refresh token is incorrect.");
    }
}
