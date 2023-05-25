package io.odysz.transact.sql;

import java.util.ArrayList;

import io.odysz.anson.Anson;

/**
 * <p>DB records' page information.</p>
 * 
 * <p>This type should presented in all request with A = records in the near future.</p>
 * 
 * <h6>Use case:</h6>
 * 1. Semantic.jserv/io.oz.spreadsheet.SpreadsheetReq.pageInf,<br>
 *    Anclient.ts:<br>
 *    curriculum/views/north/AllDecissionsComp.conds<br>
 *    curriculum/views/north/MyComp.conds<br>
 *    ...<br>
 *    
 * 2. Docsync.jserv/io.oz.jserv.dbsync.DBSyncReq.pageInf,<br>
 *    Anclient.java:<br>
 *    Docsync.jserv/io.oz.jserv.dbsync.DBWorker#syncTabl()<br>
 *    
 * 3. Sandbox, Album-jserv/Streetier.stree
 * 
 * 4. ...
 * @author odys-z@github.com
 *
 */
public class PageInf extends Anson {

	public long page;
	public long size;
	public long total;
	public ArrayList<String[]> condts;
}
