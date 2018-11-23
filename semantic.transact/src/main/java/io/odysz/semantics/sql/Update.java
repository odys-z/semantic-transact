package io.odysz.semantics.sql;

import java.util.ArrayList;

public class Update extends Statement {
	private ArrayList<Object[]> nvs;

	Update(Transcxt transc, String tabl) {
		super(transc, tabl, null);
	}

	public Statement nv(String n, Object v) {
		if (nvs == null)
			nvs = new ArrayList<Object[]>();
		nvs.add(new Object[] {n, v});
		return this;
	}

	@Override
	public String sql() {
		// TODO Auto-generated method stub
		return null;
	}

}
