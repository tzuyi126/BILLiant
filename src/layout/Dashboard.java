package layout;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
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
		
		this.setSize(600, 600);
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
		userMenu.setText("Hi, " + user.getUsername());
		userMenu.add(logout);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(userMenu);
		
		JPanel header = new JPanel();
		JLabel welcome = new JLabel("Welcome, " + user.getUsername() + "!", JLabel.CENTER);
		header.add(welcome);
		
		JTabbedPane panes = new JTabbedPane();
		JPanel dashboard = createUserDashboard();
		JPanel groups = createGroupDashBoard();
		panes.addTab("Dashboard", dashboard);
		panes.addTab("My Groups", groups);
		
		this.setJMenuBar(menuBar);
		this.add(header, BorderLayout.PAGE_START);
		SwingUtilities.updateComponentTreeUI(this);
		this.add(panes, BorderLayout.CENTER);
	}
	
	public JPanel createUserDashboard() {
		JPanel panel = new JPanel();
		ArrayList<Expense> expenses = Database.getExpenses(user);
		JTextArea ta = new JTextArea();
		JTable table = new JTable();
		ta.setText(expenses.toString());
		ExpenseTableModel tableModel = processExpenses();
		table.setModel(tableModel);
		JScrollPane sp = new JScrollPane(table);
		
		JPanel controls = new JPanel();
		JButton addExpense = new JButton("Add expense");
		addExpense.addActionListener(new AddExpense(this));
		controls.add(addExpense);
		
		panel.add(sp, BorderLayout.CENTER);
		panel.add(addExpense, BorderLayout.PAGE_END);
//		panel.setLayout();
		return panel;
	}
	
	class AddExpense extends JDialog implements ActionListener {
		private Dashboard dashboard;
		
		public AddExpense(Dashboard dashboard) {
			super(dashboard);
			this.dashboard = dashboard;
			
			JLabel titleLabel = new JLabel("Title: ");
			JLabel dateLabel = new JLabel("Date: ");
			JLabel amountLabel = new JLabel("Amount: ");
			JTextField title = new JTextField();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			JFormattedTextField date = new JFormattedTextField(dateFormat);
			NumberFormat amountFormat = NumberFormat.getNumberInstance();
			JFormattedTextField amount = new JFormattedTextField(amountFormat);
			JLabel groupLabel = new JLabel("Group:");
			
			JPanel addPanel = new JPanel();
			addPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 0;
			addPanel.add(titleLabel, c);
			c.gridx = 1;
			c.gridy = 0;
			addPanel.add(title, c);
			c.gridx = 0;
			c.gridy = 1;
			addPanel.add(amountLabel, c);
			c.gridx = 1;
			c.gridy = 1;
			addPanel.add(amount, c);
			c.gridx = 0;
			c.gridy = 2;
			addPanel.add(dateLabel, c);
			c.gridx = 1;
			c.gridy = 2;
			addPanel.add(date, c);
			c.gridx = 0;
			c.gridy = 3;
			addPanel.add(groupLabel, c);
			c.gridx = 0;
			c.gridy = 4;
			addPanel.add(switchAddType(), c);
			
			this.setSize(500, 400);
			this.add(addPanel, BorderLayout.CENTER);
			this.setLocationRelativeTo(dashboard);
		}
		
		private JPanel switchAddType() {
			JPanel panel = new JPanel();
			JPanel cards = new JPanel(new CardLayout());
			JPanel selectPane = new JPanel();
			String[] selectGroup = {"None", "Group"};
			
			JPanel noGroup = new JPanel();
			noGroup.add(new JTextField("recipient"));
			
			JPanel group = new JPanel();
			JCheckBox u1 = new JCheckBox("Friend 1");
			JCheckBox u2 = new JCheckBox("Friend 2");
			group.add(u1);
			group.add(u2);
			
			cards.add(noGroup, "None");
			cards.add(group, "Group");
			
			JComboBox cb = new JComboBox(selectGroup);
			cb.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					CardLayout c = (CardLayout)(cards.getLayout());
					c.show(cards, (String)e.getItem());
				}
			});
			selectPane.add(cb);
			
			panel.add(selectPane, BorderLayout.PAGE_START);
			panel.add(cards, BorderLayout.LINE_START);
			return panel;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			this.setVisible(true);
		}
		
	}
	
	public JPanel createGroupDashBoard() {
		JPanel panel = new JPanel();
		ArrayList<Expense> expenses = Database.getExpenses(user);
		JTextArea ta = new JTextArea();
		JTable table = new JTable();
		ta.setText(expenses.toString());
		ExpenseTableModel tableModel = processExpenses();
		table.setModel(tableModel);
		JScrollPane sp = new JScrollPane(table);
		panel.add(sp);
//		panel.setLayout();
		return panel;
	}
	
	public JPanel createFriendsView() {
		JPanel panel = new JPanel();
		ArrayList<Expense> expenses = Database.getExpenses(user);
		JTextArea ta = new JTextArea();
		JTable table = new JTable();
		ta.setText(expenses.toString());
		ExpenseTableModel tableModel = processExpenses();
		table.setModel(tableModel);
		JScrollPane sp = new JScrollPane(table);
		panel.add(sp);
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
					e.getTitle(), e.getAmount(), e.getTime(), e.getCreditorStr(), e.getDebtorStr(), e.getGroupId()
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

	public static void main(String[] args) {
		Dashboard dashboard = new Dashboard();
//		dashboard.pack();
		dashboard.setVisible(true);
	}
}
