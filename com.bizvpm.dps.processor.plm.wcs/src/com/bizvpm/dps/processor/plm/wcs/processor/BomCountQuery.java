package com.bizvpm.dps.processor.plm.wcs.processor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class BomCountQuery extends WindchillProcessRunnable {

	public BomCountQuery() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		List<String> idList = (List<String>) processTask.get("ids");

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT wtpartusagelink.ida3a5 as F_OBJOUID,");
		sql.append("count(wtpartusagelink.ida3a5) as COUNT");
		sql.append(" from wtpartusagelink");
		sql.append(" where wtpartusagelink.ida3a5 " + getInClause(idList));
		sql.append(" group by wtpartusagelink.ida3a5");		

		ProcessResult result = new ProcessResult();
		
		Object value = generateSqlReqult(sql.toString());
		result.put("result", value);
		return result;
	}

}
