package rso.dfs.commons;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class DFSProperties {	
	private String dbname;
	private String dbuser;
	private String dbpassword;
	private Integer dbport;
	private Integer namingServerPort;
	private Integer storageServerPort;
	private Long namingServerMemory;
	private Long storageServerMemory;
	private Long filePartSize;
	private List<String> servers;
	
	private static DFSProperties dfsproperties;
	
	private DFSProperties(){ 
		this.servers = new ArrayList();
	}
	
	public static DFSProperties getProperties(){
		if(dfsproperties == null){
			dfsproperties = new DFSProperties();
			load();
		}
		return dfsproperties;
	}
	
	private static void load(){
		Properties properties = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("./../config.properties");
			properties.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		dfsproperties.setDbname(properties.getProperty("database"));
		dfsproperties.setDbpassword(properties.getProperty("dbpassword"));
		dfsproperties.setDbport(Integer.parseInt(properties.getProperty("dbport")));
		dfsproperties.setDbuser(properties.getProperty("dbuser"));
		dfsproperties.setFilePartSize(Long.parseLong(properties.getProperty("file_part_size")));
		dfsproperties.setNamingServerMemory(Long.parseLong(properties.getProperty("naming_server_memory")));
		dfsproperties.setNamingServerPort(Integer.parseInt(properties.getProperty("naming_server_port")));
		dfsproperties.setStorageServerMemory(Long.parseLong(properties.getProperty("storage_server_memory")));
		dfsproperties.setStorageServerPort(Integer.parseInt(properties.getProperty("storage_server_port")));
		Enumeration<?> e = properties.propertyNames();
		while (e.hasMoreElements()) {
			String key = e.nextElement().toString();
			if(key.matches("server.*")){
				dfsproperties.getServers().add(properties.getProperty(key));
			}
		}
	}
	
	public String getDbname() {
		return dbname;
	}
	public void setDbname(String dbname) {
		this.dbname = dbname;
	}
	public String getDbuser() {
		return dbuser;
	}
	public void setDbuser(String dbuser) {
		this.dbuser = dbuser;
	}
	public String getDbpassword() {
		return dbpassword;
	}
	public void setDbpassword(String dbpassword) {
		this.dbpassword = dbpassword;
	}
	public Integer getDbport() {
		return dbport;
	}
	public void setDbport(Integer dbport) {
		this.dbport = dbport;
	}
	public Integer getNamingServerPort() {
		return namingServerPort;
	}
	public void setNamingServerPort(Integer namingServerPort) {
		this.namingServerPort = namingServerPort;
	}
	public Integer getStorageServerPort() {
		return storageServerPort;
	}
	public void setStorageServerPort(Integer storageServerPort) {
		this.storageServerPort = storageServerPort;
	}
	public Long getNamingServerMemory() {
		return namingServerMemory;
	}
	public void setNamingServerMemory(Long namingServerMemory) {
		this.namingServerMemory = namingServerMemory;
	}
	public Long getStorageServerMemory() {
		return storageServerMemory;
	}
	public void setStorageServerMemory(Long storageServerMemory) {
		this.storageServerMemory = storageServerMemory;
	}
	public Long getFilePartSize() {
		return filePartSize;
	}
	public void setFilePartSize(Long filePartSize) {
		this.filePartSize = filePartSize;
	}
	public List<String> getServers() {
		return servers;
	}
	public void setServers(List<String> servers) {
		this.servers = servers;
	}
	
}
