package io.odysz.semantics.sql;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

/**Transaction / Batching context
 * @author ody
 *
 */
public class Transcxt {

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

	public DbTable getTable(String tabl) {
		return ds.getTable(tabl);
	}

	public DbColumn getColumn(String tabl, String col) {
		return ds.getColumn(tabl, col);
	}
	
}
