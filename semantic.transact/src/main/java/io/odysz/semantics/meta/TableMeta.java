package io.odysz.semantics.meta;

import java.util.HashMap;

import io.odysz.semantics.meta.ColMeta.coltype;

public class TableMeta {
	@SuppressWarnings("unused")
	private String conn;
	private HashMap<String, ColMeta> types;

	public TableMeta(String tbl, String ... conn) {
		types = new HashMap<String, ColMeta>();
		this.conn = conn != null && conn.length > 0 ? conn[0] : null;
	}

	public TableMeta col(String col, coltype t) {
		types.put(col, new ColMeta(t));
		return this;
	}
	
	public boolean isText(String col) {
		return types.containsKey(col) ? 
				types.get(col).isText()
				: true;
	}

	public TableMeta col(String col, String t, Integer len) {
		types.put(col, new ColMeta(t).len(len));
		return this;
	}

	public TableMeta col(String col, String type) {
		return col(col, type, 0);
	}

	public coltype coltype(String col) {
		return types == null && types.containsKey(col) ?
				coltype.text : types.get(col).type();
	}
}
