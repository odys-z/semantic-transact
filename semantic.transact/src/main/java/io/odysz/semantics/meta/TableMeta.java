package io.odysz.semantics.meta;

import static io.odysz.common.LangExt.eq;
import static io.odysz.common.LangExt.eqs;
import static io.odysz.common.LangExt.isNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import io.odysz.common.Utils;
import io.odysz.semantics.meta.ColMeta.coltype;
import io.odysz.transact.x.TransException;

public class TableMeta {
	public static boolean debug = false;

	/**
	 * <p>A helper for test on sqlite.</p>
	 * 
	 * Subclass must load this like in constructor:
	 * <pre>{
	 * 	ddlSqlite = Utils.loadTxt(Subclass.class, \"file-path\")");
	 * }</pre>
	 * 
	 * @since 1.4.25
	 */
	public String ddlSqlite;

	/** {col-name: {@link ColMeta}} */
	protected HashMap<String, ColMeta> ftypes;
	public HashMap<String, ColMeta> ftypes() { return ftypes; }

	/**
	 * Is the column types already loaded from DB?
	 * @since 1.4.25
	 * @return true if there are some types
	 */
	public boolean typesInited() {
		return ftypes != null && ftypes.size() > 0;
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
		ftypes = new HashMap<String, ColMeta>();
		this.conn = isNull(conn) ? null : conn[0];
	}

	public TableMeta col(String col, coltype t) {
		ftypes.put(col, new ColMeta(t));
		return this;
	}
	
	public boolean isQuoted(String col) {
		return ftypes.containsKey(col) ? 
				ftypes.get(col).isQuoted()
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
		ftypes.put(coln, cm.tlen(len));
		return this;
	}

	public TableMeta col(String col, String type) {
		return col(col, type, 0);
	}

	public coltype coltype(String col) {
		return ftypes != null && ftypes.containsKey(col) ?
				ftypes.get(col).type() : null; //coltype.text;
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
			throw new TransException("[TableMeta#clone()] Table name or connection Id are not identical, %s : %s; %s : %s",
					conn, from.conn, tbl, from.tbl);

		ftypes = from.ftypes;
		
		Class<? extends TableMeta> clazz = getClass();
		Field[] fields = getClass().getDeclaredFields();

	    while ( clazz != TableMeta.class ) {
	    	for (Field f : fields) {
	    		f.setAccessible(true);
	    		try {
	    			if (String.class != f.getType())
	    				continue;
					String fv = (String) f.get(this);
					if (!Modifier.isStatic(f.getModifiers()) && !ftypes.containsKey(fv)) {
						Semantation ann = f.getAnnotation(Semantation.class);
						if (ann == null || !ann.noDBExists())
						Utils.warnT(new Object() {},
							"Meta field %s#%s(value: %s) is not defined in table '%s' (conn %s).\n" +
							"To suppress this warning, add @Semantation (notDBExists = true) to the field.",
	    					clazz.getTypeName(), f.getName(), fv, tbl, conn);
					}
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
	 * @since 1.4.25 Only sqlite is supported
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
	 * @since 1.4.25 only mysql is supported
	 * @param col
	 * @param key
	 * @return
	 */
	public TableMeta constrain(String col, String key) {
		if (eq(key, "PRI"))
			this.pk = col;
		return this;
	}

	/**
	 * Expose the column name mapping algorithm.
	 * @since 1.5.60
	 * @param colnames
	 * @param field
	 * @return col index
	 */
	public static int colx(HashMap<String, Object[]> colnames, String field) {
		return (int)colnames.get(field.toUpperCase())[0];
	}

	/**
	 * Get cell string at col, by mapping column names from colnames, starting index at 1.
	 * 
	 * @param colnames
	 * @param row
	 * @param col
	 * @return value at row(index).
	 */
	public static String cellstr(HashMap<String, Object[]> colnames, ArrayList<Object> row, String col) {
		return (String) row.get(TableMeta.colx(colnames, col) - 1);
	}
}
