package socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client implements Runnable {
	
	private Socket socket = null;
	
	private DataInputStream fromServer = null;
	
	private DataOutputStream toServer = null;
	
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
	
	public String executeAndGetResponse(String command) throws IOException {
		try {
			toServer.writeUTF(command);
			toServer.flush();
			
			String response = fromServer.readUTF();
			
			System.out.println("execute command: " + command + ", response: " + response);
			
			return response;
		} catch (IOException e) {
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
