package ninja.oakley.backupbuddy.encryption;

public class KeyAlreadyExistsException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -7818568077488899050L;

    public KeyAlreadyExistsException() {
        super();
    }

    public KeyAlreadyExistsException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public KeyAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyAlreadyExistsException(String message) {
        super(message);
    }

    public KeyAlreadyExistsException(Throwable cause) {
        super(cause);
    }

}
