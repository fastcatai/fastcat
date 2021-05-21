package exception;

public class NoJsonSuperframeLoaderAvailable extends RuntimeException {

    public NoJsonSuperframeLoaderAvailable() {
        // none
    }

    public NoJsonSuperframeLoaderAvailable(String message) {
        super(message);
    }
}
