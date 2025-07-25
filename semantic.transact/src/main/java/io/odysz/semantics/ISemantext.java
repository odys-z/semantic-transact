package io.odysz.semantics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.odysz.common.LangExt;
import io.odysz.common.dbtype;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.sql.Delete;
import io.odysz.transact.sql.Insert;
import io.odysz.transact.sql.Statement;
import io.odysz.transact.sql.Statement.IPostOptn;
import io.odysz.transact.sql.Statement.IPostSelectOptn;
import io.odysz.transact.sql.Transcxt;
import io.odysz.transact.sql.Update;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.x.TransException;

/**
 * <h6>Interface for Semantic Event Handler</h6>
 * <p>In short, an ISemantext is the statement runtime context for building sql.</p>
 * <p>A semantic event can be for example starting an insert transaction with a data row.
 * Such data may needing some processing defined by the application requirements.</p>
 *
 * <p>For example, a "fullpath" field in a table means a deep first traveling notation of tree nodes.</p>
 * In this case, a user defined semantic event handler can compose this data (fullpath) according to it's parent's
 * fullpath, then append the field into the inserting row.</p>
 *
 * <p>Semantic-transact is designed only process SQL structure, not handling the data semantics,
 * so it only fire the event to the handler, to the implementation of ISemantext. </p>
 *
 * <p>An ISemantext instance is used as an sql composing context
 * by semantic-transact when travel the AST and composing SQL(s). There must be an {@link #insert}
 * event which fired at the beginning of composing an insert sql, and one event for inserting each row,
 * the {@link #onInsert}.</p>
 * @author odys-z@github.com
 *
 */
public interface ISemantext {
	/**Match referencing string like "RESULVE tabl.col".<br>
	 * Regex.findGroups():<br>
	 * [0] RESULVE<br>
	 * [1] task_nodes<br>
	 * [2] taskId
	 * replaced by {@link io.odysz.transact.sql.parts.Resulving Resulving}
	String refPattern = "^\\s*(RESULVE)\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*$";
	 * */

	/**
	 * @return current connId
	 */
	public String connId();

	/**Set connId for committing statement.
	 * @param conn
	 * @return this context
	 */
	public ISemantext connId(String conn);

	/**
	 * <p>Create a context for the insert-sql composing process.</p>
	 * <p>Called when starting an inserting transaction's sql composing.</p>
	 * <p>The parameter <i>usr</i> is optional if the semantics handler don't care about user's fingerprint.</p>
	 * @param insert
	 * @param mainTabl
	 * @param usr user information used for modify sql AST
	 * @return the new ISemantext context instance for resolving semantics.
	 * @throws SQLException
	 */
	public ISemantext insert(Insert insert, String mainTabl, IUser usr) throws SQLException;

	/**
	 * <p>Create a context for the update-sql composing process.</p>
	 * <p>Called when starting an update transaction sql composing.</p>
	 * <p>The parameter usr is optional if the semantics handler don't care about user's fingerprint.</p>
	 * @param update
	 * @param mainTabl
	 * @param usr user information used for modify sql AST
	 * @return new ISemantext for update statement
	 * @throws SQLException
	 */
	public ISemantext update(Update update, String mainTabl, IUser usr) throws SQLException;

	/**
	 * <p>Resolving inserting values, e.g an AUTO key is generated here.</p>
	 * <p>Called each time an {@link Insert} statement found itself will composing a insert-sql ({@link Insert#sql(ISemantext)})</p>
	 * @param insert
	 * @param tabl
	 * @param rows [ list[Object[n, v], ... ], ... ]
	 * @return the ISemantext context, a thread safe context for resolving semantics like FK value resolving.<br>
	 * @throws SemanticException
	 */
	ISemantext onInsert(Insert insert, String tabl, List<ArrayList<Object[]>> rows) throws TransException;

	/**
	 * <p>Resolves values for updating.</p>
	 * Called each time an {@link Update} statement found itself will composing an update-sql.
	 * @param update
	 * @param tabl
	 * @param nvs
	 * @return the update context
	 * @throws SemanticException
	 */
	public ISemantext onUpdate(Update update, String tabl, ArrayList<Object[]> nvs) throws TransException;

	public ISemantext onDelete(Delete delete, String tabl, Condit whereCondt) throws TransException;

	/**Handle wiring back resulved values, etc.
	 * Called when all children sql generated (posts' commit() called).
	 * @param stmt
	 * @param mainTabl
	 * @param row
	 * @param sqls
	 * @return this
	 * @throws TransException failed handling semantics
	 */
	public ISemantext onPost(Statement<?> stmt, String mainTabl, ArrayList<Object[]> row, ArrayList<String> sqls) throws TransException;

	/**
	 * Get results from semantics' handling context.<br>
	 * Typically it's a new inserting records' auto Id,
	 * which should usually let the caller / client know about it.
	 * 
	 * @param table
	 * @param col
	 * @param idx element index, -1 for the last one, -2 for the last 2nd, and so on.
	 * @return RESULt resoLVED VALues in tabl.col, or null if not exists.
	 * @since 1.4.40 The resulved value is from an ArrayList, indexed by {@code idx}.
	 */
	default Object resulvedVal(String table, String col, int idx) {
		List<Object> lst = resulvedVals(table, col);
		if (LangExt.isNull(lst))
			return null;
		return idx < 0 ? lst.get(lst.size() + idx) : lst.get(idx);
	}

	/**
	 * Get results from semantics' handling context.
	 * 
	 * @param table
	 * @param col
	 * @return the list of resulved values
	 */
	List<Object> resulvedVals(String table, String col);

