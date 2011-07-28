package cucumber.runtime.transformers;

public class TransformationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3893851462286949513L;
	
	public TransformationException(Throwable t) {
		super(t);
	}
	
	public TransformationException(String message, Throwable t) {
		super (message, t);
	}
	
	public TransformationException(String message) {
		super (message);
	}
}
