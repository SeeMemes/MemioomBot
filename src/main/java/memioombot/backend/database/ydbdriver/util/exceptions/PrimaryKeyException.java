package memioombot.backend.database.ydbdriver.util.exceptions;

public class PrimaryKeyException extends RuntimeException{
    public PrimaryKeyException() {
        super();
    }

    public PrimaryKeyException(String message) {
        super(message);
    }

    public PrimaryKeyException(String message, Throwable cause) {
            super(message, cause);
    }

    public PrimaryKeyException(Throwable cause) {
        super(cause);
    }
}
