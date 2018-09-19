package com.bizvpm.dps.processor.plm.wcs.processor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class SoftAttrQuery extends WindchillProcessRunnable {

	public SoftAttrQuery() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		List<String> ouidList = (List<String>) processTask.get("ouidList");
		List<String> attrList = (List<String>) processTask.get("attrList");
		
		StringBuffer sql = new StringBuffer();

		sql.append("select stringvalue.ida3a4 as OBJOUID,"
				+ "stringdefinition.name as SOFTNAME,"
				+ "stringdefinition.displayname as DISPLAYNAME,"
				+ "stringvalue.value2 as SOFTVALUE");
		sql.append(" from stringdefinition,stringvalue");
		sql.append(" where stringvalue.ida3a6 = stringdefinition.ida2a2");
		sql.append(" and stringvalue.ida3a4 " + getInClause(ouidList));
		sql.append(" and stringdefinition.name " + getStrInClause(attrList));
		
		ProcessResult result = new ProcessResult();
		
		Object value = generateSqlReqult(sql.toString());
		result.put("result", value);
		return result;
	}

}
