package database;

import utils.Encryption;

public class Expense {
	
	private String id;
	
	private String title;
	
	private double amount;
	
	private String time;
	
	private String creditorStr;
	
	private String debtorStr = "";
	
	public Expense() {
		
	}

	public Expense(String id, String title, double amount, String time, String creditor, String debtor) {
		this.id = id;
		this.title = title;
		this.amount = amount;
		this.time = time;
		this.creditorStr = creditor;
		this.debtorStr = debtor;
	}
	
	public Expense(String title, double amount, String time, String creditor, String debtor) {
		this.id = Encryption.getRandomeId();
		this.title = title;
		this.amount = amount;
		this.time = time;
		this.creditorStr = creditor;
		this.debtorStr = debtor;
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
	
	public String getCreditorStr() {
		return creditorStr;
	}
	
	public String getDebtorStr() {
		return debtorStr;
	}
	
	@Override
	public String toString() {
		return title + ": $" + amount + ", paid by " + creditorStr + " for " + debtorStr;
	}

}
