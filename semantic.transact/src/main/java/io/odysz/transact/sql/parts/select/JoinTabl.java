package io.odysz.transact.sql.parts.select;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.antlr.ConditVisitor;
import io.odysz.transact.sql.parts.condition.Condit;


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
	protected String jtabl;
	private Condit on;

	public JoinTabl(join joinType, String tabl, Condit on) {
		this.on = on;
		this.jtabl = tabl;
		this.jtype = joinType;
	}

	public JoinTabl(join joinType, String tabl, String alias, Condit... on) {
		// super(on == null || on.length == 0 ? null : on[0]);
		this.on = on == null || on.length == 0 ? null : on[0];
		this.jtype = joinType;
		this.jtabl = tabl;
		this.jtablias = alias;
	}

	@Override
	public String sql(ISemantext sctx) {
		if (jtype == join.main)
			return String.format("from %s %s", jtabl, jtablias == null ? "" : jtablias);

		// String condt = super.sql(sctx);
		if (on != null)
			return String.format("%s %s %s on %s", sql(jtype),
					// jtablias == null ? "" : jtablias, super.sql(sctx));
					jtabl, jtablias == null ? "" : jtablias,
					on.sql(sctx));
		else
			return String.format("%s %s %s", sql(jtype), jtabl,
				jtablias == null ? "" : jtablias);
	}

	private String sql(join jt) {
		if (jt == join.main) return "from";
		else if (jt == join.j) return "join";
		else if (jt == join.r) return "right outer join";
		else if (jt == join.l) return "left outer join";
		else return "";
	}

}
