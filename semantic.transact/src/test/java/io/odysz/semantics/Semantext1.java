package io.odysz.semantics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.odysz.common.DateFormat;
import io.odysz.common.LangExt;
import io.odysz.common.dbtype;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.sql.Delete;
import io.odysz.transact.sql.Insert;
import io.odysz.transact.sql.Statement;
import io.odysz.transact.sql.Statement.IPostOperat;
import io.odysz.transact.sql.Statement.IPostSelectOperat;
import io.odysz.transact.sql.Update;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.x.TransException;

/**Basic semantic context (semantics instance) for resolving "AUTO" when generating sql.
 * @author odys-z@github.com
 */
public class Semantext1 implements ISemantext {

	private String tabl;
	private HashMap<Object, Object> autoVals;
	private HashMap<String, Semantics2> semantics;
	private Statement<?> callerStatement;
	private HashMap<String, TableMeta> metas;

	public Semantext1(String tabl, HashMap<String,Semantics2> semantics, HashMap<String, TableMeta> metas) {
		this.tabl = tabl;
		this.semantics = semantics;
		this.metas = metas;
	}

	/**When inserting, replace inserting values in 'AUTO' columns, e.g. generate auto PK for rec-id.<br>
	 * @param valuesNv
	 */
	@Override
	public ISemantext onInsert(Insert insert, String tabl, List<ArrayList<Object[]>> valuesNv) {
		// This template implementation already showed the interface should be here
		callerStatement = (Insert) insert;
		if (valuesNv != null)
			for (ArrayList<Object[]> value : valuesNv) {
				for (Object[] nv : value)
					if (nv != null && nv.length > 0 && "AUTO".equals(nv[1])) {
						if (autoVals == null)
							autoVals = new HashMap<Object, Object>();
						if (autoVals.containsKey(nv[0]))
							nv[1] = autoVals.get(nv[0]);
						else {
							nv[1] = nv[1] + " #" + DateFormat.getTimeStampYMDHms(dbtype.mysql);
							autoVals.put(nv[0], nv[1]);
						}
					}
			}
		return this;
	}

	/**Resolve AUTO generated by "insert"<br>
	 * @see io.odysz.semantics.ISemantext#onUpdate(java.util.ArrayList)
	 */
	@Override
	public ISemantext onUpdate(Update update, String tabl, ArrayList<Object[]> nvs) {
//		if (autoVals != null && nvs != null)
//			for (Object[] nv : nvs)
//				if (nv != null && nv.length > 0 && "AUTO".equals(nv[1]))
//					nv[1] = autoVals == null ? nv[1] : autoVals.get(nv[0]);
		return this;
	}

	@Override
	public ISemantext onDelete(Delete delete, String tabl, Condit condt) {
		return this;
	}
	
	@Override
	public ISemantext insert(Insert insert, String tabl, IUser... usr) {
		return new Semantext1(tabl, semantics, metas);
	}

	@Override
	public ISemantext update(Update update, String mainTabl, IUser... usr) {
		return null;
	}

	@Override
	public Object resulvedVal(String tabl, String col) {
		return autoVals == null ? null
				: ((SemanticObject)autoVals.get(tabl)).get(col);
	}

	@Override
	public dbtype dbtype() { return dbtype.sqlite; }

	String conn;
	@Override
	public ISemantext connId(String conn) {
		this.conn = conn;
		return this;
	}

	@Override
	public String connId() { return conn; }
	
	@Override
	public String genId(String tabl, String col) throws SQLException, TransException {
		return "";
	}

	@Override
	public ISemantext clone(IUser usr) {
		return new Semantext1(tabl, semantics, metas);
	}

	public ISemantext reset() { return this; }
	
	@Override
	public SemanticObject resulves() { return null; }
	
	@Override
	public TableMeta colType(String tabl) {
		return metas.get(tabl);
	}

	@Override
	public ISemantext onPost(Statement<?> stmt, String mainTabl, ArrayList<Object[]> row, ArrayList<String> sqls)
			throws TransException { return null; }

	@Override
	public String relativpath(String... sub) throws TransException {
		return path(sub);
	}

	/**compose path
	 * Don't use this to compose filepath name, only for testing.
	 * @param fn
	 * @param root
	 * @param sub
	 * @return fake path
	 * @throws TransException 
	 */
	static String path(String... sub) throws TransException {
		String subp = "";
		if (sub != null)
			for (String s : sub)
				if (!LangExt.isblank(s, "/*"))
					subp += s + "/";
		return subp;
	}

	@Override
	public void onCommitted(ISemantext ctx) throws TransException, SQLException { }

	@Override
	public void addOnOkOperate(IPostOperat op) { }

	@Override
	public String containerRoot() { return null; }

	@Override
	public void onSelected(Object resultset) throws SQLException, TransException { }

	@Override
	public AbsPart composeVal(Object v, String tabl, String col) {
		return Statement.composeVal(v, metas.get(tabl), col);
	}

	@Override
	public boolean hasOnSelectedHandler(String name) {
		return false;
	}

	@Override
	public void addOnSelectedHandler(String name, IPostSelectOperat op) { }
}
