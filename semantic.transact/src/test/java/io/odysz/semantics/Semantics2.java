package io.odysz.semantics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.SAXException;

import io.odysz.common.AESHelper;
import io.odysz.semantics.x.SemanticException;

/**Default data structure semantics description and supporter.<br>
 * The basic {@link Semantext2} use this to manage semantics configuration for resolving data semantics.
 * <h3>What's Semantics2 for?</h3>
 * <p>Well, the word semantics is a computer science term. The author don't want to redefine this word,
 * but here is some explanation what <i>semantic-transact</i> is trying to support.</p>
 * <p>In a typical relational database based application, the main operation of data is CRUD.
 * And the most often such data operation can be abstracted to some operation pattern,
 * and they are always organized as a database transaction/batch operation described in SQL.</p>
 * <p>Take "book-author" relation for example, the author's ID is also the parent referenced by
 * book's author FK. If trying to delete an author in DB, there are 2 typical policies can be applied
 * by the application. The first is delete all books by the author accordingly; the second is warn and
 * deny the operation if some books are referencing the author. Both of this must been organized into
 * a transact/batch operation, with the second transact as check-then-delete.</p>
 * <p>In this case, you will find the FK relationship can be handled in a generalized operation, through
 * parameterizing some variables like table name, child referencing column name and parent ID.</p>
 * <p>Take the {@link Semantics2.smtype#parentChildrenOnDel} for example, it's automatically support
 * "deleting all children when deleting parent" semantics. What the user (application developer) need to
 * do is configure a semantics item then delete the parent directly.</p>
 * <p>Now you (a developer) will definitely understand what's the "parentChildrenOnDel" for. Semantic-transact
 * abstract and hide these patterns, wrapped them automatically into a transaction. That's what semantic-
 * transact want to do.</p>
 * <h3>How to Use</h3>
 * <p>To use this function:</p>
 * <p>1. Configure the "semantics.xml". See example in test/resources/semantics.xml.<br>
 * 2. Set the configured semantics as context of {@link io.odysz.transact.sql.Statement}. See example in
 * {@link io.odysz.transact.SemanticsTest}. Then use Statement's subclass's commit() method to generate SQLs</p>
 * <h3>Is this Enough?</h3>
 * <p>The 9 to 10 types of semantics defined in {@link Semantics2.smtype} is enough for some enterprise projects.
 * It depends on how abstract the semantics we want to support.</p>
 * </p>Another consideration is that semantic-transact never take supporting all semantics logic as it's goal.
 * It only trying to release burden of daily repeated tasks. Fortunately, such tasks' logic is simple, and the
 * burden is heavy. Let semantic-transact handle these simple logic, that's semantic-transact designed for. If
 * the semantics is complex, use anything you are familiar with.</p>
 * <p>Before doing that, check the semantics-cheapflow workflow engine first, which is based on semantics-transact,
 * and can handle typical - not very cheap if by our define - logics all necessary for enterprise applications.
 * It's a good example illustrating that if the semantics is designed carefully, those semantics supported by
 * this class is enough. </p>
 * <p>But it do need the application developers follow some design conventions. If you need you own semantics
 * implementation, implement the interface {@link ISemantext}, or simply initialize {@link io.odysz.transact.sql.Transcxt}
 * with null semantics, which will disable semantic supporting. 
 * @author ody
 */
class Semantics2 {
	/**error code key word*/
	public static final String ERR_CHK = "err_smtcs";;

