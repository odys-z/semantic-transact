package io.odysz.transact.sql;

import java.util.stream.Collectors;

import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;

public class QueryPage extends Query {

	public QueryPage(Transcxt transc, Query sub, String... alias) {
		super(transc, sub, alias);
	}

	public QueryPage(Transcxt transc, String tabl, String... alias) {
		super(transc, tabl, alias);
	}

	@Override
	public String sql(ISemantext sctx) {
		long i1 = pg * pgSize;
		String r2 = String.valueOf(i1 + pgSize);
		String r1 = String.valueOf(i1);
		String pre, rear;

		if (sctx.dbtype() == dbtype.oracle) {
			pre = "select * from (select t.*, rownum r_n_ from (";
			rear= String.format(") t order by rownum) t where r_n_ > %s and r_n_ <= %s", r1, r2);
		}
		else if (sctx.dbtype() == dbtype.ms2k) {
			pre = "select * from (SELECT ROW_NUMBER() OVER(ORDER BY (select NULL as noorder)) AS RowNum, * from (";
			rear= String.format(") t) t where rownum > %s and rownum <= %s", r1, r2);
		}
		else if (sctx.dbtype() == dbtype.sqlite) {
			// https://stackoverflow.com/a/51380906
			pre = "select * from (";
			rear= String.format(") limit %s offset %s", String.valueOf(pgSize), r1);
		}
		else {
			// mysql
			pre = "select * from (select t.*, @ic_num := @ic_num + 1 as rnum from (";
			rear= String.format(") t, (select @ic_num := 0) ic_t) t1 where rnum > %s and rnum <= %s", r1, r2);
		}

		return isQueryExpr
			? sqlstream(sctx).collect(Collectors.joining(" ", "(" + pre, rear + ")"))
			: sqlstream(sctx).collect(Collectors.joining(" ", pre, rear));
	}
}
