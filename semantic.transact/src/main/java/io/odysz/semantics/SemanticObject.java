package io.odysz.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.odysz.transact.x.TransException;

/**<p>The equivalent of JsonObject in previous projects.
 * Is this class the duplication of JMessage?</p>
 * <p>If an json request object is handled by a port, a.k.a SQuery,
 * is their any property name not known by the port?</p>
 * <p>If no such properties, then there shouldn't be pub() and get().</p>
 * @author ody
 *
 */
public class SemanticObject extends Object {

	private HashMap<String, Object> props;

	public Object get(String prop) {
		return props == null ? null : props.get(prop);
	}

	public String getString(String prop) {
		return props == null ? null : (String) props.get(prop);
	}

	public SemanticObject put(String prop, String v) {
		if (props == null)
			props = new HashMap<String, Object>();
		props.put(prop, v);
		return this;
	}

	public SemanticObject put(String prop, Map obj) {
		if (props == null)
			props = new HashMap<String, Object>();
		props.put(prop, obj);
		return this;
	}

	public SemanticObject put(String prop, SemanticObject obj) {
		if (props == null)
			props = new HashMap<String, Object>();
		props.put(prop, obj);
		return this;
	}

	/**Add element 'elem' to array 'prop'.
	 * @param prop
	 * @param elem
	 * @throws TransException 
	 */
	@SuppressWarnings("unchecked")
	public void add(String prop, Object elem) throws TransException {
		if (props == null)
			props = new HashMap<String, Object>();
		if (!props.containsKey(prop))
			props.put(prop, new ArrayList<Object>());
		if (props.get(prop) instanceof List)
			((ArrayList<Object>) props.get(prop)).add(elem);
		else throw new TransException("%s seams is not an array. elem %s can't been added", prop, elem);
	}

//	public void html(PrintWriter writer) { }
}
