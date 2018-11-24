package io.odysz.transact.sql.parts.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.odysz.semantics.x.StException;
import io.odysz.transact.sql.parts.condition.AbsPart;

public class ConstList extends AbsPart {
	protected ArrayList<String> valst;

	protected String[] valsArr;
	private boolean valsArrIsnull;
	
	public ConstList(int size) {
		if (size > 0)
			valsArr = new String[size];
		valsArrIsnull = true;
	}

	public void constv(int idx, String v) throws StException {
		if (idx < 0 || v == null)
			return;

		if (valst != null)
			throw new StException("Don't use both list and array mode in ConstList.");

		valsArr[idx] = v;
		valsArrIsnull = false;
	}

	public void constv(String v) throws StException {
		if (valsArr != null)
			throw new StException("Don't use both list and array mode in ConstList.");

		if (valst == null)
			valst = new ArrayList<String>();
		valst.add(v);
	}

	@Override
	public String sql() {
		if (valst == null && valsArrIsnull) return "null";
		else if (valst != null){
			return valst.stream().map(v -> v == null ? "null" : "'" + v + "'").collect(Collectors.joining(", "));
		}
		else
			return Arrays.stream(valsArr).map(v -> v == null ? "null" : "'" + v + "'").collect(Collectors.joining(", "));
	}

}
