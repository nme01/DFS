package rso.dfs.model;

/**
 * Enum describing server roles, with mapping to DB signs
 * 
 * @author Mateusz Statkiewicz
 * @author Adam Papros <adam.papros@gmail.com>
 * 
 * */

public enum ServerRole {

	MASTER("M"), SHADOW("H"), SLAVE("L"), DOWN("D");

	private String code;

	private ServerRole(String role) {
		this.code = role;
	}

	/**
	 * @return DB role code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code
	 * @return corresponding ServerRole
	 */
	static public ServerRole getServerRole(String code) {
		for (ServerRole sr : ServerRole.values())
			if (code.equals(sr.getCode()))
				return sr;
		return null;
	}
}
