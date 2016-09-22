package pl.wesolucky.shop.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Shop 
{
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

	private String domain;
	private boolean httpsEnabled = false;
	private boolean reverseIdInUrl = false;
	private String baseUrl;
	
	/**
	 * Collection used to read products from MySQL 
	 * and compare to saved in application data base
	 */
	@JsonIgnore
	@Transient
	private List<PrestaProduct> mySqlProductsList;
	
	// ::: MySQL parameters :::

	private String host;
	private int port;
	private String user;
	private String password;
	private String dbName;
	
	
	@Override
	public String toString()
	{
		String log = "";
		log += "id: " + this.id;
		log += " | domain: " + this.domain;
		return log;
	}
	
	
	// ::: GET / SET :::
	
	public List<PrestaProduct> getMySqlProductsList() {
		return mySqlProductsList;
	}

	public void setMySqlProductsList(List<PrestaProduct> mySqlProductsList) {
		this.mySqlProductsList = mySqlProductsList;
	}

	
	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public boolean isHttpsEnabled() {
		return httpsEnabled;
	}

	public void setHttpsEnabled(boolean httpsEnabled) {
		this.httpsEnabled = httpsEnabled;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}


	public boolean isReverseIdInUrl() {
		return reverseIdInUrl;
	}


	public void setReverseIdInUrl(boolean reverseIdInUrl) {
		this.reverseIdInUrl = reverseIdInUrl;
	}

	
	
}
