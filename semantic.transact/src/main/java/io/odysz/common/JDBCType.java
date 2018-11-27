package io.odysz.common;

public enum JDBCType {mysql(0), ms2k(1), oracle(2), sqlite(3), postGIS(4);
	private final int value;
	private JDBCType(int value) { this.value = value; }
	public int getValue() { return value; }
}