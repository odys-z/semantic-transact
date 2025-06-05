package io.odysz.transact.sql.parts;

import static io.odysz.common.LangExt.f;

import java.util.Random;

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

	String prefix;
	final String fileId;
	final String filename;

//	ISemantext ctx;

	public ExtFilePaths(String docId, String filename) {
		this.fileId = docId;
		this.filename = filename;
	}
	
	public ExtFilePaths(ExprPart resulv_const_path, ISemantext ctx, String filename) throws TransException {
		this.fileId = resulv_const_path.sql(ctx);
		this.filename = filename;
//		this.ctx = ctx;
	}

	public ExtFilePaths(String nameId, Random random, String filename) {
		this(nameId, f("%s %s", Radix32.toString(random.nextInt(), 4), filename));
	}

	public ExtFilePaths subpath(String extroot, String... subs) {
		prefix = FilenameUtils.concat(FilenameUtils.concat(prefix, extroot), subs);
		return this;
	}

	public ExtFilePaths subpath(String... subs) {
		prefix = FilenameUtils.concat(prefix, subs);
		return this;
	}

	public String abspath() {

//		String relatvFn = encodeUri(Transcxt.cfgroot(), prefix, fileId, filename, ctx);
		String relatvFn = dburi(false);
		return decodeUri(Transcxt.runtimeRoot(), relatvFn);
	}
	
	public String dburi(boolean escapeSep) {
		String relatvFn = encodeUri(Transcxt.cfgroot(), prefix, fileId, filename);
		return escapeSep ? relatvFn.replaceAll("\\\\", "/") : relatvFn;
	}

	public static String encodeUri(String cfgRoot, String prefix, String nameId, String filename) {
		if (!LangExt.isblank(filename, "\\.", "\\*"))
			nameId += " " + filename;
		return FilenameUtils.concat(cfgRoot, prefix, nameId);
	}
	
	public static String decodeUri(String runtimePath, String dbUri) {
		return EnvPath.decodeUri(runtimePath, dbUri);
	}

	// call runtimeRoot(DATranscxt.runtimeRoot())
	// needed?
//	public ExtFilePaths runtimeRoot(String runtimeRoot) {
//		// TODO Auto-generated method stub
//		return this;
//	}

//	public ExtFilePaths setup() {
//		// TODO Auto-generated method stub
//
//		subpath(, prefix);
//		return this;
//	}

	// some helpers
// stx.containerRoot()
//					String fn = EnvPath.decodeUri(DATranscxt.runtimeRoot(), _ref[0].uri64);
//					String targetpth = DocUtils.resolvExtroot(conn, fn, docmeta);
	public ExtFilePaths setup(String conn, String tbl) {
		// TODO Auto-generated method stub
		return null;
	}

	public String avoidConflict(String absoluteFn) {
		// TODO Auto-generated method stub
		return null;
//			while (Files.exists(f, LinkOption.NOFOLLOW_LINKS)) {
//				Random random = new Random();
//				// String rand = Radix32.toString(random.nextInt(), 4);
//
//
//				extpaths = new ExtFilePaths(nameId, random,  filename).subpath(args);
//				f = Paths.get(absoluteFn);
//			}
	}
}
