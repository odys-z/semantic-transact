package io.odysz.transact.sql.parts.select;

import static io.odysz.common.LangExt.isNull;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.Utils;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.Tabl;
import io.odysz.transact.sql.parts.select.SelectElem.ElemType;
import io.odysz.transact.x.TransException;

public class WithClause extends AbsPart {

	boolean recursive;
	ArrayList<AbsPart[]> withs;
	
	public WithClause(boolean recursive, ArrayList<AbsPart[]> withs) {
		this.recursive = recursive;
		this.withs = withs;
	}

	public WithClause(boolean recursive) {
		this.recursive = recursive;
		this.withs = new ArrayList<AbsPart[]>();
	}

	public WithClause recursive(boolean recur) {
		this.recursive = recur;
		return this;
	}
	
	@Override
	public String sql(ISemantext sctx) throws TransException {
		
		return isNull(withs) ? "" :
			recursive ?
				withs.stream()
					.filter(e -> e != null)
					.map((q) -> { return
							Stream.of(
								"recursive ", 
								((Tabl)q[0]).sql(sctx),
								" as (", 
								((SelectElem) q[1]).sql(sctx),
								" union all ",
								((Query) q[2]).sql(sctx),
								")")
							.filter(e -> e != null)
							.collect(Collectors.joining());
					})
					.collect(Collectors.joining(", ", "with ", "")) 
				:
				withs.stream()
					.filter(e -> e != null)
					.map((q) -> { return
							Stream.of(
								((Query)q[0]).alias().sql(sctx),
								" as (", 
								((Query) q[0]).sql(sctx),
								")")
							.filter(e -> e != null)
							.collect(Collectors.joining());
					})
					.collect(Collectors.joining(", ", "with ", "")); 
	}

	public WithClause with(String recurTabl, String rootValue, Query subSelect) {
		if (this.withs == null)
			this.withs = new ArrayList<AbsPart[]>();
		this.withs.add(new AbsPart[] {new Tabl(recurTabl), new SelectElem(ElemType.constant, rootValue), subSelect});
		return this;
	}

	public WithClause with(Query q0, Query[] qi) {
		if (this.withs == null)
			this.withs = new ArrayList<AbsPart[]>();

		if (!isNull(q0)) {
			if (isblank(q0.alias()))
					Utils.warn("Adding with-table without alias? %s", q0.sql(null));;
			this.withs.add(new AbsPart[] {q0});
		}

		if (!isNull(qi))
			for (Query q : qi) {
				if (isblank(q.alias()))
						Utils.warn("Adding with-table without alias? %s", q.sql(null));;
				this.withs.add(new AbsPart[] {q});
			}

		return this;

	}

}