	/**Get all the resolved results,
	 * a.k.a return value of {@link Update#doneOp(io.odysz.transact.sql.Statement.IPostOptn)}.*/
	public SemanticObject resulves();

	/**
	 * Get the dbtype handled by the context
	 * 
	 * @return db type
	 */
	public dbtype dbtype();

	/**
	 * Generate an auto increasing ID for tabl.col, where connection is initialized when constructing this implementation.<br>
	 * The new generated value is managed in this implementation class (for future resolving).<br>
	 * 
	 * <b>side effect</b>: generated auto key already been put into autoVals, can be referenced later.
	 * 
	 * @param tabl
	 * @param col
	 * @param prefixCol optional for key's prefix, must fed with data when auto-key generation triggered
	 * @return new auto key.
	 * @throws SQLException
	 * @throws TransException
	 */
	String genId(String conn, String tabl, String col, String ... prefixCol) throws SQLException, TransException;

	/**Create a new instance for a semantics processing.
	 * @param usr
	 * @return new semantext instance
	 */
	public ISemantext clone(IUser usr);

	/**
	 * TODO rename as tableMeta()
	 * @param tabl
	 * @return meta
	 */
	public TableMeta tablType(String tabl);

	/**Concatenate the path for the file system (without file name) for the running environment
	 * - typically for resolving a relative path to the WEB-INF/sub[0]/sub[1]/...
	 * @param subs
	 * @return either a {@link io.odysz.transact.sql.parts.Resulving Resulving} or a constant string
	 * @throws TransException path resolving failed
	 */
	public String relativpath(String... subs) throws TransException;

	/**Get the container's runtime root path<br>
	 * For servlet, return the absolute WEB-ROOT, for java application, return the starting relative dir.
	 * 
	 * @deprecated TODO FIXME redundant to {@link Transcxt#runtimeRoot()}
	 * @return the root path
	 */
	public String containerRoot();

	void addOnRowsCommitted(IPostOptn op);

	/**
	 * Add table wise handler for successful commitment
	 * - multiple rows in one table can only be called once.
	 * @param tabl
	 * @param op
	 */
	default void addOnTableCommitted(String tabl, IPostOptn op) { }

	default IPostOptn onTableCommittedHandler(String tabl) { return null; }

	/**
	 * <p>When the commitment succeeded, there are still things must be done,
	 * like deleting external files.</p>
	 * The operations which are (instances of {@link IPostOptn} lambda expression,
	 * are pushed into semantext while handling semantics, via {@link #addOnRowsCommitted(IPostOptn)}
	 * &amp; {@link #addOnTableCommitted(String, IPostOptn)}.
	 * @param ctx
	 * @param tabl
	 * @throws TransException
	 * @throws SQLException
	 */
	void onCommitted(ISemantext ctx, String tabl) throws TransException, SQLException;

	/**On selected event handler, the chance that the resultset can be modified.
	 * @param resultset any result object that can be understood by handler. e.g. SResultSet
	 * @throws SQLException iterating on resultset failed
	 * @throws TransException handling failed
	 */
	public void onSelected(Object resultset) throws SQLException, TransException;

	/**Check is an operator already exists.
	 * @param name handler name
	 * @return true if has the named operator
	 */
	boolean hasOnSelectedHandler(String name);

	/**Add an post selected operation.
	 * <p>E.g. extFile Funcall will add a post file reading and replacing operation here.</p>
	 * <p>Only one type of handler can be added to a context. Use {@link #hasOnSelectedHandler(String)}
	 * to check is there a same type of handler already been added.</p>
	 * <p>Operations are managed as a linked hash map. All rows are iterated and processed by
	 * op one by one, from first to last, independently.</p>
	 * <p>For each row, operations are iterated in the order of been added.</p>
	 * @param op
	 */
	void addOnSelectedHandler(String name, IPostSelectOptn op);

	/**
	 * <p>Compose the v provide by client into target table column's value represented in sql,
	 * whether add single quote or not.</p>
	 * <p>If v is an instance of string, add "'" according to db type;
	 * if it is an instance of {@link io.odysz.transact.sql.parts.AbsPart AbsPart}, return it directly.</p>
	 * The null/empty values are handled differently according data meta.<br>
	 * See the <a href='https://odys-z.github.io/notes/semantics/ref-transact.html#ref-transact-empty-vals'>discussions</a>.
	 * which makes the method parameter complicate.
	 * @deprecated moved to {@link io.odysz.transact.sql.Transcxt#quotation(Object, String, String, String)}
	 * @param v
	 * @param tabl
	 * @param col
	 * @return the composed value object
	 * @throws TransException
	 */
	public default AbsPart composeVal(Object v, String tabl, String col) throws NullPointerException {
		throw new NullPointerException("This method is replaced by io.odysz.transact.sql.Transcxt#quotation().");
	};

	/**
	 * Get table meta. The returned meta is a semantics extended meta.
	 * E.g. the SyntityMeta will register itself for handling synchronizing semantics.
	 *
	 * All metas are managed by {@code Semantic.DA/io.odysz.semantic.DA.Connects}, so not usable
	 * without a Semantic.DA layer.
	 * @param tbl
	 * @return meta
	 */
	public default TableMeta getTableMeta(String tbl) { return null; }

	/**
	 * Set the creating builder as basic builder.
	 * A helper for switching semantics hander.
	 * In 2.0.0, only builder used by semantics handler should return itself
	 * by implementing this.
	 * 
	 * @since 1.4.41
	 * @param  creator
	 * @return this
	 */
	public default <B extends Transcxt>  ISemantext creator(B creator) { return this; } 
}
