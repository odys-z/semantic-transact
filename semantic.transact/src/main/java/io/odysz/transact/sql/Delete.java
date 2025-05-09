package io.odysz.transact.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.Utils;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

public class Delete extends Statement<Delete>  {
	Delete(Transcxt transc, String tabl) {
		super(transc, tabl, null);
	}

	/**
	 * Commit delete statements.
	 * @param stx
	 * @return results of postOp.onCommitOk. For Delete created by DATrasct#delete(), should be
	 * SemanticObject with {"total", rows, "resulved" sctx.resulves()}.
	 * @throws TransException
	 * @throws SQLException
	 */
	public Object d(ISemantext stx) throws TransException, SQLException {
		if (postOp != null) {
			ArrayList<String> sqls = new ArrayList<String>(); 
			commit(stx, sqls);
			return postOp.onCommitOk(stx, sqls);
		}
		else
			Utils.warn("On operation for built sqls. Intend to call subclass' Delete(tbl, user)?");
		return null;
	}

	public Delete commit(ISemantext cxt, ArrayList<String> sqls) throws TransException {
		if ((where == null || where.isEmpty()))
			throw new TransException("Empty conditions for deleting. io.odysz.transact.sql.Delete is enforcing deletion with conditions.");
		
		if (cxt != null)
			cxt.onDelete(this, mainTabl.name(), where);

		return super.commit(cxt, sqls);
	}

	@Override
	public String sql(ISemantext sctx) throws TransException {
		if (where == null)
			throw new TransException("semantic.transact doesn't allow any delete statement without conditions. table: %s", mainTabl);
		
		// update tabl t set col = 'val' where t.col = 'val'
		Stream<String> s = // Stream.concat(
					Stream.of(
							withs,
							new ExprPart("delete from"),
							mainTabl,
							where == null ? null : new ExprPart("where"), 
							where
					)
					.filter(x -> x != null)
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
