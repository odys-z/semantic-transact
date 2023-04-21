package io.odysz.semantics.meta;

public class T_SyntityMeta extends TableMeta {
	
	final String forSubClass;

	public T_SyntityMeta(String tbl, String... conn) {
		super(tbl, conn);

		forSubClass = "forSubClass";
	}

}
