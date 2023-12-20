package socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import database.Database;
import database.Expense;
import database.User;
import utils.Encryption;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server extends JFrame implements Runnable {
	
	private ServerSocket server;
	
	private static final int port = 9217;
	
	private JTextArea ta;
	
	private List<ClientHandler> clients = new ArrayList<>();
	
	private Key privateKey;
	
	public Server() {
		super("Server");
		
		try {
			privateKey = Encryption.readPrivateKey("keypairs/pkcs8_key");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("problem loading private key: " + e.getMessage());
			System.exit(1);
		}
		
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
		
		private Key communicationKey;
		
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
			handleAesSeed();
			
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
		
		private void handleAesSeed() throws Exception {
			try {
				int len = fromClient.readInt();
				byte[] encryptedBytes = new byte[len];
				fromClient.readFully(encryptedBytes, 0, len);
				
				byte[] decryptedBytes = Encryption.pkDecrypt(privateKey, encryptedBytes);
				
				communicationKey = Encryption.generateAESKey(decryptedBytes);
				System.out.println("key is: " + communicationKey);
			} catch (Exception e) {
				System.err.println("error handling AES seed");
				e.printStackTrace();
				throw e;
			}
		}

		@Override
		public void run() {
			ta.append("connected to a new client at " + new Date() + '\n');
			
			try {
				while (true) {
					String inString = fromClient.readUTF();
					
					String decrypted = Encryption.decrypt(communicationKey, inString);
					
					execute(decrypted);
				}
			} catch (Exception e) {
				ta.append("client might be closed\n");
				clients.remove(this);
			}
		}
		
		private void execute(String command) {
			try {
				if (command.startsWith("getuser ")) {
					String requestUsername = command.split(" ", 2)[1];
					User user = Database.getUser(requestUsername);
					
					if (user == null) {
						write("no user");
					} else {
						write(user.toString());
					}
				} else if (command.startsWith("verify ")) {
					String requestUsername = command.split(" ", 3)[1];
					String requestPassword = command.split(" ", 3)[2];
					
					write(String.valueOf(Database.verifyLogin(requestUsername, requestPassword)));
				} else if (command.startsWith("adduser ")) {
					String requestUser = command.split(" ", 2)[1];
					User user = new User(requestUser);
					
					Database.addUser(user);

					write("done");
				} else if (command.startsWith("getexpenses ")) {
					String requestUsername = command.split(" ", 2)[1];
					User user = Database.getUser(requestUsername);
					
					ArrayList<Expense> expenses = Database.getExpenses(user);
					
					expenses.forEach(expense -> write(expense.toString() + "\n"));
				} else if (command.startsWith("addexpense ")) {
					String requestExpense = command.split(" ", 2)[1];
					Expense expense = new Expense(requestExpense);
					
					Database.addExpense(expense);

					write("done");
				} else {
					ta.append("client: " + command + '\n');
				}

			} catch (Exception e) {
				e.printStackTrace();
				ta.append("failed to execute command: " + command + '\n');

				write(e.getMessage());
			}

			write("over");
		}
		
		private void write(String command) {
			try {
				String encrypted = Encryption.encrypt(communicationKey, command);
				
				toClient.writeUTF(encrypted);
				toClient.flush();
			} catch (Exception e) {
				ta.append("error writing to client\n");
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		server.setVisible(true);
	}
}
