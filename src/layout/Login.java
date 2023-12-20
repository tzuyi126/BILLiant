package layout;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import database.User;

class Login extends JDialog {
	
	private Dashboard dashboard;
	
	public Login(Dashboard dashboard) {
		super(dashboard);
		
		this.dashboard = dashboard;
		
		this.setTitle("Log In");
		this.setSize(300, 200);
		this.setLocationRelativeTo(null);
		this.add(createLoginPanel());
	}
	
	private JPanel createLoginPanel() {
		JLabel userLabel = new JLabel("Username");
		JLabel pwdLabel = new JLabel("Password");
		
		JTextField userField = new JTextField();
		JPasswordField pwdField = new JPasswordField();
		
		JButton login = new JButton("Login");
		JButton register = new JButton("Register");
		
		JPanel userPanel = new JPanel();
		userPanel.setLayout(new GridLayout(2, 1));
		userPanel.add(userLabel);
		userPanel.add(userField);
		
		JPanel pwdPanel = new JPanel();
		pwdPanel.setLayout(new GridLayout(2, 1));
		pwdPanel.add(pwdLabel);
		pwdPanel.add(pwdField);
		
		JPanel btnPanel = new JPanel();
		btnPanel.add(login);
		btnPanel.add(register);
		
		login.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String username = userField.getText().trim();
				String password = new String(pwdField.getPassword()).trim();
				
				if (username.isEmpty() || password.isEmpty()) {
					JOptionPane.showMessageDialog(Login.this, "Please enter username and password.", "Oops!", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				try {
					if (verifyUser(username, password)) {
						loginWith(username);
					} else {
						JOptionPane.showMessageDialog(Login.this, "Wrong username or password, please try again or register.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(Login.this, "Something went wrong. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
					
				}
			}
		});

		register.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String username = userField.getText().trim();
				String password = new String(pwdField.getPassword()).trim();
				
				if (username.isEmpty() || password.isEmpty()) {
					JOptionPane.showMessageDialog(Login.this, "Please enter username and password.", "Oops!", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				try {
					addUser(username, password);
					
					JOptionPane.showMessageDialog(Login.this, "Successfully registered!", "Success", JOptionPane.PLAIN_MESSAGE);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(Login.this, "This username has been registered!", "Oops!", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3, 1));
		panel.add(userPanel);
		panel.add(pwdPanel);
		panel.add(btnPanel);
		return panel;
	}
	
	private boolean verifyUser(String username, String password) throws Exception {
		try {
			return dashboard.verifyUser(username, password);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private void loginWith(String username) throws Exception {
		try {
			User user = dashboard.getUser(username);
			
			if (user == null) {
				throw new Exception("error logging in");
			}
			
			dashboard.refreshFrame(user);
			
			this.setVisible(false);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private void addUser(String username, String password) throws Exception {
		User user = new User(username, password);
		dashboard.addUser(user);
	}
}
