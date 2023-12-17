package layout;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import database.User;
import socket.Client;

public class Dashboard extends JFrame {
	
	private User user;
	
	private Client client;

	public Dashboard() {
		super("BILLiant");
		
		client = new Client();
		
		this.setSize(600, 500);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setLoginMenu();
	}
	
	private void setLoginMenu() {
		this.user = null;
		
		JMenuItem login = new JMenuItem("Log In");
		login.addActionListener((e) -> new Login(this).setVisible(true));
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(login);

		this.setJMenuBar(menuBar);
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	public void refreshFrame(User user) {
		this.user = user;
		
		JMenuItem logout = new JMenuItem("Log Out");
		logout.addActionListener((e) -> setLoginMenu());
		
		JMenu userMenu = new JMenu();
		userMenu.setText("Hi, " + user.getUsername());
		userMenu.add(logout);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(userMenu);
		
		this.setJMenuBar(menuBar);
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	public User getUser(String username) throws Exception {
		try {
			String response = client.executeAndGetResponse("getuser " + username);
			
			if (response.equals("no user")) return null;
			
			return new User(response);
		} catch (Exception e) {
			System.err.println("error finding user with username: " + username);
			throw e;
		}
	}
	
	public void addUser(User user) throws Exception {
		try {
			String response = client.executeAndGetResponse("adduser " + user.toString());
			
			if (!response.equals("done")) throw new Exception(response);
			
		} catch (Exception e) {
			System.err.println("error adding user to database");
			throw e;
		}
	}

	public static void main(String[] args) {
		Dashboard dashboard = new Dashboard();
		dashboard.setVisible(true);
	}
}
