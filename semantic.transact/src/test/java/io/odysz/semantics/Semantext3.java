package io.odysz.semantics;

import java.util.HashMap;

import io.odysz.semantics.meta.TableMeta;
import io.odysz.common.dbtype;

public class Semantext3 extends Semantext2 {

	private dbtype dbtype;

	public Semantext3(String tabl, HashMap<String, Semantics2> semantics, HashMap<String, TableMeta> metas) {
		super(tabl, semantics, metas);
	}

	public Semantext2 dbtype(dbtype dbtype) {
		this.dbtype = dbtype;
		return this;
	}
	
	@Override
	public dbtype dbtype() {
		return dbtype;
	}

}
