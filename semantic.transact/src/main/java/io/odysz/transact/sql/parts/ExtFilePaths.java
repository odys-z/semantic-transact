package io.odysz.transact.sql.parts;

import static io.odysz.common.LangExt.eq;
import static io.odysz.common.LangExt.f;
import static io.odysz.common.LangExt.mustnonull;
import static io.odysz.common.FilenameUtils.concat;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.odysz.common.EnvPath;
import io.odysz.common.FilenameUtils;
import io.odysz.common.LangExt;
import io.odysz.common.Radix32;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.Transcxt;
import io.odysz.transact.sql.parts.condition.ExprPart;
import io.odysz.transact.x.TransException;

/**
 * Ext-file's paths pair, for handling volume path mapping.
 * 
 * @since 1.5.60
 */
public class ExtFilePaths {

	String volume;
	/** The entire sub-path in $VOLUME/sub.../.../PID resname.typ */ 
	String prefix;
	public ExtFilePaths prefix(String docref_uri) {
		this.prefix = docref_uri;
		return this;
	}

	final String fileId;

	String filename;

	/**
	 * This is used for bridge the discrepancy between WEB-INF and container root path,
	 * which is used to decode volume root.
	 * 
	 * <p>Details: The volume path is now configured in WEB-INF/setting.json, 
	 * of which the relative path can be more readable to human if it is starting there.
	 * But this makes resolve the path incorrect if from runtime root, which is know
	 * to the system.</p>
	protected static String config_root;
	 */
	/**
	 * @param webinf relative path from runtime root to WEB-INF, or the configuring folder.
	public static void init(String webinf) {
		config_root = webinf;
	}
	 */

	public ExtFilePaths filename(String f) {
		this.filename = f;
		return this;
	}

	public ExtFilePaths(String volume, String docId, String filename) {
		this.volume = volume;
		this.fileId = docId;
		this.filename = filename;
	}
	
	public ExtFilePaths(String volume, ExprPart resulv_const_path, ISemantext ctx, String filename) throws TransException {
		this.volume = volume;
		this.fileId = resulv_const_path.sql(ctx);
		this.filename = filename;
	}

	public ExtFilePaths(String volume, String nameId, Random random, String filename) {
		this(volume, nameId, f("%s %s", Radix32.toString(random.nextInt(), 4), filename));
	}

	public ExtFilePaths subpath(String[] subs, Map<String, Integer> cols, ArrayList<Object[]> row) throws TransException {
		for (String subname : subs) {
			if (!cols.containsKey(subname)) 
				throw new TransException("To insert (create file), all required fields must be provided by user (missing %s).\nConfigured fields: %s.\nGot cols: %s",
						subname,
						Stream.of(subs).collect(Collectors.joining(", ")),
						cols.keySet().stream().collect(Collectors.joining(", ")));
			else
				prefix = concat(prefix, row.get(cols.get(subname))[1].toString());
		}
		return this;
	}
	
	public String decodeUriPath() {
//		mustnonull(config_root, "ExtFilePaths.condig_root is null. Call ExtFilePaths.init(config_root) first.\n"
//				+ "This is used for bridge the discrepancy between WEB-INF and container root path, which is used to decode volume root.");

		String relatvFn = dburi(false);

		// String root = Transcxt.runtimeRoot();
		String root = Transcxt.cfgroot();
		return decodeUri(eq(root, ".") ? "" : root, relatvFn);
	}
	
	public static String decodeUriPath(String dburi) {
		String root = Transcxt.cfgroot();
		return decodeUri(eq(root, ".") ? "" : root, dburi);
	}
	
	public String dburi(boolean escapeSep) {
		String relatvFn = encodeUri(volume, prefix, fileId, filename);
		return escapeSep ? relatvFn.replaceAll("\\\\", "/") : relatvFn;
	}

	public static String encodeUri(String volume, String prefix, String id4name, String filename) {
		if (!LangExt.isblank(filename, "\\.", "\\*"))
			id4name += " " + filename;
		return concat(volume, prefix, id4name);
	}
	
	protected static String decodeUri(String runtimePath, String dbUri) {
		return EnvPath.decodeUri(runtimePath, dbUri);
	}

	public String avoidConflict(String absoluteFn) {
		String fn = this.filename;
		Path f = Paths.get(this.decodeUriPath());
		while (Files.exists(f, LinkOption.NOFOLLOW_LINKS)) {
			Random random = new Random();
			String rand = Radix32.toString(random.nextInt(), 4);
			
			this.filename = rand + " " + fn;

			f = Paths.get(this.decodeUriPath());
		}
		return f.toAbsolutePath().toString();
	}
}
