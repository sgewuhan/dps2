package com.bizvpm.dps.processor.plm.wcs.processor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class BomQuery extends WindchillProcessRunnable {

	public BomQuery() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		List<String> idList = (List<String>) processTask.get("ids");
		
		StringBuffer sql = new StringBuffer();
		sql.append("select * from(");
		sql.append("SELECT wtpartusagelink.ida3a5 as F_OBJOUID");
		sql.append(",wtpartusagelink.ida2a2 as L_OBJOUID");
		sql.append(",wtpartusagelink.amounta7 as L_LINENUMBER");
		sql.append(",wtpartusagelink.amounta7 as L_QUANTITY");
		sql.append(",wtpart.ida2a2 as OBJOUID");
		sql.append(",wtpartmaster.wtpartnumber as OBJNUMBER");
		sql.append(",wtpartmaster.name as OBJNAME");
		sql.append(",(wtpart.versionida2versioninfo||'.'||wtpart.iterationida2iterationinfo) as VERSION");
		sql.append(",wtview.name as objview");
		sql.append(",wtpart.statestate as state");
		sql.append(",wtpart.source as SOURCE"); 
		sql.append(",wtpartmaster.defaultunit as UNIT");
		sql.append(",V_Container.Namecontainerinfo as CONTAINER");
		sql.append(",wtuser.name as creator");
		sql.append(",wtpart.createstampa2 as createtime");
		sql.append(",wtpart.modifystampa2 as modifytime");
		sql.append(",row_number() over(partition by wtpartusagelink.ida3a5,wtpartmaster.wtpartnumber order by wtpart.ida2a2 desc) as rn");
		sql.append(" from wtpartusagelink,wtpartmaster,wtpart,wtuser,wtview,");
		sql.append(" ((select Namecontainerinfo,Ida2a2 from wtlibrary) "
				+ "union (select Namecontainerinfo,Ida2a2 from Pdmlinkproduct)) V_Container"); 
		sql.append(" where wtpartusagelink.ida3a5 " + getInClause(idList));
		sql.append(" and wtpartusagelink.ida3b5 = wtpartmaster.ida2a2");
		sql.append(" and wtpartmaster.ida2a2 = wtpart.ida3masterreference");
		sql.append(" and wtpart.ida3view = wtview.ida2a2");
		sql.append(" and wtpart.ida3b2iterationinfo = wtuser.ida2a2");
		sql.append(" and wtpart.ida3containerreference = V_Container.Ida2a2");
		sql.append(") where rn =1");

		ProcessResult result = new ProcessResult();
		
		Object value = generateSqlReqult(sql.toString());
		result.put("result", value);
		return result;

	}

}