	/**<b>0. {@link #autoPk} </b> key-word: "auto" | "pk" | "a-k"<br>
	 * <b>1. {@link #fullpath} </b> key-word: "fullpath" | "fp" | "f-p"<br>
	 * <b>2. {@link #parentChildren} </b> key-word: "pc-del-all" | "parent-child-del-all"<br>
	 * <b>3. {@link #dencrypt} </b> key-word: "d-e" | "de-encrypt" <br>
	 * <b>4. {@link #opTime}</b> key-word: "o-t" | "oper-time"<br>
	 * <b>5. {@link #checkSqlCountOnDel} </b> key-word: "ck-cnt-del" | "check-count-del" <br>
	 * <b>6. {@link #checkSqlCountOnInsert} </b> key-word: "ck-cnt-ins" | "check-count-insert" <br>
	 * <b>7. {@link #checkDsCountOnDel} </b> key-word: "ds-cnt-ins" | "ds-count-insert" <br>
	 * <b>8. {@link #composingCol} </b> key-word: "cmp-col" | "compose-col" | "compose-column" <br>
	 * <b>9. {@link #stampUp1ThanDown} </b> key-word: "stamp-up1" <br>
	 * <b>10.{@link #orclob} </b> key-word: "clob"<br>
	 * UpdateBatch supporting:<br>
	 * on inserting, up-stamp is the value of increased down stamp, or current time if it's not usable;<br>
	 * on updating, up-stamp is set as down stamp increased if down stamp value not presented in sql, or,
	 * up stamp will be ignored if down stamp presented. (use case of down stamp updating by synchronizer).<br>
	 * <b>x. orclob</b>: the field must saved as clob when driver type is orcl;
	 */
	public enum smtype {
		/**"auto" | "pk" | "a-k": Generate auto pk for the field when inserting */
		autoPk,
		/** "f-p" | "fp" | "fullpath": when updating, auto update fullpath field according to parent-id and current record id */
		fullpath,
		/** "p-c-del-all" | "parent-child-del-all": delete children before delete parent */
		parentChildrenOnDel,
		/** "d-e" | "de-encrypt": decrypt then encrypt (target col cannot be pk or anything other semantics will updated */
		dencrypt,
		/** "o-t" | "oper-time": oper and operTime that must auto updated when a user updating a record*/
		opTime,
		/** "ck-cnt-del" | "check-count-del": check is this record a referee of children records - results from sql.select(count, description-args ...). The record(s) can't been deleted if referenced;*/
		checkSqlCountOnDel,
		/** "ck-cnt-ins" | "ck-cnt-insert": check is this record count when inserting - results from sql.select(count, description-args ...). The record(s) can't been inserted if count > 0;*/
		checkSqlCountOnInsert,
		/** "ds-cnt-del" | "ds-count-del": check is this record a referee of children records - results from detaset.select(count, description-args ...). This is the oracle adaptive version of checkSqlCountOnDel; */
		// checkDsCountOnDel,
		/** "cmp-col" | "compose-col" | "compse-column": compose a column from other columns;*/
		composingCol,
		/** "s-up1" | "stamp-up1": add 1 more second to down-stamp column and save to up-stamp*/
		stamp1MoreThanRefee,
		/** "clob" | "orclob": the column is a CLOB field, semantic-transact will read/write separately in stream and get final results.*/
		orclob;

		public static smtype parse(String type) throws SemanticException {
			if (type == null) throw new SemanticException("semantics is null");
			type = type.toLowerCase().trim();
			if ("auto".equals(type) || "pk".equals(type) || "a-k".equals(type) || "autopk".equals(type))
				return autoPk;
			else if ("fullpath".equals(type) || "f-p".equals(type))
				return fullpath;
			else if ("pc-del-all".equals(type) || "parent-child-del-all".equals(type) || "parentchildondel".equals(type))
				return parentChildrenOnDel;
			else if ("d-e".equals(type) || "de-encrypt".equals(type) || "dencrypt".equals(type))
				return dencrypt;
			else if ("o-t".equals(type) || "oper-time".equals(type) || "optime".equals(type))
				return opTime;
			else if ("ck-cnt-del".equals(type) || "check-count-del".equals(type) || "checksqlcountondel".equals(type))
				return checkSqlCountOnDel;
			else if ("ck-cnt-del".equals(type) || "check-count-del".equals(type) || "checksqlcountoninsert".equals(type))
				return checkSqlCountOnInsert;
//			else if ("ds-cnt-del".equals(type) || "ds-count-del".equals(type) || "checkdscountondel".equals(type))
//				return checkDsCountOnDel;
			else if ("cmp-col".equals(type) || "compose-col".equals(type) || "compse-column".equals(type) || "composingcol".equals(type))
				return composingCol;
			else if ("s-up1".equals(type) || type.startsWith("stamp1"))
				return stamp1MoreThanRefee;
			else if ("clob".equals(type) || "orclob".equals(type))
				return orclob;
			else throw new SemanticException("semantics not known: " + type);
		}
	}
	
