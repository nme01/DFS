package rso.dfs.commons;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
	private Long replicationFactor;
	private Long pingEvery;
	private Integer isFileUsedTimeout;
	private Integer deleteCounter;
	private List<String> servers;
	private int defaultClientTimeout;
	private boolean debug;
	private String directory;
	private Integer debugWaitTime;
	private Integer shadowCount;
	
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
	
	final static Logger log = LoggerFactory.getLogger(DFSProperties.class);
	
	private static void load(){
		Properties properties = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("./../config.properties");
			properties.load(input);
		} catch (IOException ex) {
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(input == null)
		{
			try {
				input = new FileInputStream("config.properties");
				properties.load(input);
			} catch (IOException ex) {
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		if (properties.isEmpty())
		{
			log.error("Critical error: configuration file not found");
			System.exit(-1);
		}
		
		dfsproperties.setIsFileUsedTimeout(Integer.parseInt(properties.getProperty("isFileUsedTimeout")));
		dfsproperties.setDeleteCounter(Integer.parseInt(properties.getProperty("deleteCounter")));
		dfsproperties.setDbname(properties.getProperty("database"));
		dfsproperties.setDbpassword(properties.getProperty("dbpassword"));
		dfsproperties.setDbport(Integer.parseInt(properties.getProperty("dbport")));
		dfsproperties.setDbuser(properties.getProperty("dbuser"));
		dfsproperties.setFilePartSize(Long.parseLong(properties.getProperty("file_part_size")));
		dfsproperties.setNamingServerMemory(Long.parseLong(properties.getProperty("naming_server_memory")));
		dfsproperties.setNamingServerPort(Integer.parseInt(properties.getProperty("naming_server_port")));
		dfsproperties.setStorageServerMemory(Long.parseLong(properties.getProperty("storage_server_memory")));
		dfsproperties.setReplicationFactor(Long.parseLong(properties.getProperty("replicationFactor")));
		dfsproperties.setPingEvery(Long.parseLong(properties.getProperty("pingEvery")));
		dfsproperties.setStorageServerPort(Integer.parseInt(properties.getProperty("storage_server_port")));
		dfsproperties.setDefaultClientTimeout(Integer.parseInt(properties.getProperty("defaultClientTimeout")));
		dfsproperties.setDebug(Boolean.parseBoolean(properties.getProperty("debug")));
		dfsproperties.setDirectory(properties.getProperty("directory"));
		dfsproperties.setDebugWaitTime(Integer.parseInt(properties.getProperty("debugWaitTime")));
		dfsproperties.setShadowCount(Integer.parseInt(properties.getProperty("shadows")));
		
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

	public Integer getDeleteCounter() {
		return deleteCounter;
	}

	public void setDeleteCounter(Integer deleteCounter) {
		this.deleteCounter = deleteCounter;
	}

	public Integer getIsFileUsedTimeout() {
		return isFileUsedTimeout;
	}

	public void setIsFileUsedTimeout(Integer isFileUsedTimeout) {
		this.isFileUsedTimeout = isFileUsedTimeout;
	}

	public int getDefaultClientTimeout() {
		return defaultClientTimeout;
	}

	public void setDefaultClientTimeout(int defaultClientTimeout) {
		this.defaultClientTimeout = defaultClientTimeout;
	}

	public Long getReplicationFactor() {
		return replicationFactor;
	}

	public void setReplicationFactor(Long replicationFactor) {
		this.replicationFactor = replicationFactor;
	}

	public Long getPingEvery() {
		return pingEvery;
	}

	public void setPingEvery(Long pingEvery) {
		this.pingEvery = pingEvery;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}
	
	public void setDebugWaitTime(Integer debugWaitTime) {
		this.debugWaitTime=debugWaitTime;
	}
	
	public Integer getDebugWaitTime() {
		return debugWaitTime;
	}

	public Integer getShadowCount() {
		return shadowCount;
	}

	public void setShadowCount(Integer shadowCount) {
		this.shadowCount = shadowCount;
	}

}
