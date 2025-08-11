package io.odysz.common;

import static io.odysz.common.LangExt.isNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * <p>A helper to handler environment variable affecting file path.</p>
 * Suppose $VOLUME_HOME = "/home/ody/volume"
 * <pre>
 args: $VOLUME_HOME/shares,uri,userId,cate,docName
 encoded: $VOLUME_HOME/shares/ody/000001 f.txt:
 decoded: /home/ody/volume/shares/000001 f.txt</pre>
 * relative upload folder:<pre>
 args: upload,uri,userId,cate,docName
 encoded: upload/admin/000002 f.txt
 decoded: src/test/res/upload/admin/000002 f.txt</pre>
 * absoluted upload folder:<pre>
 args: /home/ody/upload,uri,userId,cate,docName
 encoded: /home/ody/upload/admin/000003 f.txt
 decoded: /home/ody/upload/admin/000003 f.txt</pre>

 * Since v1.4.2, system property has higher priority than environment variable.
 * @author Odys Zhou
 *
 */
public class EnvPath {
	static String regSrc = "\\$(\\w+)";
	static Regex reg = new Regex(regSrc);

	static Map<String, String> sysenv;

	static {
		// 0.7.6
		sysenv = new HashMap<String, String>(System.getenv());
		Properties pros = System.getProperties();
		if (pros != null)
		for(Object p : pros.keySet()) {
			if (p instanceof String)
				sysenv.put((String)p, pros.get(p).toString());
			
		}
	}
	
	/**
	 * Extend system variables.
	 * @param k
	 * @param v
	 * @since 1.4.45
	 */
	public static void extendEnv(String k, String v) {
		Map<String, String> env2 = new HashMap<String, String>(sysenv.size() + 1);
		for (String sk : sysenv.keySet())
			env2.put(sk, sysenv.get(sk));
		env2.put(k, v);
		sysenv = env2;
	}

	/**
	 * Format a replaced string with the configured map of environment variables.
	 * @see #replaceEnv(String, Map)
	 * @see #extendEnv(String, String)
	 * @since 1.5.0
	 * @param src string have bash $ variable to be replaced, e.g. $HOME/volume.sqlite
	 * @return string replaced with environment variables
	 */
	public static String replaceEnv(String src) {
		return replaceEnv(src, sysenv);
	}
	
	/**
	 * Replace environment variable, e.g. used for setting up file paths.
	 * 
	 * <h6>FYI.</h6>
	 * 
	 * <p>Semantic-* will be used in the future mainly in docker container.
	 * In docker, volume can not be mounted to tomcat/webapp's sub folder - will prevent war unpacking.
	 * See <a href='https://stackoverflow.com/q/15113700'>this problem</a>.</p>
	 * 
	 * <p>So it's necessary have file paths not only relative, but also can be
	 * parsed for replacing environment variables.
	 * 
	 * @param src string have bash $ variable to be replaced, e.g. $HOME/volume.sqlite
	 * @return string replaced with environment variables
	 * @since 1.5.0
	 */
	public static String replaceEnv(String src, Map<String, String> sysenvs) {
		if (src == null) return null;
		
		List<String> envs = reg.findGroups(src);
		if (envs != null) {
			// Map<String, String> sysenvs = System.getenv();

			for (String env : envs) {
				String v = System.getProperty(env);
				v = v == null ? sysenvs.get(env) : v;
				if (v != null) // still can be null
					src = src.replaceAll("\\$" + env, FilenameUtils.winpath2unix(v));
				else {
					src = src.replaceAll("\\$" + env, "");
					Utils.warn("Cannot found repleacement for environment variable: %s\nsrc:%s",
							env, src);
				}
			}
		}
		if (src.startsWith("\\$"))
			Utils.warn("Requried env variable may not parsed correctly: %s", src);
		return src;
	
	}

	public static String abspath(String p) {
		return new File(replaceEnv(p)).getAbsolutePath();
	}

	/**Decode URI - convert file records' uri into absolute path, according to env.
	 *
	 * @see FilenameUtils#concat(String, String)
	 *
	 * @param root (optinal) runtiem root path
	 * @param uri, the saved path with env variables. Make sure are ":" are cleaned.
	 * @return decode then concatenated absolute path, for file accessing.
	 */
	public static String decodeUri(String root, String uri) {
		root = root == null ? "" : root;
		return FilenameUtils.concat(replaceEnv(root), replaceEnv(uri));
	}

	/**
	 * @deprecated This must be removed immediately
	 * @param stx
	 * @param uri
	 * @return
	public static String decodeUri(ISemantext stx, String uri) {
		return decodeUri(stx.containerRoot(), uri);
	}
	 */

	/**
	 * Replace env token in {@code root} and concat the path.
	 * 
	 * @param root
	 * @param subpath
	 * @param filename
	 * @return return of {@link FilenameUtils#concat(String, String...)}
	 */
	public static String decodeUri(String root, String subpath, String filename) {
		root = root == null ? "" : root;
		return FilenameUtils.concat(replaceEnv(root),
					replaceEnv(subpath),
					filename);
	}

	public static String decodeUri(String root, String subpath, String folder, String filename) {
		root = root == null ? "" : root;
		return FilenameUtils.concat(replaceEnv(root),
					replaceEnv(subpath),
					replaceEnv(folder),
					filename);
	}

	static String workdir = "";
	static String web_inf = "WEB-INF";

	/**
	 * Set working dir.
	 * Must be called before initialize singleton or any configurations.
	 *
	 * @since 1.4.25
	 * @param absWorkDir
	 */
	public static void workDir(String absWorkDir) {
	}

	/**
	 * Get absolute dir to WEB-INF.
	 *
	 * @since 1.4.25
	 * @param webInf
	 * @return abs path
	 */
	public static String webINF(String... webInf) {
		web_inf = isNull(webInf) ? "WEB-INF" : webInf[0];
		return FilenameUtils.concat(workdir, web_inf);
	}

	public static String xml(String xml) {
		return FilenameUtils.concat(workdir, webINF(), xml);
	}

	public static String concat(String workdir, String... rels) {
		String work = FilenameUtils.rel2abs(replaceEnv(workdir));
		if (!isNull(rels))
			for (String sub : rels)
				work = FilenameUtils.concat(work, EnvPath.replaceEnv(sub));
		return work;
	}
}
