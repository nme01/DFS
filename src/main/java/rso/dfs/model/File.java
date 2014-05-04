package rso.dfs.model;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class File {

	private Long id;

	private String name;

	private Long size;

	private FileStatus status;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public FileStatus getStatus() {
		return status;
	}

	public void setStatus(FileStatus status) {
		this.status = status;
	}

}
