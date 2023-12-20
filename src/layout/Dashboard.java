package layout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import database.Expense;
import database.User;
import socket.Client;
import utils.Calculator;

public class Dashboard extends JFrame {
	
	private User user;
	
	private Client client;
	
	private List<Expense> expenses = new ArrayList<>();
	
	JTable dashboardTable;
	JTable loanTable;
	JTable debtTable;
	
	JPanel display;
	JPanel controls;
	
	JTextArea summary;
	
	int toEdit = -1;
	String editId;

	public Dashboard() {
		super("BILLiant");
		
		client = new Client();
		
		this.setSize(500, 700);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setLoginMenu();

		this.setVisible(true);
	}
	
	private void setLoginMenu() {
		this.user = null;
		
		JMenuItem login = new JMenuItem("Log In");
		login.addActionListener((e) -> new Login(this).setVisible(true));
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(login);

		this.setJMenuBar(menuBar);
		
		if (display != null) {
			this.remove(display);
		}
		
		if (controls != null) {
			this.remove(controls);
		}

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
		
		JPanel dashboard = createUserDashboard();
		JPanel loan = createLoanDashboard();
		JPanel debt = createDebtDashboard();

		JTabbedPane panes = new JTabbedPane();
		panes.addTab("Dashboard", dashboard);
		panes.addTab("Loan", loan);
		panes.addTab("Debt", debt);
		
		JButton addExpense = new JButton("Add an expense");
		addExpense.addActionListener((e) -> new ExpenseInterface(this, user, "Add", null).setVisible(true));
		
		JButton editExpense = new JButton("Edit selected expense");
		editExpense.addActionListener((e) -> handleEdit());
		
		JButton refresh = new JButton("Refresh");
		refresh.addActionListener((e) -> refresh());

		summary = new JTextArea();
		summary.setEditable(false);
		summary.setPreferredSize(new Dimension(200, 100));
		JScrollPane sp = new JScrollPane(summary);
		
		display = new JPanel();
		display.add(panes);
		display.add(sp);
		
		controls = new JPanel();
		controls.add(addExpense);
		controls.add(editExpense);
		controls.add(refresh);
		
		this.setJMenuBar(menuBar);
		this.add(display, BorderLayout.CENTER);
		this.add(controls, BorderLayout.SOUTH);

		refresh();
		
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	private void handleEdit() {
		if (toEdit == -1) {
			JOptionPane.showMessageDialog(this, "No expense selected", "Oops!", JOptionPane.ERROR_MESSAGE);
		} else {
			Expense currExpense = null;
			try {
				currExpense = getExpense(editId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			new ExpenseInterface(this, user, "Edit", currExpense).setVisible(true);
		}
	}
	
	private JPanel createUserDashboard() {
		JPanel panel = new JPanel();

		dashboardTable = new JTable();
		dashboardTable.setModel(setupDashboard(expenses));
		
		JScrollPane sp = new JScrollPane(dashboardTable);
		
		panel.add(sp, BorderLayout.CENTER);
		
		return panel;
	}
	
	private JPanel createLoanDashboard() {
		JPanel panel = new JPanel();
		
		loanTable = new JTable();
		loanTable.setModel(setupDashboard(expenses.stream().filter(e -> e.getPayer().equals(user.getUsername())).collect(Collectors.toList())));
		
		JScrollPane sp = new JScrollPane(loanTable);
		
		panel.add(sp, BorderLayout.CENTER);
		
		return panel;
	}
	
	private JPanel createDebtDashboard() {
		JPanel panel = new JPanel();
		
		debtTable = new JTable();
		debtTable.setModel(setupDashboard(expenses.stream().filter(e -> e.getPayee().equals(user.getUsername())).collect(Collectors.toList())));
		
		JScrollPane sp = new JScrollPane(debtTable);
		
		panel.add(sp, BorderLayout.CENTER);
		
		return panel;
	}
	
	private ExpenseTableModel setupDashboard(List<Expense> expenses) {
		ExpenseTableModel tableModel = new ExpenseTableModel();
		
		if (user == null) return tableModel;
		
		try {
			for (Expense e : expenses) {
				tableModel.addRow(new Object[] {
						new Boolean(false), e.getTitle(), e.getAmount(), e.getTime(), e.getPayer(), e.getPayee(), e.getId()
				});
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Something went wrong when fetching data!", "Oops!", JOptionPane.ERROR_MESSAGE);
		}
		
		tableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				int row = e.getFirstRow();
				int column = e.getColumn();
				if (column == 0) {
					boolean value = (boolean) tableModel.getValueAt(row, column);
					if (value == true) {
						tableModel.setSelectedRow(row);
						toEdit = row;
						editId = tableModel.getSelectedRowId(row);
					} else {
						toEdit = -1;
					}
				}
			}
		});
		
		return tableModel;
	}
	
	private void summary() {
		StringBuilder loanStr = new StringBuilder();
		StringBuilder debtStr = new StringBuilder();

		if (!expenses.isEmpty()) {
			HashMap<String, Double> total = Calculator.computeTotal(user, expenses);
			
			for(Map.Entry<String, Double> entry : total.entrySet()) {
				String username = entry.getKey();
				Double sum = entry.getValue();
				
				if (sum.compareTo(0.0) > 0) {
					loanStr.append("$" + sum + " from " + username + "\n");
				} else if (sum.compareTo(0.0) < 0) {
					debtStr.append(username + " $" + Math.abs(sum) + "\n");
				}
			}
		}
		
		StringBuilder totalStr = new StringBuilder();
		
		if (loanStr.length() != 0) {
			totalStr.append("You get back:\n" + loanStr.toString() + "\n");
		} else {
			totalStr.append("You get back from no one.\n\n");
		}
		
		if (debtStr.length() != 0) {
			totalStr.append("You owe:\n" + debtStr.toString() + "\n");
		} else {
			totalStr.append("You owe no one.\n\n");
		}
		
		summary.setText(totalStr.toString());
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
			ArrayList<Expense> expenses = new ArrayList<>();
			
			String response = client.executeAndGetResponse("getexpenses " + user.getUsername());
			
			String[] strArr = response.split("\n");
			
			for (String str: strArr) {
				if (str.isEmpty()) continue;
				expenses.add(new Expense(str));
			}

			summary();
			return expenses;
		} catch (Exception e) {
			System.err.println("error getting expenses for " + user.getUsername());
			throw e;
		}
	}
	
