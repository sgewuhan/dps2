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
	 * �����µ����ӳ�
	 * 
	 * @param name
	 *            ���ӳ�����
	 * @param URL
	 *            ���ݿ��JDBC URL
	 * @param user
	 *            ���ݿ��ʺ�,�� null
	 * @param password
	 *            ����,�� null
	 * @param maxConn
	 *            �����ӳ������������������
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
	 * ������ʹ�õ����ӷ��ظ����ӳ�
	 * 
	 * @param con
	 *            �ͻ������ͷŵ�����
	 */
	public synchronized void freeConnection(Connection con) {
		// ��ָ�����Ӽ��뵽����ĩβ
		if (con == null)
			return;
		freeConnections.add(con);
		checkedOut--;
		notifyAll();
	}

	/**
	 * �����ӳػ��һ����������.��û�п��е������ҵ�ǰ������С��������� ������,�򴴽�������.��ԭ���Ǽ�Ϊ���õ����Ӳ�����Ч,�������ɾ��֮,
	 * Ȼ��ݹ�����Լ��Գ����µĿ�������.
	 * @throws Exception 
	 */
	public synchronized Connection getConnection() throws Exception {
		Connection con = null;
		if (freeConnections.size() > 0) {
			// ��ȡ�����е�һ����������
			con = freeConnections.get(0);
			freeConnections.remove(0);
			try {
				if (con.isClosed()) {
					// System.out.println("�����ӳ�" + name + "ɾ��һ����Ч����");
					// �ݹ�����Լ�,�����ٴλ�ȡ��������
					con = getConnection();
				}
			} catch (Exception e) {
				System.out.println("�����ӳ�" + name + "ɾ��һ����Ч����");
				// �ݹ�����Լ�,�����ٴλ�ȡ��������
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
	 * �����ӳػ�ȡ��������.����ָ���ͻ������ܹ��ȴ����ʱ�� �μ�ǰһ��getConnection()����.
	 * 
	 * @param timeout
	 *            �Ժ���Ƶĵȴ�ʱ������
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
				// wait()���ص�ԭ���ǳ�ʱ
				return null;
			}
		}
		return con;
	}

	/**
	 * �ر���������
	 * @throws Exception 
	 */
	public synchronized void release() throws Exception {
		Iterator<Connection> allConnections = freeConnections.iterator();
		while (allConnections.hasNext()) {
			Connection con = allConnections.next();
			try {
				con.close();
				// System.out.println("�ر����ӳ�" + name + "�е�һ������");
			} catch (Exception e) {
				throw new Exception("�޷��ر����ӳ�" + name + "�е�����", e);
			}
		}
		freeConnections.clear();
	}

	/**
	 * �����µ�����
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
			// System.out.println("���ӳ�" + name + "����һ���µ�����");
		} catch (Exception e) {
			throw new Exception("�޷���������URL������: " + URL, e);
		}
		return con;
	}
}
