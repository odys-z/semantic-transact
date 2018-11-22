package io.odysz.semantics.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.odysz.semantics.x.StException;

public class Insert extends Statement {

	private HashMap<Integer, String> colnames;
	private String pk;

	Insert(Transcxt transc, String tabl) {
		super(transc, tabl, null);
	}

	public Insert cols(String pk, String... cols) {
		if (colnames == null)
			colnames = new HashMap<Integer, String>();
		this.pk = pk;
		colnames.put(0, pk);
		if (cols != null)
			for (int c = 0; c < cols.length; c++)
				colnames.put(c + 1, cols[c]);
		return this;
	}

	@Override
	public Insert commit(ArrayList<String> sqls) throws StException {
		return this;
	}

	public Query select(Query j) {
		// TODO Auto-generated method stub
		return null;
	}

	public Query values(List<Object[]> vals) {
		// TODO Auto-generated method stub
		return null;
	}

}
