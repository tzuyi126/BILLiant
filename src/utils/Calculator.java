package utils;

import java.util.HashMap;
import java.util.List;

import database.Expense;
import database.User;

public class Calculator {
	
	public static HashMap<String, Double> computeLoan(User user, List<Expense> expenses) {
		HashMap<String, Double> map = new HashMap<>();
		
		if (expenses.isEmpty()) return map;
		
		expenses.stream().filter(e -> e.getPayer().equals(user.getUsername()))
		.forEach(e -> {
			String payee = e.getPayee();
			
			if (!map.containsKey(payee)) {
				map.put(payee, 0.0);
			}
			
			map.put(payee, map.get(payee) + e.getAmount());
		});
		
		return map;
	}
	
	public static HashMap<String, Double> computeDebt(User user, List<Expense> expenses) {
		HashMap<String, Double> map = new HashMap<>();
		
		if (expenses.isEmpty()) return map;
		
		expenses.stream().filter(e -> e.getPayee().equals(user.getUsername()))
		.forEach(e -> {
			String payer = e.getPayer();
			
			if (!map.containsKey(payer)) {
				map.put(payer, 0.0);
			}
			
			map.put(payer, map.get(payer) + e.getAmount());
		});
		
		return map;
	}
	
	public static HashMap<String, Double> computeTotal(User user, List<Expense> expenses) {
		HashMap<String, Double> map = new HashMap<>();
		
		if (expenses.isEmpty()) return map;
		
		expenses.forEach(e -> {
			String payer = e.getPayer();
			String payee = e.getPayee();
			
			if (payer.equals(user.getUsername())) {
				if (!map.containsKey(payee)) {
					map.put(payee, 0.0);
				}
				
				map.put(payee, map.get(payee) + e.getAmount());
			} else if (payee.equals(user.getUsername())) {
				if (!map.containsKey(payer)) {
					map.put(payer, 0.0);
				}
				
				map.put(payer, map.get(payer) - e.getAmount());
			}
		});
		
		return map;
	}
}
