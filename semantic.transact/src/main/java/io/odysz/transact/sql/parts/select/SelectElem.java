package io.odysz.transact.sql.parts.select;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.Alias;
import io.odysz.transact.sql.parts.Colname;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.condition.Funcall;

/**Select_list_elem:
 * For gramar, see {@link io.odysz.transact.sql.parts.antlr.SelectElemVisitor}.
 * https://github.com/antlr/grammars-v4/blob/master/tsql/TSqlParser.g4
 * https://msdn.microsoft.com/en-us/library/ms176104.aspx
 *
 * @author odys-z
 */
public class SelectElem extends ExprPart {

	/**Select element type, one of *, tabl.col, col, f(), expr, 'const' */
	public enum ElemType { asterisk, tableCol, col, expr, constant }

	private ElemType elemtype;
	private Alias tabl;
	/**in simple case, this is Column name,
	 * for {@link #elemtype} = {@link ElemType#expr}, this is not used - using {@link #expr} instead.
	 */
	private Colname col;
	/**in func case, this is the expression part.
	 * for {@link #elemtype} = {@link ElemType#col}, using {@link #col} instead.
	 */
	private ExprPart expr;

	private Alias alias;


	public SelectElem(ElemType elemType, String col) {
		super((String)null);
		this.elemtype = elemType;
		this.col = new Colname(col);
	}

	public SelectElem(ElemType type, String tabl, String col) {
		super((String)null);
		this.elemtype = type;
		this.tabl = new Alias(tabl);
		this.col = new Colname(col);
	}
	
	public SelectElem(ExprPart expr) {
		super((String)null);
		this.elemtype = ElemType.expr;
		this.expr = expr;
	}

	@Override
	public String sql(ISemantext sctx) {
		String sql = "";
		try {
			if (elemtype == ElemType.asterisk)
				sql = col.sql(sctx);
			// expr also handling func?
			else if (elemtype == ElemType.expr) {
				if (expr instanceof Funcall)
					// extFile() needing handle post selected results,
					// it is needing to know the alias to find out the results to be replaced 
					((Funcall)expr).selectElemAlias(alias);
				sql = expr == null ? "null" : expr.sql(sctx); // + " " + alias;
			}
			else if (tabl == null)
				sql = col.sql(sctx);
			else 
				sql = tabl.sql(sctx) + "." + col.sql(sctx);

			if (alias != null)
				return sql += " " + alias.sql(sctx);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return sql;
	}

	/**
	 * @since 1.4.40
	 * @param alias
	 * @return this
	 */
	public SelectElem tableAlias(String alias) {
		this.tabl = new Alias(alias);
		return this;
	}

	/**
	 * @since 1.4.40
	 * @param alias
	 * @return this
	 */
	public SelectElem as(String alias) {
		this.alias = new Alias(alias);
		return this;
	}

}
