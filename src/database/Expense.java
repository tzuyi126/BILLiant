package database;

import utils.Encryption;

public class Expense {
	
	private String id;
	
	private String title;
	
	private double amount;
	
	private String time;
	
	private String payer;
	
	private String payee;
	
	public Expense(String title, double amount, String time, String payer, String payee) {
		this.id = Encryption.getRandomeId();
		this.title = title;
		this.amount = amount;
		this.time = time;
		this.payer = payer;
		this.payee = payee;
	}

	public Expense(String id, String title, double amount, String time, String payer, String payee) {
		this.id = id;
		this.title = title;
		this.amount = amount;
		this.time = time;
		this.payer = payer;
		this.payee = payee;
	}
	
	public Expense(String expenseStr) {
		String[] splited = expenseStr.split(",");
		this.id = splited[0];
		this.title = splited[1];
		this.amount = Double.parseDouble(splited[2]);
		this.time = splited[3];
		this.payer = splited[4];
		this.payee = splited[5];
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
	
	public String getPayer() {
		return payer;
	}
	
	public String getPayee() {
		return payee;
	}
	
	public boolean isPayer(User user) {
		return payer.equals(user.getUsername());
	}
	
	public boolean isPayee(User user) {
		return payee.equals(user.getUsername());
	}
	
	@Override
	public String toString() {
		return String.join(",", id, title, Double.toString(amount), time, payer, payee);
	}

}
