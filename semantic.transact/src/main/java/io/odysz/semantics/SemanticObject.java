package io.odysz.semantics;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.odysz.transact.x.TransException;

/**<p>The semantics data used internally by semantic-DA to handle semantics configuration.</p>
 * <p>SemanticObject implement methods to write itself as a json value with a writer provided by the caller.
 * This can be used to write the object into other structured object.</p>
 * <p><b>Note:</b> The equivalent of JsonObject in a request is JMessage.
 * <p>Question: If a json request object is handled by a port, e.g. SQuery,
 * is their any property name not known by the port?</p>
 * <p>If no such properties, then there shouldn't be put() and get().</p>
 * @author ody
 *
 */
public class SemanticObject extends Object {

	private HashMap<String, Object> props;
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

	public Object remove(String prop) {
		if (props == null && props.containsKey(prop))
			return props.remove(prop);
		else return null;
	}

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


//	/**Serialize without dependency of Gson
//	 * @param os
//	 * @throws IOException
//	 */
//	public void json(OutputStream os) throws IOException {
//		os.write('{');
//		boolean comma = false;
//		if (props != null)
//			for (String k : props.keySet()) {
//				if (comma) os.write(',');
//				os.write(k.getBytes());
//				os.write(':');
//				Object obj = props.get(k);
//				writeValue(os, obj);
//				// os.write(props.get(k).toString().getBytes());
//				comma = true;
//			}
//		os.write('}');
//	}
//
//	public String json() throws IOException {
//		ByteArrayOutputStream os = new ByteArrayOutputStream();
//		json(os);
//		return os.toString();
//	}
//
//	private void writeValue(OutputStream os, Object obj) throws IOException {
//		if (obj instanceof SemanticObject)
//			((SemanticObject)obj).json(os);
//		if (obj instanceof List)
//			writeList(os, (List<?>)obj);
//		if (obj instanceof Map)
//			writeMap(os, (Map<?, ?>)obj);
//		else 
//			os.write(obj == null ? "null".getBytes() : obj.toString().getBytes());
//	}
//
//	private void writeMap(OutputStream os, Map<?, ?> map) throws IOException {
//		os.write('[');
//		boolean comma = false;
//		if (map != null)
//			for (Object k : map.keySet()) {
//				if (comma) os.write(',');
//				os.write(k.toString().getBytes());
//				os.write(':');
//				writeValue(os, map.get(k));
//				comma = true;
//			}
//		os.write(']');
//	}
//
//	private void writeList(OutputStream os, List<?> lst) throws IOException {
//		os.write('[');
//		boolean comma = false;
//		if (lst != null)
//			for (Object k : lst) {
//				if (comma) os.write(',');
//				os.write(k.toString().getBytes());
//				comma = true;
//			}
//		os.write(']');
//	}
}
