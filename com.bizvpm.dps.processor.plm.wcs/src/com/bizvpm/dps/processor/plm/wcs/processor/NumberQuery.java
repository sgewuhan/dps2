package com.bizvpm.dps.processor.plm.wcs.processor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class NumberQuery extends WindchillProcessRunnable {

	public NumberQuery() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		List<String> idList = (List<String>) processTask.get("ids");
		
		StringBuffer sql = new StringBuffer();
		sql.append("select * from(");
		
		sql.append("SELECT wtpart.ida2a2 as objOUID,");
		sql.append("wtpartmaster.wtpartnumber as objnumber,");
		sql.append("wtpartmaster.name as objname,");
		sql.append("wtpart.versionida2versioninfo as version,");
		sql.append("wtpart.iterationida2iterationinfo as iteration,");
		sql.append("wtview.name as objview,");
		sql.append("wtpart.statestate as state,");
		sql.append("wtuser.name as creator,");
		sql.append("wtpart.createstampa2 as createtime,");
		sql.append("wtpart.modifystampa2 as modifytime,");
		sql.append("row_number() over(partition by wtpartmaster.wtpartnumber order by wtpart.versionida2versioninfo desc,wtpart.iterationida2iterationinfo desc) as rn");
		sql.append(" from wtpartmaster,wtpart,wtuser,wtview");
		sql.append(" where wtpartmaster.ida2a2 = wtpart.ida3masterreference");
		sql.append(" and wtpartmaster.wtpartnumber " + getStrInClause(idList));
		sql.append(" and wtpart.ida3view = wtview.ida2a2");
		sql.append(" and wtpart.ida3b2iterationinfo = wtuser.ida2a2");
		sql.append(" and wtpart.latestiterationinfo = 1");

		sql.append(") where rn =1");
		
		ProcessResult result = new ProcessResult();
		
		Object value = generateSqlReqult(sql.toString());
		result.put("result", value);
		return result;
	}

}
