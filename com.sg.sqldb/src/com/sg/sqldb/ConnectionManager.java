package com.sg.sqldb;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 管理类DBConnectionManager支持对一个或多个由属性文件定义的数据库连接
 * 池的访问.客户程序可以调用getInstance()方法访问本类的唯一实例.
 */
class ConnectionManager {
	static private ConnectionManager instance; // 唯一实例
	static private int clients;

	private CopyOnWriteArrayList<Driver> drivers = new CopyOnWriteArrayList<Driver>();
	private Map<String, DBConnectionPool> pools = new ConcurrentHashMap<String, DBConnectionPool>();

	/**
	 * 返回唯一实例.如果是第一次调用此方法,则创建实例
	 * 
	 * 
	 * @return DBConnectionManager 唯一实例
	 */
	static synchronized public ConnectionManager getInstance(Properties properties) {
		if (instance == null) {
			instance = new ConnectionManager(properties);
		}
		clients++;
		return instance;
	}

	/**
	 * 建构函数私有以防止其它对象创建本类实例
	 * 
	 * @param properties
	 */
	private ConnectionManager(Properties properties) {
		init(properties);
	}

	/**
	 * 将连接对象返回给由名字指定的连接池
	 * 
	 * @param name
	 *            在属性文件中定义的连接池名字
	 * @param con
	 *            连接对象
	 */
	public void freeConnection(String name, Connection con) {
		DBConnectionPool pool = (DBConnectionPool) pools.get(name);
		if (pool != null) {
			pool.freeConnection(con);
		}
	}

	/**
	 * 获得一个可用的(空闲的)连接.如果没有可用连接,且已有连接数小于最大连接数 限制,则创建并返回新连接
	 * 
	 * @param name
	 *            在属性文件中定义的连接池名字
	 * @return Connection 可用连接或null
	 */
	public Connection getConnection(String name) {
		DBConnectionPool pool = (DBConnectionPool) pools.get(name);
		if (pool != null) {
			Connection conn = pool.getConnection();
			if (conn == null) {
				conn = pool.getConnection(5000);
			}
			return conn;
		}
		return null;
	}

	/**
	 * 获得一个可用连接.若没有可用连接,且已有连接数小于最大连接数限制, 则创建并返回新连接.否则,在指定的时间内等待其它线程释放连接.
	 * 
	 * @param name
	 *            连接池名字
	 * @param time
	 *            以毫秒计的等待时间
	 * @return Connection 可用连接或null
	 */
	public Connection getConnection(String name, long time) {
		DBConnectionPool pool = (DBConnectionPool) pools.get(name);
		if (pool != null) {
			return pool.getConnection(time);
		}
		return null;
	}

	public int getClient() {
		return clients;
	}

