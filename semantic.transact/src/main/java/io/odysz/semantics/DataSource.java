//package io.odysz.semantics;
//
//import java.util.HashMap;
//
//import io.odysz.semantics.meta.ColumnMeta;
//import io.odysz.semantics.meta.TableMeta;
//import io.odysz.semantics.x.SemanticException;
//
///**@deprecated not used in Transact.
// * Database meta data, not data source in DA.<br>
// * The caller of Semantic Transaction should prepare this before call it. 
// * @author ody
// */
//public class DataSource {
//
//	private String srcId;
////	private DbSchema dbSchema;
//	private HashMap<String, TableMeta> tables;
//	private HashMap<String, HashMap<String, ColumnMeta>> tablCols;
////
//	public DataSource(String srcId) {
//		this.srcId = srcId;
//
////		dbSchema = new DbSpec().addDefaultSchema();
////		tables = new HashMap<String, DbTable>();
////		tablCols = new HashMap<String, HashMap<String, DbColumn>>();
//	}
////
////	public DataSource addTabl(String tabl) {
////		DbTable tab = dbSchema.addTable(tabl);
////		tables.put(tabl, tab);
////		return this;
////	}
////	
////	public DataSource addColumn(String tabl, String colname, String type, int len) {
////		DbTable tab = tables.get(tabl);
////		DbColumn col = tab.addColumn(colname, type, len);
////		
////		if (!tablCols.containsKey(tabl))
////			tablCols.put(tabl, new HashMap<String, DbColumn>());
////		tablCols.get(tabl).put(colname, col);
////
////		return this;
////	}
////
//	public TableMeta getTable(String tabl) {
//		return tables.get(tabl);
//	}
//
//	public TableMeta getTable(String srcId, String tabl) throws SemanticException {
//		if (this.srcId == null || !this.srcId.equals(srcId))
//			throw new SemanticException("Datasources are not matched.");
//		return getTable(tabl);
//	}
////
//	public ColumnMeta getColumn(String tabl, String col) {
//		return tablCols.containsKey(tabl) ? tablCols.get(tabl).get(col) : null;
//	}
//	
//}
