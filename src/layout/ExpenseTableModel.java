package layout;

import javax.swing.table.DefaultTableModel;

public class ExpenseTableModel extends DefaultTableModel {
	private String[] cols = {"Title", "Amount", "Time", "Payer", "Payee"};
	
	public ExpenseTableModel() {
		super();
		this.setColumnIdentifiers(cols);
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}
