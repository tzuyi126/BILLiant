package layout;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import database.Expense;

public class ExpenseInterface extends JDialog {
	
	private Dashboard dashboard;
	
	JTextField title;
	JTextField date;
	JFormattedTextField amount;
	
	public ExpenseInterface(Dashboard dashboard) {
		super(dashboard);
		
		this.dashboard = dashboard;
		
		this.setTitle("Add an expense");
		
		JLabel titleLabel = new JLabel("Title: ");
		JLabel dateLabel = new JLabel("Date: ");
		JLabel amountLabel = new JLabel("Amount: ");
		
		title = new JTextField();
		title.setColumns(10);
//		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//		date = new JFormattedTextField(dateFormat);
		date = new JTextField();
		date.setColumns(10);
		NumberFormat amountFormat = NumberFormat.getNumberInstance();
		amount = new JFormattedTextField(amountFormat);
		amount.setColumns(10);
		
		JButton addExpense = new JButton("Add an expense");
		addExpense.addActionListener(new AddExpense());
		
		JPanel titlePanel = new JPanel();
		
		titlePanel.add(titleLabel, BorderLayout.LINE_START);
		titlePanel.add(title, BorderLayout.LINE_END);
		
		JPanel datePanel = new JPanel();
		
		datePanel.add(dateLabel, BorderLayout.LINE_START);
		datePanel.add(date, BorderLayout.LINE_END);
		
		JPanel amountPanel = new JPanel();
		
		amountPanel.add(amountLabel, BorderLayout.LINE_START);
		amountPanel.add(amount, BorderLayout.LINE_END);
		
		JPanel panel = new JPanel();
		panel.add(titlePanel);
		panel.add(datePanel);
		panel.add(amountPanel);
		
		panel.add(addExpense);
		this.add(panel, BorderLayout.CENTER);
		
		this.setSize(300, 200);
		this.setVisible(true);
	}
	
	class AddExpense implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				String etitle = title.getText().trim();
				if (etitle.isEmpty()) throw new Exception("");
			
				if (amount.getValue() == null) throw new Exception();
				
				double eamount = ((Number) amount.getValue()).doubleValue();
				
				String edate = date.getText().trim();
				if (edate.isEmpty()) throw new Exception();
				
				// TODO: new Expense
//				Expense exp = new Expense(etitle, eamount, edate, user.getUsername(), debtor);
				try {
					// TODO: call dashboard.addExpense(expense);
//					Database.addExpense(exp);
					
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Missing data.", "Oops!", JOptionPane.WARNING_MESSAGE);
					e1.printStackTrace();
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Please check your input data!", "Oops!", JOptionPane.WARNING_MESSAGE);
			}
		}
	}
}
