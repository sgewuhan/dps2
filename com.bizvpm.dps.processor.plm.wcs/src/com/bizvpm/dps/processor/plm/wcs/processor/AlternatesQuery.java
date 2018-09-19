package com.bizvpm.dps.processor.plm.wcs.processor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class AlternatesQuery extends WindchillProcessRunnable {

	public AlternatesQuery() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		List<String> idList = (List<String>) processTask.get("ids");

		StringBuffer sql = new StringBuffer();
		sql.append("select * from (");
		sql.append("SELECT p1.ida2a2 as F_OBJOUID");
		sql.append(",WTPartAlternateLink.replacementtype as L_REPTYPE");
		sql.append(",p2.ida2a2 as OBJOUID");
		sql.append(",pm2.wtpartnumber as OBJNUMBER");
		sql.append(",pm2.name as OBJNAME");
		sql.append(",(p2.versionida2versioninfo||'.'||p2.iterationida2iterationinfo) as VERSION");
		sql.append(",wtview.name as objview");
		sql.append(",p2.statestate as state");
		sql.append(",p2.source as SOURCE"); 
		sql.append(",pm2.defaultunit as UNIT");
		sql.append(",V_Container.Namecontainerinfo as CONTAINER");
		sql.append(",wtuser.name as creator");
		sql.append(",p2.createstampa2 as createtime");
		sql.append(",p2.modifystampa2 as modifytime");
		sql.append(",row_number() over(partition by p1.ida2a2,pm2.wtpartnumber "
				+ "order by p2.ida2a2 desc) as rn"); 
		sql.append(" from WTPartAlternateLink");
		sql.append(",(select * from wtpart where wtpart.ida2a2 " + getInClause(idList)+") p1");
		sql.append(",(select wtpartmaster.* from wtpartmaster,wtpart where wtpartmaster.ida2a2 = wtpart.ida3masterreference");  
		sql.append(" and wtpart.ida2a2 " + getInClause(idList)+") pm1");
		sql.append(" ,((select Namecontainerinfo,Ida2a2 from wtlibrary) "
				+ "union (select Namecontainerinfo,Ida2a2 from Pdmlinkproduct)) V_Container"); 
		sql.append(" ,wtpart p2,wtpartmaster pm2,wtuser,wtview");
		sql.append(" where pm1.ida2a2 = WTPartAlternateLink.ida3a5");
		sql.append(" and WTPartAlternateLink.ida3b5 = pm2.ida2a2");
		sql.append(" and pm1.ida2a2 = p1.ida3masterreference");
		sql.append(" and pm2.ida2a2 = p2.ida3masterreference");
		sql.append(" and p2.ida3view = wtview.ida2a2");
		sql.append(" and p2.ida3b2iterationinfo = wtuser.ida2a2");
		sql.append(" and p2.ida3containerreference = V_Container.Ida2a2");
		sql.append(")where rn =1");
		
		
//		select * from (
//		SELECT p1.ida2a2 as F_OBJOUID,
//		WTPartAlternateLink.replacementtype as L_REPTYPE,
//		p2.ida2a2 as OBJOUID,
//		pm2.wtpartnumber as OBJNUMBER,
//		pm2.name as OBJNAME,
//		p2.versionida2versioninfo as VERSION,
//		p2.iterationida2iterationinfo as ITERATION,
//		wtview.name as objview,
//		p2.statestate as state,
//		wtuser.name as creator,
//		p2.createstampa2 as createtime,
//		p2.modifystampa2 as modifytime, 
//		row_number() over(partition by p1.ida2a2,pm2.wtpartnumber order by p2.versionida2versioninfo desc,p2.iterationida2iterationinfo desc) as rn 
//		from WTPartAlternateLink,
//		(select * from wtpart where wtpart.ida2a2 in (26296033,26096238,25656164)) p1,
//		(select wtpartmaster.* from wtpartmaster,wtpart where wtpartmaster.ida2a2 = wtpart.ida3masterreference  
//		    and wtpart.ida2a2 in (26296033,26096238,25656164)) pm1,
//		wtpart p2,wtpartmaster pm2,wtuser,wtview
//
//		where pm1.ida2a2 = WTPartAlternateLink.ida3a5 
//		and WTPartAlternateLink.ida3b5 = pm2.ida2a2 
//		and pm1.ida2a2 = p1.ida3masterreference 
//		and pm2.ida2a2 = p2.ida3masterreference 
//		and p2.ida3view = wtview.ida2a2 
//		and p2.ida3b2iterationinfo = wtuser.ida2a2
//		)
//		where rn =1
		
		ProcessResult result = new ProcessResult();
		
		Object value = generateSqlReqult(sql.toString());
		result.put("result", value);
		return result;
	}

}
