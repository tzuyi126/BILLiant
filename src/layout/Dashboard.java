package layout;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import database.Database;
import database.Expense;
import database.User;
import socket.Client;

public class Dashboard extends JFrame {
	
	private User user;
	
	private Client client;

	public Dashboard() {
		super("BILLiant");
		
		client = new Client();
		
		this.setSize(500, 600);
		this.setLocationRelativeTo(null);
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
		userMenu.setText(user.getUsername());
		userMenu.add(logout);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(userMenu);
		
		JTabbedPane panes = new JTabbedPane();
		JPanel dashboard = createUserDashboard();

		panes.addTab("Dashboard", dashboard);
		
		this.setJMenuBar(menuBar);
		this.add(panes, BorderLayout.CENTER);
		
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	public JPanel createUserDashboard() {
		JPanel panel = new JPanel();
		
		ExpenseTableModel tableModel = processExpenses();

		JTable table = new JTable();
		table.setModel(tableModel);
		
		JScrollPane sp = new JScrollPane(table);
		
		JButton addExpense = new JButton("Add an expense");
		addExpense.addActionListener((e) -> new ExpenseInterface(this).setVisible(true));

		JPanel controls = new JPanel();
		controls.add(addExpense);
		
		panel.add(sp, BorderLayout.CENTER);
		panel.add(addExpense, BorderLayout.PAGE_END);
		
		return panel;
	}
	
	public ExpenseTableModel processExpenses() {
		ArrayList<Expense> expenses = Database.getExpenses(user);
		String[] cols = {"Title", "Amount", "Time", "Creditor", "Debtor(s)", "Group"};
		ExpenseTableModel tableModel = new ExpenseTableModel();
		
		tableModel.setColumnIdentifiers(cols);
		Object[] row = new Object[6];
		for (Expense e : expenses) {
			tableModel.addRow(new Object[] {
					e.getTitle(), e.getAmount(), e.getTime(), e.getCreditorStr(), e.getDebtor()
			});
		}
		return tableModel;
	}
	
	class ExpenseTableModel extends DefaultTableModel {
		private String[] cols = {"Title", "Amount", "Time", "Creditor", "Debtor(s)", "Group"};

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
	
	public boolean verifyUser(String username, String password) throws Exception {
		try {
			String response = client.executeAndGetResponse("verify " + username + " " + password);
			
			return Boolean.parseBoolean(response);
		} catch (Exception e) {
			System.err.println("error verifying user with username: " + username);
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

	public void addExpense(Expense expense) throws Exception {
		try {
			String response = client.executeAndGetResponse("addexpense " + expense.toString());
			
			if (!response.equals("done")) throw new Exception(response);
			
		} catch (Exception e) {
			System.err.println("error adding an expense");
			throw e;
		}
	}
	
	public ArrayList<Expense> getExpenses(User user) throws Exception {
		try {
			String response = client.executeAndGetResponse("getexpenses " + user.getUsername());
			
		} catch (Exception e) {
			System.err.println("error getting expenses for " + user.getUsername());
			throw e;
		}
		// TODO: call client.executeAndGetResponse(user.getUsername());
		
		// TODO: parse response into ArrayList of Expense
		return new ArrayList<>();
	}

	public static void main(String[] args) {
		Dashboard dashboard = new Dashboard();
		dashboard.setVisible(true);
	}
}
