package io.odysz.transact.sql.parts;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.Logic.op;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

/**
 * Value to be resulved, the same as {@link Resulving}, but the auto key at an index
 * of those generated in the context, instead of the last one.
 *
 * <p>With {@link Resulving}, a child row referring to its parent in the same table
 * gets the latest key generated, which is the child's own (or the last child's) key,
 * as the child's key is generated before its references are resolved.
 * Resulving2 refers to the parent's key by its order of generation, e.g. in one
 * multi-row insert of a tree, rows in depth first order, parents first:</p>
 * <pre>
 * section: parentId = 'root'                                  key 0
 * p1:      parentId = new Resulving2("s_docnodes", "nid", 0)  key 1
 * sub:     parentId = new Resulving2("s_docnodes", "nid", 0)  key 2
 * sub.p1:  parentId = new Resulving2("s_docnodes", "nid", 2)  key 3
 * </pre>
 *
 * <p>The index is of {@link ISemantext#resulvedVal(String, String, int)}: 0 for the first key
 * generated in the context, -1 for the last.</p>
 *
 * <p>Note: This class can not been used to resolve post fk value, see {@link Resulving}.</p>
 *
 * @since 1.5.81
 * @author odys-z@github.com
 */
public class Resulving2 extends ExprPart {

	private String autok;
	private String tabl;
	private int index;

	private boolean asConstr = false;

	/**
	 * @param tabl
	 * @param autok
	 * @param index of the keys generated for tabl.autok in the context: 0 for the first, -1 for the last
	 */
	public Resulving2(String tabl, String autok, int index) {
		super(op.eq, "", "");
		this.tabl = tabl;
		this.autok = autok;
		this.index = index;
	}

	@Override
	public String sql(ISemantext context) throws TransException {
		Object o;
		try { o = context.resulvedVal(tabl, autok, index); }
		catch (IndexOutOfBoundsException e) { o = null; } // not generated (yet)
		if (o == null)
			throw new TransException("Can't resolve auto Id - %s.%s[%s]. Possible error: wrong configure; empty row (no insertion triggered)",
					tabl, autok, index);
		if (o instanceof AbsPart)
			try {
				return asConstr ? "'" + ((AbsPart) o).sql(context) + "'" : ((AbsPart) o).sql(context);
			} catch (TransException e) {
				e.printStackTrace();
				return "'" + tabl + "." + autok + "'";
			}
		else return "'" + o.toString() + "'";
	}

	/**Get the resulved value
	 * @param smtx
	 * @return the resulved value or 'tabl.pk' (for null semantext)
	 */
	public String resulved(ISemantext smtx) {
		if (smtx == null)
			return "'" + tabl + "." + autok + "'";
		return (String) smtx.resulvedVal(tabl, autok, index);
	}

	public Resulving2 asConstr() {
		asConstr = true;
		return this;
	}

}
