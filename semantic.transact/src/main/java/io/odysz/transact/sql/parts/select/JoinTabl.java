package io.odysz.transact.sql.parts.select;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.LangExt;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.Sql;
import io.odysz.transact.sql.parts.antlr.ConditVisitor;
import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;


/**Select query's join clause.<br>
 * For grammar definition, see {@link ConditVisitor} 
 * @author ody
 *
 */
public class JoinTabl extends AbsPart {
	public enum join { main, j, r, l;

	public static join parse(String name) {
		if (name == null || name.trim().length() == 0)
			return join.j;
		name = name.toLowerCase();
		if (j.name().equals(name))
			return join.j;
		else if (r.name().equals(name))
			return join.r;
		else if (l.name().equals(name))
			return join.l;
		else return join.main;
	} };

	protected join jtype;
	protected String jtablias;
	protected AbsPart jtabl;
	private Condit on;

	public JoinTabl(join joinType, String tabl, Condit on) {
		this.on = on;
		this.jtabl = new ExprPart(tabl);
		this.jtype = joinType;
	}

	public JoinTabl(join joinType, String tabl, String alias, Condit... on) {
		this.on = on == null || on.length == 0 ? null : on[0];
		this.jtype = joinType;
		this.jtabl = new ExprPart(tabl);
		this.jtablias = alias;
	}
	
	public JoinTabl(join jt, Query select, String alias, String onCondit) {
		joinTabl(jt, select, alias, Sql.condt(onCondit));
	}

	public JoinTabl(join jt, Query select, String alias, Condit... on) {
		joinTabl(jt, select, alias, on);
	}

	private void joinTabl(join jt, Query select, String alias, Condit... on) {
		this.on = on == null || on.length == 0 ? null : on[0];
		this.jtype = jt;
		this.jtabl = select;
		if (LangExt.isblank(alias))
			this.jtablias = select.alias();
		else 
			this.jtablias = alias;
	}

	@Override
	public String sql(ISemantext sctx) throws TransException {
		if (jtype == join.main)
			return String.format("from %s %s", jtabl.sql(sctx), jtablias == null ? "" : jtablias);

		Stream<String> s = Stream.of(
				sql(jtype),
				jtabl instanceof Query ? "(" : null,
					jtabl,
				jtabl instanceof Query ? ")" : null,
					jtablias,
				on == null ? null : "on",
				on)
				.filter(p -> p != null)
				.map(m -> {
					try {
						return m instanceof String ? (String)m : ((AbsPart)m).sql(sctx);
					} catch (TransException e) {
						e.printStackTrace();
						return "";
					}
				}) ;
		return s.collect(Collectors.joining(" "));
	}

	private String sql(join jt) {
		if (jt == join.main) return "from";
		else if (jt == join.j) return "join";
		else if (jt == join.r) return "right outer join";
		else if (jt == join.l) return "left outer join";
		else return "";
	}
}
