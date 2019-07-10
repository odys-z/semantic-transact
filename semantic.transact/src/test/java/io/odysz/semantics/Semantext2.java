package io.odysz.semantics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.odysz.common.DateFormat;
import io.odysz.common.dbtype;
import io.odysz.semantics.Semantics2.smtype;
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

/**<p>Basic semantic context (semantics instance) for resolving semantics when generating sql.</p>
 * <p>This testing implementation handling autoPk, fullpath, and may be more.</p>
 * @author odys-z@github.com
 *
 */
class Semantext2 implements ISemantext {

	private String tabl;
	private HashMap<String, Semantics2> semantics;
	private HashMap<String, TableMeta> metas;

	public Semantext2(String tabl, HashMap<String, Semantics2> semantics, HashMap<String, TableMeta> metas) {
		this.tabl = tabl;
		this.semantics = semantics;
		this.metas = metas;
	}

	/**When inserting, replace inserting values in 'AUTO' columns, e.g. generate auto PK for rec-id.
	 * @see io.odysz.semantics.ISemantext#onInsert(io.odysz.transact.sql.Insert, java.lang.String, java.util.List)
	 */
	@Override
	public ISemantext onInsert(Insert insert, String tabl, List<ArrayList<Object[]>> valuesNv) {
		if (valuesNv != null)
			for (ArrayList<Object[]> value : valuesNv) {
				Semantics2 s = semantics.get(tabl);
				if (s == null)
					continue;
				Map<String, Integer> cols = insert.getColumns();
				if (s.is(smtype.autoPk)) {
					String pk = s.autoPk();
					String n = (String) value.get(cols.get(pk))[0];
					if (n.equals(pk))
						value.get(cols.get(pk))[1] = "TEST-" + DateFormat.format(new Date());
				}
				if (s.is(smtype.fullpath)) {
					String n = s.getFullpathField();
					Object fp = s.genFullpath(value, cols);
					Object[] nv = null;
					if (!cols.containsKey(n)) {
						// append fullpath nv
						int c = cols.size();
						nv = new Object[] {n, fp};
						value.add(nv);
						cols.put(n, c);
					}
					else {
						nv = value.get(cols.get(n));
						nv[1] = fp;
					}
				}
			}
		return this;
	}

	String conn;
	@Override
	public ISemantext connId(String conn) {
		this.conn = conn;
		return this;
	}

	@Override
	public String connId() { return conn; }

	@Override
	public ISemantext onUpdate(Update update, String tabl, ArrayList<Object[]> nvs) {
		return this;
	}

	@Override
	public ISemantext onDelete(Delete delete, String tabl, Condit condt) { return this; }

	@Override
	public ISemantext insert(Insert insert, String tabl, IUser... usr) {
		return new Semantext2(tabl, semantics, metas);
	}

	@Override
	public ISemantext update(Update update, String mainTabl, IUser... usr) { return null; }

	@Override
	public Object resulvedVal(String tabl, String col) { return null; }

	@Override
	public dbtype dbtype() { return dbtype.sqlite; }

	@Override
	public String genId(String tabl, String col) throws SQLException, TransException {
		return "";
	}

	@Override
	public ISemantext clone(IUser usr) {
		return new Semantext2(tabl, semantics, metas);
	}

	@Override
	public SemanticObject resulves() { return null; }
	
	@Override
	public TableMeta colType(String tabl) { return metas.get(tabl); }

	@Override
	public ISemantext onPost(Statement<?> stmt, String mainTabl, ArrayList<Object[]> row, ArrayList<String> sqls)
			throws TransException { return null; }

	@Override
	public String relativpath(String... sub) throws TransException {
		return Semantext1.path(sub);
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
	public void addOnSelectedHandler(IPostSelectOperat op) { }

	@Override
	public AbsPart composeVal(Object v, String tabl, String col) {
		return Statement.composeVal(v, metas.get(tabl), col);
	}
}
