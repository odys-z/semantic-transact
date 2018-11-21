/* Search condtion 
 * see https://github.com/antlr/grammars-v4/blob/master/tsql/TSqlParser.g4
 * section select_statement, seach_conditon_list and expression
 * compile:
 * java -jar /home/ody/d/ubuntu/antlr4/antlr-4.7.1-complete.jar TSqlLexer.g4 -package gen.antlr.sql.exprs
 * java -jar /home/ody/d/ubuntu/antlr4/antlr-4.7.1-complete.jar SearchExprs.g4 -visitor -package gen.antlr.sql.exprs
 */
 

parser grammar SearchExprs;
options { tokenVocab=TSqlLexer; }

search_condition
    : search_condition_and (OR search_condition_and)*
    ;

search_condition_and
    : search_condition_not (AND search_condition_not)*
    ;

search_condition_not
    : NOT? predicate
    ;

predicate
    // maybe replace subquery with sql-id?
    : expression comparison_operator expression
    | expression NOT? IN '(' expression_list ')'
    | expression NOT? LIKE expression 
    | expression IS null_notnull
    | '(' search_condition ')'
    ;

// Expression.

expression_list
    : expression (',' expression)*
    ;

expression
    : primitive_expression
    | function_call
    | full_column_name
    | bracket_expression
    | unary_operator_expression
    | expression op=('*' | '/' | '%') expression
    | expression op=('+' | '-' | '&' | '^' | '|' | '||') expression
    | expression comparison_operator expression
    | expression assignment_operator expression
    ;

function_call
    : aggregate_windowed_function
    | func_proc_name '(' expression_list? ')'
    ;

// https://msdn.microsoft.com/en-us/library/ms173454.aspx
aggregate_windowed_function
    : (AVG | MAX | MIN | SUM | STDEV | STDEVP | VAR | VARP)
      '(' full_column_name ')'
    | (COUNT | COUNT_BIG)
      '(' ('*' | full_column_name) ')'
    ;

func_proc_name
    : procedure=id
    ;

/*  There are some RESERVED WORDS that can be column names */
full_column_name
    : (table_name '.')? column_name=id
    ;

table_name
    : (database=id '.' (schema=id)? '.' | schema=id '.')? table=id
    | (database=id '.' (schema=id)? '.' | schema=id '.')? BLOCKING_HIERARCHY
    ;


primitive_expression
    : DEFAULT | NULL | LOCAL_ID | constant
    ;

unary_operator_expression
    : '~' expression
    | op=('+' | '-') expression
    ;

bracket_expression
    : '(' expression ')' 
    ;

// Odys-z: Not Used?
constant_expression
    : NULL
    | constant
    // system functions: https://msdn.microsoft.com/en-us/library/ms187786.aspx
    | function_call
    // TODO: variables
    // | LOCAL_ID         // TODO: remove.
    | '(' constant_expression ')'
    ;

// https://msdn.microsoft.com/en-us/library/ms188074.aspx
// Spaces are allowed for comparison operators.
comparison_operator
    : '=' | '>' | '<' | '<' '=' | '>' '=' | '<' '>' | '!' '=' | '!' '>' | '!' '<' | '%' '~' | '%' | '~' '%' 
    ;

assignment_operator
    : '+=' | '-=' | '*=' | '/=' | '%=' | '&=' | '^=' | '|='
    ;

null_notnull : NOT? NULL ;

// https://msdn.microsoft.com/en-us/library/ms179899.aspx
constant
    : STRING // string, datetime or uniqueidentifier
    | BINARY
    | sign? DECIMAL
    | sign? (REAL | FLOAT)  // float or decimal
    // TODO - WHAT FOR?
    // | sign? dollar='$' (DECIMAL | FLOAT)       // money
    ;

sign
    : '+'
    | '-'
    ;

id
    : ID
    | AGGREGATE
    | AVG
    | BIGINT
    | BINARY_BASE64
    | COUNT
    | COUNT_BIG
    | COUNTER
    | DATEADD
    | DATEDIFF
    | DATENAME
    | DATEPART
    | DAYS
    | FIRST
    | FIRST_VALUE
    | FOLLOWING
    | HOURS
    | IDENTITY_VALUE
    | INIT
    | INT
    | LAST
    | LAST_VALUE
    | LOW
    | MAX
    | MIN
    | MINUTES
    | NUMBER
    | ROW
    | ROW_NUMBER
    | ROWCOUNT
    | SUM
    | TIME
    ;

