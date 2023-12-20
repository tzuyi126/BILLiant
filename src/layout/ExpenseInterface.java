package layout;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import database.Expense;
import database.User;
import utils.FormatUtil;

public class ExpenseInterface extends JDialog {
	
	private Dashboard dashboard;
	
	private User user;
	
	private String command;
	
	private Expense currExpense;
	
	private static final String ACTION_ADD = "Add";
	
	private static final String ACTION_EDIT = "Edit";
	
	JTextField title;
	JTextField date;
	JFormattedTextField amount;
	
	String secondUsername;
	JRadioButton payerBtn;
	JRadioButton payeeBtn;
	
	public ExpenseInterface(Dashboard dashboard, User user, String command, Expense expense) {
		super(dashboard);
		
		this.dashboard = dashboard;
		
		this.user = user;
		
		this.currExpense = expense;
		
		this.command = command;
		
		this.setTitle(command + " an expense");
		
		JPanel panel = handleExpense();
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener((e) -> this.dispose());

		JPanel controlPanel = new JPanel();
		controlPanel.add(cancel);
		
		if (command.equals(ACTION_ADD)) {
			JButton addExpense = new JButton(command + " an expense");
			addExpense.addActionListener(new AddExpense());
			controlPanel.add(addExpense);
		} else if (command.equals(ACTION_EDIT)) {
			JButton editExpense = new JButton("Save edits");
			JButton deleteExpense = new JButton("Delete expense");
			editExpense.addActionListener(new AddExpense());
			deleteExpense.addActionListener(new DeleteExpense());
			controlPanel.add(editExpense);
			controlPanel.add(deleteExpense);
		}
		
		this.add(panel, BorderLayout.CENTER);
		this.add(controlPanel, BorderLayout.SOUTH);
		
		this.setSize(550, 200);
		this.setLocationRelativeTo(dashboard);
	}
	
	public JPanel handleExpense() {
		JLabel titleLabel = new JLabel("Title: ");
		JLabel amountLabel = new JLabel("Amount: ");
		JLabel dateLabel = new JLabel("Date: ");
		
		title = new JTextField();
		title.setColumns(10);

		NumberFormat amountFormat = NumberFormat.getNumberInstance();
		amount = new JFormattedTextField(amountFormat);
		amount.setColumns(10);
		
		date = new JTextField(FormatUtil.dateToString(new Date()));
		date.setColumns(10);
		
		JTextField otherUsername = new HintTextField("Enter an username");
		otherUsername.setColumns(10);
		
		otherUsername.addCaretListener((e) -> {
			secondUsername = otherUsername.getText();
			
			if (!secondUsername.isEmpty()) {
				payerBtn.setText("You paid for " + secondUsername + ".");
				payeeBtn.setText(secondUsername + " paid for you.");
			} else {
				payerBtn.setText("You paid for nobody.");
				payeeBtn.setText("Nobody paid for you.");
			}
		});
		
		payerBtn = new JRadioButton();
		payerBtn.setText("You paid for nobody.");
		
		payeeBtn = new JRadioButton();
		payeeBtn.setText("Nobody paid for you.");
		
		if (command.equals("Edit")) {
			title.setText(currExpense.getTitle());
			amount.setValue(currExpense.getAmount());
			date.setText(currExpense.getTime());
			if (currExpense.isPayer(user)) {
				payerBtn.setSelected(true);
				otherUsername.setText(currExpense.getPayee());
				secondUsername = currExpense.getPayee();
			} else if (currExpense.isPayee(user)) {
				payeeBtn.setSelected(true);
				otherUsername.setText(currExpense.getPayer());
				secondUsername = currExpense.getPayer();
			}
			payerBtn.setText("You paid for " + secondUsername + ".");
			payeeBtn.setText(secondUsername + " paid for you.");
		}
		
		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(payerBtn);
		btnGroup.add(payeeBtn);
		
		JButton addExpense = new JButton("Add an expense");
		addExpense.addActionListener(new AddExpense());
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener((e) -> this.dispose());
		
		JPanel titlePanel = new JPanel();
		
		titlePanel.add(titleLabel, BorderLayout.LINE_START);
		titlePanel.add(title, BorderLayout.LINE_END);
		
		JPanel amountPanel = new JPanel();
		
		amountPanel.add(amountLabel, BorderLayout.LINE_START);
		amountPanel.add(amount, BorderLayout.LINE_END);

		JPanel datePanel = new JPanel();
		
		datePanel.add(dateLabel, BorderLayout.LINE_START);
		datePanel.add(date, BorderLayout.LINE_END);
		
		JPanel relationPanel = new JPanel();
		relationPanel.setLayout(new GridLayout(3, 1));
		relationPanel.add(otherUsername);
		relationPanel.add(payerBtn);
		relationPanel.add(payeeBtn);
		
		JPanel panel = new JPanel();
		panel.add(titlePanel);
		panel.add(amountPanel);
		panel.add(datePanel);
		panel.add(relationPanel);
		
		return panel;
	}
	
	class DeleteExpense implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent evt) {
			int delete = JOptionPane.showConfirmDialog(ExpenseInterface.this,
					"Are you sure you want to delete this expense?", "Delete",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			
			if (delete == JOptionPane.YES_OPTION) {
				try {
					dashboard.deleteExpense(currExpense.getId());
					JOptionPane.showMessageDialog(ExpenseInterface.this, "Expense deleted!", "Success", JOptionPane.PLAIN_MESSAGE);
					
					ExpenseInterface.this.dispose();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(ExpenseInterface.this, "Something went wrong. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
	}
	
	class AddExpense implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				String etitle = title.getText().trim();
				if (etitle.isEmpty()) 
					throw new Exception("Please enter title.");
				if (etitle.contains(" "))
					throw new Exception("Title contains illegal characters (no spaces)");
			
				if (amount.getValue() == null) 
					throw new Exception("Please enter amount.");
				
				double eamount = ((Number) amount.getValue()).doubleValue();
				
				String edate = date.getText().trim();
				if (edate.isEmpty() || !FormatUtil.isValidDate(edate)) 
					throw new Exception("Please enter date with format \"yyyy-MM-dd\".");
				
				if (secondUsername.isEmpty()) {
					throw new Exception("Please enter the other user's username.");
				}
				
				if (dashboard.getUser(secondUsername) == null) {
					throw new Exception("There is no user named: " + secondUsername);
				}
				
				String payerUsername;
				String payeeUsername;
				
				if (payerBtn.isSelected()) {
					payerUsername = user.getUsername();
					payeeUsername = secondUsername;
				} else if (payeeBtn.isSelected()) {
					payerUsername = secondUsername;
					payeeUsername = user.getUsername();
				} else {
					throw new Exception("Please select the correct relationship of you and " + secondUsername + ".");
				}
				
				Expense expense = new Expense(etitle, eamount, edate, payerUsername, payeeUsername);
				try {
					if (command.equals(ACTION_ADD)) {
						dashboard.addExpense(expense);
						JOptionPane.showMessageDialog(ExpenseInterface.this, "You added an expense!", "Success", JOptionPane.PLAIN_MESSAGE);
					} else if (command.equals(ACTION_EDIT)) {
						dashboard.editExpense(currExpense.getId(), expense);
						JOptionPane.showMessageDialog(ExpenseInterface.this, "Expense saved!", "Success", JOptionPane.PLAIN_MESSAGE);
					}
					
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(ExpenseInterface.this, "Something went wrong. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(ExpenseInterface.this, e.getLocalizedMessage(), "Oops!", JOptionPane.WARNING_MESSAGE);
			}
		}
	}
}
