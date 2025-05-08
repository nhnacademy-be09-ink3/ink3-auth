package shop.ink3.auth.exception;

public class DormantException extends RuntimeException {
    public DormantException() {
        super("This account is dormant.");
    }
}
