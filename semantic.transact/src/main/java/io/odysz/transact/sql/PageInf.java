package io.odysz.transact.sql;

import static io.odysz.common.LangExt.eq;
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
 * TODO move general query conditions to PageInf<br>
 * Actually, all query submitted via Anson are paged. 
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
	
	/**
	 * @param page
	 * @param size
	 * @param whereqs (n0, v0), (n1, v1), ..., must be even number of elements.
	 */
	public PageInf(long page, long size, String... whereqs) {
		this.page = page;
		this.size = size;
		this.arrCondts = new ArrayList<String[]>();

		if (!isNull(whereqs))
			for (int cx = 0; cx < whereqs.length; cx+=2)
				arrCondts.add(
						new String[] {whereqs[cx],
						cx+1 < whereqs.length ? whereqs[cx+1] : null});

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

	public PageInf mergeArgs() {
		if (len(mapCondts) > 0)
			for (String k : mapCondts.keySet())
				arrCondts.add(new String[] {k, (String)mapCondts.get(k)});
		mapCondts.clear();
		return this;
	}

	/**
	 * Reshape 2D array of n-v pairs to string array for string.format().
	 * 
	 * @return args array
	 */
	public String[] arrCondts2args() {
		ArrayList<String> args = new ArrayList<String>(arrCondts.size());
		for (String[] arg : arrCondts) {
			args.add(isNull(arg) ? "" : arg[arg.length - 1]);
		}
		return args.toArray(new String[0]);
	}
	
	public String getArg(String argName) {
		if (mapCondts != null && mapCondts.containsKey(argName))
			return (String)mapCondts.get(argName);
		else if (arrCondts != null) {
			for (String[] nv : arrCondts)
				if (eq(nv[0], argName))
					return nv[1];
		}
		return null;
	}
}
