package io.odysz.transact.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.condition.Predicate;
import io.odysz.transact.x.TransException;

public class Delete extends Statement<Delete>  {
	/**In select condition.
	 * TODO should moved to super and support Update? */
	private Predicate inSelectCond;

//	private ArrayList<Object[]> nvs;

	Delete(Transcxt transc, String tabl) {
		super(transc, tabl, null);
	}

	public Object d(ISemantext stx) throws TransException, SQLException {
		if (postOp != null) {
			ArrayList<String> sqls = new ArrayList<String>(); 
			commit(stx, sqls);
			return postOp.op(stx, sqls);
		}
		return null;
	}

	public Delete commit(ISemantext cxt, ArrayList<String> sqls) throws TransException {
		if ((where == null || where.isEmpty())
			&& (inSelectCond == null || inSelectCond.empty()))
			throw new TransException("Empty conditions for deleting. io.odysz.transact.sql.Delete is enforcing deletiong with conditions.");
		return super.commit(cxt, sqls);
	}

	@Override
	public String sql(ISemantext sctx) throws TransException {
		if (sctx != null)
			sctx.onDelete(this, mainTabl, where);
		
		if (where == null && inSelectCond == null)
			throw new TransException("semantic.transact doesn't allow any delete statement without conditions. table: %s", mainTabl);
		
		// update tabl t set col = 'val' where t.col = 'val'
		Stream<String> s = // Stream.concat(
					Stream.of(  new ExprPart("delete from"),
								new ExprPart(mainTabl),
								new ExprPart("where"), 
								where == null ? inSelectCond : where
					).map(m -> {
					try {
						return m == null ? "" : m.sql(sctx);
					} catch (TransException e) {
						e.printStackTrace();
						return "";
					}
				});
		return s.collect(Collectors.joining(" "));
	}

	public Delete whereIn(Predicate inCondt) throws TransException {
		inSelectCond = inCondt;
		return this;
	}

}