	public static HashMap<String, Semantics2> init(String path) throws SAXException, SemanticException {
		HashMap<String, Semantics2> ss = new HashMap<String, Semantics2>();

//		XMLTable conn = XMLDataFactory.getTable(new Log4jWrapper("") , "semantics", path,
//						new IXMLStruct() {
//							@Override public String rootTag() { return "semantics"; }
//							@Override public String tableTag() { return "t"; }
//							@Override public String recordTag() { return "s"; }});
//
//		conn.beforeFirst();	
//		while (conn.next()) {
//			String tabl = conn.getString("tabl");
//			Semantics2 s = ss.get(tabl);
//			if (s == null)
//				s = new Semantics2(conn.getString("smtc"), tabl,
//					conn.getString("pk"), conn.getString("args"));
//			else s.addSemantics(conn.getString("smtc"), tabl,
//					conn.getString("pk"), conn.getString("args"));
//			ss.put(tabl, s);
//		}

		String tabl = "a_functions";
		Semantics2 s = new Semantics2("fullpath", tabl, "funcId",
				"parentId,sibling,fullpath");
		s.addSemantics("pc-del-all", tabl, "funcId", "a_rolefunc,funcId");
		ss.put(tabl, s);

		tabl = "a_roles";
		s = new Semantics2("ck-cnt-del", tabl, "roleId",
				"a_rolefunc, select count(*) cnt from a_rolefunc where roleId = '%s',cnt");
		ss.put(tabl, s);

		return ss;
	}
	
	private String target;

	// fullpath
	private String pathSufix;
	private String parentField;
	private String fullPath;
	private String descFullpath;
	
	/** parent-children constrains. All child table referencing this table's fk info.<br>
	 * String[][] of {String[]{[0] child-table, [1] child-fk}, String[]{...}, ...}
	 */
	private ArrayList<String[]> childConstraints;

	/** Readable description */
	private String descPC;
	private String idField;
	
	// de-encrypt
	private String cipherCol;
	private String ivCol;
	private String decryptK;
	private String encryptK;

	private ArrayList<String> lobFields;

	// oper-time
	private String operField;
	private String opTimeField;
	
	// check referee count
	@SuppressWarnings("unused")
	private String checkCountErrFormat_Del;
	private String checkCountSql_Del;
	private String checkCountPkCol_Del;

	@SuppressWarnings("unused")
	private String checkCountErrFormat_Ins;
	private String checkCountSql_Ins;
	private String checkCountValueCol_Ins;
	private String deschkIns;

	private String autoPk;
	public String autoPk() { return autoPk; }

	/**semantic = fullpath, args["parentId-field", "suffix-field", "fullpath-field"]<br>
	 * semantic = parentChildrenOnDel, args["child-table", "child-fk-field"]<br>
	 * semantic = dencrypt, args["iv-col", "decrypt-key", "encrypt-key"]<br>
	 * semantic = opTime, args["oper-col", "operTime-col"]<br>
	 * semantic = checkCountOnDel(sql), args["desc-template", "select count-arg0, recName-arg1, ...", "deleting-pk-col"]<br>
	 * semantic = checkCountOnInsert(sql), args["desc-template", "select count-arg0, recName-arg1, ...", "deleting-pk-col"]<br>
	 * semantic = composed-col = concat(composing-cols), args["composed-col", "composing-col1", "'const'", ...]<br>
	 * semantic = stampUp1ThanDown, args["up-stamp", "down-stamp"]<br>
	 * semantic = clob, args["lob-field"]
	 * @param semantic
	 * @param tabl
	 * @param recId
	 * @param args
	 * @throws SemanticException 
	 */
	public Semantics2(smtype semantic, String tabl, String recId, String args) throws SemanticException {
		addSemantics(semantic, tabl, recId, args);
	}
	public Semantics2(String semantic, String tabl, String recId, String args) throws SemanticException {
		addSemantics(smtype.parse(semantic), tabl, recId, args);
	}
	
