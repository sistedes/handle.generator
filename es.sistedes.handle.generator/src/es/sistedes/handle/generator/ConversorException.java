package es.sistedes.handle.generator;

/**
 * Signals that an {@link Exception} of some sort has occurred during conversion
 *
 * @author agomez
 */
public class ConversorException extends Exception {

	static final long serialVersionUID = 1L;

	/**
	 * Constructs a {@code ConversorException} with {@code null} as its error
	 * detail message.
	 */
	public ConversorException() {
		super();
	}

	/**
	 * Constructs a {@code ConversorException} with the specified detail
	 * message.
	 *
	 * @param message
	 *            The detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method)
	 */
	public ConversorException(String message) {
		super(message);
	}

	/**
	 * Constructs a {@code ConversorException} with the specified detail message
	 * and cause.
	 *
	 * <p>
	 * Note that the detail message associated with {@code cause} is <i>not</i>
	 * automatically incorporated into this exception's detail message.
	 *
	 * @param message
	 *            The detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method)
	 *
	 * @param cause
	 *            The cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A null value is permitted, and
	 *            indicates that the cause is nonexistent or unknown.)
	 *
	 */
	public ConversorException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a {@code ConversorException} with the specified cause and a
	 * detail message of {@code (cause==null ? null : cause.toString())} (which
	 * typically contains the class and detail message of {@code cause}).
	 * 
	 * @param cause
	 *            The cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A null value is permitted, and
	 *            indicates that the cause is nonexistent or unknown.)
	 *
	 */
	public ConversorException(Throwable cause) {
		super(cause);
	}
}
