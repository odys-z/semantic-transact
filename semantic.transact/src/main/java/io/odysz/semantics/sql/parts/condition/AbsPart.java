package io.odysz.semantics.sql.parts.condition;

/**<pre>
https://github.com/antlr/grammars-v4/blob/master/tsql/TSqlParser.g4
search_condition     : search_condition_and (OR search_condition_and)* ;
search_condition_and : search_condition_not (AND search_condition_not)* ;
search_condition_not : NOT? predicate ;
predicate            : expression comparison_operator expression
                     | (search_condtion);
expression           : expression op expression
                     | (search_condition);
</pre>
 * @author ody
 *
 */
abstract public class AbsPart {

}
