package com.bizvpm.dps.processor.email;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.MailcapCommandMap;
import javax.mail.internet.MimeUtility;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.processor.email.preferences.EMailPreferenceConstants;
import com.bizvpm.dps.runtime.DPSUtil;
import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class SendEMailProcessor implements IProcessorRunable {

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");

		Email email = init(processTask);
		String result = email.send();

		ProcessResult r = new ProcessResult();
		r.put("result", result);
		return r;
	}

	private Email init(ProcessTask processTask) throws Exception {
		Email email;
		String emailType = (String) processTask.get("emailType");
		String message = (String) processTask.get("message");
		String title = (String) processTask.get("title");
		if ("html".equals(emailType)) {
			email = new HtmlEmail();
			((HtmlEmail) email).setHtmlMsg(message);
			initAttachments((HtmlEmail) email, (List<?>) processTask.get("attachment"));
		} else if ("imghtml".equals(emailType)) {
			email = new ImageHtmlEmail();
			((ImageHtmlEmail) email).setHtmlMsg(message);
			initAttachments((ImageHtmlEmail) email, (List<?>) processTask.get("attachment"));
		} else if ("multipart".equals(emailType)) {
			email = new MultiPartEmail();
			((MultiPartEmail) email).setMsg(message);
			initAttachments((MultiPartEmail) email, (List<?>) processTask.get("attachment"));
		} else {
			email = new SimpleEmail();
			((SimpleEmail) email).setMsg(message);
		}

		email.setCharset("UTF-8");
		email.setSubject(title);

		addTo(email, (List<?>) processTask.get("to"));

		Object cc = processTask.get("cc");
		if (cc instanceof List<?> && ((List<?>) cc).size() > 0) {
			addCc(email, (List<?>) cc);
		}


		Boolean useServerAddress = (Boolean) processTask.get("useServerAddress");
		String smtpHost, senderAddress, senderPassword;
		int smtpPort;
		Boolean smtpUseSSL;
		if (Boolean.TRUE.equals(useServerAddress)) {
			smtpHost = Activator.getDefault().getPreferenceStore().getString(EMailPreferenceConstants.EMAIL_HOSTNAME);
			smtpPort = Activator.getDefault().getPreferenceStore().getInt(EMailPreferenceConstants.EMAIL_SMTPPORT);
			smtpUseSSL = Activator.getDefault().getPreferenceStore()
					.getBoolean(EMailPreferenceConstants.EMAIL_SSLONCONNECT);
			senderAddress = Activator.getDefault().getPreferenceStore()
					.getString(EMailPreferenceConstants.EMAIL_AUTHUSER);
			senderPassword = Activator.getDefault().getPreferenceStore()
					.getString(EMailPreferenceConstants.EMAIL_AUTHPASS);
		} else {
			smtpHost = (String) processTask.get("smtpHost");
			smtpPort = (int) processTask.get("smtpPort");
			smtpUseSSL = (Boolean) processTask.get("smtpUseSSL");
			senderAddress = (String) processTask.get("senderAddress");
			senderPassword = (String) processTask.get("senderPassword");
		}

		email.setHostName(smtpHost);
		email.setSmtpPort(smtpPort);
		email.setSSLOnConnect(smtpUseSSL);
		email.setAuthentication(senderAddress, senderPassword);
		
		String from = (String) processTask.get("from");
		if (from != null) {
			//String[] froms = ((String) from).split(":");
			//if (froms.length > 1) {
				email.setFrom(senderAddress, from);
			} else {
				email.setFrom(senderAddress);
			}

		return email;
	}

	@SuppressWarnings("unchecked")
	private void initAttachments(MultiPartEmail email, List<?> list) throws Exception {
		if (list != null) {
			for (Object obj : list) {
				long time = new Date().getTime();
				initAttachment(email, (Map<String, Object>) obj, time);
			}
		}
	}

	private void initAttachment(MultiPartEmail email, Map<String, Object> map, long time) throws Exception {
		String pathName = DPSUtil.getTempDirector(getClass(), true);

		EmailAttachment attachment = new EmailAttachment();
		attachment.setDisposition(EmailAttachment.ATTACHMENT);
		if (map != null && !map.isEmpty() && map.size() > 0) {
			Object obj = map.get("file");
			if (obj instanceof String) {
				attachment.setURL(new URL((String) obj));
			} else {
				File file = new File(pathName);
				file = new File(pathName + time);
				writeToFile((DataHandler) obj, file);
				attachment.setPath(file.getPath());
			}
			String string = (String) map.get("filename");
			attachment.setName(MimeUtility.encodeText(string, "gb2312", "b"));
			email.attach(attachment);
		} else {
			throw new Exception("This type of attachment is not supported!");
		}
	}

	private void addTo(Email email, List<?> tolist) throws Exception {
		for (Object tos : tolist) {
			String[] to = ((String) tos).split(":");
			if (to.length > 1) {
				email.addTo(to[0], to[1]);
			} else {
				email.addTo(to[0]);
			}
		}
	}

	private void addCc(Email email, List<?> tolist) throws Exception {
		for (Object tos : tolist) {
			String[] to = ((String) tos).split(":");
			if (to.length > 1) {
				email.addCc(to[0], to[1]);
			} else {
				email.addCc(to[0]);
			}
		}
	}

	private void writeToFile(DataHandler dataHandler, File file) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = dataHandler.getInputStream();
			os = new FileOutputStream(file);
			byte[] bytes = new byte[1024];
			int c;
			while ((c = is.read(bytes)) != -1) {
				os.write(bytes, 0, c);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (os != null) {
				os.close();
			}
			if (is != null) {
				is.close();
			}
		}
	}

}
