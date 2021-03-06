package com.framework.util;

import com.framework.config.MyFrameworkCfgContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DBPool {
	/**
	 * 数据库连接池对象，用来维护一堆数据库连接
	 */
	private static List<Connection> connPool = new ArrayList<>();

	/**
	 * DBPool的单例
	 */
	private static DBPool poolInstance = null;

	/**
	 * 连接池的大小
	 */
	private static int poolSize = 0;


	private DBPool(int poolSize) {
		DBPool.poolSize = poolSize;
	}


	/**
	 * 这个poolsize不可以动态调整，在第一次初始化之后就定了
	 * @param poolSize
	 * @return
	 */
	public static DBPool getInstance(
			int poolSize,
			String dbUsername,
			String dbPassword,
			String url,
			String driverName
	) {
		if(poolInstance == null) {
			poolInstance = new DBPool(poolSize);
			poolInstance.initPool(dbUsername, dbPassword, url, driverName);
		}
		return poolInstance;
	}

	public static DBPool getInstance() {
		// 这里调整为从配置文件中获取 "com.mysql.jdbc.Driver"
		Integer poolSize = MyFrameworkCfgContext.get("framework.db.connPoolSize", Integer.class);
		String dbUsername = MyFrameworkCfgContext.get("framework.db.userName", String.class);
		String dbPassword = MyFrameworkCfgContext.get("framework.db.password", String.class);
		String url = MyFrameworkCfgContext.get("framework.db.url", String.class);
		String driverName = MyFrameworkCfgContext.get("framework.db.driverName", String.class);
		return getInstance(poolSize, dbUsername, dbPassword, url, driverName);
	}

	/**
	 * 初始化连接池
	 * @param dbUsername
	 * @param dbPassword
	 * @param url
	 * @param driverName
	 * @return
	 */
	private List<Connection> initPool(String dbUsername, String dbPassword, String url, String driverName){
		//根据给定的连接池大小初始化连接
		System.out.println("初始化连接池");
		try {
			Class.forName(driverName);
			for (int i = 0 ; i < DBPool.poolSize ; i ++) {
				//添加连接到连接池
				DBPool.connPool.add(
							DriverManager.getConnection(url, dbUsername, dbPassword)
						);
			}
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return connPool;
	}


	/**
	 * 从数据库连接池中获取一个连接
	 * @return
	 */
	public synchronized Connection getConnFromPool() {
		System.out.println("从数据库连接池中获取一个连接");
		Connection conn = null;
		try {
			while(DBPool.connPool.isEmpty()) {
				System.out.println("连接池中所有的连接都被使用完毕，等待其他进程还回连接");
				wait();
			}
			conn = DBPool.connPool.remove(0);
			System.out.println("取走连接，现在连接池的大小：" + connPool.size());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}


	/**
	 * 将连接对象还给连接池
	 */
	public synchronized void restoreConnToPool(Connection conn) {
		try {
			DBPool.connPool.add(conn);
			System.out.println("还回连接，现在连接池的大小：" + connPool.size());
			notifyAll();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
