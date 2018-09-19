package com.tmt.dps.processor.sms;

import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;
import com.tmt.dps.processor.sms.client.BS3Webservice;
import com.tmt.dps.processor.sms.client.BS3WebserviceSoap;
import com.tmt.dps.processor.sms.preferences.TMTSmsPreferenceConstants;

public class TMTSmsProcessor implements IProcessorRunable {

	@SuppressWarnings("unchecked")
	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor,
			IProcessContext context) throws Exception {
		String mobileNumber;
		String textMessage = (String) processTask.get("textMessage");
		String sendTime = (String) processTask.get("sendTime");
		String mobileCount = (String) processTask.get("mobileCount");
		Map<String, String> toList = (Map<String, String>) processTask
				.get("to");
		String[] keySet = toList.keySet().toArray(new String[0]);
		boolean userWebService = Activator.getDefault().getPreferenceStore()
				.getBoolean(TMTSmsPreferenceConstants.USERWEBSERVICE);
		if (userWebService) {
			String url = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.WEBSERVICEWSDL);
			String user = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.WEBSERVICEUSER);
			String pwd = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.WEBSERVICEPASSWORD);
			BS3WebserviceSoap bs3WebserviceSoap = new BS3Webservice(
					new URL(url)).getBS3WebserviceSoap();
			for (int i = 0; i < keySet.length; i++) {
				String senderId = keySet[i];
				mobileNumber = toList.get(senderId);
				String sendSMS = bs3WebserviceSoap.sendSMS(user, pwd, "", "",
						sendTime, mobileNumber, textMessage);
				Document doc = DocumentHelper.parseText(sendSMS);
				Element root = doc.getRootElement();
				Element status = root.element("Status");
				String statusText = status.getText();
				if (!"1".equals(statusText)) {
					String errCode = root.element("ErrCode").getText();
					String errDespr = root.element("ErrDespr").getText();
					throw new Exception("Error Code :" + errCode
							+ " Error Description:" + errDespr);
				}
			}
		} else {
			String[] sqls = new String[keySet.length];
			for (int i = 0; i < keySet.length; i++) {
				String senderId = keySet[i];
				mobileNumber = toList.get(senderId);
				String sql = "INSERT INTO MT_SMS(SEND_USER_ID,SCH_MARK,IS_HANDLE,MOBILE_NUMBER,CONTENT,"
						+ "SEND_TIME,SMS_Status,Mobile_Count) " + "values ('"
						+ senderId + "','now','0','" + mobileNumber + "','"
						+ textMessage + "','" + sendTime + "',0," + mobileCount
						+ ")";
				sqls[i] = sql;
				System.out.println(sql);
			}
			executeSql(sqls);
		}
		ProcessResult r = new ProcessResult();
		r.put("results", "success");
		return r;
	}

	private void executeSql(String[] sqls) throws Exception {
		Connection conn = Activator.getDefault().getConnection();
		Statement stat = null;
		boolean autoCommit = conn.getAutoCommit();
		try {
			conn.setAutoCommit(false);
			stat = conn.createStatement();
			for (int i = 0; i < sqls.length; i++) {
				stat.executeUpdate(sqls[i]);
			}
			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			for (int i = 0; i < sqls.length; i++) {
				System.out.println("SQL£º" + sqls[i]);
			}
			throw e;
		} finally {
			conn.setAutoCommit(autoCommit);
			try {
				if (stat != null)
					stat.close();
			} catch (Exception e) {
				throw e;
			}
			Activator.getDefault().freeConnection(conn);
		}
	}

}
