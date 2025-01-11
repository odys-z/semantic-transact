package io.odysz.transact.sql;

import static io.odysz.common.LangExt.f;

import io.odysz.semantics.ISemantext;
import io.odysz.semantics.IUser;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.select.WithClause;
import io.odysz.transact.x.TransException;

/**
 * <p>Transaction builder, also can be take as a Transaction / Batching SQL builder,
 * with semantics context's creator.</p> 
 * <p>A Transcxt is typically plugged in with ISemantext, which is the handler of semantics.</p>
 * <p>When building sql, events like onInserting, etc. are fired to ISemantext.
 * TODO this is supposed to be renamed as TransBuilder in the future.
 * 
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
	 * @param semantext A static semantic providing basic DB access, used to generate autoID etc.
	 */
	public Transcxt(ISemantext semantext) {
		basictx = semantext;
		if (semantext != null)
			semantext.creator(this);
	}
	
	private WithClause removeWiths() {
		WithClause w = withClause;
		withClause = null;
		return w;
	}
	
	public Query select(String tabl, String ... alias) {
		Query q = (Query) new Query(this, tabl, alias).with(removeWiths());
		this.withClause = null;
		return q;
	}
	
	public Query select(Query sub, String ... alias) {
		Query q = (Query) new Query(this, sub.asQueryExpr(true), alias).with(removeWiths());
		this.withClause = null;
		return q;
	}
	
	public Query selectPage(String tabl, String ... alias) {
		Query q = (Query) new QueryPage(this, tabl, alias).with(removeWiths());
		this.withClause = null;
		return q;
	}

	public Query selectPage(Query sub, String ... alias) {
		Query q = (Query) new QueryPage(this, sub.asQueryExpr(true), alias).with(removeWiths());
		this.withClause = null;
		return q;
	}

	public Insert insert(String tabl) {
		return new Insert(this, tabl);
	}

	public InsertExp insertExp(String tbl) {
		return new InsertExp(this, tbl);
	}
	
	public Update update(String tabl) {
		return (Update) new Update(this, tabl).with(removeWiths());
	}
	
	public Delete delete(String tabl) {
		return (Delete) new Delete(this, tabl).with(removeWiths());
	}

	public TableMeta tableMeta(String tabl) {
		if (basictx != null && basictx.tablType(tabl) == null)
			// Utils.warn(
			throw new NullPointerException(f(
				"[%s]\nERROR: Table information doesn't exist.\n" +
				"Table: %s, Connection: %s." +
				"\nSince 1.5.0, table metas are loaded for differenct connections respectively - can't reference across connections.",
				basictx.connId(), tabl, basictx == null ? null : basictx.connId()));
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
		throw new TransException("This method must be ovrriden by a DA layer.");
	}

	/**
	 * <p>If v is an instance of string, add "'" according to db type;
	 * if it is an instance of {@link io.odysz.transact.sql.parts.AbsPart AbsPart}, return it directly.</p>
	 * The null/empty values are handled differently according to data's meta.<br>
	 * See the <a href='https://odys-z.github.io/notes/semantics/ref-transact.html#ref-transact-empty-vals'>discussions</a>.
	 * which makes the method parameter complicate.
	 * @param v
	 * @param conn
	 * @param tabl
	 * @param col
	 * @return Sql AST node for generating sql.
	 */
	public AbsPart quotation(Object v, String conn, String tabl, String col) {
		throw new NullPointerException("This method must be ovrriden by a DA layer.");
	}

	/**
	 * With clausse for query. Will be cleared when consumed by select().
	 * 
	 * @since 1.4.36 tested with SQLite.
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
	 * <pre>
	 *  st.with(st
	 *       .select("a_users", "u")
	 *       .j("h_photo_org", "ho", "ho.oid=u.orgId")
	 *       .whereEq("u.userId", "ody"))
	 *   .select("h_photos", "p")
	 *   .col(avg("filesize"), "notes")
	 *   .je("p", null, "u", "shareby", "userId")
	 *   .commit(st.instancontxt(null, null), sqls);
	 *
	 *   assertEquals("with " +
	 *       "u as (select * from a_users u join h_photo_org ho on ho.oid = u.orgId where u.userId = 'ody') " +
	 *       "select avg(filesize) notes from h_photos p join  u on p.shareby = u.userId",
	 *       sqls.get(0));</pre>
	 * @since 1.4.36
	 * @param q0
	 * @param qi
	 * @return this
	 */
	public Transcxt with(Query q0, Query... qi) {
		if (this.withClause == null)
			this.withClause = new WithClause(false);
		this.withClause.with(q0, qi);
		return this;
	}

	/**
	 * For adding a recursive table.
	 * <pre>
    st.with(true,
        "orgrec(orgId, parent, deep)", 
        "values('kerson', 'ur-zsu', 0)",
        st.select("a_orgs", "p")
            .col("p.orgId").col("p.parent").col(Funcall.add("ch.deep", 1))
            .je("p", "orgrec", "ch", "orgId", "parent"))
      .select("a_orgs", "o")
      .cols("orgName", "deep")
      .je("o", null, "orgrec", "orgId")
      .orderby("deep")
      .commit(st.instancontxt(null, null), sqls);
        
    assertEquals("with recursive "
      + "orgrec(orgId, parent, deep) as (values('kerson', 'ur-zsu', 0) union all select p.orgId, p.parent, (ch.deep + 1) from a_orgs p join orgrec ch on p.orgId = ch.parent) "
      + "select orgName, deep from a_orgs o join  orgrec on o.orgId = orgrec.orgId order by deep asc",
        sqls.get(0));
     * </pre>
     * 
     * The generated clause will be cleared after calling {@link #select(String, String...)}.
	 * 
	 * @param recursive
	 * @param recurTabl recursive table name, e. g. orgrec
	 * @param rootValue starting value, e. g. "values('kerson', 'ur-zsu', 0)"
	 * @param q the query used to union within this recursive table
	 * @return this
	 * @since 1.4.36 tested with SQLite.
	 */
	public Transcxt with(boolean recursive, String recurTabl, String rootValue, Query q) {
		if (withClause == null)
			this.withClause = new WithClause(recursive);
		withClause.with(recurTabl, rootValue, q);
		return this;
	}
}
