package io.odysz.semantics.meta;

public class ColType {
	public enum coltype {
		number, text
	}


	private coltype t;

	public ColType(coltype type) {
		t = type;
	}

	public boolean isText() {
		return this.t == coltype.text;
	}

}
