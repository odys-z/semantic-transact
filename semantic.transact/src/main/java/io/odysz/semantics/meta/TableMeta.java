package io.odysz.semantics.meta;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;

import io.odysz.common.Utils;
import io.odysz.semantics.meta.ColMeta.coltype;
import io.odysz.transact.x.TransException;

import static io.odysz.common.LangExt.isNull;

public class TableMeta {

	protected static String sqlite;
	/**
	 * A helper for test on sqlite.
	 * @since 1.5.0
	 */
	public static String ddlSqlite () {
		try {
			return Utils.loadTxt(sqlite);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private HashMap<String, ColMeta> types;
	/**
	 * Is the column types already loaded from DB?
	 * @since 1.5.0
	 * @return true if there are some types
	 */
	public boolean typesInited() {
		return types != null && types.size() > 0;
	}

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
		// this.conn = conn != null && conn.length > 0 ? conn[0] : null;
		this.conn = isNull(conn) ? null : conn[0];
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
				types.get(col).type() : null; //coltype.text;
	}

	/**
	 * Clone information form DB, and check subclass' fields with DB fields.
	 * This method uses shallow copy - coping references.
	 * 
	 * @param from
	 * @return this
	 * @throws TransException 
	 */
	@SuppressWarnings("unchecked")
	public TableMeta clone(TableMeta from) throws TransException {
		types = from.types;
		conn = from.conn;
		tbl = from.tbl;
		pk = from.pk;
		
		Class<? extends TableMeta> clazz = getClass();
		Field[] fields = getClass().getDeclaredFields();

	    while ( clazz != TableMeta.class ) {
	    	for (Field f : fields) {
	    		if (!types.containsKey(f.getName()))
	    			throw new TransException("Filed %s#%s is not defined in table '%s'.",
	    					clazz.getTypeName(), f.getName(), tbl);
	    	}
	    	clazz = (Class<? extends TableMeta>) clazz.getSuperclass();
	    	fields = clazz.getDeclaredFields();
	    }
	
		return this;
	}
}
