package io.odysz.transact.sql.parts.insert;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import io.odysz.semantics.ISemantext;

public class ColumnListOrcl extends ColumnList {
	String[] cols;

	public ColumnListOrcl(Map<String, Integer> colIdx) {
//		if (colIdx == null)
//			return;
//
//		cols = new String[colIdx.size()];
//		for (String n : colIdx.keySet()) {
//			Integer ix = colIdx.get(n);
//			if (ix == null)
//				continue;
//			// ix--; // because colindext set early is starting from 1! disgusting
//			if (ix >= cols.length)
//				Utils.warn("Column ignored, possibly results from duplicate name (%s) in column list.", n);
//			else
//			cols[ix] = n;
//		}
		super(colIdx);
	}

	@Override
	public String sql(ISemantext context) {
		if (cols == null)
			return "";
		else
			return "(\"" + Arrays.stream(cols).collect(Collectors.joining("\", \"")) + "\")";
	}


}
