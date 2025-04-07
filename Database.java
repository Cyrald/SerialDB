import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Database {

	private Connection connection;
	private Statement statement;
	private static final String DATABASE_NAME = "DataStore";

	public Database() {
		String url = "jdbc:derby:" + DATABASE_NAME + ";create=true";
		connect(url);
	}

	public Database(String path) {
		String url = "jdbc:derby:" + path + ";create=true";
		connect(url);
	}

	public Database(String user, String password) {
		String url = "jdbc:derby:" + DATABASE_NAME + ";create=true;user=" + user + ";password=" + password;
		connect(url);
	}

	public Database(String path, String user, String password) {
		String url = "jdbc:derby:" + path + ";create=true;user=" + user + ";password=" + password;
		connect(url);
	}

	public boolean createTable(String tableName) {
		if (isTableExist(tableName)) {
			System.err.println("Table " + tableName + " already exists!");
			return false;
		}

		try {
			String query = "CREATE TABLE " + tableName
					+ " (nId INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY(Start with 0, Increment by 1), data BLOB)";
			statement.executeUpdate(query);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteTable(String tableName) {
		if (!isTableExist(tableName)) {
			System.err.println("Table " + tableName + " does not exist!");
			return false;
		}

		try {
			String query = "DROP TABLE " + tableName;
			statement.executeUpdate(query);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<String> listTables() {

		List<String> result = new ArrayList<>();

		try {
			DatabaseMetaData metaData = connection.getMetaData();
			String[] types = { "TABLE" };
			ResultSet resultSet = metaData.getTables(null, null, "%", types);

			while (resultSet.next()) {
				String tableName = resultSet.getString("TABLE_NAME");
				result.add(tableName);
			}

			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;

	}

	public boolean isTableExist(String tableName) {
		return listTables().contains(tableName.toUpperCase());
	}

	public boolean writeData(String tableName, byte[] bytes) {
		if (!isTableExist(tableName)) {
			System.err.println("Table " + tableName + " does not exist!");
			return false;
		}

		try {
			String query = "INSERT INTO " + tableName + " (data) VALUES (?)";
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setBytes(1, bytes);
			preparedStatement.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<byte[]> readData(String tableName) {
		if (!isTableExist(tableName)) {
			createTable(tableName);
			return Collections.emptyList();
		}

		List<byte[]> result = new ArrayList<>();

		try {
			String query = "SELECT * FROM " + tableName;
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				Blob blob = resultSet.getBlob("data");
				byte[] bytes = blob.getBytes(1, (int) blob.length());
				result.add(bytes);
			}

			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;

	}

	public boolean deleteDataByTableId(String tableName, int id) {
		try {
			String query = "DELETE FROM " + tableName + " WHERE nId = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, id);
			int rowsDeleted = preparedStatement.executeUpdate();
			return rowsDeleted > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteDataByIndex(String tableName, int index) {
		return deleteDataByTableId(tableName, getIdByIndex(tableName, index));
	}

	public boolean overwriteDataByTableId(String tableName, int id, byte[] bytes) {
		try {
			String query = "UPDATE " + tableName + " SET data = ? WHERE nId = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setBytes(1, bytes);
			preparedStatement.setInt(2, id);
			int rowsDeleted = preparedStatement.executeUpdate();
			return rowsDeleted > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean overwriteDataByIndex(String tableName, int index, byte[] bytes) {
		return overwriteDataByTableId(tableName, getIdByIndex(tableName, index), bytes);
	}

	public int getIdByIndex(String tableName, int index) {

		try {
			String query = "SELECT nId FROM " + tableName;
			ResultSet resultSet = statement.executeQuery(query);

			int i = 0;
			while (resultSet.next()) {
				if (i == index)
					return resultSet.getInt("nId");
				i++;

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;

	}
	
	
	public List<byte[]> readDataInRange(String tableName, int startIndex, int endIndex) {
		if (!isTableExist(tableName)) {
			System.err.println("Table " + tableName + " does not exist!");
			return Collections.emptyList();
		}

		if (startIndex < 0 || endIndex < startIndex) {
			System.err.println("Invalid index range: startIndex=" + startIndex + ", endIndex=" + endIndex);
			return Collections.emptyList();
		}

		List<byte[]> result = new ArrayList<>();

		try {
			String countQuery = "SELECT COUNT(*) as total FROM " + tableName;
			ResultSet countResult = statement.executeQuery(countQuery);
			int totalRows = 0;
			
			if (countResult.next()) {
				totalRows = countResult.getInt("total");
			}
			countResult.close();
			
			if (totalRows == 0 || startIndex >= totalRows) {
				return Collections.emptyList();
			}
			
			if (endIndex >= totalRows) {
				endIndex = totalRows - 1;
			}
			
			String query = "SELECT t.nId, t.data FROM " + tableName + " t " +
					"JOIN (SELECT nId, ROW_NUMBER() OVER() as rownum FROM " + tableName + ") as r " +
					"ON t.nId = r.nId " +
					"WHERE r.rownum BETWEEN ? AND ?";
					
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, startIndex + 1);
			preparedStatement.setInt(2, endIndex + 1);
			
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				Blob blob = resultSet.getBlob("data");
				byte[] bytes = blob.getBytes(1, (int) blob.length());
				result.add(bytes);
			}

			resultSet.close();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public int getTableSize(String tableName) {
	    if (!isTableExist(tableName)) {
	        System.err.println("Table " + tableName + " does not exist!");
	        return -1;
	    }
	    try {
	        String countQuery = "SELECT COUNT(*) as total FROM " + tableName;
	        ResultSet countResult = statement.executeQuery(countQuery);
	        int totalRows = -1;
	        
	        if (countResult.next()) {
	            totalRows = countResult.getInt("total");
	        }
	        countResult.close();
	        return totalRows;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return -1;
	    }
	}
	
	
	

	public Connection getConnection() {
		return connection;
	}

	public void close() {

		try {
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private boolean connect(String url) {
		try {
			connection = DriverManager.getConnection(url);
			statement = connection.createStatement();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

}
