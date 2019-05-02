package io.odysz.transact.sql.parts.select;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;

/**Select_list_elem:<pre>
https://github.com/antlr/grammars-v4/blob/master/tsql/TSqlParser.g4

// https://msdn.microsoft.com/en-us/library/ms176104.aspx
select_list
    : select_list_elem (',' select_list_elem)*
	;
select_list_elem
    : asterisk
    | column_elem
    // TODO to be understood
    // | udt_elem
    
    // Modified
    // | expression_elem
    | expression as_column_alias?
	;

as_column_alias
    : AS? column_alias
	;
	
column_elem
    // : (table_name '.')? (column_name=id | '$' IDENTITY | '$' ROWGUID) as_column_alias?
    // changed:
    : (table_name '.')? (column_name=id) as_column_alias?
    ;
 * </pre>
 * @author ody
 *
 */
public class SelectElem extends AbsPart {

	public enum ElemType { asterisk, tableCol, col, func, expr }

	private ElemType elemtype;
	private String tabl;
	private String col; bug here, it shall be one of expression, funcall, col_name
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

	@Override
	public String sql(ISemantext sctx) {
		if (elemtype == ElemType.asterisk)
			return col;
		String sql;
		if (tabl == null)
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
