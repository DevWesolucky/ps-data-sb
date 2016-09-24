package pl.wesolucky.shop.presta;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.wesolucky.shop.domain.Shop;


public class PrestaMySqlDAO 
{
	
	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;
	
	private String errorMsg;
	private final PrestaMySqlParser prestaMySqlParser = new PrestaMySqlParser();
	private Map<String, String[]> tablesMap;
	
	private final Logger log = LoggerFactory.getLogger(PrestaMySqlDAO.class);
	
	public PrestaMySqlDAO()
	{
		setupProductTablesMap(); // setup custom tables and fields
	}
	

	/**
	 * Reads PrestaShop product data from custom tables and fields.
	 * 
	 * @param shop The shop to get data from
	 */
	public void readAllPrestaProductData(Shop shop) 
	{
		log.debug("\n--- PrestaMySqlDAO.readAllPrestaProductData for: " + shop.getId() + " ...");
		
		errorMsg = "";
		long startTime = System.currentTimeMillis();
		
		connection = getConnection(shop);
		log.debug("PrestaMySqlDAO.readAllPrestaProductData > connection: " + connection);
		statement = null;
		
		prestaMySqlParser.resetCollections(shop);
		
		try {
			statement = connection.createStatement();
			
			// iterate through all custom tables and gets data from custom columns
			for (String table : tablesMap.keySet())
			{
				log.debug("::: table: " + table);
				String[] columnsArr = tablesMap.get(table);
				String sql = "SELECT " + String.join(",", columnsArr) + " FROM " + table;
				log.debug("sql: " + sql);
				resultSet = statement.executeQuery(sql);
				// parse results for current table
				prestaMySqlParser.parseResultSetForTable(resultSet, table, shop);
				resultSet.close();
			}
		} catch (SQLException e) 
		{
			errorMsg = e.getMessage();
			log.debug("\n" + "PrestaMySqlDAO.readAllPrestaProductStartupData SQLException ");
			log.debug(errorMsg);
		}
		
		closeConnection();
		
		double processTime = (System.currentTimeMillis() - startTime) * 0.001;
		log.debug("TOTAL read and parse time: " + processTime);
		
	}
	
	/**
	 * Checks connection by connect to data base and gets list of tables
	 * 
	 * @param shop The shop to check connections
	 * @return errorMsg Empty string if success or error message
	 */
	public String checkTables(Shop shop)
	{
		log.debug("\n--- PrestaMySqlDAO.showTables for: " + shop.getId() + " ...");
		
		errorMsg = "";
		long startTime = System.currentTimeMillis();
		
		connection = getConnection(shop);
		if (!errorMsg.equals("")) return errorMsg;
		
		statement = null;
		
		try {
			statement = connection.createStatement();

			String sql = "show tables";
			resultSet = statement.executeQuery(sql);
			
			List <String> tablesList = new ArrayList<>();
			while (resultSet.next())
			{
				tablesList.add(resultSet.getString(1));
			}
			log.debug("tablesList.size(): " + tablesList.size());
			resultSet.close();
			// wrong data base > TODO get more specific data e.g. PrestaShop version
			if (!tablesList.contains("ps_product")) errorMsg = "Can't find ps_product table.";
		} catch (SQLException e) 
		{
			errorMsg = e.getMessage();
			log.debug("\n  SQLException " + errorMsg);
		}
		
		closeConnection();
		
		double processTime = (System.currentTimeMillis() - startTime) * 0.001;
		log.debug("TOTAL read and parse time: " + processTime);
		
		return errorMsg;
	}
	
	
	// ::: CONNECTION :::
	
	private Connection getConnection(Shop shop)
	{
		String dbUrl = "jdbc:mysql://" + shop.getHost() + ":" + shop.getPort() + "/" + shop.getDbName();
		log.debug("PrestaMySqlDAO.getConnection for database: " + dbUrl + "...");
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
	    
	    if (!errorMsg.equals("")) log.debug("PrestaMySqlDAO.getConnection ERROR: " + errorMsg);
	    double processTime = (System.currentTimeMillis() - startProcessTime) * 0.001;
		log.debug("Connecting time: " + processTime);
		
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
		// data order has matter for parse process > first properties > main products 
		// > attribute products > availability > setup real products
		tablesMap = new LinkedHashMap<String, String[]>();
		String[] fieldsArr;
		
		
		fieldsArr = new String[] {"id_product", "name", "link_rewrite"};
		tablesMap.put("ps_product_lang", fieldsArr);
		
		fieldsArr = new String[] {"id_supplier", "name"};
		tablesMap.put("ps_supplier", fieldsArr);
		
		fieldsArr = new String[] {"id_tax", "rate"};
		tablesMap.put("ps_tax", fieldsArr);
		
		fieldsArr = new String[] {"id_attribute", "id_attribute_group"};
		tablesMap.put("ps_attribute", fieldsArr);
		
		fieldsArr = new String[] {"id_attribute", "name"};
		tablesMap.put("ps_attribute_lang", fieldsArr);
		
		fieldsArr = new String[] {"id_product_attribute", "id_attribute"};
		tablesMap.put("ps_product_attribute_combination", fieldsArr);
		
		fieldsArr = new String[] {"id_attribute_group", "url_name"};
		tablesMap.put("ps_layered_indexable_attribute_group_lang_value", fieldsArr);
		
		fieldsArr = new String[] {"id_image", "id_product_attribute"};
		tablesMap.put("ps_product_attribute_image", fieldsArr);
		
		fieldsArr = new String[] {"id_image", "id_product"};
		tablesMap.put("ps_image", fieldsArr);
		
		fieldsArr = new String[] {"id_product", "reference", "id_category_default", "id_supplier", "id_tax_rules_group", 
				"visibility", "wholesale_price", "price", "ean13", "weight", "date_upd", "date_add"};
		tablesMap.put("ps_product", fieldsArr);
		
		fieldsArr = new String[] {"id_product", "id_product_attribute", "reference", "wholesale_price", "price", 
				"ean13", "weight", "default_on"};
		tablesMap.put("ps_product_attribute", fieldsArr);
		
		fieldsArr = new String[] {"id_product", "id_product_attribute", "quantity"};
		tablesMap.put("ps_stock_available", fieldsArr);
	}
	

}