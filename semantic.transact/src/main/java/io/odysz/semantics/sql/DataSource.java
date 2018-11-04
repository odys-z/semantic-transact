package io.odysz.semantics.sql;

import java.util.HashMap;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

/**Database meta data, not data source in DA.<br>
 * The caller of Semantic Transaction should prepare this before call it. 
 * @author ody
 */
public class DataSource {

	private String srcId;
	private DbSchema dbSchema;
	private HashMap<String, DbTable> tables;
	private HashMap<String, HashMap<String, DbColumn>> tablCols;

	public DataSource(String srcId) {
		this.srcId = srcId;

		dbSchema = new DbSpec().addDefaultSchema();
		HashMap<String, DbTable> tables = new HashMap<String, DbTable>();
		HashMap<String, HashMap<String, DbColumn>> tablCols = new HashMap<String, HashMap<String, DbColumn>>();
	}

	public DataSource addTabl(String tabl) {
		DbTable tab = dbSchema.addTable(tabl);
		tables.put(tabl, tab);
		return this;
	}
	
	public DataSource addColumn(String tabl, String colname, String type, int len) {
		DbTable tab = tables.get(tabl);
		DbColumn col = tab.addColumn(colname, type, len);

		return this;
	}

	public DbTable getTable(String tabl) {
		return tables.get(tabl);
	}
	
}
