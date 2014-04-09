package rso.dfs.nameServer;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * 
 * */

public class File {

	private String name;
	private int id;

	public File(String name, int id) {
		super();
		this.name = name;
		this.id = id;
	}

	@Override
	public String toString() {
		return "File [name=" + name + ", id=" + id + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
