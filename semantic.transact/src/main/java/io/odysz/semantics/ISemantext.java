package io.odysz.semantics;

import java.util.ArrayList;
import java.util.List;

import io.odysz.transact.sql.Insert;
import io.odysz.transact.sql.Update;

/**<p>Interface for semantic event handler.</p>
 * <p>A semantic event can be for example starting an insert transaction with a data row.
 * Such data may needing some processing defined by the application requirements.</p>
 * <p>For example, a "fullpath" field in table means a deep first traveling record of tree nodes.</p>
 * Then a user defined semantic event handler can compose this data according to it's parent's
 * fullpath, then append the field into the inserting row.</p>
 * <p>Semantic-transact don't handle the semantics, it only fire the event to the handler,
 * the implementation of ISemantext. An ISemantext instance is used as an sql composing context
 * by semantic-transact when travel the AST and composing SQL(s). There must a {@link #insert}
 * event which fired at beginning composing an insert sql, and 1 or more times for inserting row
 * event, the {@link #onInsert}.</p>
 * @author ody
 *
 */
public interface ISemantext {
	/**Called when starting a insert transaction sql composing.<br>
	 * Create a context for the insert-sql composing process.
	 * @param insert
	 * @param mainTabl
	 * @return the new ISemantext context instance for resolving semantics.
	 */
	public ISemantext insert(Insert insert, String mainTabl);

	/**Called when starting an update transaction sql composing.<br>
	 * Create a context for the update-sql composing process.
	 * @param update
	 * @param mainTabl
	 * @return
	 */
	public ISemantext update(Update update, String mainTabl);

	/**Called each time an <@link Insert} statement found itself will composing a insert-sql.<br>
	 * Resolving inserting values, e.g an AUTO key is generated here.
	 * @param insert
	 * @param tabl 
	 * @param valuesNv [ list[Object[n, v], ... ], ... ]
	 * @return the ISemantext context, a thread safe context for resolving semantics like FK value resolving.<br>
	 */
	public ISemantext onInsert(Insert insert, String tabl, List<ArrayList<Object[]>> valuesNv);


	/**Called each time an <@link Update} statement found itself will composing an update-sql.
	 * @param update
	 * @param tabl
	 * @param nvs
	 * @return
	 */
	public ISemantext onUpdate(Update update, String tabl, ArrayList<Object[]> nvs);






}
