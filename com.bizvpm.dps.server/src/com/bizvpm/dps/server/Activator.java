package com.bizvpm.dps.server;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.ws.Endpoint;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.MongoClientOptions.Builder;

public class Activator implements BundleActivator {

	private Endpoint endpointPersistence;
	private Endpoint endpointServer;
	private DB database;
	private String dpsAddress;

	@Override
	public void start(BundleContext context) throws Exception {
		context.addBundleListener(new BundleListener() {
			
			@Override
			public void bundleChanged(BundleEvent event) {
				System.out.print(event.getType() + "---");
				System.out.print(event.getBundle().getBundleId() + "---");
				System.out.println(event.getBundle().getSymbolicName());
			}
		});
		loadProperties();
		endpointPersistence = createPersistenceEndpoint();
		endpointServer = createServerEndpoint();
	}

	private Endpoint createServerEndpoint() {
		Endpoint endpoint = Endpoint.create(new DPServer(database));
		String url = "http://" + dpsAddress + "/dps/server";
		endpoint.publish(url);
		System.out.println(url);
		return endpoint;
	}

	private Endpoint createPersistenceEndpoint() {
		Endpoint endpoint = Endpoint.create(new Persistence(database));
		String url = "http://" + dpsAddress + "/dps/persistence";
		endpoint.publish(url);
		System.out.println(url);
		return endpoint;
	}

	@SuppressWarnings("deprecation")
	private void loadProperties() {
		InputStream is = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(System.getProperty("user.dir") //$NON-NLS-1$
					+ "/configuration/dpserver.properties"); //$NON-NLS-1$
			is = new BufferedInputStream(fis);
			Properties props = new Properties();
			props.load(is);

			MongoClient mongo = createMongoClient(props);
			String dbname = props.getProperty("db.name"); //$NON-NLS-1$
			database = mongo.getDB(dbname);
			dpsAddress = props.getProperty("dps.address");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
	}

	public static MongoClient createMongoClient(Properties props)
			throws UnknownHostException {
		String host = props.getProperty("db.host"); //$NON-NLS-1$
		String _port = props.getProperty("db.port");
		int port = _port == null ? 10001 : Integer.parseInt(_port); //$NON-NLS-1$
		ArrayList<ServerAddress> serverList = null;
		String replicaSet = props.getProperty("db.replicaSet"); //$NON-NLS-1$
		if (replicaSet != null && replicaSet.length() > 0) {
			serverList = new ArrayList<ServerAddress>();
			String[] arr = replicaSet.split(" ");
			for (int i = 0; i < arr.length; i++) {
				String[] ari = arr[i].split(":");
				ServerAddress address = new ServerAddress(ari[0],
						Integer.parseInt(ari[1]));
				serverList.add(address);
			}
		}

		Builder builder = MongoClientOptions.builder();
		//		builder.autoConnectRetry("true".equalsIgnoreCase(props //$NON-NLS-1$
		//				.getProperty("db.options.autoConnectRetry"))); //$NON-NLS-1$
		builder.connectionsPerHost(Integer.parseInt(props
				.getProperty("db.options.connectionsPerHost"))); //$NON-NLS-1$
		builder.maxWaitTime(Integer.parseInt(props
				.getProperty("db.options.maxWaitTime"))); //$NON-NLS-1$
		builder.socketTimeout(Integer.parseInt(props
				.getProperty("db.options.socketTimeout"))); //$NON-NLS-1$
		builder.connectTimeout(Integer.parseInt(props
				.getProperty("db.options.connectTimeout"))); //$NON-NLS-1$
		builder.threadsAllowedToBlockForConnectionMultiplier(Integer.parseInt(props
				.getProperty("db.options.threadsAllowedToBlockForConnectionMultiplier"))); //$NON-NLS-1$
		ServerAddress address = new ServerAddress(host, port);
		if (serverList != null) {
			return new MongoClient(serverList);
		} else {
			return new MongoClient(address, builder.build());
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		endpointPersistence.stop();
		endpointServer.stop();
	}

}
