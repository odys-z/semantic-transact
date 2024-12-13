package io.odysz.transact.sql;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.Utils;
import io.odysz.common.dbtype;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.sql.parts.insert.ColumnList;
import io.odysz.transact.sql.parts.insert.InsertValues;
import io.odysz.transact.sql.parts.insert.InsertValuesOrcl;
import io.odysz.transact.sql.parts.update.SetList;
import io.odysz.transact.x.TransException;

/**
 * sql: insert into tabl(...) values(...) / select ...
 * 
 * Experimental version for supporting onDuplicate (UPSERT).
 * 
 * @author Ody
 *
 */
public class InsertExp extends Insert {
	/**
	 * Is Upsert.
	 * @since 1.4.40
	 */
	protected boolean upsert;
	
	protected String[] onConflictFields;

	/**
	 * Updating nvs for Upsert.
	 * @since 1.4.40
	 */
	protected ArrayList<Object[]> updateNvs;

	/**
	 * Insert statement intialization, not come with a post operation for committing SQL.
	 * So don't confused with DATranscxt#Insert(String tabl, IUser usr), which will instert into 
	 * DB when calling ins().
	 * 
	 * @param transc
	 * @param tabl
	 */
	protected InsertExp(Transcxt transc, String tabl) {
		super(transc, tabl);
	}

	/**
	 * <p>Solution for UPSERT.</p>
	 * <h5>NOTE:</h5>
	 * <p>UPSERT is not a standard SQL syntax (April 2024), and currently only sql for
	 *  Sqlite3 are verified (call {@link #onConflict(String[], Object...)}, not this).
	 *  For MS Sql Server, must use {@link #onDuplicate(Query)}.
	 *  Open an issue at <a href="https://github.com/odys-z/semantic-transact/issues">Github</a>
	 * if the features are needed.</p>
	 * <h5>References:</h5>
	 * <ul>
	 * <li><a href="https://sqlite.org/lang_upsert.html">Sqlite UPSERT</a><br>
	 * INSERT INTO ... VALUES('jovial') ON CONFLICT(word) DO UPDATE SET count=count+1</li>
	 * <li><a href="https://dev.mysql.com/doc/refman/8.0/en/insert-on-duplicate.html">
	 * 15.2.7.2 INSERT ... ON DUPLICATE KEY UPDATE Statement, MySql Documentation</a> and
	 * <a href="https://blog.devart.com/mysql-upsert.html">
	 * MySQL UPSERT: Comprehensive Examples and Use Cases</a><br>
	 * INSERT INTO table_name (column1, column2, ...) VALUES (value1, value2, ...)
	 * ON DUPLICATE KEY UPDATE column1 = value1, column2 = value2, ...;
	 * </li>
	 * <li><a href="https://stackoverflow.com/a/27989832/7362888">
	 * StackOverflow: How to upsert (update or insert) in SQL Server 2005</a><br>
	 * IF NOT EXISTS (SELECT * FROM dbo.Employee WHERE ID = @SomeID)
	 * INSERT INTO dbo.Employee(Col1, ..., ColN)
	 * VALUES(Val1, .., ValN)
	 * ELSE UPDATE dbo.Employee
	 * SET Col1 = Val1, Col2 = Val2, ...., ColN = ValN
	 * WHERE ID = @SomeID
	 * </li>
	 * <li><a href="https://docs.oracle.com/en/database/other-databases/nosql-database/23.3/sqlreferencefornosql/upsert-statement.html">
	 * Upsert statement, Oracle Help Center</a><br>
	 * <pre>upsert_statement ::=
	 * [variable_declaration]
	 * UPSERT INTO table_name 
	 * [AS] table_alias]
	 * ["(" id ("," id)* ")"]
	 * VALUES "(" insert_clause ("," insert_clause)* ")"
	 * [SET TTL ttl_clause ]
	 * [returning_clause]</pre>
	 * </li>
	 * </ul>
	 * @param unvs updating name-value pairs, if null, use all values set by
	 * {@link #nv(String, AbsPart) nv()} or {@link Update#nvs(ArrayList) nvs()} for insert statement.
	 * @return this
	 */
	public InsertExp onDuplicate(ArrayList<Object[]> unvs) {
		this.upsert = true;
		this.updateNvs = unvs;
		return this;
	}
	
	public InsertExp onDuplicate(Object... unv) {
		this.upsert = true;
		this.updateNvs = new ArrayList<Object[]>();
		for (int i = 0; unv != null && i < unv.length; i += 2) {
			updateNvs.add(new Object[] {unv[i], unv[i+1]});
		}
		return this;
	}

	/**
	 * For MS Sql Server only. Not implemented.
	 * @see #onDuplicate(ArrayList)
	 * @param select
	 * @return this
	 */
	public InsertExp onDuplicate(Query select) {
		return this;
	}

	/**
	 * For Sqlite's UPSERT only.
	 * @see #onDuplicate(ArrayList)
	 * @return this
	 * @since 1.4.40
	 */
	public InsertExp onConflict(String[] fields, ArrayList<Object[]> nvs) {
		this.onConflictFields = fields;
		return onDuplicate(nvs);
	}
	
	/**
	 * For Sqlite' UPSERT only.
	 * @see #onDuplicate(ArrayList)
	 * @return this
	 */
	public InsertExp onConflict(String[] fields, Object ... nvs) {
		this.onConflictFields = fields;
		return onDuplicate(nvs);
	}

	/**
	 * sql: insert into tabl(...) values(...) / select ...
	 * 
	 * @see io.odysz.transact.sql.parts.AbsPart#sql(ISemantext)
	 */
	@Override
	public String sql(ISemantext sctx) {
		boolean hasVals = valuesNv != null 
				&& valuesNv.size() > 0
				&& valuesNv.get(0) != null
				&& valuesNv.get(0).size() > 0;
		if (!hasVals && selectValues == null) {
			Utils.warn("[Insert#sql()] Trying to stream a Insert statement without values, table %s, conn %s.",
					this.mainTabl.name(), sctx.connId());
			return "";
		}

		Stream<String> s = Stream.of(upsert && sctx.dbtype() == dbtype.oracle ?
				new ExprPart("upsert into") : new ExprPart("insert into"),
				mainTabl, mainAlias,
				// (...)
				new ColumnList(insertCols),
				// values(...)
				hasVals
					? sctx != null && sctx.dbtype() == dbtype.oracle ?
						new InsertValuesOrcl(mainTabl.name(), insertCols, valuesNv) :
						new InsertValues(mainTabl.name(), insertCols, valuesNv)
					: null,
				selectValues,

				// ON DUPLICATE KEY UPDATE 
				upsert && sctx.dbtype() == dbtype.sqlite ? new ExprPart(String.format("on conflict(%s) do update set", Stream.of(onConflictFields).filter(f -> f != null).collect(Collectors.joining(", ")))) : null,
				upsert && sctx.dbtype() == dbtype.mysql ? new ExprPart("on duplicate key update") : null,
				upsert && (sctx.dbtype() == dbtype.sqlite || sctx.dbtype() == dbtype.mysql) && updateNvs != null ? new SetList(updateNvs) : null
		).filter(w -> w != null)
		.map(m -> {
				try {
					return m == null ? "" : m.sql(sctx);
				} catch (TransException e) {
					e.printStackTrace();
					return "";
				}
			});

		return s.collect(Collectors.joining(" "));
	}

}
