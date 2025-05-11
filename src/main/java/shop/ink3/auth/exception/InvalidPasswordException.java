package shop.ink3.auth.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("The password is incorrect.");
    }
}
