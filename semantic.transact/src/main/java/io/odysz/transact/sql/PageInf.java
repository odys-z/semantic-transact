package io.odysz.transact.sql;

import static io.odysz.common.LangExt.isNull;
import static io.odysz.common.LangExt.len;

import java.util.ArrayList;
import java.util.HashMap;

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
	public ArrayList<String[]> arrCondts;
	public HashMap<String, ?> mapCondts;
	
	public PageInf() {
		this.arrCondts = new ArrayList<String[]>();
		this.mapCondts = new HashMap<String, Object>();
	}
	
	public PageInf(long page, long size, String... condt) {
		this.page = page;
		this.size = size;
		this.arrCondts = new ArrayList<String[]>();
		if (!isNull(condt))
			arrCondts.add(condt);
		mapCondts = new HashMap<String, Object>();
	}

	/**
	 * condts = [[...arg0s], string[] other-args]
	 * @param arg0s
	 * @return this
	 */
	public PageInf insertCondt(String... arg0s) {
		this.arrCondts.add(0, arg0s);
		return this;
	}

	public void mergeArgs() {
		if (len(mapCondts) > 0)
			for (String k : mapCondts.keySet())
				arrCondts.add(new String[] {k, (String)mapCondts.get(k)});
	}
}
