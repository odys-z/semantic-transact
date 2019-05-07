/* Select parts, such as select_list_elem.
 * see https://github.com/antlr/grammars-v4/blob/master/tsql/TSqlParser.g4
 * compile:
 * java -jar /home/ody/d/ubuntu/antlr4/antlr-4.7.1-complete.jar SelectParts.g4 -visitor -package gen.antlr.sql.select
 */

parser grammar SelectParts;
options { tokenVocab=TSqlLexer; }
import SearchExprs;

// https://msdn.microsoft.com/en-us/library/ms176104.aspx
select_list
    : select_list_elem (',' select_list_elem)*
	;

select_list_elem
    : asterisk
    | column_elem
    // TODO to be understood
    // | udt_elem
    // Odys-z function-call
    | function_call as_column_alias
    
    // Modified
    // | expression_elem
    | expression as_column_alias?
    | expression IS null_notnull
	;

as_column_alias
    : AS? column_alias
	;
	
column_alias
    : id
    | STRING
	;

column_elem
    // : (table_name '.')? (column_name=id | '$' IDENTITY | '$' ROWGUID) as_column_alias?
    // changed:
    : (table_name '.')? (column_name=id) as_column_alias?
    ;

asterisk
    : '*'
    | table_name '.' asterisk
    ;

