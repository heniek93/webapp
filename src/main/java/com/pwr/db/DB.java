package com.pwr.db;


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

public class DB {
	public static Connection conn = null;
	static public void connect() throws ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		
		conn = null;
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/skinp","skinp", "(k[n3&A&%[5Yc3,U");			
		} catch (SQLException e) {
			throw new IllegalStateException("Cannot connect the database!", e);
		}
	}
}
