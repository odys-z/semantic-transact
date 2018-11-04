package io.odysz.semantics.sql.parts;

public class Logic {
	public enum op {eq, lt, le, gt, ge, like, not, in, isnull};
	
	//public static final String eq = "=";

	public static op op(String logic) {
		// TODO Auto-generated method stub
		return op.eq;
	}

}
