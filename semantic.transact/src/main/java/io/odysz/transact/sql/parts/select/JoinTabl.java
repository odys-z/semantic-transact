package io.odysz.transact.sql.parts.select;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.odysz.transact.sql.parts.Logic;
import io.odysz.transact.sql.parts.Logic.op;
import io.odysz.transact.sql.parts.Logic.type;
import io.odysz.transact.sql.parts.antlr.ConditVisitor;
import io.odysz.transact.sql.parts.condition.Condit;


/**For grammar definition, see {@link ConditVisitor} 
 * @author ody
 *
 */
public class JoinTabl extends Condit {
	public enum join { main, j, r, l };

	protected join jtype;
	protected String jtablias;
	protected String jtabl;

	public JoinTabl(join joinType, String tabl, Condit on) {
		super(on);
		this.jtabl = tabl;
		this.jtype = joinType;
	}

	public JoinTabl(join joinType, String tabl, String alias, Condit... on) {
		super(on == null || on.length == 0 ? null : on[0]);
		this.jtype = joinType;
		this.jtabl = tabl;
		this.jtablias = alias;
	}

	public String sql() {
		if (jtype == join.main)
			return String.format("from %s %s", jtabl, jtablias == null ? "" : jtablias);

		String condt = super.sql();
		if (condt != null && condt.length() > 0)
			return String.format("%s %s %s on %s", sql(jtype), jtabl,
				jtablias == null ? "" : jtablias, super.sql());
		else
			return String.format("%s %s %s", sql(jtype), jtabl,
				jtablias == null ? "" : jtablias);
	}

	private String sql(join jt) {
		switch (jt) {
		case main: return "from";
		case j: return "join";
		case r: return "right outer join";
		case l: return "left outer join";
		default: return "";
		}
	}

}
