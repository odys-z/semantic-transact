package io.odysz.transact.x;

import io.odysz.common.LangExt;

public class TransException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public TransException(String format, Object... args) {
		super(LangExt.isblank(format) ? null
			: args != null && args.length > 0 ?
					String.format(format, args) : format);
	}
	
	@Override
	public String getMessage() {
		String s = super.getMessage();
		return LangExt.isblank(s)
			? super.getCause() == null
			? "" : super.getCause().getMessage() : s;
	}
}
