package pl.wesolucky.shop.presta;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.wesolucky.shop.domain.Shop;


public class PrestaMySqlDAO 
{
	
	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;
	
	private String processStatus;
	private String errorMsg;
	
	private PrestaMySqlParser prestaMySqlParser;
	
	private Map<String, String[]> tablesMap;
	List<String> startupTableList;
	
	
	public PrestaMySqlDAO()
	{
		prestaMySqlParser = new PrestaMySqlParser();
		setupProductTablesMap();
	}
	

	public void readAllPrestaProductData(Shop shop) 
	{
		System.out.println("\n--- PrestaMySqlDAO.readAllPrestaProductData for: " + shop.getId() + " ...");
		
		errorMsg = "";
		long startTime = System.currentTimeMillis();
		
		
		connection = getConnection(shop);
		System.out.println("PrestaMySqlDAO.readAllPrestaProductData > connection: " + connection);
		statement = null;
		
		prestaMySqlParser.initCollections(shop);
		
		try {
			statement = connection.createStatement();

			for (String table : startupTableList)
			{
				System.out.println("::: table: " + table);
				String[] columnsArr = tablesMap.get(table);
				String sql = "SELECT " + String.join(",", columnsArr) + " FROM " + table;
				System.out.println("sql: " + sql);
				resultSet = statement.executeQuery(sql);
				prestaMySqlParser.parseResultSetForTable(resultSet, table, shop);
				resultSet.close();
			}
		} catch (SQLException e) 
		{
			errorMsg = e.getMessage();
			System.out.println("\n" + "PrestaMySqlDAO.readAllPrestaProductStartupData SQLException ");
			System.out.println(errorMsg);
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
	    
	    if (!errorMsg.equals("")) System.out.println("PrestaMySqlDAO.getConnection ERROR: " + errorMsg);
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
	
	private void setupProductTablesMap()
	{
		// map with table name as key, and column names array as value
		tablesMap = new HashMap<String, String[]>();
		String[] fieldsArr;
		
		// for startup data order has matter > first properties > main products > attribute products 
		// > availability > setup real products
		startupTableList = new ArrayList<>();
		
		fieldsArr = new String[] {"id_product", "name", "link_rewrite"};
		tablesMap.put("ps_product_lang", fieldsArr);
		startupTableList.add("ps_product_lang");
		
		fieldsArr = new String[] {"id_supplier", "name"};
		tablesMap.put("ps_supplier", fieldsArr);
		startupTableList.add("ps_supplier");
		
		fieldsArr = new String[] {"id_tax", "rate"};
		tablesMap.put("ps_tax", fieldsArr);
		startupTableList.add("ps_tax");
		
		fieldsArr = new String[] {"id_attribute", "id_attribute_group"};
		tablesMap.put("ps_attribute", fieldsArr);
		startupTableList.add("ps_attribute");
		
		fieldsArr = new String[] {"id_attribute", "name"};
		tablesMap.put("ps_attribute_lang", fieldsArr);
		startupTableList.add("ps_attribute_lang");
		
		fieldsArr = new String[] {"id_product_attribute", "id_attribute"};
		tablesMap.put("ps_product_attribute_combination", fieldsArr);
		startupTableList.add("ps_product_attribute_combination");
		
		fieldsArr = new String[] {"id_attribute_group", "url_name"};
		tablesMap.put("ps_layered_indexable_attribute_group_lang_value", fieldsArr);
		startupTableList.add("ps_layered_indexable_attribute_group_lang_value");
		
		
		fieldsArr = new String[] {"id_image", "id_product_attribute"};
		tablesMap.put("ps_product_attribute_image", fieldsArr);
		startupTableList.add("ps_product_attribute_image");
		
		fieldsArr = new String[] {"id_image", "id_product"};
		tablesMap.put("ps_image", fieldsArr);
		startupTableList.add("ps_image");
		
		
		fieldsArr = new String[] {"id_product", "reference", "id_category_default", "id_supplier", "id_tax_rules_group", 
				"visibility", "wholesale_price", "price", "ean13", "weight", "date_upd", "date_add"};
		tablesMap.put("ps_product", fieldsArr);
		startupTableList.add("ps_product");
		
		fieldsArr = new String[] {"id_product", "id_product_attribute", "reference", "wholesale_price", "price", 
				"ean13", "weight", "default_on"};
		tablesMap.put("ps_product_attribute", fieldsArr);
		startupTableList.add("ps_product_attribute");
		
		
		fieldsArr = new String[] {"id_product", "id_product_attribute", "quantity"};
		tablesMap.put("ps_stock_available", fieldsArr);
		startupTableList.add("ps_stock_available");
	}
	


	public String getProcessStatus() 
	{
		return processStatus;
	}

	public String getErrorMsg() 
	{
		return errorMsg;
	}
	

}