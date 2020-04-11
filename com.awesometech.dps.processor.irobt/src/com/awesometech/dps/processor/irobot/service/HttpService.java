package com.awesometech.dps.processor.irobot.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.BiConsumer;

/**
 * 调用Http请求，获取返回值和cookie
 * @author Zhangjc
 *
 */
public class HttpService {

	public static void callIRobot(String urlPath, String cookie, int timeOut, BiConsumer<String, String> result)
			throws IOException {
		String backCookie = null;
		URL url = new URL(urlPath);
		URLConnection conn = url.openConnection(); 
		if (null != cookie && !"".equals(cookie)) {
			conn.setRequestProperty("Cookie", cookie);
		}
		conn.setConnectTimeout(timeOut);
		conn.setDoInput(true);
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		// 获取返回的cookie信息，并转换成字符串（原本是一个数组）
		if(null != conn.getHeaderFields().get("Set-Cookie"))
			backCookie = conn.getHeaderFields().get("Set-Cookie").toString();
		StringBuffer sb = new StringBuffer();
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		result.accept(sb.toString(), backCookie);
	}
}
