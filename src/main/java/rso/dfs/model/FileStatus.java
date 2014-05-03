package rso.dfs.model;

/**
 * 
 * Enum describing file statuses, with mapping to DB signs
 * 
 * @author Mateusz Statkiewicz
 * @author Adam Papros <adam.papros@gmail.com>
 * 
 * */

public enum FileStatus {

	UPLOAD("U"), TO_DELETE("D"), HELD("H");

	private final String status;

	private FileStatus(final String status) {
		this.status = status;
	}

	/**
	 * @return DB status sign
	 */
	public String getStatusChar() {
		return status;
	};

	/**
	 * @param s
	 *            - DB status sign
	 * @return corresponding FileStatus
	 */
	static public FileStatus getFileStatus(String role) {
		for (FileStatus fs : FileStatus.values())
			if (role.equals(fs))
				return fs;
		return null;
	};
}
