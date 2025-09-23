package io.odysz.transact.sql.parts;

import static io.odysz.common.LangExt.eq;
import static io.odysz.common.LangExt.isblank;
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
import io.odysz.common.Regex;
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
	/**
	 * Change the file's sub-paths.
	 * This should only happens when handling doc-refs from different synodes.
	 * <p>NOTE: The one for db version is {@link #subpath(String[], Map, ArrayList)}.</p>
	 */
	public ExtFilePaths prefix(String docref_uri) {
		this.prefix = docref_uri;
		return this;
	}

	final String fileId;

	String filename;
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
		this(volume, resulv_const_path.sql(ctx), filename);
	}

	ExtFilePaths subpath(String[] subs, Map<String, Integer> cols, ArrayList<Object[]> row) throws TransException {
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
		String relatvFn = dburi(false);
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

	static String encodeUri(String volume, String prefix, String id4name, String filename) {
		if (!LangExt.isblank(filename, "\\.", "\\*"))
			id4name += " " + filename;
		return concat(volume, prefix, id4name);
	}
	
	static String decodeUri(String runtimePath, String dbUri) {
		return EnvPath.decodeUri(runtimePath, dbUri);
	}

	String avoidConflict(String absoluteFn) {
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

	// TODO Refactor Move to ExtFilePaths 
	public static String relativeFolder(String uri64, String abs) {
		return isblank(uri64) ? uri64
				: FilenameUtils.getPathNoEndSeparator(
				  Regex.removeVolumePrefix(uri64, abs));
	}
}
