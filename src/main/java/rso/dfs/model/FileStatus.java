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

	private final String code;

	private FileStatus(final String code) {
		this.code = code;
	}

	/**
	 * @return DB status code
	 */
	public String getCode() {
		return code;
	};

	/**
	 * @param code
	 * @return corresponding FileStatus
	 */
	static public FileStatus getFileStatus(String code) {
		for (FileStatus fs : FileStatus.values())
			if (code.equals(fs))
				return fs;
		return null;
	};
}
