package io.odysz.semantics.sql;

import java.util.ArrayList;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.SelectQuery;

public class Query extends Statement {
	private SelectQuery q;

	Query(Transc transc, String tabl, String... alias) {
		super(transc, tabl, alias == null || alias.length == 0 ? null : alias[0]);
	}

	public Query j(String withTabl, String onCondtion) {
		q = new SelectQuery();
		return this;
	}

	public Query where(String logic, String loperand, String roperand) {
		q.addCondition(BinaryCondition.equalTo(loperand, "bob"));
		return this;
	}

	public Query commit(ArrayList<String> sqls) {
		return this;
	}

	public Query column(String vexpr, String alias) {
		return this;
	}

}