	public Expense getExpense(String id) throws Exception {
		try {
			String response = client.executeAndGetResponse("getexpense " + id);
			
			if (response.equals("no expense")) return null;
			
			return new Expense(response);
		} catch (Exception e) {
			System.err.println("error finding expense with id: " + id);
			throw e;
		}
	}
	
	public void editExpense(String id, Expense expense) throws Exception {
		try {
			String response = client.executeAndGetResponse("editexpense " + id + " " + expense.toString());
			
			if (!response.equals("done")) throw new Exception(response);
			
		} catch (Exception e) {
			System.err.println("error editing an expense: " + e.getLocalizedMessage());
			throw e;
		}
	}
	
	public void deleteExpense(String id) throws Exception {
		try {
			String response = client.executeAndGetResponse("deleteexpense " + id);
			
			if (response.equals("no expense"));
		} catch (Exception e) {
			System.err.println("error finding expense with id to delete: " + id);
			throw e;
		}
	}
	
	private void refresh() {
		try {
			expenses = getExpenses(user);
			dashboardTable.setModel(setupDashboard(expenses));
			loanTable.setModel(setupDashboard(expenses.stream().filter(e -> e.getPayer().equals(user.getUsername())).collect(Collectors.toList())));
			debtTable.setModel(setupDashboard(expenses.stream().filter(e -> e.getPayee().equals(user.getUsername())).collect(Collectors.toList())));
			
			dashboardTable.getColumnModel().removeColumn(dashboardTable.getColumnModel().getColumn(6));
			loanTable.getColumnModel().removeColumn(loanTable.getColumnModel().getColumn(6));
			debtTable.getColumnModel().removeColumn(debtTable.getColumnModel().getColumn(6));
			summary();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Dashboard();
	}
}
