package io.odysz.semantics.meta;

import java.util.HashMap;

import io.odysz.semantics.meta.ColMeta.coltype;

public class TableMeta {
	private HashMap<String, ColMeta> types;

	public String tbl;
	public String pk;

	protected String conn;
	public String conn() { return conn; }
	public TableMeta conn(String id) {
		conn = id;
		return this;
	}

	public TableMeta(String tbl, String ... conn) {
		this.tbl = tbl;
		types = new HashMap<String, ColMeta>();
		this.conn = conn != null && conn.length > 0 ? conn[0] : null;
	}

	public TableMeta col(String col, coltype t) {
		types.put(col, new ColMeta(t));
		return this;
	}
	
	public boolean isQuoted(String col) {
		return types.containsKey(col) ? 
				types.get(col).isQuoted()
				: true;
	}

	public TableMeta col(String coln, String t, int len) {
		ColMeta cm = new ColMeta(t);
		types.put(coln, cm.tlen(len));
		return this;
	}

	public TableMeta col(String col, String type) {
		return col(col, type, 0);
	}

	public coltype coltype(String col) {
		return types != null && types.containsKey(col) ?
				types.get(col).type() : coltype.text;
	}
}
