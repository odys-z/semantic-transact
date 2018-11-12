package io.odysz.semantics.sql;

import java.util.ArrayList;

import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;
import com.healthmarketscience.sqlbuilder.dbspec.Table;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;

import io.odysz.semantics.hms.SelectQry;
import io.odysz.semantics.sql.parts.condition.Condit;

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

	/**
	 * @param col example: f.funcId, count(*), ifnull(f.roleId, '0')
	 * @param alias
	 * @return
	 */
	public Query col(String col, String... alias) {
		allColumn = false;
		if (alias == null || alias.length <= 0 || alias[0] == null)
			// q.addCustomColumns(col);
			addCol(q, col);
		else {
			// TODO test expression as col
			DbColumn dcol = transc.getColumn(mt, col);
			if (dcol != null)
				// TODO
				// "stamp", "st" -> "t2.stamp AS st" (malias == null)
				// "stamp", "st" -> "lg.stamp AS st" (malias != null)
				q.addAliasedColumn(dcol, alias[0]);
			else
				// "l.stamp", "st" -> "l.stamp AS st"
				q.addAliasedColumn(new CustomSql(col), alias[0]);
		}
		return this;
	}

	/**Try figure out is the col is an expression (a.col1, f(a.col), ...), then add the col to qry.
	 * @param qry
	 * @param col
	 */
	private void addCol(SelectQry qry, String col) {
		// TODO
		// parse col
		String[] colss = col.split(".");
		String colAlias = colss[0];
		String colExpr = colss[1];

		if (colAlias == null) {
			DbColumn dbcol = transc.getColumn(mt, colExpr);
			if (dbcol != null) {
				qry.addColumns(dbcol);
				return;
			}
		}

		qry.addCustomColumns(col);
	}

	/**Inner Join
	 * @param withTabl
	 * @param onCondtion e.g "t.f1='a' t.f2='b'", 2 AND conditions
	 * @return
	 */
	public Query j(String withTabl, Condit onCondtion) {
		return this;
	}

	@Override
	public Statement commit(ArrayList<String> sqls) {
		sqls.add(q.validate().toString());
		return this;
	}
}
