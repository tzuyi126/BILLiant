package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

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
//			statement.executeUpdate("DROP TABLE IF EXISTS Expense");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS User (id string NOT NULL, username string PRIMARY KEY, password string NOT NULL, key string NOT NULL)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Expense (id string PRIMARY KEY, title string, amount double NOT NULL, time text, payer string NOT NULL, payee string NOT NULL, FOREIGN KEY(payer) references User(username), FOREIGN KEY(payee) references User(username))");
			
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
			
			String insertString = "INSERT INTO User VALUES (?, ?, ?, ?)";
			statement = con.prepareStatement(insertString);
			statement.setString(1, user.getId());
			statement.setString(2, user.getUsername());
			statement.setString(3, user.getPassword());
			statement.setString(4, user.getKey());
			
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
						rs.getString("key")
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
	
	public static Expense getExpense(String id) {
		PreparedStatement statement;
		
		try {
			con = connect();
			String queryString = "SELECT * FROM Expense WHERE id = ?";
			statement = con.prepareStatement(queryString);
			statement.setString(1, id);
			ResultSet rs = statement.executeQuery();
			
			while (rs.next()) {
				return new Expense(
						rs.getString("id"),
						rs.getString("title"),
						rs.getDouble("amount"),
						rs.getString("time"),
						rs.getString("payer"),
						rs.getString("payee")
						);
			}
			
			throw new Exception("no expense: " + id);
		} catch (Exception e) {
			System.err.println("error getting expense");
			e.printStackTrace();
			
			return null;
		} finally {
			disconnect();
		}
	}
	
	public static void addExpense(Expense expense) throws Exception {
		PreparedStatement statement;
		try {
			con = connect();
			
			String insertString = "INSERT INTO Expense VALUES (?, ?, ?, ?, ?, ?)";
			statement = con.prepareStatement(insertString);
			statement.setString(1, expense.getId());
			statement.setString(2, expense.getTitle());
			statement.setDouble(3, expense.getAmount());
			statement.setString(4, expense.getTime());
			statement.setString(5, expense.getPayer());
			statement.setString(6, expense.getPayee());
			
			statement.execute();
			
		} catch (Exception e) {
			System.err.println("error adding expense to database");
			throw e;
		} finally {
			disconnect();
		}
	}
	
	public static ArrayList<Expense> getExpenses(User user) {
		PreparedStatement statement;
		ArrayList<Expense> expenses = new ArrayList<Expense>();
		try {
			con = connect();
			
			String queryString = "SELECT * FROM Expense WHERE payer = ? OR payee = ?";
			statement = con.prepareStatement(queryString);
			statement.setString(1, user.getUsername());
			statement.setString(2, user.getUsername());
			ResultSet rs = statement.executeQuery();
			
			while (rs.next()) {
				expenses.add(new Expense(
						rs.getString("id"),
						rs.getString("title"),
						rs.getDouble("amount"),
						rs.getString("time"),
						rs.getString("payer"),
						rs.getString("payee")
						));
			}
			return expenses;
		} catch (Exception e) {
			System.err.println("error getting expenses for user " + user.getUsername());
			e.printStackTrace();
			return expenses;
		} finally {
			disconnect();
		}
	}
	
	public static void editExpense(String id, Expense expense) throws Exception {
		PreparedStatement statement;
		try {
			con = connect();
			
			String updateString = "UPDATE Expense SET title = ?, amount = ?, time = ?, "
					+ "payer = ?, payee = ? WHERE id = ?";
			statement = con.prepareStatement(updateString);
			statement.setString(1, expense.getTitle());
			statement.setDouble(2, expense.getAmount());
			statement.setString(3, expense.getTime());
			statement.setString(4, expense.getPayer());
			statement.setString(5, expense.getPayee());
			statement.setString(6, id);
			
			statement.execute();
			
		} catch (Exception e) {
			System.err.println("error editing expense" + e.getLocalizedMessage());
			throw e;
		} finally {
			disconnect();
		}
	}
	
	public static void deleteExpense(String id) {
		PreparedStatement statement;
		
		try {
			con = connect();
			String queryString = "DELETE FROM Expense WHERE id = ?";
			statement = con.prepareStatement(queryString);
			statement.setString(1, id);
			statement.execute();
			
			throw new Exception("no expense: " + id);
		} catch (Exception e) {
			System.err.println("error deleting expense");
			e.printStackTrace();
		} finally {
			disconnect();
		}
	}
	
	public static void main(String[] args) {
		Database.setUpDatabase();
		
		try {
			User user = new User("hello", "world");
			Database.addUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
