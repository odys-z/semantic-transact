package io.odysz.semantics.sql;

import java.util.ArrayList;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;
import com.healthmarketscience.sqlbuilder.dbspec.Table;

import io.odysz.semantics.hms.SelectQry;
import io.odysz.semantics.sql.parts.Logic;
import io.odysz.semantics.x.StException;

public class Query extends Statement {
	private SelectQry q;
	private boolean allColumn;

	Query(Transc transc, String tabl, String... alias) {
		super(transc, tabl, alias == null || alias.length == 0 ? null : alias[0]);
		q = new SelectQry();

		Table tbl = transc.getTable(tabl);
		if (alias != null && alias[0] != null)
			tbl = new RejoinTable(tbl, alias[0]);

		q.addFromTable(tbl);
		allColumn = true;
	}

	public Query col(String col, String... alias) {
		allColumn = false;
		if (alias == null || alias.length <= 0 || alias[0] == null)
			q.addCustomColumns(col);
		else
			// TODO test expression as col
			q.addAliasedColumn(col, alias[0]);
		return this;
	}

	/**Inner Join
	 * @param withTabl
	 * @param onCondtion e.g "t.f1='a' t.f2='b'", 2 AND conditions
	 * @return
	 */
	public Query j(String withTabl, String onCondtion) {
		return this;
	}

	public Query where(String logic, String loperand, String roperand) throws StException {
		switch (Logic.op(logic)) {
		case eq:
			q.addCondition(BinaryCondition.equalTo(loperand, roperand));
			break;
		default:
			throw new StException("Logic not recogonized: %s", logic);
		}
		return this;
	}

	public Query where(String condt) {
		return this;
	}

	public Query commit(ArrayList<String> sqls) {
		sqls.add(q.validate().toString());
		return this;
	}
}