	/**@see {@link Semantics2#Semantics(smtype, String[])}
	 * @param semantic
	 * @param tabl
	 * @param args
	 * @throws SQLException
	 */
	public void addSemantics(smtype semantic, String tabl, String recId, String args) throws SemanticException {
		checkParas(tabl, recId, args);
		String[] argss = args.split(",");
		if (smtype.autoPk == semantic)
			addAutoPk(tabl, recId, argss);
		if (smtype.fullpath == semantic)
			addFullpath(tabl, recId, argss);
		else if (smtype.parentChildrenOnDel == semantic)
			addParentChildren(tabl, recId, argss);
		else if (smtype.dencrypt == semantic)
			addDencrypt(tabl, recId, argss);
		else if (smtype.orclob == semantic)
			addClob(tabl, recId, argss);
		else if (smtype.opTime == semantic)
			addOperTime(tabl, recId, argss);
		else if (smtype.checkSqlCountOnDel == semantic)
			addCheckSqlCountOnDel(tabl, recId, argss);
		else if (smtype.checkSqlCountOnInsert == semantic)
			addCheckSqlCountOnInsert(tabl, recId, argss);
		else if (smtype.composingCol == semantic)
			addComposings(tabl, recId, argss);
		else if (smtype.stamp1MoreThanRefee == semantic)
			addUpDownStamp(tabl, recId, argss);
		else throw new SemanticException("Unsuppported semantics: " + semantic);
	}

	private void addAutoPk(String tabl, String recId, String[] argss) {
		autoPk = recId;
	}
	private void addSemantics(String type, String tabl, String recId, String args) throws SemanticException {
		smtype st = smtype.parse(type);
		addSemantics(st, tabl, recId, args);
	}

	/**Throw exception if args is null or target (table) not correct.
	 * @param tabl
	 * @param recId
	 * @param args
	 * @throws SemanticException sementic configuration not matching the target or lack of args.
	 */
	private void checkParas(String tabl, String recId, String args) throws SemanticException {
		if (tabl == null || recId == null || args == null)
			throw new SemanticException(String.format(
					"adding semantics with empty targets? %s %s %s",
					tabl, recId, args));

		if (target != null && !target.equals(tabl))
			throw new SemanticException(String.format("adding semantics for different target? %s vs. %s", target, tabl));
		if (idField != null && !idField.equals(recId))
			throw new SemanticException(String.format("adding semantics for target of diferent id field? %s vs. %s", idField, recId));
	}
	
	/**
	 * @param tabl
	 * @param pk
	 * @param args 0: CLOB field
	 */
	private void addClob(String tabl, String pk, String[] args) {
		target = tabl;
		idField = pk;
		if (lobFields == null)
			lobFields = new ArrayList<String>(1);
		lobFields.add(args[0]);
	}

	/**semantic = checkCountOnDel(sql), args["tabl", "rec-id", "desc-template", "select count-arg0, recName-arg1, ...", "deleting-pk-col"]
	 * @param tabl
	 * @param pk
	 * @param args 0: error-message-format, 1: checking-sql, 2: count-col
	 */
	private void addCheckSqlCountOnDel(String tabl, String pk, String[] args) {
		target = tabl;
		idField = pk;
		checkCountErrFormat_Del = args[0];
		checkCountSql_Del = args[1];
		checkCountPkCol_Del = args[2];
	}

