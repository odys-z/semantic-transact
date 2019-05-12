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
	/**In select condition*/
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
//		ArrayList<String> sqls = new ArrayList<String>();
//		s.commit(sqls, usr);
//		inSelectCond = new Predicate(Logic.op.in, );
		inSelectCond = inCondt;
		return this;
	}

}
