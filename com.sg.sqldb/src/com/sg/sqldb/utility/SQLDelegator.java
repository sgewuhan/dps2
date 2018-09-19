package com.sg.sqldb.utility;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.sg.sqldb.DDB;

public abstract class SQLDelegator {

	
	public final SQLResult execute(String dsn) throws Exception{
		Connection conn = DDB.getDefault().getConnection(dsn);
		Statement stat = null;
		SQLResult result = new SQLResult();
		result.setDataSource(dsn);
		boolean autoCommit = false;
		try {
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			stat = conn.createStatement();
			
			result = run(stat);
			conn.commit();
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				throw e1;
			}
			throw e;
		} finally {
			try {
				conn.setAutoCommit(autoCommit);
				if (stat != null)
					stat.close();
			} catch (Exception e) {
				throw e;
			}
			DDB.getDefault().freeConnection(dsn, conn);
		}
		return result;
	}

	protected abstract SQLResult run( Statement stat) throws Exception;
}
