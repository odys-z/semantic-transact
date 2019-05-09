package io.odysz.semantics.meta;

import java.util.HashMap;

import io.odysz.semantics.meta.ColType.coltype;

public class TableMeta {
	@SuppressWarnings("unused")
	private String conn;
	private HashMap<String, ColType> types;

	public TableMeta(String tbl, String ... conn) {
		types = new HashMap<String, ColType>();
		this.conn = conn != null && conn.length > 0 ? conn[0] : null;
	}

	public TableMeta col(String col, coltype t) {
		types.put(col, new ColType(t));
		return this;
	}
	
	public boolean isText(String col) {
		return types.containsKey(col) ? 
				types.get(col).isText()
				: true;
	}
}
