package database;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import utils.Encryption;

public class Group {
	
	private String id;
	
	private String name;
	
	private String members;
	
	private HashMap<String, Double> balances = new HashMap<String, Double>();

	public Group(String name, String members) {
		this.id = Encryption.getRandomeId();
		this.name = name;
		this.members = members;
		
		List<String> membersSplit = Arrays.asList(members.split(","));
		for (String member : membersSplit) {
			balances.put(member, 0.0);
		}
	}
	
	public Group(String id, String name, String members) {
		this.id = id;
		this.name = name;
		this.members = members;
		
		List<String> membersSplit = Arrays.asList(members.split(","));
		for (String member : membersSplit) {
			balances.put(member, 0.0);
		}
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getMembersStr() {
		return members;
	}
	
	public List<String> getMembersStrArr() {
		return Arrays.asList(members.split(","));
	}
	
	public ArrayList<User> getMembers() {
		ArrayList<User> outMembers = new ArrayList<User>();
		List<String> memberStr = Arrays.asList(members.split(","));
		for (String m : memberStr) {
			outMembers.add(Database.getUser(m));
		}
		return outMembers;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
