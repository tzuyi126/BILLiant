package layout;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class ExpenseTableModel extends DefaultTableModel {
	private String[] cols = {"Select", "Title", "Amount", "Time", "Payer", "Payee", "id"};
	private int selectedRow = -1;
	
	public ExpenseTableModel() {
		super();
		this.setColumnIdentifiers(cols);
	}
	
	public void setSelectedRow(int row) {
		this.selectedRow = row;
	}
	
	public int getSelectedRow() {
		return selectedRow;
	}
	
	public String getSelectedRowId(int row) {
		return (String) getValueAt(row, 6);
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return column == 0;
	}
	
	@Override
	public Class<?> getColumnClass(int index) {
		switch (index) {
		case 0:
			return Boolean.class;
		case 2:
			return Double.class;
		default:
			return String.class;
		}
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		if (column == 0) {
			boolean change = (boolean) aValue;
			System.out.println(change);
			if (change == true) {
				int rows = getRowCount();
				for (int i = 0; i < rows; i++) {
					Vector data = (Vector)getDataVector().get(i);
					if (i != row) data.set(0, false);
				}
				fireTableDataChanged();
			}
			Vector data = (Vector)getDataVector().get(row);
			data.set(0, (boolean)aValue);
			fireTableCellUpdated(row, column);
		}
	}
}


