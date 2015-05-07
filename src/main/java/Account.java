import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Account {
	String name;
	String email;
	String sessid;
	Account(){
	}
	final static int R_EXISTS = 0;
	final static int R_ALREADY = 1;
	final static int R_P_LONG = 2;
	final static int R_P_SHORT = 3;
	final static int R_N_LONG = 4;
	final static int R_N_SHORT = 5;
	final static int R_E_LONG = 6;
	final static int R_E_SHORT = 7;
	final static int R_E_INVALID = 8;
	final static int R_SUCCESS = 9;
	public int Register(String n, String e, String p, String s){
		if(isLoggedIn(s)){
			return R_ALREADY;
		}
		if(n.length() <= 4){
			return R_N_SHORT;
		}
		if(n.length() >= 255){
			return R_N_LONG;
		}
		if(p.length() <= 6){
			return R_P_SHORT;
		}
		if(p.length() >= 255){
			return R_P_LONG;
		}
		if(e.length() <= 0){
			return R_E_SHORT;
		}
		if(e.length() >= 255){
			return R_E_LONG;
		}
		if(!e.contains("@")){
			return R_E_INVALID;
		}
		PreparedStatement statement = DataBase.getQuerry("SELECT * FROM Users WHERE name=?");
		try {
			statement.setString(1, n);
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		ResultSet set = DataBase.sendQuery(statement);
		try {
			if(set.next()){
				return R_EXISTS;
			}
		} catch (SQLException e1) {
		}
		String hashed = Hasher.Hash(p);
		
		statement = DataBase.getUpdate("INSERT INTO Users VALUES (?, ?, ?, ?);");
		
		try {
			statement.setString(1, n);
			statement.setString(2, e);
			statement.setString(3, hashed);
			statement.setString(4, s);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		DataBase.sendUpdate(statement);
		
		name = n;
		email = e;
		sessid = s;
		
		return R_SUCCESS;
	}
	boolean isLoggedIn(String s){
		PreparedStatement statement = DataBase.getQuerry("SELECT * FROM Users WHERE sessid=?");
		try {
			statement.setString(1, s);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		ResultSet set = DataBase.sendQuery(statement);
		try {
			if(set.next()){
				return true;
			}
			return false;
		} catch (SQLException e) {
			return false;
		}
	}
	final static int L_ALREADY = 10;
	final static int L_INVALID = 11;
	final static int L_SUCCESS = 12;
	public int Login(String n, String p, String s){
		if(isLoggedIn(s)){
			return L_ALREADY;
		}
		String password = new String();
		String email = new String();
		PreparedStatement statement = DataBase.getQuerry("SELECT * FROM Users WHERE name=?");
		try {
			statement.setString(1, n);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		ResultSet set = DataBase.sendQuery(statement);
		try {
			if(set.next()){
				password = set.getString("password");
				email = set.getString("email");
			}else{
				return L_INVALID;
			}
		} catch (SQLException e) {
			return L_INVALID;
		}
		String hashed = Hasher.Hash(p);
		System.out.println(hashed + ":" + password);
		if(!hashed.equals(password)){
			return L_INVALID;
		}
		statement = DataBase.getUpdate("UPDATE Users SET sessid=? WHERE name=?");
		try {
			statement.setString(1, s);
			statement.setString(2, n);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		DataBase.sendUpdate(statement);
		name = n;
		this.email = email;
		sessid = s;
		
		return L_SUCCESS;
	}
	final static int S_NOT = 13;
	final static int S_SUCCESS = 14;
	public int Session(String s){
		PreparedStatement statement = DataBase.getQuerry("SELECT * FROM Users WHERE sessid=?");
		try {
			statement.setString(1, s);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		ResultSet set = DataBase.sendQuery(statement);
		try {
			if(set.next()){
				name = set.getString("name");
				email = set.getString("email");
				sessid = s;
				return S_SUCCESS;
			}else{
				return S_NOT;
			}
		} catch (SQLException e) {
			return S_NOT;
		}
	}
	final static int O_NOT = 15;
	final static int O_SUCCESS = 16;
	public int logOut(){
		if(!isLoggedIn(sessid)){
			return O_NOT;
		}else{
			PreparedStatement statement = DataBase.getUpdate("UPDATE Users SET sessid='none' WHERE name=?");
			try {
				statement.setString(1, name);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			DataBase.sendUpdate(statement);
			return O_SUCCESS;
		}
	}
}
