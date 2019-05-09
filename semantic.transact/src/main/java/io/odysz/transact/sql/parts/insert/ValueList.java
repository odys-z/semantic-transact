package io.odysz.transact.sql.parts.insert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.odysz.transact.x.TransException;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.condition.ExprPart;

/**Support value list in update set value elem and insert values list:<br>
 * value can only be:<br>
 * subquery,<br>
 * expression,<br>
 * function_call: method_name '(' expression_list ')'
 * <p>reference:</p>
 * <pre>
https://github.com/antlr/grammars-v4/blob/master/tsql/TSqlParser.g4

// https://msdn.microsoft.com/en-us/library/ms174335.aspx
insert_statement
    : with_expression?
      INSERT (TOP '(' expression ')' PERCENT?)?
      INTO? (ddl_object | rowset_function_limited)
      insert_with_table_hints?
      ('(' column_name_list ')')?
      output_clause?
      insert_statement_value
      for_clause? option_clause? ';'?
    ;

insert_statement_value
    : table_value_constructor
    | derived_table
    | execute_statement
    | DEFAULT VALUES
    ;
    
table_value_constructor
    : VALUES '(' expression_list ')' (',' '(' expression_list ')')*
    ;

expression_list
    : expression (',' expression)*
	;

derived_table
    : subquery
    | '(' subquery ')'
    | table_value_constructor
    | '(' table_value_constructor ')'
	;
	
	
// https://msdn.microsoft.com/en-us/library/ms177523.aspx
update_statement
    : with_expression?
      UPDATE (TOP '(' expression ')' PERCENT?)?
      (ddl_object | rowset_function_limited)
      with_table_hints?
      SET update_elem (',' update_elem)*
      output_clause?
      (FROM table_sources)?
      (WHERE (search_condition_list | CURRENT OF (GLOBAL? cursor_name | cursor_var=LOCAL_ID)))?
      for_clause? option_clause? ';'?
;

update_elem
    : (full_column_name | LOCAL_ID) ('=' | assignment_operator) expression
    | udt_column_name=id '.' method_name=id '(' expression_list ')'
    //| full_column_name '.' WRITE (expression, )
;
</pre>
 *
 * @author odys-z@github.com
 */
public class ValueList extends AbsPart {
	protected ArrayList<AbsPart> valst;

	protected AbsPart[] valsArr;
	/** This is also the flag of multi-row values */
	private boolean valsArrIsnull;
	
	public ValueList(int size) {
		if (size > 0)
			valsArr = new AbsPart[size];
		valsArrIsnull = true;
	}

	public ValueList constv(int idx, String v) throws TransException {
		return v(idx, new ExprPart("'" + v + "'"));
//		ExprPart vpart;
//		if (sctx.colType().isString())
//			vpart = new ExprPart("'" + v + "'");
//		else 
//			vpart = new ExprPart(v);
//
//		return v(idx, vpart);
	}

	public ValueList v(int idx, AbsPart v) throws TransException {
		if (idx < 0 || v == null)
			return this;

		if (valst != null)
			throw new TransException("Don't use both list and array mode in ValueList.");

		// valsArr[idx] = new ExprPart("'" + v + "'");
		valsArr[idx] = v;
		valsArrIsnull = false;
		return this;
	}

	public ValueList constv(String v) throws TransException {
		throw new TransException("yes it's realy used.");
		// return v(new ExprPart("'" + v + "'"));
	}
	
	/**Add value to the last column.
	 * @param v
	 * @return
	 * @throws TransException
	 */
	public ValueList v(AbsPart v) throws TransException {
		if (valsArr != null)
			throw new TransException("Don't use both list and array mode in ValueList.");

		if (valst == null)
			valst = new ArrayList<AbsPart>();
		valst.add(v);
		return this;
	}

	@Override
	public String sql(ISemantext context) {
		if (valst == null && valsArrIsnull) return "null";
//		else if (valst != null){
//			return valst.stream().map(v -> v == null ? "null" : "'" + v + "'").collect(Collectors.joining(", "));
//		}
//		else
//			return Arrays.stream(valsArr).map(v -> v == null ? "null" : "'" + v + "'").collect(Collectors.joining(", "));
		else if (valst != null)
			return valst.stream().map(v -> {
				try {
					return v == null ? "null" : v.sql(context);
				} catch (TransException e) {
					e.printStackTrace();
					return "";
				}
			}).collect(Collectors.joining(", "));
		else
			return Arrays.stream(valsArr).map(v -> {
				try {
					return v == null ? "null" : v.sql(context);
				} catch (TransException e) {
					e.printStackTrace();
					return null;
				}
			}).collect(Collectors.joining(", ", "(", ")"));
	}


}
