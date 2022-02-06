package io.odysz.common;

import java.util.List;
import java.util.Map;

import org.apache.commons.io_odysz.FilenameUtils;

import io.odysz.semantics.ISemantext;

/**<p>A helper to handler environment variable affected file path.</p>
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
 * @author Odys Zhou
 *
 */
public class EnvPath {
	static String regSrc = "\\$(\\w+)";
	static Regex reg = new Regex(regSrc);

	/**Repace environment variable, e.g. used for setting up file paths.
	 * <h6>FYI.</h6>
	 * Semantic-* will be used in the future mainly in docker container.
	 * In docker, volume can not mounted to tomcat/webapp's sub folder - will prevent war unpacking.
	 * See <a href='https://stackoverflow.com/q/15113700'>this problem</a>. 
	 * So it's necessary have file paths not only relative, but also can be parsed for replacing environment variables.
	 * @param src string have bash style variable to be replaced, e.g. $HOME/volume.sqlite
	 * @return string replaced with environment variables
	 */
	public static String replaceEnv(String src) {
		List<String> envs = reg.findGroups(src);
		if (envs != null) {
			Map<String, String> sysenvs = System.getenv();

			for (String env : envs) {
				String v = sysenvs.get(env);
				v = v == null ? System.getProperty(env) : v;
				if (v != null) // still can be null
					src = src.replaceAll("\\$" + env, v);
				else
					src = src.replaceAll("\\$" + env, "");
			}
		}
		if (src.startsWith("\\$"))
			Utils.warn("Requried env variable may not parsed correctly: %s", src);
		return src;
	}
	
	/**Convert uri to absolute path, according to env.
	 * 
	 * @see FilenameUtils#concat(String, String)
	 * 
	 * @param root (optinal) runtiem root path 
	 * @param uri saved path with env variables
	 * @return decode then concatenated absolute path, for file accessing. 
	 */
	public static String decodeUri(String root, String uri) {
		root = root == null ? "" : root;
		return FilenameUtils.concat(replaceEnv(root), replaceEnv(uri));
	}

	/**<p>Convert raw uri to saving uri for DB persisting - can be decoded according to env.</p>
	 * E.g.<br>
	 * configRoot: $VOLUME_HOME/shares, uri: f.jpg --&gt; /home/ody/volume/shares/f.jpg <br>
	 * configRoot: upload/a_users, uri: f.jpg --&gt; [webroot/]upload/a_users/f.jpg
	 * @param configRoot relative/absolute path with env variables
	 * @param uri sub-path(s), file path, in concatenating order
	 * @return encoded uri (with env variable) for DB persisting
	 */
	public static String encodeUri(String configRoot, String... uri) {
		return FilenameUtils.concat(configRoot, uri);
	}

	public static String decodeUri(ISemantext stx, String uri) {
		return decodeUri(stx.containerRoot(), uri);
	}

}
