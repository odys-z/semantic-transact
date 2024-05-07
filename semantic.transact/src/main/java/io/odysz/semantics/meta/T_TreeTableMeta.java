package io.odysz.semantics.meta;

public class T_TreeTableMeta extends TableMeta {

	protected final String parent;
	protected String fullpath;
	protected String sort;

	public T_TreeTableMeta(String tbl, String pk, String parent, String[] conn) {
		super(tbl, conn);
		
		this.pk 	  = pk; // "pk";
		this.parent   = parent; //"parent";
		this.fullpath = "fullpath";
		this.sort     = "sort";
	}
	
	public T_TreeTableMeta sort(String field) {
		this.sort = field;
		return this;
	}

	public T_TreeTableMeta fullpath(String field) {
		this.fullpath = field;
		return this;
	}

}
