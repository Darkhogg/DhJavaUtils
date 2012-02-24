package es.darkhogg.util.concurrent;

/**
 * An exception thrown by {@link Pipe} when there are no elements and waiting time is over.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class PipeTimeoutException extends RuntimeException {
	
	/** Serial version ID */
	private static final long serialVersionUID = -3461874225933988807L;
	
	/**
	 * @param message
	 *            Message for this exception
	 */
	public PipeTimeoutException ( final String string ) {
		super( string );
	}
	
}
