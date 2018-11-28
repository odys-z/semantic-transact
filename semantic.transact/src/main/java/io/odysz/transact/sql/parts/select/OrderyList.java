package io.odysz.transact.sql.parts.select;

import java.util.ArrayList;
import java.util.stream.Collectors;

import io.odysz.transact.sql.parts.AbsPart;


public class OrderyList extends AbsPart {
	private ArrayList<String[]> orders;

	public OrderyList(ArrayList<String[]> orderList) {
		orders = orderList;
	}

	@Override
	public String sql() {
		return orders.stream().map(e -> parse(e))
				.filter(e -> e != null)
				.collect(Collectors.joining(", ", "order by ", ""));
	}
	
	protected String parse(String[] orderby) {
		if (orderby.length > 1 && orderby[1] != null)
			return orderby[0] + " " + asc(orderby[1]);
		else return orderby[0] + " asc";
	}

	private String asc(String asc) {
		return asc != null && asc.toLowerCase().trim().equals("desc") ?
				"desc" : "asc";
	}

}
