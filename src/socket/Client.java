package socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.Key;
import java.security.PublicKey;

import utils.Encryption;

public class Client implements Runnable {
	
	private Socket socket = null;
	
	private DataInputStream fromServer = null;
	
	private DataOutputStream toServer = null;
	
	private static final String SERVER_PUBLIC_KEY = "MIGeMA0GCSqGSIb3DQEBAQUAA4GMADCBiAKBgGk9wUQ4G9PChyL5SUkCyuHjTNOglEy5h4KEi0xpgjxi/UbIH27NXLXOr94JP1N5pa1BbaVSxlvpuCDF0jF9jlZw5IbBg1OW2R1zUACK+NrUIAYHWtagG7KB/YcyNXHOZ6Icv2lXXd7MbIao3ShrUVXo3u+5BJFCEibd8a/JD/KpAgMBAAE=";
	
	private PublicKey serverPublicKey;
	
	private Key communicationKey;
	
	private static final String host = "localhost";
	
	private static final int port = 9217;
	
	public Client() {
		try {
			socket = new Socket(host, port);
			
			fromServer = new DataInputStream(socket.getInputStream());
			toServer = new DataOutputStream(socket.getOutputStream());
			
		} catch (IOException e) {
			System.err.println("error connecting to server");
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			handshake();
			generateAesSeed();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		new Thread(this).start();
	}

	private void handshake() throws Exception {
		try {
			toServer.writeUTF("BILLiantConnect");
			toServer.flush();
			
			int num = fromServer.read();
			int result = ((num * 13 + 526) * 17) % 256;
			
			toServer.write(result);
			toServer.flush();
			
			String inString = fromServer.readUTF();
			
			if (!inString.equals("BRILLIANT")) {
				throw new Exception("input: \"" + inString + "\"");
			}
			
		} catch (Exception e) {
			throw new Exception("server violates handshake protocol: " + e.getMessage() + ". Abort.");
		}
	}
	
	private void generateAesSeed() throws Exception {
		try {
			byte[] byteSequence = Encryption.generateSeed();
			serverPublicKey = Encryption.readPublicKey(SERVER_PUBLIC_KEY);
			
			byte[] encryptedBytes = Encryption.pkEncrypt(serverPublicKey, byteSequence);

			toServer.writeInt(encryptedBytes.length);
			toServer.write(encryptedBytes);
			toServer.flush();

			communicationKey = Encryption.generateAESKey(byteSequence);
			System.out.println("key is: " + communicationKey.toString());	
		} catch (Exception e) {
			System.err.println("error generating AES key: " + e.getMessage());
			throw e;
		}
	}
	
	public String executeAndGetResponse(String command) throws Exception {
		try {
			String encryptedCommand = Encryption.encrypt(communicationKey, command);
			
			toServer.writeUTF(encryptedCommand);
			toServer.flush();
			
			String response = fromServer.readUTF();
			
			return Encryption.decrypt(communicationKey, response);
		} catch (Exception e) {
			System.err.println("error communicating with server");
			e.printStackTrace();
			throw e;
		}
	}
	
	@Override
	public void run() {
		try {
			while (true);
			
		} catch (Exception e) {
			System.err.println("error reading from server");
			e.printStackTrace();
		} finally {
			close();
		}
	}
	
	private void close() {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
