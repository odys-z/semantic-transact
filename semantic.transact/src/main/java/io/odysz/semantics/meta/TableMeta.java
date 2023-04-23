package io.odysz.semantics.meta;

import static io.odysz.common.LangExt.eq;
import static io.odysz.common.LangExt.eqs;
import static io.odysz.common.LangExt.isNull;

import java.lang.reflect.Field;
import java.util.HashMap;

import io.odysz.common.Utils;
import io.odysz.semantics.meta.ColMeta.coltype;
import io.odysz.transact.x.TransException;

public class TableMeta {

	/**
	 * <p>A helper for test on sqlite.</p>
	 * 
	 * Subclass must override this like:
	 * <pre>static {
	 * 	ddlSqlite = Utils.loadTxt(Subclass.class, \"file-path\")");
	 * }</pre>
	 * 
	 * @throws TransException 
	 * @since 1.5.0
	 */
	public static String ddlSqlite;

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

	/**
	 * Set column type.
	 * @param coln
	 * @param t
	 * @param len
	 * @return this
	 */
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
		if (!eqs(conn, from.conn, tbl, from.tbl))
			throw new TransException("Table name or connection Id are not identical");

		types = from.types;
		
		Class<? extends TableMeta> clazz = getClass();
		Field[] fields = getClass().getDeclaredFields();

	    while ( clazz != TableMeta.class ) {
	    	for (Field f : fields) {
	    		f.setAccessible(true);
	    		try {
	    			if (String.class != f.getType())
	    				continue;
					String fv = (String) f.get(this);
					if (!types.containsKey(fv))
						Utils.warn("Filed %s#%s(%s) is not defined in table '%s'.",
	    					clazz.getTypeName(), f.getName(), fv, tbl);
				} catch (Exception e) {
					e.printStackTrace();
				}
	    	}
	    	clazz = (Class<? extends TableMeta>) clazz.getSuperclass();
	    	fields = clazz.getDeclaredFields();
	    }
	
		return this;
	}

	/**
	 * @since 1.5.0 Only sqlite is supported
	 * @param col
	 * @param ispk for sqlite, pk = 1
	 * @return this
	 */
	public TableMeta constrain(String col, int ispk) {
		if (ispk == 1)
			this.pk = col;
		return this;
	}

	/**
	 * @since 1.5.0 only mysql is supported
	 * @param col
	 * @param key
	 * @return
	 */
	public TableMeta constrain(String col, String key) {
		if (eq(key, "PRI"))
			this.pk = col;
		return this;
	}
}
