package io.odysz.semantics;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.odysz.anson.Anson;
import io.odysz.semantics.meta.TableMeta;
import io.odysz.transact.x.TransException;

/**
 * <p>The semantics data returned by transaction as the commitment result.</p>
 * 
 * @author odys-z@github.com
 */
public class SemanticObject extends Anson {

	protected HashMap<String, Object> props;
	public HashMap<String, Object> props() { return props; }

	/**@param prop
	 * @return null if the property doesn't exists
	 */
	public Class<?> getType (String prop) {
		if (prop == null || props == null || !props.containsKey(prop))
			return null;

		Object p = props.get(prop);
		return p == null
				? Object.class // has key, no value
				: p.getClass();
	}

	public boolean has(String prop) {
		return props != null && props.containsKey(prop) && props.get(prop) != null;
	}

	public Object get(String prop) {
		return props == null ? null : props.get(prop);
	}

	public String getString(String prop) {
		return props == null ? null : (String) props.get(prop);
	}

	public SemanticObject data() {
		return (SemanticObject) get("data");
	}

	public SemanticObject data(SemanticObject data) {
		return put("data", data);
	}
	
	public void clear() {
		props.clear();
	}
	
	public String port() {
		return (String) get("port");
	}

	public SemanticObject code(String c) {
		return put("code", c);
	}
	
	public String code() {
		return (String) get("code");
	}
	
	public SemanticObject port(String port) {
		return put("port", port);
	}

	public String msg() {
		return (String) get("msg");
	}
	
	public SemanticObject msg(String msg, Object... args) {
		if (args == null || args.length == 0)
			return put("msg", msg);
		else
			return put("msg", String.format(msg, args));
	}

	/**Put result set (AnResultset) into "rs", which is a 3d array.
	 * @param resultset
	 * @param total 
	 * @return this
	 * @throws TransException
	 */
	public SemanticObject rs(Object resultset, int total) throws TransException {
		add("total", total);
		return add("rs", resultset);
	}

	public Object rs(int i) {
		return ((ArrayList<?>)get("rs")).get(i);
	}
	
	@SuppressWarnings("unchecked")
	public int total(int i) {
		if (get("total") == null)
			return -1;
		ArrayList<Object> lst = ((ArrayList<Object>)get("total"));
		if (lst == null || lst.size() <= i)
			return -1;
		Object obj = lst.get(i);
		if (obj == null)
			return -1;
		return (int)obj;
	}
	
	public int total() {
		return total(0);
	}
	
	public SemanticObject total(int rsIdx, int total) throws TransException {
		// the total(int) returned -1
		if (total < 0) return this;

		@SuppressWarnings("unchecked")
		ArrayList<Integer> lst = (ArrayList<Integer>) get("total");
		if (lst == null || lst.size() <= rsIdx)
			throw new TransException("No such index for rs; %s", rsIdx);
		lst.set(rsIdx, total);
		return this;
	}
	
	public String error() {
		return (String) get("error");
	}
	
	public SemanticObject error(String error, Object... args) {
		if (args == null || args.length == 0)
			return put("error", error);
		else
			return put("error", String.format(error, args));
	}
	
	public SemanticObject put(String prop, Object v) {
		if (props == null)
			props = new HashMap<String, Object>();
		props.put(prop, v);
		return this;
	}

	/**Add element 'elem' to array 'prop'.
	 * @param prop
	 * @param elem
	 * @return this
	 * @throws TransException 
	 */
	@SuppressWarnings("unchecked")
	public SemanticObject add(String prop, Object elem) throws TransException {
		if (props == null)
			props = new HashMap<String, Object>();
		if (!props.containsKey(prop))
			props.put(prop, new ArrayList<Object>());
		if (props.get(prop) instanceof List)
			((ArrayList<Object>) props.get(prop)).add(elem);
		else throw new TransException("%s seams is not an array. elem %s can't been added", prop, elem);
		return this;
	}

	/**Add int array.
	 * @param prop
	 * @param ints
	 * @return this
	 * @throws TransException
	 */
	public SemanticObject addInts(String prop, int[] ints) throws TransException {
		for (int e : ints)
			add(prop, e);
		return this;
	}

	public Object remove(String prop) {
		if (props != null && props.containsKey(prop))
			return props.remove(prop);
		else return null;
	}

	/**Print for reading - string can't been converted back to object
	 * @param out
	 */
	public void print(PrintStream out) {
		if (props != null)
			for (String k : props.keySet()) {
				out.print(k);
				out.print(" : ");
				Class<?> c = getType(k);
				if (c == null)
					continue;
				else if (c.isAssignableFrom(SemanticObject.class)
					|| SemanticObject.class.isAssignableFrom(c))
					if ((SemanticObject)get(k) == null)
						out.print(k + ": null");
					else
						((SemanticObject)get(k)).print(out);
				else if (Collection.class.isAssignableFrom(c) || Map.class.isAssignableFrom(c)) {
					Iterator<?> i = ((Collection<?>) get(k)).iterator(); 
					out.println("[" + ((Collection<?>) get(k)).size() + "]");
					while (i.hasNext()) {
						Object ele = i.next();
						c = ele.getClass();
						if (c.isAssignableFrom(SemanticObject.class)
								|| SemanticObject.class.isAssignableFrom(c))
							((SemanticObject)ele).print(out);
						else
							out.print(get(k));
					}
				}
				else out.print(get(k));
				out.print(",\t");
			}
		out.println("");
	}

	public String resulve(String tabl, String pk) {
		return (String) ((SemanticObject) ((SemanticObject) get("resulved")).get(tabl)).get(pk);
	}

	public int getInt(String n) {
		return Integer.valueOf(getString(n));
	}

	/**
	 * Find results for entm.pk in table etnm.tbl.
	 * @see #resulve(String, String)
	 * @param entm
	 * @return resolved resource
	 * @since 1.4.40
	 */
	public String resulve(TableMeta entm) {
		return resulve(entm.tbl, entm.pk);
	}
}
