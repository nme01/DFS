package rso.dfs;

/**
 * @author Mateusz Statkiewicz
 * 
 * Enum describing file statuses, with mapping to DB signs
 * */

public enum FileStatus {
	
	Upload('u'),
	ToDelete('d'),
	Held('h');
	
	private final char status; 
	
	/**
	 * @param DB status sign
	 */
	FileStatus(char status) {
		this.status=status;
	}

	/**
	 * @return DB status sign 
	 */
	public char getStatusChar() {
		return status;
	};
	
	/**
	 * @param s - DB status sign
	 * @return corrsponding FileStatus
	 */
	static public FileStatus getFileStatus(char s)
	{
		for (FileStatus fs : FileStatus.values())
			if (fs.getStatusChar() == s) return fs;
		return null;
	};
}
