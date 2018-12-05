package io.odysz.common;

public enum dbtype {mysql(0), ms2k(1), oracle(2), sqlite(3), postGIS(4);
	private final int value;
	private dbtype(int value) { this.value = value; }
	public int getValue() { return value; }
}