	/**
	 * 关闭所有连接,撤销驱动程序的注册
	 */
	public synchronized void release() {
		// 等待直到最后一个客户程序调用
		if (--clients != 0) {
			return;
		}

		Collection<DBConnectionPool> allPools = pools.values();
		Iterator<DBConnectionPool> iter = allPools.iterator();

		while (iter.hasNext()) {
			iter.next().release();
		}

		Iterator<Driver> iter2 = drivers.iterator();
		while (iter2.hasNext()) {
			Driver driver = iter2.next();
			try {
				DriverManager.deregisterDriver(driver);
				// System.out.println(" 撤销JDBC驱动程序 " +
				// driver.getClass().getName() + "的注册");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 根据指定属性创建连接池实例.
	 * 
	 * @param props
	 *            连接池属性
	 */
	private void createPools(Properties props) {
		Enumeration<?> propNames = props.propertyNames();
		while (propNames.hasMoreElements()) {
			String name = (String) propNames.nextElement();
			if (name.endsWith(".url")) {
				String poolName = name.substring(0, name.lastIndexOf("."));
				String url = props.getProperty(poolName + ".url");
				if (url == null) {
					System.err.println("没有为连接池" + poolName + "指定URL");
					continue;
				}
				String user = props.getProperty(poolName + ".user");
				String password = props.getProperty(poolName + ".password");
				String maxconn = props.getProperty(poolName + ".maxconn", "0");

				int max;
				try {
					max = Integer.valueOf(maxconn).intValue();
				} catch (NumberFormatException e) {
					System.err.println("ERROR SETTING OF: MAX CONNECTION COUNT" + maxconn + " .POOL NAME: " + poolName);
					max = 10;
				}
				DBConnectionPool pool = new DBConnectionPool(poolName, url, user, password, max);
				pools.put(poolName, pool);
				System.out.println("CONNECTION POOL:" + poolName + " INIT...OK");
			}
		}
	}

	public Connection createConnection(String poolName) {
		DBConnectionPool dbConnectionPool = pools.get(poolName);
		if (dbConnectionPool == null) {
			return null;
		} else {
			return dbConnectionPool.newConnection();
		}
	}

	/**
	 * 读取属性完成初始化
	 * 
	 * @param properties
	 */
	private void init(Properties properties) {
		Properties dbProps;
		if (properties == null) {
			try {
				dbProps = new Properties();
				InputStream is = new BufferedInputStream(
						new FileInputStream(System.getProperty("user.dir") + "/configuration/db.properties"));
				dbProps.load(is);
			} catch (Exception e) {
				System.err.println("不能读取数据库属性. " + "请确保db.properties在CLASSPATH指定的路径中，并得到正确的指定");
				e.printStackTrace();
				return;
			}
		} else {
			dbProps = properties;
		}
		loadDrivers(dbProps);
		createPools(dbProps);
	}

	/**
	 * 装载和注册所有JDBC驱动程序
	 * 
	 * @param props
	 *            属性
	 */
	private void loadDrivers(Properties props) {
		String driverClasses = props.getProperty("drivers");
		StringTokenizer st = new StringTokenizer(driverClasses);
		while (st.hasMoreElements()) {
			String driverClassName = st.nextToken().trim();
			try {
				Driver driver = (Driver) Class.forName(driverClassName).newInstance();
				DriverManager.registerDriver(driver);
				drivers.add(driver);
				// System.out.println(" 成功注册JDBC驱动程序" + driverClassName);
			} catch (Exception e) {
				System.err.println(" 无法注册JDBC驱动程序: " + driverClassName);
				e.printStackTrace();
			}
		}
	}

	// /**
	// * 将文本信息写入日志文件
	// */
	// private void log(String msg) {
	// log.println(new Date() + ": " + msg);
	// }
	//
	// /**
	// * 将文本信息与异常写入日志文件
	// */
	// private void log(Throwable e, String msg) {
	// log.println(new Date() + ": " + msg);
	// e.printStackTrace(log);
	// }

	/**
	 * 此内部类定义了一个连接池.它能够根据要求创建新连接,直到预定的最 大连接数为止.在返回连接给客户程序之前,它能够验证连接的有效性.
	 */
	class DBConnectionPool {
		private int checkedOut;
		private CopyOnWriteArrayList<Connection> freeConnections = new CopyOnWriteArrayList<Connection>();
		private int maxConn;
		private String name;
		private String password;
		private String URL;
		private String user;

		/**
		 * 创建新的连接池
		 * 
		 * @param name
		 *            连接池名字
		 * @param URL
		 *            数据库的JDBC URL
		 * @param user
		 *            数据库帐号,或 null
		 * @param password
		 *            密码,或 null
		 * @param maxConn
		 *            此连接池允许建立的最大连接数
		 */
		public DBConnectionPool(String name, String URL, String user, String password, int maxConn) {
			this.name = name;
			this.URL = URL;
			this.user = user;
			this.password = password;
			this.maxConn = maxConn;
		}

		/**
		 * 将不再使用的连接返回给连接池
		 * 
		 * @param con
		 *            客户程序释放的连接
		 */
		public synchronized void freeConnection(Connection con) {
			// 将指定连接加入到向量末尾
			if (con == null)
				return;
			freeConnections.add(con);
			checkedOut--;
			notifyAll();
		}

		/**
		 * 从连接池获得一个可用连接.如没有空闲的连接且当前连接数小于最大连接 数限制,则创建新连接.如原来登记为可用的连接不再有效,则从向量删除之,
		 * 然后递归调用自己以尝试新的可用连接.
		 */
		@SuppressWarnings("resource")
		public synchronized Connection getConnection() {
			Connection con = null;
			if (freeConnections.size() > 0) {
				// 获取向量中第一个可用连接
				con = freeConnections.get(0);
				freeConnections.remove(0);
				try {
					if (con.isClosed()) {
						// System.out.println("从连接池" + name + "删除一个无效连接");
						// 递归调用自己,尝试再次获取可用连接
						con = getConnection();
					}
				} catch (SQLException e) {
					System.out.println("从连接池" + name + "删除一个无效连接");
					// 递归调用自己,尝试再次获取可用连接
					con = getConnection();
				}
			} else if (maxConn == 0 || checkedOut < maxConn) {
				con = newConnection();
			}
			if (con != null) {
				checkedOut++;
			}
			return con;
		}

		/**
		 * 从连接池获取可用连接.可以指定客户程序能够等待的最长时间 参见前一个getConnection()方法.
		 * 
		 * @param timeout
		 *            以毫秒计的等待时间限制
		 */
		public synchronized Connection getConnection(long timeout) {
			long startTime = new Date().getTime();
			Connection con;
			while ((con = getConnection()) == null) {
				try {
					wait(timeout);
				} catch (InterruptedException e) {
				}
				if ((new Date().getTime() - startTime) >= timeout) {
					// wait()返回的原因是超时
					return null;
				}
			}
			return con;
		}

		/**
		 * 关闭所有连接
		 */
		public synchronized void release() {
			Iterator<Connection> allConnections = freeConnections.iterator();
			while (allConnections.hasNext()) {
				Connection con = allConnections.next();
				try {
					con.close();
					// System.out.println("关闭连接池" + name + "中的一个连接");
				} catch (SQLException e) {
					System.err.println("无法关闭连接池" + name + "中的连接");
					e.printStackTrace();
				}
			}
			freeConnections.clear();
		}

		/**
		 * 创建新的连接
		 */
		public Connection newConnection() {
			Connection con = null;
			try {
				if (user == null) {
					con = DriverManager.getConnection(URL);
				} else {
					con = DriverManager.getConnection(URL, user, password);
				}
				// System.out.println("连接池" + name + "创建一个新的连接");
			} catch (SQLException e) {
				System.err.println("无法创建下列URL的连接: " + URL);
				e.printStackTrace();
				return null;
			}
			return con;
		}
	}
}