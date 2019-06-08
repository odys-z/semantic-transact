package io.odysz.transact.sql.parts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.odysz.common.AESHelper;
import io.odysz.common.LangExt;
import io.odysz.common.Utils;
import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.condition.ExprPart;

/**External file representation.<br>
 * An ExtFile can only been used as a set value.
 * @author odys-z@github.com
 *
 */
public class ExtFile extends AbsPart {
	private String b64;
	private ExprPart resulv_const_path;
	private String prefix;
	private String filename;

	public ExtFile(ExprPart resulvingPath) {
		this.resulv_const_path = resulvingPath;
	}

	public ExtFile prefixPath(String path) {
		this.prefix = path;
		return this;
	}
	
	public ExtFile filename(String name) {
		this.filename = name;
		return this;
	}
	
	public ExtFile b64(String b64) {
		this.b64 = b64;
		return this;
	}

	@Override
	public String sql(ISemantext ctx) {
		// save file to pathname
		String fn;
		if (resulv_const_path instanceof Resulving) 
			fn = ((Resulving)resulv_const_path).resulved(ctx);
		else
			fn = resulv_const_path.sql(ctx);
		
		if (!LangExt.isblank(filename, "\\.", "\\*"))
				fn += " " + filename;
		
		String dir = LangExt.isblank(prefix) ? fn : prefix;
		mkDir(dir);

		fn = dir + "/" + fn;

		Path f = Paths.get(fn);
		try {
			byte[] b = AESHelper.decode64(b64);
			Files.write(f, b);
			return "'" + fn + "'";
		} catch (IOException e) {
			e.printStackTrace();
			return "''";
		}
	}

	private void mkDir(String dir) {
		File f = new File(dir);
		if (f.isDirectory())
			return;
		else if (!f.exists())
			// create dir
			f.mkdirs();
		else
			// must be a file
			Utils.warn("FATAL ExtFile can't create a folder, a same named file exists: ", dir);
	}

}
