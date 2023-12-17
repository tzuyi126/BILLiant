package socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import database.Database;
import database.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server extends JFrame implements Runnable {
	
	private ServerSocket server;
	
	private static final int port = 9217;
	
	private JTextArea ta;
	
	private List<ClientHandler> clients = new ArrayList<>();
	
	public Server() {
		super("Server");
		
		this.setSize(500, 300);
		
		ta = new JTextArea();
		ta.setEditable(false);
		this.add(new JScrollPane(ta));
		
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		try {
			server = new ServerSocket(port);
			ta.append("Server started at " + new Date() + '\n');
			
			while (true) {
				try {
					Socket socket = server.accept();
					
					ClientHandler client = new ClientHandler(socket);
					clients.add(client);
				} catch (Exception e) {
					System.err.println("error creating socket connection");
					e.printStackTrace();
				}
			}
			
		} catch (IOException e) {
			System.err.println("error starting server");
			e.printStackTrace();
		}
	}
	
	class ClientHandler implements Runnable {
		
		private Socket socket;
		
		private DataInputStream fromClient;
		
		private DataOutputStream toClient;
		
		public ClientHandler(Socket socket) throws Exception {
			this.socket = socket;
			
			try {
				this.fromClient = new DataInputStream(socket.getInputStream());
				this.toClient = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				System.err.println("error initializing data I/O stream");
				e.printStackTrace();
				throw e;
			}
			
			handshake();
			
			new Thread(this).start();
		}
		
		private void handshake() throws Exception {
			try {
				String inString = fromClient.readUTF();
				
				if (!inString.equals("BILLiantConnect")) {
					throw new Exception("input: \"" + inString + "\"");
				}
				
				int num = (int) (Math.random() * 256);
				int expected = ((num * 13 + 526) * 17) % 256;
				
				toClient.write(num);
				toClient.flush();
				
				int result = fromClient.read();
				
				if (result != expected) {
					toClient.writeUTF("CLOSE");
					toClient.flush();
					
					throw new Exception("wrong computation");
				}
				
				toClient.writeUTF("BRILLIANT");
				toClient.flush();
			} catch (Exception e) {
				throw new Exception("client violates handshake protocol: " + e.getMessage() + ". Abort.");
			}
		}

		@Override
		public void run() {
			ta.append("connected to a new client at " + new Date() + '\n');
			
			try {
				while (true) {
					String inString = fromClient.readUTF();
					
					ta.append("client: " + inString + '\n');
					
					execute(inString);
				}
			} catch (Exception e) {
				ta.append("client might be closed\n");
				clients.remove(this);
			}
		}
		
		private void execute(String command) {
			try {
				if (command.startsWith("getuser ")) {
					String requestUsername = command.split("getuser ", 2)[1];
					User user = Database.getUser(requestUsername);
					
					if (user == null) {
						toClient.writeUTF("no user");
						ta.append("response: no user\n");
					} else {
						toClient.writeUTF(user.toString());
						ta.append("response: " + user.toString() + '\n');
					}
					
					toClient.flush();
				} else if (command.startsWith("adduser ")) {
					String requestUser = command.split("adduser ", 2)[1];
					User user = new User(requestUser);
					
					Database.addUser(user);
					
					toClient.writeUTF("done");
					toClient.flush();
				}
			} catch (Exception e) {
				e.printStackTrace();

				try {
					toClient.writeUTF("failed to execute command: " + command);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				ta.append("failed to execute command: " + command + '\n');
			}
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		server.setVisible(true);
	}
}
