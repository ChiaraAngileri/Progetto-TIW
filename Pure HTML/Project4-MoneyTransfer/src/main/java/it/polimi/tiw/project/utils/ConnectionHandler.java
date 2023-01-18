package it.polimi.tiw.project.utils;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;
import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

public class ConnectionHandler {
	
	private static final int POOL_SIZE = 10;
	private static BasicDataSource dataSource = null;
	
	private static DataSource getDataSource(ServletContext context) {
		if(dataSource == null) {
			//fetch connection data from web.xml
			String driver = context.getInitParameter("dbDriver");
    		String url = context.getInitParameter("dbUrl");
    		String user = context.getInitParameter("dbUser");
    		String password = context.getInitParameter("dbPassword");
    		
    		dataSource = new BasicDataSource();
    		dataSource.setDriverClassName(driver);
    		dataSource.setUrl(url);
    		dataSource.setUsername(user);
    		dataSource.setPassword(password);
    		
    		dataSource.setInitialSize(POOL_SIZE);
		}
		
		return dataSource;
	}	
	
	public static Connection getConnection(ServletContext context) throws UnavailableException {
		Connection connection = null;
		
		try {
    		DataSource dataSource = getDataSource(context);
    		connection = dataSource.getConnection();
    	}catch(SQLException e) {
    		throw new UnavailableException("Couldn't get db connection");
    	}
		
		return connection;
	}
	
	public static void closeConnection(Connection connection) throws SQLException {
		if (connection != null) {
			connection.close();
		}
	}

}
