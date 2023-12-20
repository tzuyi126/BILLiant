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
import java.util.List;
import java.text.DateFormat;
import java.text.DecimalFormat;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import database.Database;
import database.Expense;
import database.Group;
import database.User;
import socket.Client;

public class Dashboard extends JFrame {
	
	private User user;
	
	private Client client;
	
	private ArrayList<Group> groups = new ArrayList<Group>();

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
		this.groups = Database.getGroups(user);
		
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

		panes.addTab("Dashboard", dashboard);
		
		for (Group group : groups) {
			System.out.println(group);
			JPanel groupPanel = createGroupDashBoard(group);
			panes.addTab(group.getName(), groupPanel);
		}
		
		this.setJMenuBar(menuBar);
		this.add(header, BorderLayout.PAGE_START);
		SwingUtilities.updateComponentTreeUI(this);
		this.add(panes, BorderLayout.CENTER);
	}
	
	public JPanel createUserDashboard() {
		JPanel panel = new JPanel();
		ArrayList<Expense> expenses = Database.getExpenses(user, null);
		JTextArea ta = new JTextArea();
		JTable table = new JTable();
		ta.setText(expenses.toString());
		ExpenseTableModel tableModel = processExpenses(null);
		table.setModel(tableModel);
		JScrollPane sp = new JScrollPane(table);
		
		JPanel controls = new JPanel();
		JButton addExpense = new JButton("Add expense");
		addExpense.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new AddExpense();
			}
		});
		controls.add(addExpense);
		
		panel.add(sp, BorderLayout.CENTER);
		panel.add(addExpense, BorderLayout.PAGE_END);
//		panel.setLayout();
		return panel;
	}
	
	class AddExpense extends JDialog implements ActionListener {
		private Dashboard dashboard;
		JLabel addTitle;
		JLabel titleLabel;
		JLabel dateLabel;
		JLabel amountLabel;
		JTextField title;
		JTextField date;
		JFormattedTextField amount;
		JLabel groupLabel;
		JComboBox<Group> chooseGroup;
		Group[] groupChoices = groups.toArray(new Group[0]);
		JButton split;
		JButton addExpense;
		JTextField cost0;
		JTextField cost1;
		JTextField cost2;
		
		public AddExpense() {
//			super(dashboard);
//			this.dashboard = dashboard;
			
			addTitle = new JLabel("Add an expense");
			titleLabel = new JLabel("Title: ");
			dateLabel = new JLabel("Date: ");
			amountLabel = new JLabel("Amount: ");
			title = new JTextField();
//			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//			date = new JFormattedTextField(dateFormat);
			date = new JTextField();
			NumberFormat amountFormat = NumberFormat.getNumberInstance();
			amount = new JFormattedTextField(amountFormat);
			groupLabel = new JLabel("Group:");
			chooseGroup = new JComboBox<>(groups.toArray(new Group[0]));
			split = new JButton("Split the bill!");
			split.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Group selectedGroup = (Group) chooseGroup.getSelectedItem();
					splitBill(selectedGroup, ((Number)amount.getValue()).doubleValue());
				}
			});
			addExpense = new JButton("Add expense");
			addExpense.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					List<String> errs = new ArrayList<String>();
					String etitle = title.getText().trim();
					if (etitle.isEmpty()) errs.add("Please enter an expense title.");
					if (amount.getValue() == null) errs.add("Please enter an expense amount.");
					double eamount = ((Number) amount.getValue()).doubleValue();
					String edate = date.getText().trim();
					if (edate.isEmpty()) errs.add("Please enter an expense title.");
					Group selectedGroup = (Group) chooseGroup.getSelectedItem();
					List<String> allMembers = selectedGroup.getMembersStrArr();
					allMembers.remove(user.getUsername());
					String debtors = String.join(",", allMembers);
					if (errs.isEmpty()) {
						Expense exp = new Expense(etitle, eamount, edate, user.getUsername(), debtors, selectedGroup.getName());
						try {
							Database.addExpense(exp);
							JOptionPane.showMessageDialog(AddExpense.this, "Added expense " + exp.toString() + " successfully",
									"Success", JOptionPane.PLAIN_MESSAGE);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(AddExpense.this, "Missing data.", "Oops!", JOptionPane.WARNING_MESSAGE);
							e1.printStackTrace();
						}
					} else {
						JOptionPane.showMessageDialog(AddExpense.this, "Missing data.", "Oops!", JOptionPane.WARNING_MESSAGE);
						return;
					}
				}
			});
			
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
			c.gridx = 1;
			c.gridy = 3;
			addPanel.add(chooseGroup, c);
			c.gridx = 0;
			c.gridy = 4;
			addPanel.add(split, c);
			c.gridx = 0;
			c.gridy = 5;
			addPanel.add(addExpense, c);
			
			this.setSize(500, 400);
			this.add(addPanel, BorderLayout.CENTER);
			this.setLocationRelativeTo(dashboard);
			this.setVisible(true);
		}
		
		private void splitBill(Group group, double targetAmount) {
			DecimalFormat dformat = new DecimalFormat("#.##");
			JDialog dialog = new JDialog();
			JPanel panel = new JPanel(new GridLayout(4, 2));
			ArrayList<User> debtors = group.getMembers();
			
			JLabel name0 = new JLabel(debtors.get(0).getUsername());
			cost0 = new JTextField();
			panel.add(name0);
			panel.add(cost0);
			JLabel name1 = new JLabel(debtors.get(1).getUsername());
			cost1 = new JTextField();
			panel.add(name1);
			panel.add(cost1);
			JLabel name2 = new JLabel(debtors.get(2).getUsername());
			cost2 = new JTextField();
			panel.add(name2);
			panel.add(cost2);
			
			panel.add(amountLabel);
			JTextField totalAmount = new JTextField();
			panel.add(totalAmount);
			totalAmount.setText(dformat.format(targetAmount));
			totalAmount.setEditable(false);
			
//			JTextArea ta = new JTextArea();
//			ta.setText(debtors.get(0));
//			panel.add(ta);
			
			dialog.setSize(300, 300);
			dialog.add(panel);
			dialog.setLocationRelativeTo(dashboard);
			dialog.setVisible(true);
			
			addDocumentListener(cost0, totalAmount, targetAmount);
			addDocumentListener(cost1, totalAmount, targetAmount);
			addDocumentListener(cost2, totalAmount, targetAmount);
		}
		
		private void addDocumentListener(JTextField inputField, JTextField totalAmount, double targetAmount) {
			inputField.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void changedUpdate(DocumentEvent arg0) {
					addToAmount();
				}

				@Override
				public void insertUpdate(DocumentEvent arg0) {
					addToAmount();
				}

				@Override
				public void removeUpdate(DocumentEvent arg0) {
					addToAmount();
				}
				
				private void addToAmount() {
					DecimalFormat dformat = new DecimalFormat("#.##");
					double c0 = parseDouble(cost0.getText());
					double c1 = parseDouble(cost1.getText());
					double c2 = parseDouble(cost2.getText());
					double total = c0 + c1 + c2;
					if (total != targetAmount) {
						if (inputField == cost0) {
							c1 += targetAmount - total;
						} else if (inputField == cost1) {
							c2 += targetAmount - total;
						} else if (inputField == cost2) {
							c0 += targetAmount - total;
						}
						cost0.setText(dformat.format(c0));
						cost1.setText(dformat.format(c1));
						cost2.setText(dformat.format(c2));
					}
				}
			});
		}
		
		private Double parseDouble(String cost) {
			try {
				return Double.parseDouble(cost);
			} catch (NumberFormatException e) {
				return 0.0;
			}
		}
		
