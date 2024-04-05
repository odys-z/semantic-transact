package io.odysz.transact.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.Utils;
import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;
import io.odysz.semantics.SemanticObject;
import io.odysz.transact.sql.Query.Ix;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.update.SetList;
import io.odysz.transact.x.TransException;

public class Update extends Statement<Update> {

	/**[col-name, col-index] */
	private Map<String,Integer> updateCols;
	public Map<String, Integer> getColumns() { return updateCols; }

	private ArrayList<Object[]> nvs;
	private String limit = null;

	Update(Transcxt transc, String tabl) {
		super(transc, tabl, null);
	}

	/**set n = v, where if v is constant, e.g. 'val', must have a '' pair.
	 * @param n
	 * @param v
	 * @return this Update statement
	 */
	public Update nv(String n, AbsPart v) {
		if (nvs == null)
			nvs = new ArrayList<Object[]>();
		// nvs.add(new Object[] {n, v});
		
		// column names
		if (updateCols == null)
			updateCols = new HashMap<String, Integer>();
		if (!updateCols.containsKey(n)) {
			updateCols.put(n, updateCols.size());
			nvs.add(new Object[] {n, v});
		}
		else {
			// replace the old one
			nvs.get(updateCols.get(n))[1] = v;
			if (verbose) Utils.warn(
				"Update.nv(%1$s, %2$s): Column's value already exists, old value replaced by new value (%1$s = %2$s)",
				n, v);
		}
		return this;
	}

	/**set array of [n, v], where if v is constant, e.g. 'val', must have a '' pair.
	 * 
	 * FIXME As Query.col() already parsing sql expression, why not this method do the same to have client feel the same?
	 * @param nvs the n-v array
	 * @return this Update statement
	 * @throws TransException 
	 */
	public Update nvs(ArrayList<Object[]> nvs) throws TransException {
		if (nvs != null)
			for (Object[] nv : nvs) {
				if (nv == null || nv[Ix.nvn] == null) {
					if (nv != null && nv[Ix.nvv] != null)
						Utils.warn("Update#nvs(): Ignoring value () for null column name.", nv[Ix.nvv]);
					continue;
				}
				Object v = nv[Ix.nvv];

				if (v instanceof AbsPart)
					nv((String)nv[Ix.nvn], (AbsPart)v);
				else if (v instanceof String)
					nv((String)nv[Ix.nvn], (String)v);
				else
					nv((String)nv[Ix.nvn], ExprPart.constVal(v));
			}
		return this;
	}
	
	public Update nvs(Object... nvs) throws TransException {
		if (nvs == null || nvs.length == 0)
			return this;

		ArrayList<Object[]> l = new ArrayList<Object[]>(nvs.length / 2);
		for (int i = 0; i < nvs.length; i += 2)
			l.add(new Object[] {nvs[i], nvs[i + 1]});

		return nvs(l);
	}
	
	/**<p>Update Limited Rows.</p>
	 * <ul><li>ms sql 2k: update top(lmtExpr) ... see <a href='https://docs.microsoft.com/en-us/sql/t-sql/queries/top-transact-sql?view=sql-server-2017#DML'>
	 * 		Limiting the rows affected by DELETE, INSERT, or UPDATE</a></li>
	 * 		<li>mysql: update ... limit N, see <a href='https://dev.mysql.com/doc/refman/8.0/en/update.html'>
	 * 			Mysql Manual: 13.2.12 UPDATE Syntax</a></li>
	 *		<li>sqlite: nothing. see <a href='https://www.sqlite.org/lang_update.html'>SQL As Understood By SQLite</a></li>
	 * 		<li>Oracle: There should be no such syntax:
	 * 		<a href='https://docs.oracle.com/cd/B19306_01/server.102/b14200/statements_10007.htm'>
	 * 		Oracle Database SQL Reference - UPDATE</a></li>
	 * </ul>
	 * @param lmtExpr
	 * @return this
	 */
	public Update limit(String lmtExpr) {
		this.limit = lmtExpr;
		return this;
	}
	
	@Override
	public Update commit(ISemantext cxt, ArrayList<String> sqls) throws TransException {
		if (where == null || where.isEmpty())
			throw new TransException("Empty conditions for updating. io.odysz.transact.sql.Update is enforcing updating with conditions.");

		if (cxt != null)
			cxt.onUpdate(this, mainTabl.name(), nvs);

		Update upd = super.commit(cxt, sqls);

		if (cxt != null)
			cxt.onPost(this, mainTabl.name(), nvs, sqls);

		return upd;
	}

	/**Commit updating sql(s) to db.
	 * @param stx semantext instance
	 * @return semanticObject, return of postOp; null if no postOp.
	 * @throws TransException
	 * @throws SQLException
	 */
	public SemanticObject u(ISemantext stx) throws TransException, SQLException {
		if (postOp != null) {
			ArrayList<String> sqls = new ArrayList<String>(); 
			commit(stx, sqls);
			// Connects.commit() usually return this for update
			return postOp.onCommitOk(stx, sqls);
		}
		return null;
	}

	@Override
	public String sql(ISemantext sctx) throws TransException {
		dbtype db = null;
		db = sctx == null ? null : sctx.dbtype();
		
		// update tabl t set col = 'val' where t.col = 'val'
		Stream<String> s1 = Stream.of(
						new ExprPart("update"),
						limit != null && db == dbtype.ms2k ? new ExprPart("top(" + limit + ")") : null,
						mainTabl, mainAlias,
						new ExprPart("set"),
						new SetList(nvs).setVal2(mainTabl), 
						where == null ? null : new ExprPart("where"),
						where,
						limit != null && db == dbtype.mysql ? new ExprPart("limit " + limit) : null
				).map(m -> {
					try {
						return m == null ? "" : m.sql(sctx);
					} catch (TransException e) {
						e.printStackTrace();
						return "";
					}
				});


		return s1.collect(Collectors.joining(" "));
	}
}
