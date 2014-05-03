package rso.dfs.model;

/**
 * Enum describing server roles, with mapping to DB signs
 * 
 * @author Mateusz Statkiewicz
 * @author Adam Papros <adam.papros@gmail.com>
 * 
 * */

public enum ServerRole {

	MASTER("M"), SHADOW("H"), SLAVE("L");

	private String role;

	private ServerRole(String role) {
		this.role = role;
	}

	/**
	 * @return DB role sign
	 */
	public String getRoleChar() {
		return role;
	}

	/**
	 * @param r
	 *            + DB role sign
	 * @return corresponding ServerRole
	 */
	static public ServerRole getServerRole(String role) {
		for (ServerRole sr : ServerRole.values())
			if (role.equals(sr))
				return sr;
		return null;
	}
}
