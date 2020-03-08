package cn.edu.fudan.se.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {
	private static final String DBUsername = "root";
	private static final String DBUserPassword = "123456";
	private static final String connectionString = "jdbc:mysql://127.0.0.1/third_party_library?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false";

	//10.222.166.23
//	private static final String connectionString = "jdbc:mysql://10.141.221.73:3306/codehub?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false";
//	private static final String DBUsername = "root";
//	private static final String DBUserPassword = "root";
	
//	private static final String connectionString = "jdbc:mysql://192.168.1.105:3306/third_party_library?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false";
//	private static final String DBUsername = "root";
//	private static final String DBUserPassword = "123456";
	
	private static Connection connection;

	static {
		try {
			// class name for mysql driver
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static final Connection getConnection() {
		if (connection == null) {
			try {
				connection = DriverManager.getConnection(connectionString, DBUsername, DBUserPassword);
				//不自动提交
				connection.setAutoCommit(false);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return connection;
	}

	public static ResultSet query(String sql) {
		Statement statement;
		try {
			statement = getConnection().createStatement();
			ResultSet result = statement.executeQuery(sql);
			// while (result.next()) {
			// personResult.add(new Person(result.getString("name"), result
			// .getString("passwd")));
			// }
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static void update(String sql) {
		Statement statement;
		try {
			statement = getConnection().createStatement();
			int rs = statement.executeUpdate(sql);                      
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public class Test {
		public void test() {
			
		}
	}
	
	
}