package com.bizvpm.dps.processor.plm.wcs.processor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class PartQuery extends WindchillProcessRunnable{

	public PartQuery() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		List<String> idList = (List<String>) processTask.get("ids");

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT wtpart.ida2a2 as OBJOUID");
		sql.append(",wtpartmaster.wtpartnumber as OBJNUMBER");
		sql.append(",wtpartmaster.name as objname");
		sql.append(",(wtpart.versionida2versioninfo||'.'||wtpart.iterationida2iterationinfo) as VERSION");
		sql.append(",wtview.name as OBJVIEW");
		sql.append(",wtpart.statestate as STATE");
		sql.append(",wtpart.source as SOURCE"); 
		sql.append(",wtpartmaster.defaultunit as UNIT");
		sql.append(",V_Container.Namecontainerinfo as CONTAINER");
		sql.append(",wtuser.name as CREATOR");
		sql.append(",wtpart.createstampa2 as CREATETIME");
		sql.append(",wtpart.modifystampa2 as MODIFYTIME");
		sql.append(" from wtpartmaster,wtpart,wtuser,wtview,");
		sql.append(" ((select Namecontainerinfo,Ida2a2 from wtlibrary) "
				+ "union (select Namecontainerinfo,Ida2a2 from Pdmlinkproduct)) V_Container"); 
		sql.append(" where wtpartmaster.ida2a2 = wtpart.ida3masterreference");
		sql.append(" and wtpart.ida2a2 " + getInClause(idList));
		sql.append(" and wtpart.ida3view = wtview.ida2a2");
		sql.append(" and wtpart.ida3b2iterationinfo = wtuser.ida2a2");
		sql.append(" and wtpart.ida3containerreference = V_Container.Ida2a2");

		ProcessResult result = new ProcessResult();
		
		Object value = generateSqlReqult(sql.toString());
		result.put("result", value);
		return result;
	}


}
