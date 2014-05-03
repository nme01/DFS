package rso.dfs;

/**
 * @author Mateusz Statkiewicz
 * 
 * Enum describing server roles, with mapping to DB signs
 * 
 * @deprecated @see rso.dfs.model package
 * */
@Deprecated
public enum ServerRole {
	
	Master('m'),
	Shadow('h'),
	Slave('l');
	
	private final char role; 
	
	/**
	 * @param role DB role sign
	 */
	ServerRole(char role) {
		this.role=role;
	}

	/**
	 * @return DB role sign
	 */
	public char getRoleChar() {
		return role;
	};
	
	/**
	 * @param r + DB role sign
	 * @return corresponding ServerRole
	 */
	static public ServerRole getServerRole(char r)
	{
		for (ServerRole sr : ServerRole.values())
			if (sr.getRoleChar() == r) return sr;
		return null;
	};
}
