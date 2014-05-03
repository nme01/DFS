package rso.dfs.dummy.server;

import java.util.ArrayList;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * 
 * */

public class Data {

	private int id;
	private ArrayList<Byte> bytes;

	public Data(int id, ArrayList<Byte> bytes) {
		super();
		this.id = id;
		this.bytes = bytes;
	}

	@Override
	public String toString() {
		return "Data [id=" + id + ", bytes=" + bytes + "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<Byte> getBytes() {
		return bytes;
	}

	public void setBytes(ArrayList<Byte> bytes) {
		this.bytes = bytes;
	}

}
