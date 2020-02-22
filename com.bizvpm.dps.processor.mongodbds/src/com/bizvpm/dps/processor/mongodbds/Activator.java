package com.bizvpm.dps.processor.mongodbds;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.bson.Document;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientSettings.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	public static MongoDatabase database;
	
	private MongoClient hostClient;

	static List<ServerAddress> databaseServerList;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		// 读取host配置文件mongodbhost.properties
		Properties props = new Properties();
		InputStream is = new BufferedInputStream(
				new FileInputStream(System.getProperty("user.dir") + "/configuration/mongodbhost.properties"));
		props.load(is);
		String hostDatabaseUser = props.getProperty("db.user"); // $NON-Activator-1$
		String hostDatabasePassword = props.getProperty("db.password"); // $NON-Activator-1$
		String hostDatabaseName = props.getProperty("db.name"); // $NON-Activator-1$
		databaseServerList = getServerList(props.getProperty("db.hosts"));
		String ssl = props.getProperty("db.ssl");

		database = connectDatabase(hostDatabaseName, hostDatabaseUser, hostDatabasePassword,
				"true".equalsIgnoreCase(ssl));

		// 加载域数据库
		database.getCollection("domain").find().forEach((Document d) -> new Domain(d).start());

	}

	private static List<ServerAddress> getServerList(String replicaSet) {
		final List<ServerAddress> serverList = new ArrayList<ServerAddress>();
		String[] arr = replicaSet.split(" ");
		for (int i = 0; i < arr.length; i++) {
			String[] ari = arr[i].split(":");
			ServerAddress address = new ServerAddress(ari[0], Integer.parseInt(ari[1]));
			serverList.add(address);
		}
		return serverList;
	}

	private MongoDatabase connectDatabase(String databaseName, String user, String password, boolean ssl) {
		Builder builder = MongoClientSettings.builder();
		builder.applyToClusterSettings(b -> b.hosts(databaseServerList));
		// 用户身份验证
		if (user!=null && password!=null && !user.isEmpty() && !password.isEmpty()) {
			builder.credential(MongoCredential.createCredential(user, databaseName, password.toCharArray()));
		}
		// 使用SSL
		builder.applyToSslSettings(b -> b.enabled(ssl));

		hostClient = MongoClients.create(builder.build());

		MongoDatabase hostDatabase = hostClient.getDatabase(databaseName);
		System.out.println("连接数据库：" + databaseName);

		return hostDatabase;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
