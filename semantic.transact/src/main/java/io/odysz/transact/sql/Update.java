package io.odysz.transact.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.Utils;
import io.odysz.semantics.ISemantext;
import io.odysz.semantics.SemanticObject;
import io.odysz.transact.sql.Query.Ix;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.update.SetList;
import io.odysz.transact.x.TransException;

public class Update extends Statement<Update> {

	/**[col-name, col-index] */
	private Map<String,Integer> updateCols;
	public Map<String, Integer> getColumns() { return updateCols; }


	private ArrayList<Object[]> nvs;

	Update(Transcxt transc, String tabl) {
		super(transc, tabl, null);
	}

	/**set n = v, where if v is constant, e.g. 'val', must have a '' pair.
	 * @param n
	 * @param v
	 * @return this Update statement
	 */
	public Update nv(String n, Object v) {
		if (nvs == null)
			nvs = new ArrayList<Object[]>();
		nvs.add(new Object[] {n, v});
		
		// column names
		if (updateCols == null)
			updateCols = new HashMap<String, Integer>();
		if (!updateCols.containsKey(n))
			updateCols.put(n, updateCols.size());
		else Utils.warn("Column's value already exists, old value replaced by new value (%s = %s)",
				n, v);
		
		return this;
	}

	/**set array of [n, v], where if v is constant, e.g. 'val', must have a '' pair.
	 * @param nvs the n-v array
	 * @return this Update statement
	 */
	public Update nvs(ArrayList<Object[]> nvs) {
		if (nvs != null)
			for (Object[] nv : nvs)
				nv((String)nv[Ix.nvn], nv[Ix.nvv]);
		return this;
	}
	
	@Override
	public Update commit(ISemantext cxt, ArrayList<String> sqls) throws TransException {
		if (where == null || where.isEmpty())
			throw new TransException("Empty conditions for updating. io.odysz.transact.sql.Update is enforcing updating with conditions.");

		if (cxt != null)
			cxt.onUpdate(this, mainTabl, nvs);

		Update upd = super.commit(cxt, sqls);

		if (cxt != null)
			cxt.onPost(this, mainTabl, nvs, sqls);

		return upd;
	}

	/**Commit updating sql(s) to db.
	 * @param stx semantext instance
	 * @return semanticObject, return of postOp.
	 * @throws TransException
	 * @throws SQLException
	 */
	public SemanticObject u(ISemantext stx) throws TransException, SQLException {
		if (postOp != null) {
			ArrayList<String> sqls = new ArrayList<String>(); 
			commit(stx, sqls);
			// Connects.commit() usually return this for update

			return postOp.op(stx, sqls);
//			SemanticObject res = postOp.op(stx, sqls);
//			if (res instanceof int[])
//				res = LangExt.toString((int[])res);
//			return new SemanticObject()
//					.put("updated", res)
//					.put("autoVals", stx.resulves());
		}
		return null;
	}

	@Override
	public String sql(ISemantext sctx) throws TransException {
		if (sctx != null)
			sctx.onUpdate(this, mainTabl, nvs);
		
		// update tabl t set col = 'val' where t.col = 'val'
		Stream<String> s = Stream.concat(
					Stream.of(new ExprPart("update"),
						new ExprPart(mainTabl), new ExprPart(mainAlias),
						new ExprPart("set"), new SetList(nvs)), 
					Stream.of(new ExprPart("where"), where).filter(w -> where != null))
				  .map(m -> {
					try {
						return m == null ? "" : m.sql(sctx);
					} catch (TransException e) {
						e.printStackTrace();
						return "";
					}
				});

		return s.collect(Collectors.joining(" "));
	}

}