//		private JPanel switchAddType() {
//			JPanel panel = new JPanel();
//			JPanel cards = new JPanel(new CardLayout());
//			JPanel selectPane = new JPanel();
//			String[] selectGroup = {"None", "Group"};
//			
//			JPanel noGroup = new JPanel();
//			noGroup.add(new JTextField("recipient"));
//			
//			JPanel group = new JPanel();
//			JCheckBox u1 = new JCheckBox("Friend 1");
//			JCheckBox u2 = new JCheckBox("Friend 2");
//			group.add(u1);
//			group.add(u2);
//			
//			cards.add(noGroup, "None");
//			cards.add(group, "Group");
//			
//			JComboBox<String> cb = new JComboBox<String>(selectGroup);
//			cb.addItemListener(new ItemListener() {
//				@Override
//				public void itemStateChanged(ItemEvent e) {
//					CardLayout c = (CardLayout)(cards.getLayout());
//					c.show(cards, (String)e.getItem());
//				}
//			});
//			selectPane.add(cb);
//			
//			panel.add(selectPane, BorderLayout.PAGE_START);
//			panel.add(cards, BorderLayout.LINE_START);
//			return panel;
//		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
//			this.setVisible(true);
		}
		
	}
	
	public JPanel createGroupDashBoard(Group group) {
		JPanel panel = new JPanel();
		ArrayList<Expense> expenses = Database.getExpenses(user, group);
		JTextArea ta = new JTextArea();
		JTable table = new JTable();
		ta.setText(expenses.toString());
		ExpenseTableModel tableModel = processExpenses(group);
		table.setModel(tableModel);
		JScrollPane sp = new JScrollPane(table);
		panel.add(sp);
//		panel.setLayout();
		return panel;
	}
	
	public JPanel createFriendsView() {
		JPanel panel = new JPanel();
		ArrayList<Expense> expenses = Database.getExpenses(user, null);
		JTextArea ta = new JTextArea();
		JTable table = new JTable();
		ta.setText(expenses.toString());
		ExpenseTableModel tableModel = processExpenses(null);
		table.setModel(tableModel);
		JScrollPane sp = new JScrollPane(table);
		panel.add(sp);
		return panel;
	}
	
	public ExpenseTableModel processExpenses(Group group) {
		ArrayList<Expense> expenses = Database.getExpenses(user, group);
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
	
//	public boolean addExpense(Expense expense) throws Exception {
//		try {
//			String response = client.executeAndGetResponse(getName());
//			return Boolean.parseBoolean(response);
//		} catch (Exception e) {
//			System.err.println("error adding expense " + expense.getTitle());
//			throw e;
//		}
//	}
	
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
