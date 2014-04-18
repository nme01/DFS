package rso.dfs.dbManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import rso.dfs.FileStatus;
import rso.dfs.ServerRole;

/**
 * @author Mateusz Statkiewicz
 * 
 */
public class DbManager {

	private static final String db_driver = "org.postgresql.Driver";
	private static final String db_connection = "jdbc:postgresql://";

	private String db_name;
	private Integer db_port;
	private String db_user;
	private String db_password;

	private Connection local_con;
	private ArrayList<Connection> shadows_con;

	public DbManager() {
		db_name = "dfs_db";
		db_port = 5432;
		db_user = "dfs_user";
		db_password = "dfs_pass";
	}

	/**
	 * @param r
	 *            - server role
	 * @return collection of addresses with ip numbers of servers
	 */
	public Collection<InetAddress> getServersByRole(ServerRole r) {
		ArrayList<InetAddress> addrs = null;
		final String selectServersSQL = "SELECT ip FROM servers WHERE role = ?;";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = getLocalConnection().prepareStatement(selectServersSQL);
			stmt.setString(1, String.valueOf(r.getRoleChar()));
			rs = stmt.executeQuery();
			if (rs != null)
				addrs = new ArrayList<>();
			while (rs.next()) {
				addrs.add(InetAddress.getByName(rs.getString(1)));
			}

		} catch (SQLException | UnknownHostException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
			} catch (SQLException se) {
			}
		}
		return addrs;
	}

	/**
	 * @param fileId
	 * @return
	 */
	public Collection<InetAddress> getServersByFile(int fileId) {

		ArrayList<InetAddress> addrs = null;
		final String selectServersSQL = "SELECT ip FROM files_on_servers WHERE id = ? ORDER BY priority;";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = getLocalConnection().prepareStatement(selectServersSQL);
			stmt.setInt(1, fileId);
			rs = stmt.executeQuery();
			if (rs != null)
				addrs = new ArrayList<>();
			while (rs.next()) {
				addrs.add(InetAddress.getByName(rs.getString(1)));
			}

		} catch (SQLException | UnknownHostException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
			} catch (SQLException se) {
			}
		}
		return addrs;
	}

	/**
	 * @param fileName
	 * @return
	 */
	public int getFileId(String fileName) {
		int fileId = -1;
		final String selectServersSQL = "SELECT id FROM files WHERE name = ?;";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = getLocalConnection().prepareStatement(selectServersSQL);
			stmt.setString(1, fileName);
			rs = stmt.executeQuery();
			if (rs.next()) {
				fileId = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
			} catch (SQLException se) {
			}
		}
		return fileId;
	}

	/**
	 * @param name
	 * @param size
	 * @return
	 */
	public int addNewFile(String name, int size) {
		final String selectServersSQL = "INSERT INTO files VALUES ( nextval(file_seq),?,?,?)";
		PreparedStatement stmt = null;
		int rowsInserted;
		int fileId = -1;
		try {
			stmt = getLocalConnection().prepareStatement(selectServersSQL);
			stmt.setString(1, name);
			stmt.setInt(2, size);
			stmt.setString(2, String.valueOf(FileStatus.Upload.getStatusChar()));
			rowsInserted = stmt.executeUpdate();

			if (rowsInserted == 1)
				fileId = getFileId(name);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
			}
		}
		for (Connection con : getShadowsConnections()) {
			try {
				stmt = con.prepareStatement(selectServersSQL);
				stmt.setString(1, name);
				stmt.setInt(2, size);
				stmt.setInt(3, FileStatus.Upload.getStatusChar());
				rowsInserted = stmt.executeUpdate();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException e) {
				}
			}
		}
		return fileId;
	}

	/**
	 * @param fileId
	 * @return
	 */
	public int deleteFile(int fileId) {
		// TODO
		return 0;
	}

	/**
	 * @param fileId
	 * @param newStatus
	 * @return
	 */
	public int updateFileStatus(int fileId, FileStatus newStatus) {

		final String selectServersSQL = "UPDATE SET status = ? WHERE id = ?";
		PreparedStatement stmt = null;
		int rowsUpdated = 0;
		try {
			stmt = getLocalConnection().prepareStatement(selectServersSQL);
			stmt.setInt(1, newStatus.getStatusChar());
			stmt.setInt(2, fileId);
			rowsUpdated = stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
			}
		}
		for (Connection con : getShadowsConnections()) {
			try {
				stmt = con.prepareStatement(selectServersSQL);
				stmt.setInt(1, newStatus.getStatusChar());
				stmt.setInt(2, fileId);
				rowsUpdated += stmt.executeUpdate();

			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException e) {
				}
			}
		}
		return rowsUpdated;
	}

	/**
	 * @param ip
	 * @param memory
	 * @param role
	 * @return
	 */
	public int addNewServer(InetAddress ip, int memory, ServerRole role) {
		final String selectServersSQL = "INSERT INTO servers VALUES (?,?,?,systimestamp)";
		PreparedStatement stmt = null;
		int rowsInserted = 0;
		try {
			stmt = getLocalConnection().prepareStatement(selectServersSQL);
			stmt.setString(1, ip.toString());
			stmt.setInt(2, role.getRoleChar());
			stmt.setInt(3, memory);
			rowsInserted = stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rowsInserted;
	}

	public int addFileServerLink(InetAddress ip, int id) {
		// TODO
		return 0;
	};

	/**
	 * @param ip
	 * @param id
	 * @return
	 */
	public int deleteFileServerLink(InetAddress ip, int id) {

		// TODO
		return 0;
	};

	/**
	 * @return
	 */
	private Connection getLocalConnection() {

		if (local_con == null) {
			try {
				local_con = getConnection(InetAddress.getLocalHost());
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
		}
		return local_con;
	}

	/**
	 * @return
	 */
	private ArrayList<Connection> getShadowsConnections() {

		if (shadows_con == null) {
			shadows_con = new ArrayList<>();
			for (InetAddress addr : getServersByRole(ServerRole.Shadow)) {
				shadows_con.add(getConnection(addr));
			}
		}
		return shadows_con;
	}

	/**
	 * @param host
	 * @return
	 */
	private Connection getConnection(InetAddress host) {

		Connection con = null;
		try {
			con = DriverManager.getConnection(db_connection + host + ':'
					+ db_port + '/' + db_name, db_user, db_password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return con;
	};
}
