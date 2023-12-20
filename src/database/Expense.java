package database;

import utils.Encryption;

public class Expense {
	
	private String id;
	
	private String title;
	
	private double amount;
	
	private String time;
	
	private String creditor;
	
	private String debtor;
	
	public Expense(String title, double amount, String time, String creditor, String debtor) {
		this.id = Encryption.getRandomeId();
		this.title = title;
		this.amount = amount;
		this.time = time;
		this.creditor = creditor;
		this.debtor = debtor;
	}

	public Expense(String id, String title, double amount, String time, String creditor, String debtor) {
		this.id = id;
		this.title = title;
		this.amount = amount;
		this.time = time;
		this.creditor = creditor;
		this.debtor = debtor;
	}
	
	public Expense(String expenseStr) {
		String[] splited = expenseStr.split(",");
		this.id = splited[0];
		this.title = splited[1];
		this.amount = Double.parseDouble(splited[2]);
		this.time = splited[3];
		this.creditor = splited[4];
		this.debtor = splited[5];
	}
	
	public String getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public String getTime() {
		return time;
	}
	
	public String getCreditor() {
		return creditor;
	}
	
	public String getDebtor() {
		return debtor;
	}
	
	@Override
	public String toString() {
		return String.join(",", id, title, Double.toString(amount), time, creditor, debtor);
	}

}
