package io.odysz.transact.sql.parts.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.odysz.transact.x.TransException;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;

public class ConstList extends AbsPart {
	protected ArrayList<String> valst;

	protected String[] valsArr;
	private boolean valsArrIsnull;
	
	public ConstList(int size) {
		if (size > 0)
			valsArr = new String[size];
		valsArrIsnull = true;
	}

	public ConstList constv(int idx, String v) throws TransException {
		if (idx < 0 || v == null)
			return this;

		if (valst != null)
			throw new TransException("Don't use both list and array mode in ConstList.");

		valsArr[idx] = v;
		valsArrIsnull = false;
		return this;
	}

	public ConstList constv(String v) throws TransException {
		if (valsArr != null)
			throw new TransException("Don't use both list and array mode in ConstList.");

		if (valst == null)
			valst = new ArrayList<String>();
		valst.add(v);
		return this;
	}

	@Override
	public String sql(ISemantext context) {
		if (valst == null && valsArrIsnull) return "null";
		else if (valst != null){
			return valst.stream().map(v -> v == null ? "null" : "'" + v + "'").collect(Collectors.joining(", "));
		}
		else
			return Arrays.stream(valsArr).map(v -> v == null ? "null" : "'" + v + "'").collect(Collectors.joining(", "));
	}


}
