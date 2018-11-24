package io.odysz.transact.sql;

import io.odysz.semantics.DataSource;

/**Transaction / Batching context
 * @author ody
 */
public class Transcxt {

	// FIXME should be <interface>DA ?
	private DataSource ds;

	protected Transcxt(DataSource ds) {
		this.ds = ds;
	}

	public Query select(String tabl, String ... alias) {
		return new Query(this, tabl, alias);
	}
	
	public Insert insert(String tabl) {
		
		return new Insert(this, tabl);
	}
	
	public Update update(String tabl) {
		
		return new Update(this, tabl);
	}

//	public TableMeta getTable(String tabl) {
//		return ds.getTable(tabl);
//	}
//
//	public ColumnMeta getColumn(String tabl, String col) {
//		return ds.getColumn(tabl, col);
//	}
	
}
