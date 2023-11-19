package io.odysz.transact.sql;

import io.odysz.semantics.ISemantext;
import io.odysz.semantics.IUser;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.select.WithClause;
import io.odysz.transact.x.TransException;

/**
 * <p>Transaction Context, also can be take as a Transaction / Batching SQL builder creator.</p> 
 * <p>A Transcxt is typically plugged in with ISemantext, which is the handler of semantics.</p>
 * <p>When building sql, events like onInserting, etc. are fired to ISemantext.
 * @author odys-z@github.com
 */
public class Transcxt {

	protected ISemantext basictx;
	public ISemantext basictx() { return basictx; }

	/**
	 * Get a {@link ISemantext} that typically handle configured semantics,
	 * @param conn not used
	 * @param usr the session user
	 * @return a new instance for building sql, resulving sql, etc.
	 * @throws TransException 
	 */
	public ISemantext instancontxt(String conn, IUser usr) throws TransException {
		return basictx == null ? null : basictx.clone(usr).connId(conn == null ? basictx.connId() : conn);
	}

	/**
	 * Create a statements manager.
	 * @param staticSemantext A static semantic providing basic DB access, used to generate autoID etc.
	 */
	public Transcxt(ISemantext staticSemantext) {
		basictx = staticSemantext;
	}
	
	public Query select(String tabl, String ... alias) {
		Query q = new Query(this, tabl, alias).with(withClause);
		this.withClause = null;
		return q;
	}
	
	public Insert insert(String tabl) {
		return new Insert(this, tabl);
	}
	
	public Update update(String tabl) {
		return new Update(this, tabl);
	}
	
	public Delete delete(String tabl) {
		return new Delete(this, tabl);
	}

	public TableMeta tableMeta(String tabl) {
		return basictx == null ? null :
			basictx.tablType(tabl).conn(basictx.connId());
	}

	/**
	 * Get the connection's table meat.
	 * @param conn
	 * @param tabl
	 * @return table meta
	 * @throws TransException
	 */
	public TableMeta tableMeta(String conn, String tabl) throws TransException {
		throw new TransException("This method must be ovrriden by DA layser.");
	}

	/**
	 * <p>If v is an instance of string, add "'" according to db type;
	 * if it is an instance of {@link io.odysz.transact.sql.parts.AbsPart AbsPart}, return it directly.</p>
	 * The null/empty values are handled differently according data meta.<br>
	 * See the <a href='https://odys-z.github.io/notes/semantics/ref-transact.html#ref-transact-empty-vals'>discussions</a>.
	 * which makes the method parameter complicate.
	 * @param v
	 * @param conn
	 * @param tabl
	 * @param col
	 * @return Sql AST node for generating sql.
	 */
	public AbsPart quotation(Object v, String conn, String tabl, String col) {
		throw new NullPointerException("This method must be ovrriden by DA layser.");
	}

	/**
	 * [[with clause' as-tabl-name, {@link Query}]]
	 * 
	 * @since 1.4.36
	protected ArrayList<AbsPart[]> withs;
	protected boolean withRecursive;
	 */

	/**
	 * [[with clause' as-tabl-name, {@link Query}]]
	 * 
	 * @since 1.4.36
	 */
	private WithClause withClause;

	/**
	 * <h5>With clause for multiple tables without recursive query.</h5>
	 * <ol>
	 * 	<li><a href='https://www.sqlite.org/lang_with.html'>SQLite, The WITH Clause</a></li>
	 * 	<li><a href='https://dev.mysql.com/doc/refman/8.0/en/with.html'>
	 * MySql 8.0, 13.2.20 WITH (Common Table Expressions)</a></li>
	 * 	<li><a href='https://oracle-base.com/articles/misc/with-clause'>
	 * Oracle, WITH Clause : Subquery Factoring in Oracle</a></li>
	 * 	<li><a href='https://learn.microsoft.com/en-us/sql/t-sql/queries/with-common-table-expression-transact-sql?view=sql-server-ver16'>
	 * WITH common_table_expression (Transact-SQL)</a></li>
	 * </ol>
	 * @since 1.4.36
	 * @param q0
	 * @param qi
	 * @return this
	 */
	public Transcxt with(Query q0, Query... qi) {
//		this.withRecursive = false;
//		if (this.withs == null)
//			this.withs = new ArrayList<AbsPart[]>();
//
//		if (!isNull(q0)) {
//			if (isblank(q0.alias()))
//					Utils.warn("Adding with-table without alias? %s", q0.sql(null));;
//			this.withs.add(new AbsPart[] {null, q0});
//		}
//		if (!isNull(qi))
//			for (Query q : qi) {
//				if (isblank(q.alias()))
//						Utils.warn("Adding with-table without alias? %s", q.sql(null));;
//				this.withs.add(new AbsPart[] {null, q});
//			}
		
		if (this.withClause == null)
			this.withClause = new WithClause(false);
		this.withClause.with(q0, qi);
		return this;
	}

	/**
	 * For adding a recursive table.
	 * 
	 * @param recursive
	 * @param recurTabl
	 * @param rootValue
	 * @param q
	 * @return
	 */
	public Transcxt with(boolean recursive, String recurTabl, String rootValue, Query q) {
		if (withClause == null)
			this.withClause = new WithClause(recursive);
		withClause.with(recurTabl, rootValue, q);
		return this;
	}
}
