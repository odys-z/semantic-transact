package io.odysz.transact.sql.parts;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.Logic.op;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

/**Value to be resulved.
 * For what's resulved value, see <a href=''>RESolved resULt Value</a>.
 * <p>Note: This class can not been used to resolve post fk value.</p>
 * <p>Design Memo:<br>
 * If leting Resulving can resolve post fk value, all statement tree nodes
 * must traveled by a pre-inserting event handler, that's not possible to
 * remember all generated key if there are two nodes inserting the same table.<br>
 * So statement tree events do not travel across sub-trees is a rule of the design.
 * Then it's impossible to post resolve children pk using Resulving.</p>
 *
 * <p>Since 1.5.81, the auto key can be the one at an index of those generated in the context,
 * {@link #Resulving(String, String, int)}, instead of the last one ({@link #Resulving(String, String)}).</p>
 *
 * With the last one, a child row referring to its parent in the same table gets the latest key
 * generated, which is the child's own (or the last child's) key, as the child's key is generated
 * before its references are resolved. With an index, the parent's key is referred to by its order
 * of generation, e.g. in one multi-row insert of a tree, rows in depth first order, parents first:
 * <pre>
 * section: parentId = 'root'                                 key 0
 * p1:      parentId = new Resulving("s_docnodes", "nid", 0)  key 1
 * sub:     parentId = new Resulving("s_docnodes", "nid", 0)  key 2
 * sub.p1:  parentId = new Resulving("s_docnodes", "nid", 2)  key 3
 * </pre>
 * @author odys-z@github.com
 */
public class Resulving extends ExprPart {

	private String autok;
	private String tabl;
	/**
	 * Index of the keys generated for tabl.autok in the context, see
	 * {@link ISemantext#resulvedVal(String, String, int)}: 0 for the first, -1 for the last.
	 * @since 1.5.81
	 */
	private int index;

	private boolean asConstr = false;

	/**
	 * The last auto key generated for tabl.autok in the context.
	 * @param tabl
	 * @param autok
	 */
	public Resulving(String tabl, String autok) {
		this(tabl, autok, -1);
	}

	/**
	 * The auto key at an index of those generated for tabl.autok in the context.
	 * @param tabl
	 * @param autok
	 * @param index 0 for the first, -1 for the last
	 * @since 1.5.81
	 */
	public Resulving(String tabl, String autok, int index) {
		// super(op.eq, null, null);
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
			throw new TransException("Can't resolve auto Id - %s.%s%s. Possible error: wrong configure; empty row (no insertion triggered)",
					tabl, autok, index == -1 ? "" : "[" + index + "]");
		if (o instanceof AbsPart)
			try {
				// return ((AbsPart) o).sql(context);
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

	public Resulving asConstr() {
		asConstr = true;
		return this;
	}

}
