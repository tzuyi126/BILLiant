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
			
			statement.executeUpdate("DROP TABLE IF EXISTS Expense");
			statement.executeUpdate("DROP TABLE IF EXISTS User");
			statement.executeUpdate("DROP TABLE IF EXISTS UserGroup");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS User (id string NOT NULL, username string PRIMARY KEY, password string NOT NULL, key string NOT NULL)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Expense (id string PRIMARY KEY, title string, amount double NOT NULL, time text, creditor string NOT NULL, debtor string NOT NULL, FOREIGN KEY(creditor) references User(username), FOREIGN KEY(debtor) references User(username)");
			
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
			statement.setString(5, expense.getCreditor());
			statement.setString(6, expense.getDebtor());
			
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
			
			String queryString = "SELECT * FROM Expense WHERE creditor = ?";
			statement = con.prepareStatement(queryString);
			statement.setString(1, user.getUsername());
			ResultSet rs = statement.executeQuery();
			
			while (rs.next()) {
				expenses.add(new Expense(
						rs.getString("id"),
						rs.getString("title"),
						rs.getDouble("amount"),
						rs.getString("time"),
						rs.getString("creditor"),
						rs.getString("debtor")
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
	
	public static void main(String[] args) {
		Database.setUpDatabase();
		
		try {
			
//			User alice = new User("Alice", "hialice");
//			alice.setFriendsStr("Bob,Ivy,Jack,Frank,Grace");
//			Database.addUser(alice);
//			Database.addUser(new User("Bob", "hibob"));
//			Database.addUser(new User("Ivy", "hiivy"));
//			Database.addUser(new User("Jack", "hijack"));
//			Database.addUser(new User("Frank", "hifrank"));
//			Database.addUser(new User("Grace", "higrace"));
//			
//			Database.addGroup(new Group("Roommates", "Alice,Ivy,Grace"));
//			Database.addGroup(new Group("Trip", "Alice,Bob,Ivy,Grace"));
//			Database.addGroup(new Group("Family", "Alice,Jack,Frank"));
//			
//			Database.addExpense(new Expense("Dinner", 21.5, "2023/12/12", "Alice", "Bob,Ivy,Grace", "Trip"));
//			Database.addExpense(new Expense("Rent", 1200, "2023/12/01", "Ivy", "Alice,Grace", "Roommates"));
//			Database.addExpense(new Expense("Movie Tickets", 9.8, "2023/12/10", "Bob", "Ivy,Alice", "Trip"));
//			Database.addExpense(new Expense("Groceries", 28.9, "2023/12/03", "Alice", "Grace", "Roommates"));
//			Database.addExpense(new Expense("Gas", 5.5, "2023/12/03", "Jack", "", ""));
//			Database.addExpense(new Expense("Parking", 3.49, "2023/12/09", "Alice", "Frank", "Family"));
//			Database.addExpense(new Expense("Baseball Tickets", 49.99, "2023/11/26", "Jack", "Alice", ""));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		System.out.println(expense);
		/*
		User user = Database.getUser("Alice");
		System.out.println(user.toString());
		System.out.println(Database.getExpenses(user, null).toString());
		System.out.println(Database.getGroups(user));
		
		System.out.println(Database.verifyLogin("Alice", "hialice"));
		System.out.println(Database.verifyLogin("Alice", "byealice"));
		System.out.println(Database.verifyLogin("Ivy", "hiivy"));
		System.out.println(Database.verifyLogin("Grace", "byegrace"));*/
	}
}
