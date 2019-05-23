//package io.odysz.semantics;
//
//import static org.junit.Assert.*;
//
//import java.util.ArrayList;
//
//import org.junit.Test;
//
//import io.odysz.common.Regex;
//import io.odysz.common.Utils;
//
//public class ISemantextTest {
//
//	@Test
//	public void testResulvePattern() {
//		Utils.printCaller(false);
//
//		Regex refReg = new Regex(ISemantext.refPattern);
//		
//		ArrayList<String> grps = refReg.findGroups("RESULVE task_nodes.taskId ");
//
//		Utils.logi(grps);
//		assertEquals(3, grps.size());
//
//		grps = refReg.findGroups("RESULVE task_nodes.instId ");
//		Utils.logi(grps);
//		assertEquals(3, grps.size());
//	}
//
//}
