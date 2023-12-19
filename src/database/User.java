package database;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import utils.Encryption;

public class User {
	
	private String id;
	
	private String username;
	
	private String password;
	
	private String key;
	
	private String friendsStr = "";
	
	public User(String username, String password) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
		this.id = Encryption.getRandomeId();
		this.username = username;
		
		this.key = Encryption.getRandomKeyStr();
		this.password = Encryption.encrypt(Encryption.stringToKey(key), password);
	}
	
	public User(String id, String username, String password, String key, String friendsStr) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.key = key;
		this.friendsStr = friendsStr;
	}
	
	public User(String userStr) {
		String[] splited = userStr.split(",");
		this.id = splited[0];
		this.username = splited[1];
		this.password = splited[2];
		this.key = splited[3];
		
		if (splited.length == 5) {
			this.friendsStr = splited[4];
		}
	}

	public String getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDecryptedPassword() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
		return Encryption.decrypt(Encryption.stringToKey(key), password);
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getFriendsStr() {
		return friendsStr;
	}

	public void setFriendsStr(String friendsStr) {
		this.friendsStr = friendsStr;
	}
	
	public void addFriend(String friend) {
		if (this.friendsStr.isEmpty()) {
			this.friendsStr = friend;
		} else {
			StringBuilder sb = new StringBuilder(this.friendsStr);
			sb.append("," + friend);
			this.friendsStr = sb.toString();
		}
	}

	public List<String> getFriends() {
		return Arrays.asList(friendsStr.split(","));
	}
	
	@Override
	public String toString() {
		return String.join(",", id, username, password, key, friendsStr);
	}
}
