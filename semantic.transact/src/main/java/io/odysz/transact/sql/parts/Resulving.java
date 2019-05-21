package io.odysz.transact.sql.parts;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.Logic.op;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

/**Value to be resulved.
 * For what's resulved value, see <a href=''>RESolved resULt Value</a>
 * servral 
 * @author odys-z@github.com
 */
public class Resulving extends ExprPart {

	private String autok;
	private String tabl;

	public Resulving(String tabl, String autok) {
		super(op.eq, null, null);
		this.tabl = tabl;
		this.autok = autok;
	}

	@Override
	public String sql(ISemantext context) {
		Object o = context.resulvedVal(tabl, autok);
		if (o instanceof AbsPart)
			try {
				return ((AbsPart) o).sql(context);
			} catch (TransException e) {
				e.printStackTrace();
				return "'" + tabl + "." + autok + "'";
			}
		else return "'" + o.toString() + "'";
	}

	public String resulved(ISemantext smtx) {
		if (smtx == null)
			return "'" + tabl + "." + autok + "'";
		return (String) smtx.resulvedVal(tabl, autok);
	}

}