	/**
	 * @param tabl
	 * @param pk
	 * @param args 0: error-message-format, 1: checking-sql, 2: count-col in checking-sql
	 */
	private void addCheckSqlCountOnInsert(String tabl, String pk, String[] args) {
		target = args[0];
		idField = args[1];
		checkCountErrFormat_Ins = args[2];
		checkCountSql_Ins = args[3];
		checkCountValueCol_Ins = args[4];
		
		deschkIns = String.format("Check sql results on table %s, value-col %s", target, checkCountValueCol_Ins);
	}

	/**
	 * @param tabl
	 * @param pk
	 * @param args 0: oper-field, 1: oper-time-field;
	 */
	private void addOperTime(String tabl, String pk, String[] args) {
		target = tabl;
		idField = pk;
		operField = args[0];
		opTimeField = args[1];
	}

	/**
	 * @param tabl
	 * @param pk
	 * @param args 0: iv-col, 1: decrypt-key, 2: encrypt-key
	 */
	private void addDencrypt(String tabl, String pk, String[] args) {
		target = tabl;
		cipherCol = pk;
		ivCol = args[0];
		decryptK = args[1];
		encryptK = args[2];
	}

	private void addParentChildren(String tabl, String pk, String[] args) {
//		if (pathSufix != null) {
//			if (!target.equals(args[0]) || !idField.equals(args[1]))
//				System.err.println(String.format(
//					"WARN - IrSemantics: The adding parent.id(%s.%s) is inconsistant with the exist one(%s.%s).",
//					args[0], args[1], target, pathSufix));
//		}
		target = tabl;
		idField = pk;
		if (childConstraints == null)
			childConstraints = new ArrayList<String[]> ();
		childConstraints.add(new String[] {args[0], args[1]});
		//String.format("parent-child rule (%s.%s <-FK- '%s.%s')", target, id, childTab, childFK);
		descPC = formatPcDesc();
	}

	private String formatPcDesc() {
		String s = "";
		for (String[] c : childConstraints)
			s += String.format("\nparent-child rule (%s.%s <-FK- '%s.%s')", target, pathSufix, c[0], c[1]);
		return s;
	}

	/**add fullpath semantics.
	 * @param tabl
	 * @param pk 
	 * @param args 0: parent-field, 1: path-suffix (sibling field?), 2: fullpath-field
	 */
	private void addFullpath(String tabl, String pk, String[] args) {
//		if (pathSufix != null) {
//			if (!target.equals(args[0]) || !pathSufix.equals(args[1]))
//				System.err.println(String.format(
//					"WARN - IrSemantics: The adding parent.id(%s.%s) is inconsistant with the exist one(%s.%s).",
//					args[0], args[1], target, pathSufix));
//		}
		target = tabl;
		idField = pk;
		parentField =  args[0];
		pathSufix = args[1];
		fullPath = args[2];
		descFullpath = String.format("fullpath rule (%s = 'parent-%s.%s')", fullPath, fullPath, pathSufix);
	}

	public String genFullpath(ArrayList<Object[]> value, Map<String, Integer>colIx) {
		// can't compose fullpath as there is no parent's fullpath
		String parentId = null;
		String sibling = null;
		String recId = null;

		if (colIx.containsKey(parentField))
			parentId = (String) value.get(colIx.get(parentField))[1];

		if (colIx.containsKey(pathSufix))
			sibling = (String) value.get(colIx.get(parentField))[1];
		else sibling = pathSufix;

		recId = (String) value.get(colIx.get(idField))[1];

		return String.format("fullpath %s.%s %s", parentId, sibling, recId);
	}

//	public String genFullpath2(String conn, Object parentId, Object recId, Object siblingOrder) throws SQLException {
//		int order = 0;
//		try { order = Integer.valueOf((String) siblingOrder); } catch (Exception e) {}
//		String sibling = Radix64.toString(order, 2);
//		// 无上级节点，根 fullpath = sibling-val
//		if (parentId == null || parentId.equals(recId))
//			return String.format("%1$2s %2$s", sibling, recId); //.replace(' ', '0');
//		ICResultset rs = CpDriver.select(conn, String.format( "select %s as parentPath from %s where %s = '%s'",
//				fullPath, target, idField, parentId), DA.flag_nothing);
//		// find parent.fullpath
//		if (rs.getRowCount() == 0)
//			// no parent (path) found
//			return String.format("%1$2s %2$s", sibling, recId); //.replace(' ', '0');
//
//		rs.beforeFirst().next();
//		String parentPath = rs.getString("parentPath");
//		if (parentPath == null || parentPath.trim().length() == 0)
//			return String.format("%1$2s %2$s", sibling, recId); //.replace(' ', '0');
//		else {
////			System.out.println(String.format("WARN - using empty sufix for sibling order? %s : %s",
////					recId, getDesc(smtype.fullpath)));
//			return String.format("%s.%2s %s", parentPath, sibling, recId); //.replace(' ', '0');
//		}
//	}

