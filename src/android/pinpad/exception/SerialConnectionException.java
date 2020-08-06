package ve.com.megasoft.pinpad.exception;

/** Manejo de errores de conexión serial. */
public class SerialConnectionException extends Exception {

	/** serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor para SerialConnectionException.java.
	 * @param str Mensaje.
	 */
    public SerialConnectionException(String str) {
    	super(str);
    }

    /** Constructor para SerialConnectionException.java. */
    public SerialConnectionException() {
    	super();
    }
    
	/**
	 * Constructor para SerialConnectionException.java.
	 * @param cause Causa.
	 */
	public SerialConnectionException(Throwable cause) {
		super(cause);
	}
}




