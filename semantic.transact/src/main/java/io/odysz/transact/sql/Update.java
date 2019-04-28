package io.odysz.transact.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.LangExt;
import io.odysz.semantics.ISemantext;
import io.odysz.semantics.SemanticObject;
import io.odysz.transact.sql.Query.Ix;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.update.SetList;
import io.odysz.transact.x.TransException;

public class Update extends Statement<Update> {
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
	
	public Object u(ISemantext stx) throws TransException, SQLException {
		if (postOp != null) {
			ArrayList<String> sqls = new ArrayList<String>(); 
			commit(stx, sqls);
			// return postOp.op(stx.connId(), sqls);
			Object res = postOp.op(stx.connId(), sqls);
			
			if (res instanceof int[]) // Connects.commit() usually return this for update
				res = LangExt.toString((int[])res);
			// update results: cond = where, post autovals.
			return new SemanticObject()
					.put("updated", res)
					.put("autoVals", stx.resulves());
		}
		return null;
	}

	@Override
	public String sql(ISemantext sctx) {
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
