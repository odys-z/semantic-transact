package io.odysz.semantics.meta;

public class TreeTableMeta extends TableMeta {

	protected final String parent;
	protected final String fullpath;
	protected final String sort;

	public TreeTableMeta(String tbl, String[] conn) {
		super(tbl, conn);
		
		this.pk 	  = "pk";
		this.parent   = "parent";
		this.fullpath = "fullpath";
		this.sort     = "sort";
	}

}
