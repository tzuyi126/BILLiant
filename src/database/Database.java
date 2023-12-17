package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import utils.Encryption;

public class Database {
	
	private static Connection con = null;
	
	private static Connection connect() throws Exception {
		if (con == null) {
			try {
				con = DriverManager.getConnection("jdbc:sqlite:javabook.db");
			} catch (Exception e) {
				System.err.println("error connecting to database");
				throw e;
			}
		}
		return con;
	}
	
	private static void disconnect() {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			con = null;
		}
	}

	public static void setUpDatabase() {
		try {
			con = connect();
			
			Statement statement = con.createStatement();
			
//			statement.executeUpdate("DROP TABLE IF EXISTS User");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS User (id string NOT NULL, username string PRIMARY KEY, password string NOT NULL, key string NOT NULL, friends string)");
//			statement.executeUpdate("DROP TABLE IF EXISTS Expense");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Expense (id string PRIMARY KEY, title string, amount double NOT NULL, time text, creditor string NOT NULL, debtor string NOT NULL, groupId string)");

		} catch (Exception e) {
			System.err.println("error setting up database");
			e.printStackTrace();
		} finally {
			disconnect();
		}
	}
	
	public static boolean verifyLogin(String username, String expectedPwd) {
		PreparedStatement statement;
		
		boolean verified = false;
		
		try {
			con = connect();
			String queryString = "SELECT password, key FROM User WHERE username = ?";
			statement = con.prepareStatement(queryString);
			statement.setString(1, username);
			ResultSet rs = statement.executeQuery();
			
			while (rs.next()) {
				String encryptedPwd = rs.getString("password");
				String keyStr = rs.getString("key");
				
				String actualPwd = Encryption.decrypt(Encryption.stringToKey(keyStr), encryptedPwd);
				
				if (actualPwd.equals(expectedPwd)) {
					verified = true;
					break;
				}
			}
			
		} catch (Exception e) {
			System.err.println("error verifying login");
			e.printStackTrace();
		} finally {
			disconnect();
		}
		return verified;
	}
	
	public static void addUser(User user) throws Exception {
		PreparedStatement statement;
		try {
			con = connect();
			
			String insertString = "INSERT INTO User VALUES (?, ?, ?, ?, ?)";
			statement = con.prepareStatement(insertString);
			statement.setString(1, user.getId());
			statement.setString(2, user.getUsername());
			statement.setString(3, user.getPassword());
			statement.setString(4, user.getKey());
			statement.setString(5, user.getFriendsStr());
			
			statement.execute();
		} catch (Exception e) {
			System.err.println("error adding user to database");
			throw e;
		} finally {
			disconnect();
		}
	}
	
	public static User getUser(String username) {
		PreparedStatement statement;
		
		try {
			con = connect();
			String queryString = "SELECT * FROM User WHERE username = ?";
			statement = con.prepareStatement(queryString);
			statement.setString(1, username);
			ResultSet rs = statement.executeQuery();
			
			while (rs.next()) {
				return new User(
						rs.getString("id"),
						rs.getString("username"),
						rs.getString("password"),
						rs.getString("key"),
						rs.getString("friends")
						);
			}
			
			throw new Exception("no user: " + username);
		} catch (Exception e) {
			System.err.println("error getting user");
			e.printStackTrace();
			
			return null;
		} finally {
			disconnect();
		}
	}
	
	public static void main(String[] args) {
		Database.setUpDatabase();
		
		try {
			User user = new User("hello", "helloworld");
			Database.addUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(Database.getUser("hello").toString());
		
		System.out.println(Database.verifyLogin("hello", "helloworld"));
		System.out.println(Database.verifyLogin("ok", "ok"));
		System.out.println(Database.verifyLogin("kkk", "kkk"));
	}
}
