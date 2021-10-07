package io.odysz.common;

import java.util.List;
import java.util.Map;

import org.apache.commons.io_odysz.FilenameUtils;

public class EnvHelper {
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
				src = src.replaceAll("\\$" + env, v);
			}
		}
		return src;
	}
	
	/**A path start with "/" or "$" is absolute.
	 * @param path
	 * @return true if relative
	 */
	public static boolean isRelativePath(String path) {
		return !(path.startsWith("/") || path.startsWith("$"));
	}

	/**Get starting environment variable value
	 * @param varStr string with $Vars
	 * @return
	 */
	public static String startVar(String varStr) {
		List<String> envs = reg.findGroups(varStr);
		if (envs != null && envs.size() > 0) {
			String env = envs.get(0);

			Map<String, String> sysenvs = System.getenv();
			if (sysenvs.containsKey(env))
				return sysenvs.get(env);

			String p = System.getProperty(env);
			if (p == null)
				return "";
			return p;
		}
		return "";
	}

	public static String unreplaceEnv(String uri, String envExpr) {
		String env = startVar(envExpr);
		if (env != null) {
			return FilenameUtils.concat(env, uri);

		}
		return uri;
	}

}
