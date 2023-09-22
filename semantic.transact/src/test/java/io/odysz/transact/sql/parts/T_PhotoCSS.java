package io.odysz.transact.sql.parts;

public class T_PhotoCSS extends AnDbField {
	int size[];
	
	public T_PhotoCSS(int w, int h) {
		size = new int[] {w, h};
	}
}
