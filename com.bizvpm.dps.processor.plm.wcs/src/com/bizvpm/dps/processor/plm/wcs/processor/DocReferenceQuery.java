package com.bizvpm.dps.processor.plm.wcs.processor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class DocReferenceQuery extends WindchillProcessRunnable {

	public DocReferenceQuery() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		
		List<String> idList = (List<String>) processTask.get("ids");
		
		StringBuffer sql = new StringBuffer();
		sql.append("select * from(");
		
		sql.append("SELECT WTPartReferenceLink.ida3a5 as F_OBJOUID");
		sql.append(",wtdocument.ida2a2 as OBJOUID");
		sql.append(",WTDocumentMaster.wtdocumentnumber as OBJNUMBER");
		sql.append(",WTDocumentMaster.name as OBJNAME");
		sql.append(",(wtdocument.versionida2versioninfo||'.'||wtdocument.iterationida2iterationinfo) as VERSION");
		sql.append(",wtdocument.statestate as STATE");
		sql.append(",V_Container.Namecontainerinfo as CONTAINER");
		sql.append(",wtuser.name as creator");
		sql.append(",wtdocument.createstampa2 as createtime");
		sql.append(",wtdocument.modifystampa2 as modifytime");
		sql.append(",row_number() over(partition by WTPartReferenceLink.ida3a5,WTDocumentMaster.wtdocumentnumber order by wtdocument.ida2a2 desc) as rn");
		sql.append(" from WTPartReferenceLink,wtdocumentmaster,wtdocument,wtuser,");
		sql.append(" ((select Namecontainerinfo,Ida2a2 from wtlibrary) "
				+ "union (select Namecontainerinfo,Ida2a2 from Pdmlinkproduct)) V_Container");
		sql.append(" where WTPartReferenceLink.ida3a5 " + getInClause(idList));
		sql.append(" and WTPartReferenceLink.ida3b5 = WTDocumentMaster.ida2a2");
		sql.append(" and wtdocumentmaster.ida2a2 = wtdocument.ida3masterreference");
		sql.append(" and wtdocument.ida3b2iterationinfo = wtuser.ida2a2");
		sql.append(" and wtdocument.ida3containerreference = V_Container.Ida2a2");

		sql.append(") where rn =1");
		
		ProcessResult result = new ProcessResult();
		
		Object value = generateSqlReqult(sql.toString());
		result.put("result", value);
		return result;
	}

}
