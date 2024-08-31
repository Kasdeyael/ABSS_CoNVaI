package exception;

import java.io.IOException;


public class IncompatibleParameterException extends IOException {


	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor for IncompatibleParameterException.
	 * @param msg the specific message for the error.
	 */
	public IncompatibleParameterException(String msg) {
		super(msg);
		
	}
	
}
