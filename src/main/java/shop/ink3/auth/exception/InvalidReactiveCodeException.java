package shop.ink3.auth.exception;

public class InvalidReactiveCodeException extends RuntimeException {
    public InvalidReactiveCodeException() {
        super("Invalid or expired reactivation code.");
    }
}
