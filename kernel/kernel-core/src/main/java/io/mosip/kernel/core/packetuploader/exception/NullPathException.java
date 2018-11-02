package io.mosip.kernel.core.packetuploader.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Exception to be thrown when Path is Null
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class NullPathException extends BaseUncheckedException {

	/**
	 * Constant id for serialization
	 */
	private static final long serialVersionUID = -2576609623548108679L;

	/**
	 * Constructor for this class
	 * 
	 * @param errorCode
	 * @param errorMessage
	 * @param cause
	 */
	public NullPathException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);
	}

}
