package com.bizvpm.dps.processor.plm.wcs.processor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class SubstituteQuery extends WindchillProcessRunnable {

	public SubstituteQuery() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		List<String> idList = (List<String>) processTask.get("linkIds");

		StringBuffer sql = new StringBuffer();
		sql.append("select * from(");
		sql.append("SELECT WTPartSubstituteLink.ida3a5 as F_OBJOUID");
		sql.append(",WTPartSubstituteLink.ida2a2 as L_OBJOUID");
//		sql.append(",WTPartSubstituteLink.amounta6 as L_QUANTITY");		//windchill9.1没有替代数量
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
		sql.append(",row_number() over(partition by WTPartSubstituteLink.ida3a5,wtpartmaster.wtpartnumber order by wtpart.ida2a2 desc) as rn");
		sql.append(" from WTPartSubstituteLink,wtpartmaster,wtpart,wtuser,wtview,");
		sql.append(" ((select Namecontainerinfo,Ida2a2 from wtlibrary) "
				+ "union (select Namecontainerinfo,Ida2a2 from Pdmlinkproduct)) V_Container"); 
		sql.append(" where WTPartSubstituteLink.ida3a5 " + getInClause(idList));
		sql.append(" and WTPartSubstituteLink.ida3b5 = wtpartmaster.ida2a2");
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
