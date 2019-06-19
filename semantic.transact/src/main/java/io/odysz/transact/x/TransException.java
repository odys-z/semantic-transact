package io.odysz.transact.x;

import io.odysz.common.LangExt;

public class TransException extends Exception {
	private static final long serialVersionUID = 1L;


	public TransException(String format, Object... args) {
		super(LangExt.isblank(format) ? null : String.format(format, args));
	}
	
}
