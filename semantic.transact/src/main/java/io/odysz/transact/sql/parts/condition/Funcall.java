package io.odysz.transact.sql.parts.condition;


/**
 * <pre>
function_call
    : aggregate_windowed_function
    | func_proc_name '(' expression_list? ')'
    ;</pre>
 * @author ody
 *
 */
public class Funcall extends ExprPart {

	public Funcall(String rexp) {
		super(rexp);
	}

}
