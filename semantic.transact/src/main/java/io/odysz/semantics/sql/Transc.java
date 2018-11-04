package io.odysz.semantics.sql;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

/**Transaction / Batching context
 * @author ody
 *
 */
public class Transc {


	private DataSource ds;

	protected Transc(DataSource ds) {
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
	
}
