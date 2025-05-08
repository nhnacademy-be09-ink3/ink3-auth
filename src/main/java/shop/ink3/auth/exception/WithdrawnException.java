package shop.ink3.auth.exception;

public class WithdrawnException extends RuntimeException {
    public WithdrawnException() {
        super("This account has been withdrawn.");
    }
}
