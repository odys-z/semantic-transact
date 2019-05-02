package io.odysz.semantics;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**A {@link java.sql.ResultSet} similar interface, for separating semantic.DA and semantic.transact.
 * This interface is somehow like the {@link java.sql.ResultSet}, but the result set is not connection based.
 * All results can be used without worrying about db connect and updating - no updating supported.
 * Currently IResults has only one implementation class, the semantic.DA/io.odysz.module.rs.SResultset.
 * 
 * @author odys-z@github.com
 */
public interface IResults {

	/**Get total row count.
	 * If this result is only a page of query, the total number can be got by this.
	 * @return total row cound
	 */
	int total();
	/**Set total row count. 
	 * @param total total row count
	 * @return this
	 */
	IResults total(int total);

	IResults appendDeeply(ArrayList<Object> row);
	IResults append(ArrayList<Object> includingRow);
	int append(IResults more) throws SQLException;

	HashMap<String, Object[]> getColnames();
	ArrayList<ArrayList<Object>> getRows();

	boolean next() throws SQLException;
	void first() throws SQLException;

	String getString(int colIndex) throws SQLException;
	String getString(String colName) throws SQLException;
	/**If field is a date value, return string formatted by sdf.
	 * @param colName
	 * @param sdf
	 * @return value
	 * @throws SQLException
	 */
	String getString(String colName, SimpleDateFormat sdf) throws SQLException;
	String getString(int colIndex, SimpleDateFormat sdf) throws SQLException;
	String getStringNonull(String colName) throws SQLException;
	String getString(int rowix, String idField);
	boolean getBoolean(int colIndex) throws SQLException;
	boolean getBoolean(String colName) throws SQLException;
	double getDouble(int colIndex) throws SQLException;
	double getDouble(String colName) throws SQLException;
	BigDecimal getBigDecimal(int colIndex) throws SQLException;
	BigDecimal getBigDecimal(String colName) throws SQLException;
	Date getDate(int index) throws SQLException;
	Date getDate(String colName) throws SQLException;
	int getInt(int colIndex) throws SQLException;
	int getInt(String col, int deflt);
	long getLong(int colIndex) throws SQLException;
	long getLong(String colName) throws SQLException;
	int getInt(String colName) throws SQLException;
	Blob getBlob(int colIndex) throws SQLException;
	Blob getBlob(String colName) throws SQLException;
	Object getObject(int colIndex) throws SQLException;
	int getRow();
	int getColumnCount();
	IResults beforeFirst() throws SQLException;
	IResults before(int idx) throws SQLException;
	void close() throws SQLException;
	boolean previous() throws SQLException;
	String getColumnName(int i);
	void setColumnName(int i, String n);
	int getRowCount();
	int getColCount();
	ArrayList<Object> getRowAt(int idx) throws SQLException;
	IResults set(int colIndex, String v) throws SQLException;
	IResults set(String colName, String v) throws SQLException;
	int findFirst(String col, String regex) throws SQLException;
	ArrayList<Object> getRowCells();
	/**Print some row's columns to System.out
	 * @param err is using System.error?
	 * @param max max rows to print
	 * @param includeCols include columns to be printed
	 * @return printed rows
	 */
	int printSomeData(boolean err, int max, String... includeCols);
}
