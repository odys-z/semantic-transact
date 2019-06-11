package io.odysz.transact.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

public class Delete extends Statement<Delete>  {
	Delete(Transcxt transc, String tabl) {
		super(transc, tabl, null);
	}

	public Object d(ISemantext stx) throws TransException, SQLException {
		if (postOp != null) {
			ArrayList<String> sqls = new ArrayList<String>(); 
			commit(stx, sqls);
			return postOp.onCommitOk(stx, sqls);
		}
		return null;
	}

//	public Delete commit(ISemantext cxt, ArrayList<String> sqls) throws TransException {
//		if ((where == null || where.isEmpty()))
//			throw new TransException("Empty conditions for deleting. io.odysz.transact.sql.Delete is enforcing deletiong with conditions.");
//
//		return super.commit(cxt, sqls);
//	}

	public Delete commit(ISemantext cxt, ArrayList<String> sqls) throws TransException {
		if ((where == null || where.isEmpty()))
			throw new TransException("Empty conditions for deleting. io.odysz.transact.sql.Delete is enforcing deletiong with conditions.");
		
		if (cxt != null)
			cxt.onDelete(this, mainTabl, where);

		return super.commit(cxt, sqls);
	}

	@Override
	public String sql(ISemantext sctx) throws TransException {
//		if (sctx != null)
//			sctx.onDelete(this, mainTabl, where);
		
		// if (where == null && inSelectCond == null)
		if (where == null)
			throw new TransException("semantic.transact doesn't allow any delete statement without conditions. table: %s", mainTabl);
		
		// update tabl t set col = 'val' where t.col = 'val'
		Stream<String> s = // Stream.concat(
					Stream.of(  new ExprPart("delete from"),
								new ExprPart(mainTabl),
//								new ExprPart("where"), 
//								where == null ? inSelectCond : where
								where == null ? null : new ExprPart("where"), 
								where
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

}