	public boolean isFullpath(String f) {
		return fullPath!=null && fullPath.equals(f);
	}
	
	/**Parent field in tree table
	 * @param p
	 * @return
	 */
	public boolean isParentField(String p) {
		return parentField!=null && parentField.equals(p);
	}

	public boolean isPathSuffixField(String f) {
		return pathSufix != null && pathSufix.equals(f);
	}
	
	public boolean isIdField(String f) {
		return idField != null && idField.equals(f);
	}

	public String getDesc(smtype semantic) {
		if (smtype.parentChildrenOnDel == semantic)
			return descPC;
		else if (smtype.checkSqlCountOnInsert == semantic)
			return deschkIns;
		else return descFullpath;
	}
	
	public String getFullpathField() {
		return fullPath;
	}

	/**Is this table a parent table that a child table has a fk(single column) depends on this table?
	 * @param tabl
	 * @return
	 */
	public boolean isParentTable(String tabl) {
		return target != null && target.equals(tabl) && childConstraints != null && childConstraints.size() > 0;
	}

	public boolean isCheckCountTable(String tabl) {
		return target != null && target.equals(tabl) && checkCountPkCol_Del != null && checkCountSql_Del != null;
	}

	/**If the configured sql has a resulting count (with pk-arg select) > 0,
	 * throw an IrSemanticsException of message add while configuration.
	 * @param pkCol
	 * @param pkv
	 * @throws IrSemanticsException thrown when semantics-checking(IrSemantics.checkSqlCountOnDel, ...) failed.
	public void checkRefereeCount(String connId, String pkCol, String pkv) throws IrSemanticsException {
		try {
			if (pkCol.equals(checkCountPkCol_Del)) {
				ICResultset rs = CpDriver.select(connId, String.format(checkCountSql_Del, pkv), DA.flag_nothing);
				rs.beforeFirst();
				if (rs.next()) {
					int cnt = rs.getInt(1);
					int cols = rs.getColumnCount();
					String arg1 = cols >= 2 ? rs.getString(2) : "";
					String arg2 = cols >= 3 ? rs.getString(3) : "";
					String arg3 = cols >= 4 ? rs.getString(4) : "";
					String arg4 = cols >= 5 ? rs.getString(5) : "";
					if (cnt > 0)
						throw new IrSemanticsException(String.format(checkCountErrFormat_Del, cnt, arg1, arg2, arg3, arg4));
					// else return ok
				}
				else
					throw new IrSemanticsException(String.format(checkCountErrFormat_Del, 0, "", "", "", ""));
			} // else check nothing
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	 */

	/**[{tabl: child-1, act: {act-obj}}, <br>
	 *  {tabl: child-2, act: {act-obj}}]
	 * @param parentId
	 * @return
	@SuppressWarnings("unchecked")
	public JSONObject[] getDeleteChildrenCond(String parentId) {
		if (childConstraints == null) return null;
		JSONObject[] jreqs = new JSONObject[childConstraints.size()];

		for (int i = 0; i < jreqs.length; i++) {
		//for (String[] childfk : childConstraints) {
			String[] childfk = childConstraints.get(i);
			JSONObject cond = new JSONObject();
			cond.put("field", childfk[1]);
			cond.put("v", parentId);
			cond.put("logic", "=");
			JSONArray jconds = new JSONArray();
			jconds.add(cond);

			JSONObject req = new JSONObject();
			JSONObject act = new JSONObject();
			act.put("a", "delete");
			act.put("conds", jconds);
			req.put("tabl", childfk[0]);
			req.put("act", act);
			jreqs[i] = req;
		}
		return jreqs;
	}
	 */

