package io.odysz.semantics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.odysz.common.dbtype;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.sql.Insert;
import io.odysz.transact.sql.Statement;
import io.odysz.transact.sql.Update;
import io.odysz.transact.x.TransException;

/**<p>Interface for semantic event handler.</p>
 * 
 * <p>A semantic event can be for example starting an insert transaction with a data row.
 * Such data may needing some processing defined by the application requirements.</p>
 * 
 * <p>For example, a "fullpath" field in table means a deep first traveling order of tree nodes.</p>
 * In this case, a user defined semantic event handler can compose this data (fullpath) according to it's parent's
 * fullpath, then append the field into the inserting row.</p>
 * 
 * <p>Semantic-transact is designed only process SQL structure, not handling the data semantics,
 * so it only fire the event to the handler, the implementation of ISemantext. </p>
 * 
 * <p>An ISemantext instance is used as an sql composing context
 * by semantic-transact when travel the AST and composing SQL(s). There must be an {@link #insert}
 * event which fired at the beginning of composing an insert sql, and 1 or more times for inserting row
 * event, the {@link #onInsert}.</p>
 * @author ody
 *
 */
public interface ISemantext {
	/**Match referencing string like "RESULVE tabl.col".<br>
	 * Regex.findGroups():<br>
	 * [0] RESULVE<br>
	 * [1] task_nodes<br>
	 * [2] taskId*/
	String refPattern = "^\\s*(RESULVE)\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*$";

	public String connId();
	
	/**Called when starting a insert transaction's sql composing.<br>
	 * Create a context for the insert-sql composing process.<br>
	 * Parameter usr is optional if the semantics handler don't care about user's fingerprint. 
	 * @param insert
	 * @param mainTabl
	 * @param usr user information used for modify sql AST
	 * @return the new ISemantext context instance for resolving semantics.
	 */
	public ISemantext insert(Insert insert, String mainTabl, IUser... usr);

	/**Called when starting an update transaction sql composing.<br>
	 * Create a context for the update-sql composing process.<br>
	 * Parameter usr is optional if the semantics handler don't care about user's fingerprint. 
	 * @param update
	 * @param mainTabl
	 * @param usr user information used for modify sql AST
	 * @return new ISemantext for update statement
	 */
	public ISemantext update(Update update, String mainTabl, IUser... usr);

	/**Called each time an <@link Insert} statement found itself will composing a insert-sql ({@link Insert#sql(ISemantext)})<br>
	 * Resolving inserting values, e.g an AUTO key is generated here.
	 * @param insert
	 * @param tabl 
	 * @param rows [ list[Object[n, v], ... ], ... ]
	 * @return the ISemantext context, a thread safe context for resolving semantics like FK value resolving.<br>
	 * @throws SemanticException 
	 */
	public ISemantext onInsert(Insert insert, String tabl, List<ArrayList<Object[]>> rows) throws TransException;

	public ISemantext onPrepare(Insert insert, String tabl, List<ArrayList<Object[]>> rows);

	/**Called each time an <@link Update} statement found itself will composing an update-sql.
	 * @param update
	 * @param tabl
	 * @param nvs
	 * @return the update context
	 */
	public ISemantext onUpdate(Statement<? extends Statement<?>> update, String tabl, ArrayList<Object[]> nvs);

	/**Get results from handling semantics. typically new inserting records' auto Id,
	 * which should usually let the caller / client know about it.
	 * @param table
	 * @param col
	 * @return RESULt resoLVED VALue in tabl.col
	 */
	Object resulvedVal(String table, String col);

	/**If parameter is a string in patter of "RESOLVE x.y" (formated by {@link #formatResulv(String, String)},
	 * Find and return referee.
	 * @param ref
	 * @return resolved value from restult
	 */
	public Object resulvedVal(String ref);

	/**Get all the resolved results,
	 * a.k.a return value of {@link Update#doneOp(io.odysz.transact.sql.Statement.IPostOperat)}.*/
	public SemanticObject resulves();
	
	/**Format special escaping string that will be resolve value later.
	 * @see #refPattern
	 * @param tabl
	 * @param pk
	 * @return resolving string: "RESULVE tabl.pk"
	 */
	public default String formatResulv(String tabl, String pk) {
		return String.format("RESULVE %s.%s", tabl, pk);
	}

	/**Get the dbtype handled by the context
	 * @return db type
	 */
	public dbtype dbtype();

	/**<p>The implementation of this return different stream to compose paging sql.</p>
	 * Sql examples: </p>
	 * mysql<br>
	 * "select * from (select t.*, @ic_num := @ic_num + 1 as rnum from (%s) t, (select @ic_num := 0) ic_t) t1 where rnum > %s and rnum <= %s"<br><br>
	 * oracle<br>
	 * return String.format("select * from (select t.*, rownum r_n_ from (%s) t WHERE rownum <= %s  order by rownum) t where r_n_ > %s"<br><br>
	 * ms sql sever 2000 - 2010<br>
	 * return String.format("select * from (SELECT ROW_NUMBER() OVER(ORDER BY (select NULL as noorder)) AS RowNum, * from (%s) t) t where rownum >= %s and rownum <= %s <br><br>
	 * @param s stream that will be collected into select statement
	 * @param pageIx page index
	 * @param pgSize page size
	 * @return stream that will be used to join paging select statement
	 * @throws TransException 
	public Stream<String> pagingStream(Stream<String> s, int pageIx, int pgSize) throws TransException;
	 */

	/**Generate an auto increasing ID for tabl.col, where connection is initialized when constructing this implementation.<br>
	 * The new generated value is managed in this implementation class (for future resolving).<br>
	 * <b>side effect</b>: generated auto key already been put into autoVals, can be referenced later. 
	 * @param tabl
	 * @param col
	 * @return new auto key.
	 * @throws SQLException
	 * @throws TransException
	 */
	String genId(String tabl, String col) throws SQLException, TransException;

	/**Create a new instance for a semantics processing.
	 * @param usr 
	 * @return new semantext instance
	 */
	public ISemantext clone(IUser usr);

	public TableMeta colType(String tabl);

}
