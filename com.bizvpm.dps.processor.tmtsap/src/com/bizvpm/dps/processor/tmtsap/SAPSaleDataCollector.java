package com.bizvpm.dps.processor.tmtsap;

import org.bson.Document;

import com.bizvisionsoft.sqldb.SqlQuery;
import com.mongodb.client.MongoCollection;

public class SAPSaleDataCollector implements ISaleDataCollector {

	@Override
	public void runGetData(final MongoCollection<Document> saleDataCol, int year, int month) throws Exception {

		String gjahr = "" + year; //$NON-NLS-1$
		String perde = String.format("%03d", month); //$NON-NLS-1$
		SqlQuery sql = new SqlQuery("sap").sql( //$NON-NLS-1$
				"select PALEDGER,GJAHR,PERDE ,MATNR ,VV010,VV030,VV040,BUKRS,BZIRK,KNDNR,VKBUR,VKGRP,VKORG,VRTNR,VV020 "
						+ "From SAPSR3.CE14000 " //$NON-NLS-1$
						+ "WHERE MANDT = '700' and PALEDGER = '02' and GJAHR = '" //$NON-NLS-1$ KNDNR--客户， BUKRS--
																					// 公司代码，BZIRK--销售地区， VKORG --
																					// 销售组织，VKBUR --销售部门，VKGRP --销售组，
																					// VRTNR -- 销售人员，VV020 --销售金额(不含税)
						+ gjahr + "' and PERDE = " + perde + "");
		sql.forEach(doc -> {
			saleDataCol.insertOne(doc);
		});
	}

}
