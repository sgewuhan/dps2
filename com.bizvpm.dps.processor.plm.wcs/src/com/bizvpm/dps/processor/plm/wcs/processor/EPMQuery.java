package com.bizvpm.dps.processor.plm.wcs.processor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class EPMQuery extends WindchillProcessRunnable {

	public EPMQuery() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		List<String> idList = (List<String>) processTask.get("ids");

		StringBuffer sql = new StringBuffer();
		
		sql.append("SELECT EPMBuildhistory.ida3b5 as F_OBJOUID");
		sql.append(",to_Char(EPMBuildhistory.buildtype) as L_BUILDTYPE");
		sql.append(",EPMDocument.ida2a2 as OBJOUID");
		sql.append(",EPMDocumentMaster.documentnumber as OBJNUMBER");
		sql.append(",EPMDocumentMaster.name as OBJNAME");
		sql.append(",EPMDocumentMaster.cadname as CADNAME");
		sql.append(",EPMDocumentMaster.doctype as DOCTYPE");
		sql.append(",(EPMDocument.versionida2versioninfo||'.'||EPMDocument.iterationida2iterationinfo) as VERSION");
		sql.append(",EPMDocument.statestate as STATE");
		sql.append(",V_Container.Namecontainerinfo as CONTAINER");
		sql.append(",wtuser.name as CREATOR");
		sql.append(",EPMDocument.createstampa2 as CREATETIME");
		sql.append(",EPMDocument.modifystampa2 as MODIFYTIME");
		sql.append(" from EPMBuildhistory,EPMDocumentMaster,EPMDocument,wtuser,");
		sql.append(" ((select Namecontainerinfo,Ida2a2 from wtlibrary) "
				+ "union (select Namecontainerinfo,Ida2a2 from Pdmlinkproduct)) V_Container"); 
		sql.append(" where EPMBuildhistory.ida3b5 " + getInClause(idList));
		sql.append(" and EPMBuildhistory.ida3a5 = EPMDocument.ida2a2");
		sql.append(" and EPMDocumentMaster.ida2a2 = EPMDocument.ida3masterreference");
		sql.append(" and EPMDocument.ida3b2iterationinfo = wtuser.ida2a2");
		sql.append(" and EPMDocument.ida3containerreference = V_Container.Ida2a2");

		//通过三维找二维，表：EPMReferenceLink，通过master找
		
//		(SELECT EPMBuildhistory.ida3b5 as F_OBJOUID
//				,to_char(EPMBuildhistory.buildtype) as L_BUILDTYPE
//				,EPMDocument.ida2a2 as OBJOUID
//				,EPMDocumentMaster.documentnumber as OBJNUMBER
//				,EPMDocumentMaster.name as OBJNAME
//				,EPMDocumentMaster.doctype as DOCTYPE
//				,(EPMDocument.versionida2versioninfo||'.'||EPMDocument.iterationida2iterationinfo) as VERSION
//				,EPMDocument.statestate as STATE
//				,wtuser.name as CREATOR
//				,EPMDocument.createstampa2 as CREATETIME
//				,EPMDocument.modifystampa2 as MODIFYTIME
//				from EPMBuildhistory,EPMDocumentMaster,EPMDocument,wtuser 
//				where EPMBuildhistory.ida3b5 
//				in (26296033,26096238,25656164) 
//				and EPMBuildhistory.ida3a5 = EPMDocument.ida2a2 
//				and EPMDocumentMaster.ida2a2 = EPMDocument.ida3masterreference 
//				and EPMDocument.ida3b2iterationinfo = wtuser.ida2a2)
//				union
//				(select F_OBJOUID,L_BUILDTYPE,OBJOUID,OBJNUMBER,OBJNAME,DOCTYPE,VERSION,STATE,CREATOR,CREATETIME,MODIFYTIME from(
//				SELECT EPMDocument.ida2a2 as F_OBJOUID
//				,EPMReferenceLink.referencetype as L_BUILDTYPE
//				,EPMDocument.ida2a2 as OBJOUID
//				,EPMDocumentMaster.documentnumber as OBJNUMBER
//				,EPMDocumentMaster.name as OBJNAME
//				,EPMDocumentMaster.doctype as DOCTYPE
//				,(EPMDocument.versionida2versioninfo||'.'||EPMDocument.iterationida2iterationinfo) as VERSION
//				,EPMDocument.statestate as STATE
//				,wtuser.name as CREATOR
//				,EPMDocument.createstampa2 as CREATETIME
//				,EPMDocument.modifystampa2 as MODIFYTIME 
//				,row_number() over(partition by EPMDocumentMaster.ida2a2 order by EPMDocument.versionida2versioninfo desc,EPMDocument.iterationida2iterationinfo desc) as rn
//				from EPMReferenceLink,EPMDocumentMaster,EPMDocument,wtuser 
//				where EPMReferenceLink.ida2a2 
//				    in (select EPMReferenceLink.ida2a2 as DocKey
//				        from EPMBuildhistory , EPMReferenceLink , EPMDocument
//				        where EPMBuildhistory.ida3b5 in (26296033,26096238,25656164)
//				        and EPMBuildhistory.ida3a5 = EPMDocument.ida2a2 
//				        and EPMDocument.ida3masterreference = EPMReferenceLink.ida3b5) 
//				and EPMReferenceLink.ida3a5 = EPMDocument.ida2a2 
//				and EPMDocumentMaster.ida2a2 = EPMDocument.ida3masterreference 
//				and EPMDocument.ida3b2iterationinfo = wtuser.ida2a2
//				) where rn=1)
		
		
		ProcessResult result = new ProcessResult();
		
		Object value = generateSqlReqult(sql.toString());
		result.put("result", value);
		return result;
	}

}
