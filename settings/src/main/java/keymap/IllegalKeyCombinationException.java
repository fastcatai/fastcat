package keymap;

public class IllegalKeyCombinationException extends RuntimeException {

    public IllegalKeyCombinationException() {
    }

    public IllegalKeyCombinationException(String message) {
        super(message);
    }

    public IllegalKeyCombinationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalKeyCombinationException(Throwable cause) {
        super(cause);
    }
}
