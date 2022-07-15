package io.odysz.transact.sql;

import java.util.ArrayList;

import io.odysz.anson.Anson;

public class PageInf extends Anson {

	public long page;
	public long size;
	public long total;
	public ArrayList<String[]> condts;
}
