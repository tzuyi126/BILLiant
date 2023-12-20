package database;

import java.util.ArrayList;
import java.util.Date;

import utils.Encryption;

public class Expense {
	private String id;
	
	private String title;
	
	private double amount;
	
	private String time;
	
	private String creditorStr;
	
//	private User creditor;
	
	private String debtorStr = "";
	
//	private ArrayList<User> debtor;
	
	private String groupId = "";
	
	public Expense() {
		
	}

	public Expense(String id, String title, double amount, String time, String creditor, String debtor, String groupId) {
		this.id = id;
		this.title = title;
		this.amount = amount;
		this.time = time;
		this.creditorStr = creditor;
		this.debtorStr = debtor;
		this.groupId = groupId;
	}
	
//	public Expense(String id, String title, double amount, String time, User creditor, String debtor) {
//		this.id = id;
//		this.title = title;
//		this.amount = amount;
//		this.time = time;
//		this.creditor = creditor;
//		this.debtorStr = debtor;
//	}
	
	public Expense(String title, double amount, String time, String creditor, String debtor, String groupId) {
		this.id = Encryption.getRandomeId();
		this.title = title;
		this.amount = amount;
		this.time = time;
		this.creditorStr = creditor;
		this.debtorStr = debtor;
		this.groupId = groupId;
	}
	
	public Expense(String title, double amount, String time, String creditor) {
		this.id = Encryption.getRandomeId();
		this.title = title;
		this.amount = amount;
		this.time = time;
		this.creditorStr = creditor;
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
	
//	public User getCreditor() {
//		return creditor;
//	}
//	
//	public ArrayList<User> getDebtor() {
//		return debtor;
//	}
	
	public String getDebtorStr() {
		return debtorStr;
	}
	
	public String getGroupId() {
		return groupId;
	}
	
	@Override
	public String toString() {
		return title + ": $" + amount + ", paid by " + creditorStr + " for " + debtorStr;
	}

}
