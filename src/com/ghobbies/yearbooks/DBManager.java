/**
 * 
 */
package com.ghobbies.yearbooks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author ZhuangXulin
 *
 */
public class DBManager {

	/**
	 * 
	 */
	public static void getConnect() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("加载驱动失败");
			e.printStackTrace();
		}
		try {
			YearbooksManager.conn = DriverManager.getConnection("jdbc:mysql://db.ghobbies.com:3306/yearbooks_development", "yearbooks", "ZhuangXulin2003YearBooks");
			YearbooksManager.stmt = YearbooksManager.conn.createStatement();
			System.out.println("数据库连接成功");
		} catch (SQLException e) {
			System.out.println("数据库连接失败");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param stmt
	 * @param conn
	 */
	public static void closeConnection(Statement stmt, Connection conn) {
		try {
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			System.out.println("关闭数据库失败");
			e.printStackTrace();
		}

	}
}
