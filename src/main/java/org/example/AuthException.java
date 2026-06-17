package org.example;

/**
 * [AuthException.java]
 * Checked exception describing an authentication or account-related failure in a
 * way the UI can present to the user. Each instance carries a {@link Kind} that
 * the login/sign-up screens map to an inline, specific error message.
 *
 * @author Ian Leung
 * @version 3.0
 */
public class AuthException extends Exception {

    /**
     * Kind
     * The category of authentication failure. The UI uses this to decide what
     * message to show and (sometimes) which field to highlight.
     */
    public enum Kind {
        INVALID_CREDENTIALS,
        EMAIL_NOT_CONFIRMED,
        EMAIL_EXISTS,
        WEAK_PASSWORD,
        INVALID_EMAIL,
        VALIDATION,
        NETWORK,
        SERVER,
        UNKNOWN
    }

    private final Kind kind;
    private final int httpStatus;
    private final String userMessage;

    /**
     * Constructor for AuthException.
     *
     * @param kind        The category of failure.
     * @param httpStatus  The HTTP status code that triggered this, or 0 if not applicable.
     * @param userMessage A short, user-facing message describing the failure.
     * @param cause       The underlying cause, or null.
     */
    public AuthException(Kind kind, int httpStatus, String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.kind = kind;
        this.httpStatus = httpStatus;
        this.userMessage = userMessage;
    }

    /**
     * getKind
     * Returns the category of this failure.
     *
     * @return The failure kind.
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * getUserMessage
     * Returns a short, user-facing message describing the failure.
     *
     * @return The user-facing message.
     */
    public String getUserMessage() {
        return userMessage;
    }

    /**
     * getHttpStatus
     * Returns the HTTP status code associated with this failure, or 0 if none.
     *
     * @return The HTTP status code.
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * userMessageFor
     * Returns the canonical user-facing message for a given failure kind.
     *
     * @param kind The failure kind.
     * @return The canonical message string for that kind.
     */
    public static String userMessageFor(Kind kind) {
        if (kind == null) {
            return "Something went wrong. Please try again.";
        }
        switch (kind) {
            case INVALID_CREDENTIALS:
                return "Incorrect email or password.";
            case EMAIL_NOT_CONFIRMED:
                return "Please confirm your email before logging in.";
            case EMAIL_EXISTS:
                return "An account with that email already exists.";
            case WEAK_PASSWORD:
                return "Password must be at least 6 characters.";
            case INVALID_EMAIL:
                return "Please enter a valid email address.";
            case VALIDATION:
                return "Please check the form and try again.";
            case NETWORK:
                return "Cannot reach the server. Check your internet connection.";
            case SERVER:
                return "Server error. Please try again later.";
            case UNKNOWN:
            default:
                return "Something went wrong. Please try again.";
        }
    }
}
