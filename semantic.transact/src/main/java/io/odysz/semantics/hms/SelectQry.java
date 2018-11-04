/*
package com.healthmarketscience.sqlbuilder;


Copyright (c) 2008 Health Market Science, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package io.odysz.semantics.hms;

import com.healthmarketscience.sqlbuilder.SelectQuery;

/**
 * Query which generates a SELECT statement.  Supports arbitrary columns
 * (including "DISTINCT" modifier), "FOR UPDATE" clause, all join types,
 * "WHERE" clause, "GROUP BY" clause, "ORDER BY" clause, and "HAVING" clause.
 * <p/>
 * Note that the "OFFSET" and "FETCH NEXT" clauses are supported from "SQL
 * 2008".
 * <p/>
 * If Columns are used for any referenced columns, and no complicated joins
 * are required, the table list may be left empty and it will be
 * auto-generated in the append call.  Note, that this is not the most
 * efficient method (as this list will not be cached for the future due to
 * mutability constraints on <code>appendTo</code>).
 * <p/>
 * Note that this query supports custom SQL syntax, see {@link Hook} for more
 * details.
 *
 * @author James Ahlborn
 */
public class SelectQry extends SelectQuery {
}
