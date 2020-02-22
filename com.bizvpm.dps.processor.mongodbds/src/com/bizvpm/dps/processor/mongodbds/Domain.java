package com.bizvpm.dps.processor.mongodbds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientSettings.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Domain {

	private static Map<String, Domain> map = new ConcurrentHashMap<>();

	private String id;

	private String databaseUser;

	private String databasePassword;

	private boolean databaseSSLConnection;

	private List<String> site;

	private MongoClient client;

	private MongoDatabase database;

	@SuppressWarnings("unchecked")
	public Domain(Map<String, ?> domainData) {
		id = (String) domainData.get("_id");
		databaseUser = (String) domainData.get("databaseUser");
		databasePassword = (String) domainData.get("databasePassword");
		databaseSSLConnection = Boolean.TRUE.equals(domainData.get("databaseSSLConnection"));
		site = (List<String>) domainData.get("site");
	}

	private void startDatabase() {
		Builder builder = MongoClientSettings.builder();
		builder.applyToClusterSettings(b -> b.hosts(Activator.databaseServerList));
		// 用户身份验证
		if (databaseUser != null && databasePassword != null && !databaseUser.isEmpty()
				&& !databasePassword.isEmpty()) {
			builder.credential(MongoCredential.createCredential(databaseUser, id, databasePassword.toCharArray()));
		}
		// 使用SSL
		builder.applyToSslSettings(b -> b.enabled(databaseSSLConnection));

		client = MongoClients.create(builder.build());
		database = client.getDatabase(id);
		System.out.println("连接数据库，domain:" + id);

	}

	private void stopDatabase() {
		client.close();
	}

	public void start() {
		startDatabase();
		map.put(id, Domain.this);
	}

	public void stop() {
		stopDatabase();
	}

	public String getId() {
		return id;
	}

	public MongoDatabase getDatabase() {
		return database;
	}

	public String getDatabaseUrl() {
		// mongodb://[username:password@]host1[:port1][,host2[:port2],…[,hostN[:portN]]][/[database][?options]]
		// "mongodb://host01:27017,host02:27017,host03:27017/?replicaSet=myreplset&ssl=true"
		StringBuffer sb = new StringBuffer();
		sb.append("mongodb://");
		if (databaseUser != null && databasePassword != null && !databaseUser.isEmpty()
				&& !databasePassword.isEmpty()) {
			sb.append(databaseUser + ":" + databasePassword + "@");
		}

		for (int i = 0; i < Activator.databaseServerList.size(); i++) {
			ServerAddress sa = Activator.databaseServerList.get(i);
			if (i > 0)
				sb.append(",");
			sb.append(sa.getHost());
			sb.append(":" + sa.getPort());
		}
		sb.append("/" + id);
		sb.append("?ssl=" + databaseSSLConnection);
		return sb.toString();
	}

	public static Domain get(String domainId) {
		return map.get(domainId);
	}

	public static MongoDatabase getDatabase(String domainId) {
		if (domainId != null && !domainId.isEmpty()) {
			return get(domainId).getDatabase();
		} else {
			return Activator.database;
		}
	}

	public static MongoCollection<Document> getCollection(String domainId, String collectionName) {
		return getDatabase(domainId).getCollection(collectionName);
	}

	public static String getDatabaseUser(String domain) {
		return get(domain).databaseUser;
	}

	public static String getDatabasePassword(String domain) {
		return get(domain).databasePassword;
	}

	public static List<String> listId() {
		return new ArrayList<>(map.keySet());
	}

	public static List<String> getSite(String domain) {
		return get(domain).site;
	}

}
