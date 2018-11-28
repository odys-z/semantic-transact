package io.odysz.transact.sql.parts.select;

import java.util.ArrayList;
import java.util.stream.Collectors;

import io.odysz.transact.sql.parts.AbsPart;

/**Order by list. A short cut for:<pre>
https://github.com/antlr/grammars-v4/blob/master/tsql/TSqlParser.g4

// https://msdn.microsoft.com/en-us/library/ms176104.aspx
query_specification
    : SELECT (ALL | DISTINCT)? top_clause?
      select_list
      // https://msdn.microsoft.com/en-us/library/ms188029.aspx
      (INTO table_name)?
      (FROM table_sources)?
      (WHERE where=search_condition)?
      // https://msdn.microsoft.com/en-us/library/ms177673.aspx
      (GROUP BY (ALL)? group_by_item (',' group_by_item)*)?
      (HAVING having=search_condition)?
	;

group_by_item
	: expression
	;
</pre>
 * @author ody
 */
public class GroupbyList extends AbsPart {

	private ArrayList<String> groups;

	public GroupbyList(ArrayList<String> groupBys) {
		groups = groupBys;
	}

	@Override
	public String sql() {
		return groups.stream().collect(Collectors.joining(", ", "group by ", ""));
	}

}
