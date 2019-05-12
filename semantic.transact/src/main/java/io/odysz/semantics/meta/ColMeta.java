package io.odysz.semantics.meta;

import java.util.ArrayList;

import io.odysz.common.Regex;

public class ColMeta {
	/** 0: text, 1: datetime, 2: number, 3: clob, 4: bin */
	static Regex regtext = new Regex("(varchar.?|text|char)?(.*time.*|date.*)?(int.*|float|decimal)?(clob)?(b?lob|bin|binary)?");

	public enum coltype {
		number, text, datetime, clob, bin
	}


	private coltype t;
	private int len = 0;

	public ColMeta(coltype type) {
		t = type;
	}

	public ColMeta(String type) {
		t = parse(type);
		len = 0;
	}
	
	private coltype parse(String type) {
		if (type == null)
			return coltype.text;
		ArrayList<String> g = regtext.findGroups(type.toLowerCase());
		if (g.get(0) != null)
			return coltype.text;
		else if (g.get(1) != null)
			return coltype.datetime;
		else if (g.get(2) != null)
			return coltype.number;
		else if (g.get(3) != null)
			return coltype.clob;
		else if (g.get(4) != null)
			return coltype.bin;
		return coltype.text;
	}

	public ColMeta tlen(int len) {
		this.len = len;
		return this;
	}
	
	public int len() { return len; }

	public boolean isText() {
		return this.t == coltype.text;
	}

	public coltype type() { return t; }

}
