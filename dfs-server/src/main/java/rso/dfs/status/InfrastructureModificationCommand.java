package rso.dfs.status;

public class InfrastructureModificationCommand {
	public enum ModificationType {
		ADDSERVER, DEADSLAVE, DEADSHADOW
	}

	private String serverIP;
	private ModificationType mt;
	
	public InfrastructureModificationCommand(String serverIP, ModificationType mt) {
		this.serverIP = serverIP;
		this.mt = mt;
	}

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	public ModificationType getMt() {
		return mt;
	}

	public void setMt(ModificationType mt) {
		this.mt = mt;
	}
	
	
}
