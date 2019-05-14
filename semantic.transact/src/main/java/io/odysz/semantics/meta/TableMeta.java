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
	
	public boolean isQuoted(String col) {
		return types.containsKey(col) ? 
				types.get(col).isQuoted()
				: true;
	}

	public TableMeta col(String coln, String t, int len) {
		ColMeta cm = new ColMeta(t);
//		// weird maven behavior
//		Utils.warn("weird weird weird  %s %s %s...............................", coln, t, len);
//		if (types == null)
//			// strange
//			Utils.warn("That's so strange ...............................");
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
