package layout;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class HintTextField extends JTextField implements FocusListener {
	
	private final String defaultString;
	
	private boolean isHintText;
	
	public HintTextField(String defaultString) {
		super(defaultString);
		
		this.defaultString = defaultString;
		this.isHintText = true;
		
		super.addFocusListener(this);
	}
	
	@Override
	public void focusGained(FocusEvent arg0) {
		if (this.getText().isEmpty()) {
			super.setText("");
			isHintText = false;
			
		}
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		if (this.getText().isEmpty()) {
			super.setText(defaultString);
			isHintText = true;
		}
	}
	
	@Override
	public String getText() {
		return isHintText? "" : super.getText();
	}
}
