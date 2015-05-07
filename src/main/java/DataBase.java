import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {
	
	static Connection connection;
	static PreparedStatement statement;
	
	public static void connect(String url, String username, String password){
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}catch(Exception e){
			e.printStackTrace();
		}
		try {
			connection = DriverManager.getConnection(url, username, password);
		} catch (SQLException ex) {
			printSQLError(ex);
		}
	}
	public static PreparedStatement getQuerry(String q){
		try {
			statement = connection.prepareStatement(q);
			return statement;
		} catch (SQLException e) {
			printSQLError(e);
			return null;
		}
	}
	public static PreparedStatement getUpdate(String q){
		try {
			statement = connection.prepareStatement(q);
			return statement;
		} catch (SQLException e) {
			printSQLError(e);
			return null;
		}
	}
	public static ResultSet sendQuery(PreparedStatement query){
		try {
			return query.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void sendUpdate(PreparedStatement update){
		try {
			update.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private static void printSQLError(SQLException ex){
		System.out.println("SQLException: " + ex.getMessage());
		System.out.println("SQLState: " + ex.getSQLState());
		System.out.println("VendorError: " + ex.getErrorCode());
	}
	public static void close(){
		try {
			statement.close();
			connection.close();
		} catch (SQLException e) {
			printSQLError(e);
		}
	}
}