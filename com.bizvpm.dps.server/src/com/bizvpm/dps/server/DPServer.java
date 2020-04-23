package com.bizvpm.dps.server;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class DPServer implements IPersistenceConstants {

	private DB db;

	public DPServer(DB database) {
		this.db = database;
	}

	@WebMethod
	public StringList listOnlineProcessService() {
		DBCollection col = db.getCollection(DPS_COLLECTION);
		BasicDBObject query = new BasicDBObject().append(F_STATUS, 1);
		DBCursor cur = col.find(query);
		List<String> items = new ArrayList<String>();
		while (cur.hasNext()) {
			DBObject data = cur.next();
			String host = (String) data.get(F_HOSTNAME);
			String address = getNamedProcessService(host);
			items.add(address);
		}
		cur.close();
		StringList hostList = new StringList();
		hostList.setItems(items);
		return hostList;
	}

	@WebMethod
	public String getProcessService() {
		DBCollection col = db.getCollection(DPS_COLLECTION);
		BasicDBObject query = new BasicDBObject().append(F_STATUS, 1);
		DBObject data = col.findOne(query);
		if (data == null)
			throw new RuntimeException("Cannot find process service");
		String host = (String) data.get(F_HOSTNAME);
		return getNamedProcessService(host);
	}

	@WebMethod
	public String getNamedProcessService(String host) {
		DBCollection col = db.getCollection(DPS_ACCOUNT);
		Object pattern = Pattern.compile(host, Pattern.CASE_INSENSITIVE);
		DBObject query = new BasicDBObject();
		query.put(F_HOSTNAME, pattern);
		DBObject d = col.findOne(query);
		if (d != null) {
			return "http://" + d.get(F_HOSTIP) + ":" + d.get(F_HOSTPORT) + "/processor?wsdl";
		} else {
			return "";
		}
	}

}