	public boolean isCipherField(String c) { return c != null && c.equals(cipherCol); }

	public boolean isIvField(String c) { return c != null && c.equals(ivCol); }

	public String getCipherField() { return cipherCol; }

	public String getIvField() { return ivCol; }

	/**
	 * @param pB64 cipher in Base64
	 * @param ivB64
	 * @return [cipher-base64, new-iv-base64]
	 * @throws SQLException
	 */
	public String[] dencrypt(String pB64, String ivB64) throws SQLException {
		try {
			return AESHelper.dencrypt(pB64, decryptK, ivB64, encryptK);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new SQLException (e.getMessage()); 
		}
	}

	public boolean is(smtype type) {
		switch (type) {
		case autoPk:
			return autoPk != null;
		case fullpath:
			return fullPath != null;
		case parentChildrenOnDel:
			return childConstraints != null && childConstraints.size() > 0;
		case checkSqlCountOnInsert:
			return checkCountSql_Ins != null && checkCountValueCol_Ins != null;
		case checkSqlCountOnDel:
			return checkCountSql_Del != null;
//		case checkDsCountOnDel:
//			return :
		case dencrypt:
			return cipherCol != null && decryptK.length() > 0 && encryptK.length() > 0;
		case opTime:
			return operField != null || opTimeField != null;
		case composingCol:
			return composedCol != null && composingCols != null && composingCols.length > 0;
		case stamp1MoreThanRefee:
			return upStamp != null && downStamp != null;
		case orclob:
			return lobFields != null && lobFields.size() > 0;
		default:
			return false;
		}
	}

	/**
	 * @param conn
	 * @param n
	 * @return
	public boolean isClob(String conn, String n) {
		if (!CpDriver.isOracle(conn) || lobFields == null) return false;
		for (String lob : lobFields)
			if (lob.equals(n))
				return true;
		return false;
	}
	 */

	public boolean hasOperTime() {
		return operField != null;
	}

	public String getOperField() {
		return operField;
	}

	public String getOpTimeField() {
		return opTimeField;
	}

	/**
	 * @param connId
	 * @param chkf
	 * @param chkv
	 * @throws IrSemanticsException
	public void checkSqlCountIns(String connId, String chkf, Object chkv) throws IrSemanticsException {
		try {
			if (chkf.equals(checkCountValueCol_Ins)) {
				ICResultset rs = CpDriver.select(connId, String.format(checkCountSql_Ins, chkv), DA.flag_nothing);
				rs.beforeFirst();
				if (rs.next()) {
					int cnt = rs.getInt(1);
					int cols = rs.getColumnCount();
					String arg1 = cols >= 2 ? rs.getString(2) : "";
					String arg2 = cols >= 3 ? rs.getString(3) : "";
					String arg3 = cols >= 4 ? rs.getString(4) : "";
					String arg4 = cols >= 5 ? rs.getString(5) : "";
					if (cnt > 0)
						throw new IrSemanticsException(String.format(checkCountErrFormat_Ins, cnt, arg1, arg2, arg3, arg4));
					// else return ok
				}
				else
					throw new IrSemanticsException(String.format(checkCountErrFormat_Ins, 0, "", "", "", ""));
			} // else check nothing
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	 */

	public boolean isCheckCountInsert(String tabl) {
		return this.target.equals(tabl) && checkCountValueCol_Ins != null;
	}

	public boolean isChkCntInsField(String vcol) {
		return checkCountValueCol_Ins.equals(vcol);
	}

	////////////////////////////////// composing //////////////////////////////////////////////
	private String composedCol;

	private String[] composingCols;

