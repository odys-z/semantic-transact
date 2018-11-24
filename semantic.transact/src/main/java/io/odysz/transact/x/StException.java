package io.odysz.transact.x;

public class StException extends Exception {
	private static final long serialVersionUID = 1L;


	public StException(String format, Object... args) {
		super(String.format(format, args));
	}
	
}
