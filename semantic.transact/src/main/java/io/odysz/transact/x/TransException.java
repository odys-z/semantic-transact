package io.odysz.transact.x;

public class TransException extends Exception {
	private static final long serialVersionUID = 1L;


	public TransException(String format, Object... args) {
		super(String.format(format, args));
	}
	
}
