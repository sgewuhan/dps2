package com.tmt.dps.processor.sms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Date;
import java.util.Iterator;

public class DBConnectionPool {
	private int checkedOut;
	private CopyOnWriteArrayList<Connection> freeConnections = new CopyOnWriteArrayList<Connection>();
	private int maxConn;
	private String name;
	private String password;
	private String URL;
	private String user;
	private String classforname = "net.sourceforge.jtds.jdbc.Driver";

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
	public DBConnectionPool(String name, String URL, String user,
			String password, int maxConn) {
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
	 * @throws Exception 
	 */
	public synchronized Connection getConnection() throws Exception {
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
			} catch (Exception e) {
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
	 * @throws Exception 
	 */
	public synchronized Connection getConnection(long timeout) throws Exception {
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
	 * @throws Exception 
	 */
	public synchronized void release() throws Exception {
		Iterator<Connection> allConnections = freeConnections.iterator();
		while (allConnections.hasNext()) {
			Connection con = allConnections.next();
			try {
				con.close();
				// System.out.println("关闭连接池" + name + "中的一个连接");
			} catch (Exception e) {
				throw new Exception("无法关闭连接池" + name + "中的连接", e);
			}
		}
		freeConnections.clear();
	}

	/**
	 * 创建新的连接
	 * @throws Exception 
	 */
	public Connection newConnection() throws Exception {
		Connection con = null;
		try {
			Class.forName(classforname);
			if (user == null) {
				con = DriverManager.getConnection(URL);
			} else {
				con = DriverManager.getConnection(URL, user, password);
			}
			// System.out.println("连接池" + name + "创建一个新的连接");
		} catch (Exception e) {
			throw new Exception("无法创建下列URL的连接: " + URL, e);
		}
		return con;
	}
}
