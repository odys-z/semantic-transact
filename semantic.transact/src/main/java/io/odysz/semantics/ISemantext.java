package io.odysz.semantics;

import java.util.ArrayList;
import java.util.List;

import io.odysz.transact.sql.Insert;
import io.odysz.transact.sql.Update;

public interface ISemantext {

	/**Get a thread safe context for resolving semantics like FK value resolving.<br>
	public <U extends Statement<U>> ISemantext onInsert(U insert, List<ArrayList<Object[]>> valuesNv);
	 * @param insert
	 * @param tabl 
	 * @param valuesNv [ list[Object[n, v], ... ], ... ]
	 * @return the semanticx context
	 */
	public ISemantext onInsert(Insert insert, String tabl, List<ArrayList<Object[]>> valuesNv);


	public ISemantext onUpdate(Update update, String tabl, ArrayList<Object[]> nvs);


	public ISemantext insert(Insert insert, String mainTabl);




}
