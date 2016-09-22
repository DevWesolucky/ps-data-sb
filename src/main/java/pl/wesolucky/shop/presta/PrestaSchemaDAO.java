package pl.wesolucky.shop.presta;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import pl.wesolucky.shop.domain.Shop;

public class PrestaSchemaDAO 
{
	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;
	
	private String errorMsg;
	
	
	public String checkTables(Shop shop)
	{
		System.out.println("\n--- PrestaSchemaDAO.showTables for: " + shop.getId() + " ...");
		
		errorMsg = "";
		long startTime = System.currentTimeMillis();
		
		connection = getConnection(shop);
		if (!errorMsg.equals("")) return errorMsg;
		
		statement = null;
		
		try {
			statement = connection.createStatement();

			String sql = "show tables";
			resultSet = statement.executeQuery(sql);
			checkResultSetMetaData(resultSet, "show table");
			
			int resultCounter = 0;
			while (resultSet.next())
			{
				resultCounter++;
				if (resultCounter > 5) break;
				System.out.println(" " + resultCounter + "\t" + resultSet.getString(1) );
			}
			resultSet.close();
		} catch (SQLException e) 
		{
			errorMsg = e.getMessage();
			System.out.println("\n  SQLException " + errorMsg);
		}
		
		closeConnection();
		
		double processTime = (System.currentTimeMillis() - startTime) * 0.001;
		System.out.println("TOTAL read and parse time: " + processTime);
		
		return errorMsg;
	}
	
	
	private void checkResultSetMetaData(ResultSet resultSet, String request)
	{
		System.out.println("*** checkResultSetMetaData: " + request);
		
		try {
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) 
			{
				System.out.println("--- " + i);
				String key = resultSetMetaData.getColumnName(i);
				System.out.println("key: " + key);
				System.out.println("type: " + resultSetMetaData.getColumnType(i));
//				String value = resultSet.getString(1);
//				System.out.println("value: " + value);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void checkTable(Shop shop)
	{
		System.out.println("\n--- PrestaSchemaDAO.checkTable for: " + shop.getId() + " ...");
		
		errorMsg = "";
		long startTime = System.currentTimeMillis();
		
		connection = getConnection(shop);
		statement = null;
		
		try {
			statement = connection.createStatement();

			String sql = "show tables";
			resultSet = statement.executeQuery(sql);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int resultCounter = 0;
			while (resultSet.next())
			{
				resultCounter++;
//				if (resultCounter > 5) break;
				System.out.println("resultCounter " + resultCounter );
				for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) 
				{
					System.out.println("--- " + i);
					String key = resultSetMetaData.getColumnName(i);
					System.out.println("key: " + key);
					System.out.println("type: " + resultSetMetaData.getColumnType(i));
//					String value = resultSet.getString(1);
//					System.out.println("value: " + value);
				}
					
			}
			

			resultSet.close();
		} catch (SQLException e) 
		{
			errorMsg = e.getMessage();
			System.out.println("\n  SQLException " + errorMsg);
		}
		
		closeConnection();
		
		double processTime = (System.currentTimeMillis() - startTime) * 0.001;
		System.out.println("TOTAL read and parse time: " + processTime);
	}
	
	
	// ::: CONNECTION :::
	
	private Connection getConnection(Shop shop)
	{
		String dbUrl = "jdbc:mysql://" + shop.getHost() + ":" + shop.getPort() + "/" + shop.getDbName();
		System.out.println("PrestaMySqlDAO.getConnection for database: " + dbUrl + "...");
		double startProcessTime = System.currentTimeMillis();
		
	    connection = null;
	    
	    try {
	    	Class.forName("com.mysql.jdbc.Driver");
	    	connection = DriverManager.getConnection(dbUrl, shop.getUser(), shop.getPassword());
		} catch (SQLException | ClassNotFoundException e) {
			errorMsg = "Can't connect. " + e.getMessage();
		} catch (Exception ex) {
			errorMsg = "Can't connect. " + ex.getMessage();
		} finally {
			try {
				if (!errorMsg.equals("") && connection != null) connection.close();
			} catch (SQLException closeException) {
				closeException.printStackTrace();
			}
		}
	    
	    if (!errorMsg.equals("")) System.out.println("PrestaSchemaDAO.getConnection ERROR: " + errorMsg);
	    double processTime = (System.currentTimeMillis() - startProcessTime) * 0.001;
		System.out.println("Connecting time: " + processTime);
		
	    return connection;
	}
	
	
	private void closeConnection()
	{
		try {
			if (resultSet != null) resultSet.close();
			if (statement != null) statement.close();
			if (connection != null) connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
}