	private void addComposings(String tabl, String pk, String[] args) {
		target = args[0];
		idField = args[1];
		composedCol = args[2];
		composingCols = new String[args.length - 3];
		for (int i = 3; i < args.length; i++) {
			composingCols[i - 3] = args[i];
		}
	}

	public boolean isComposedCol(String n) {
		return composedCol != null && composedCol.equals(n);
	}

	public int compondsCount() {
		return composingCols == null ? -1 : composingCols.length;
	}

	public int compondIdx(String n) {
		if (composingCols == null)
			return -1;
		for (int i = 0; i < composingCols.length; i++)
			if (composingCols[i].equals(n))
				return i;
		return -1;
	}

	public String getComposedCol() {
		return composedCol;
	}

	/**Composing componds and newIds into composed column.<br>
	 * Calling UpdateBatch.resolveAuto to resolve reference.
	 * @param newIds
	 * @param componds
	 * @return
	public String getComposedV(ArrayList<String> newIds, String[] componds) {
		String res = "";
		for (int ix = 0; ix < composingCols.length; ix++) {
			String t = composingCols[ix];
			// constant
			if (t.startsWith("'") && t.endsWith("'")) {
				res += t.replaceFirst("^'", "").replaceAll("'$", "");
			}
			// variable from client
			else res += UpdateBatch.resolveAuto(componds[ix], newIds);
		}
		return res;
	}
	 */
	
	//////////////////// stamp up-stamp 1 more than down-stamp ///////////////////
	private String upStamp;
	@SuppressWarnings("unused")
	private boolean supportUpStampOnUpdate;
	private String downStamp;
	private String defltRefeeVal;

	/**
	 * @param tabl
	 * @param pk
	 * @param args [0] up-stamp, [1] down-stamp, [2] default down stamp, [3] generate up-stamp
	 */
	private void addUpDownStamp(String tabl, String pk, String[] args) {
		target = tabl;
		idField = pk;
		upStamp = args[0];
		downStamp = args[1];
		defltRefeeVal = args.length > 2 ? args[2] : "0";
		supportUpStampOnUpdate = args.length > 3 ? Boolean.valueOf(args[3]) : false;
	}

	public String upstamp() {
		return upStamp;
	}

	public boolean isDownstamp(String n) {
		return downStamp != null && downStamp.equals(n);
	}

	public String defltRefeeVal() { return defltRefeeVal; }

	/**For null, '0', invalid date string, return "1776-07-04 00:00:00", other wise add 1 second to dataStr.
	 * @param conn
	 * @param dateStr
	 * @return
	public String upstampOnInsert(String conn, Object dateStr) {
		try {
			if (dateStr == null || ((String)dateStr).trim().length() < 6)
				// len(76-7-4) = 6
				return DateFormat.incSeconds(DA.getConnType(conn), "1776-07-04 00:00:00", 0);
			return DateFormat.incSeconds(DA.getConnType(conn), (String) dateStr, 1);
		} catch (Exception e) { return DateFormat.getTimeStampYMDHms(DA.getConnType(conn)); }
	}
	 */

	public boolean isUpstamp(String n) {
		return upStamp != null && upStamp.equals(n);
	}

	/**Get up-stamp on update (increase down stamp).<br>
	 * Return null if semantics is specified as no up-stamp generating (like on DB that support timestamp on update)
	 * @param conn
	 * @param tabl
	 * @param recId
	 * @return
	public String upstampOnUpdate(String conn, String tabl, String recId) {
		if (!supportUpStampOnUpdate)
			return null;
		try {
			// <mysql>select Date_add(%s, interval 1 second) upstamp from %s where %s = '%s'</mysql>
			// 
			String sql = DatasetCfg.getSqlx(conn, "semantics.get-downstamp",
					downStamp, tabl, this.idField, recId);
			ICResultset rs = DA.select(sql, DA.flag_nothing);
			rs.next();
			return rs.getString("upstamp");
		} catch (SQLException e) {
			return DateFormat.getTimeStampYMDHms(DA.dirverType(conn));
		}
	}
	 */

}


