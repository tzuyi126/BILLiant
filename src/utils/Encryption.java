package utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
	
	private static final String SYMBOL = "@#$%&";
	
	private static final String UPPER_LETTER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	private static final String LOWER_LETTER = "abcdefghijklmnopqrstuvwxyz";
	
	private static final String NUMBER = "0123456789";
	
	private static final String RSA = "RSA";
	
	private static final String AES = "AES";
	
	public static String getRandomeId() {
		return Encryption.getRandomeId(7);
	}

	public static String getRandomeId(int len) {
		String candidateChar = SYMBOL + UPPER_LETTER + LOWER_LETTER + NUMBER;
		
		StringBuilder username = new StringBuilder();
		
		Random random = new Random();
		
		for (int i = 0; i < len; i++) {
			char randomChar = candidateChar.charAt(random.nextInt(candidateChar.length()));
			username.append(randomChar);
		}
		
		return username.toString();
	}
	
	public static Key getRandomKey() throws NoSuchAlgorithmException {
		return KeyGenerator.getInstance(AES).generateKey();
	}
	
	public static String getRandomKeyStr() throws NoSuchAlgorithmException {
		Key key = KeyGenerator.getInstance(AES).generateKey();
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	public static String keyToString(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	public static Key stringToKey(String keyStr) {
		byte[] keyByte = Base64.getDecoder().decode(keyStr);
		return new SecretKeySpec(keyByte, 0, keyByte.length, AES);
	}
	
	public static String encrypt(Key key, String input) 
			throws NoSuchPaddingException, NoSuchAlgorithmException,
		    InvalidAlgorithmParameterException, InvalidKeyException,
	    BadPaddingException, IllegalBlockSizeException {
	    
	    Cipher cipher = Cipher.getInstance(AES);
	    cipher.init(Cipher.ENCRYPT_MODE, key);
	    byte[] cipherText = cipher.doFinal(input.getBytes());
	    return Base64.getEncoder()
	        .encodeToString(cipherText);
	}
	
	public static String decrypt(Key key, String cipherText) 
			throws NoSuchPaddingException, NoSuchAlgorithmException,
		    InvalidAlgorithmParameterException, InvalidKeyException,
	    BadPaddingException, IllegalBlockSizeException {
	    
	    Cipher cipher = Cipher.getInstance(AES);
	    cipher.init(Cipher.DECRYPT_MODE, key);
	    byte[] plainText = cipher.doFinal(Base64.getDecoder()
	        .decode(cipherText));
	    return new String(plainText);
	}
	
	public static byte[] pkEncrypt(Key key, byte[] plaintext) 
			throws NoSuchAlgorithmException, NoSuchPaddingException, 
			InvalidKeyException, IllegalBlockSizeException, 
			BadPaddingException
	{
	    Cipher cipher = Cipher.getInstance(RSA);// /ECB/OAEPWithSHA1AndMGF1Padding");   
	    cipher.init(Cipher.ENCRYPT_MODE, key);  
	    return cipher.doFinal(plaintext);
	}

	public static byte[] pkDecrypt(Key key, byte[] ciphertext) 
			throws NoSuchAlgorithmException, NoSuchPaddingException, 
			InvalidKeyException, IllegalBlockSizeException, 
			BadPaddingException
	{
	    Cipher cipher = Cipher.getInstance(RSA); // /ECB/OAEPWithSHA1AndMGF1Padding");   
	    cipher.init(Cipher.DECRYPT_MODE, key);  
	    return cipher.doFinal(ciphertext);
	}

	public static Key generateAESKey(byte[] sequence) {

	    System.out.println("generated key with bytes "+ Arrays.toString(sequence));
	    return new SecretKeySpec(sequence, "AES");
	}
	
	public static byte[] generateSeed() {
		SecureRandom secureRandom = new SecureRandom();
		byte[] secureRandomKeyBytes = new byte[16];
	    secureRandom.nextBytes(secureRandomKeyBytes);
	    return secureRandomKeyBytes;
	}
	

	public static PrivateKey readPrivateKey(String filename)
		    throws Exception {

	    byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

	      PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
	      KeyFactory kf = KeyFactory.getInstance(RSA);
	      return kf.generatePrivate(spec);
	}
	
	public static PublicKey readPublicKey(String keyString)
		    throws Exception {

	    byte[] keyBytes = Base64.getDecoder().decode(keyString);

	    X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
	      KeyFactory kf = KeyFactory.getInstance(RSA);
	      return kf.generatePublic(spec);
	      
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			System.out.println("username_" + Encryption.getRandomeId(7));
		}
		
		try {
			Key key = Encryption.getRandomKey();
			System.out.println(Encryption.keyToString(key));
			String encryptStr = Encryption.encrypt(key, "helloworld");
			System.out.println(encryptStr);
			System.out.println(Encryption.decrypt(key, encryptStr));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
