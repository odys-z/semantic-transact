package io.odysz.transact.sql.parts.select;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.condition.Funcall;

/**Select_list_elem:
 * For gramar, see {@link io.odysz.transact.sql.parts.antlr.SelectElemVisitor}.
 * https://github.com/antlr/grammars-v4/blob/master/tsql/TSqlParser.g4
 * https://msdn.microsoft.com/en-us/library/ms176104.aspx
 *
 * @author odys-z
 */
public class SelectElem extends AbsPart {

	/**Select element type, one of *, tabl.col, col, f(), expr, 'const' */
	public enum ElemType { asterisk, tableCol, col, expr, constant }

	private ElemType elemtype;
	private String tabl;
	/**in simple case, this is Column name,
	 * for {@link #elemtype} = {@link ElemType#expr}, this is not used - using {@link #expr} instead.
	 */
	private String col;
	/**in func case, this is the expression part.
	 * for {@link #elemtype} = {@link ElemType#col}, using {@link #col} instead.
	 */
	private ExprPart expr;

	private String alias;


	public SelectElem(ElemType elemType, String col) {
		this.elemtype = elemType;
		this.col = col;
	}

	public SelectElem(ElemType type, String tabl, String col) {
		this.elemtype = type;
		this.tabl = tabl;
		this.col = col;
	}
	
	public SelectElem(ExprPart expr) {
		this.elemtype = ElemType.expr;
		this.expr = expr;
	}

	@Override
	public String sql(ISemantext sctx) {
		String sql;
		if (elemtype == ElemType.asterisk)
			sql = col;
//		else if (elemtype == ElemType.func)
//			sql = new Funcall(col).sql(sctx) + " " + alias;
		// expr also handling func?
		else if (elemtype == ElemType.expr)
			sql = expr.sql(sctx) + " " + alias;
		else if (tabl == null)
			sql = col;
		else 
			sql = tabl + "." + col;

		if (alias != null)
			return sql += " " + alias;
		return sql;
	}

	public void as(String alias) {
		this.alias = alias;
	}

}
