package io.odysz.semantics.sql.parts.select;

import io.odysz.semantics.sql.parts.condition.AbsPart;

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

	@Override
	public String sql() {
		return null;
	}

	public void as(String alias) {
		
	}

}
