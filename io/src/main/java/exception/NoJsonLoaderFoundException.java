package exception;

public class NoJsonLoaderFoundException extends RuntimeException {

    public NoJsonLoaderFoundException() {
        // none
    }

    public NoJsonLoaderFoundException(String message) {
        super(message);
    }
}
