package tainavi;

import java.awt.ItemSelectable;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;


public class JComboBoxPanel extends JPanel implements ItemSelectable,WideComponent {

	private static final long serialVersionUID = 1L;

	private JComboBoxWithPopup jcombobox = null;
	private JLabel jlabel = null;
	
	// 旧版
	public JComboBoxPanel(String s, int labelWidth, int comboboxWidth) {
		super();
		makeComboBoxPanel(s, labelWidth, false);
	}

	// 新版
	public JComboBoxPanel(String s, int labelWidth, int comboboxWidth, boolean horizontal) {
		super();
		makeComboBoxPanel(s, labelWidth, horizontal);
	}
	
	private void makeComboBoxPanel(String s, int labelWidth, boolean horizontal) {
		
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		
		jlabel = new JLabel(s);
		jcombobox = new JComboBoxWithPopup();

		this.add(jlabel);
		this.add(jcombobox);
		
		if ( horizontal == true ) {
			// 左・右
			layout.putConstraint(SpringLayout.NORTH, jlabel, 1, SpringLayout.NORTH, this);
			layout.putConstraint(SpringLayout.WEST, jlabel, 1, SpringLayout.WEST, this);
			layout.putConstraint(SpringLayout.SOUTH, jlabel, -1, SpringLayout.SOUTH, this);
			layout.putConstraint(SpringLayout.EAST, jlabel, labelWidth, SpringLayout.WEST, this);
			
			layout.putConstraint(SpringLayout.NORTH, jcombobox, 1, SpringLayout.NORTH, this);
			layout.putConstraint(SpringLayout.WEST, jcombobox, 0, SpringLayout.EAST, jlabel);
			layout.putConstraint(SpringLayout.SOUTH, jcombobox, -1, SpringLayout.SOUTH, this);
			layout.putConstraint(SpringLayout.EAST, jcombobox, -1, SpringLayout.EAST, this);
		}
		else {
			// 上・下
			layout.putConstraint(SpringLayout.NORTH, jlabel, 1, SpringLayout.NORTH, this);
			layout.putConstraint(SpringLayout.WEST, jlabel, 1, SpringLayout.WEST, this);
			//
			layout.putConstraint(SpringLayout.EAST, jlabel, -1, SpringLayout.EAST, this);
			
			layout.putConstraint(SpringLayout.NORTH, jcombobox, 1, SpringLayout.SOUTH, jlabel);
			layout.putConstraint(SpringLayout.WEST, jcombobox, 5, SpringLayout.NORTH, this);
			layout.putConstraint(SpringLayout.SOUTH, jcombobox, -1, SpringLayout.SOUTH, this);
			layout.putConstraint(SpringLayout.EAST, jcombobox, -1, SpringLayout.EAST, this);
		}
	}

	public void removeAllItems() {
		this.jcombobox.removeAllItems();
	}
	
	public void removeItemAt(int anIndex) {
		this.jcombobox.removeItemAt(anIndex);
	}
	
	public void insertItemAt(Object anObject, int index) {
		this.jcombobox.insertItemAt(anObject, index);
	}
	
	public void addItem(Object o) {
		this.jcombobox.addItem(o);
	}
	
	public int getSelectedIndex() {
		return this.jcombobox.getSelectedIndex();
	}
	public Object getSelectedItem() {
		return this.jcombobox.getSelectedItem();
	}
	public Object getItemAt(int index) {
		return this.jcombobox.getItemAt(index);
	}
	
	public int getItemCount() {
		return this.jcombobox.getItemCount();
	}
	
	public int indexOf(Object o) {
		return this.jcombobox.indexOf(o);
	}
	
	public void setSelectedItem(Object o) {
		this.jcombobox.setSelectedItem(o);
	}
	public void setSelectedIndex(int anIndex) {
		this.jcombobox.setSelectedIndex(anIndex);
	}
	
	public void setEditable(boolean b) {
		this.jcombobox.setEditable(b);
	}
	
	@Override
	public void setEnabled(boolean b) {
		this.jlabel.setEnabled(b);
		this.jcombobox.setEnabled(b);
	}
	@Override
	public boolean isEnabled() {
		return this.jcombobox.isEnabled();
	}
	
	public void setToolTipText(String s) {
		this.jlabel.setToolTipText(s);
	}
	
	public void setText(String s) {
		this.jlabel.setText(s);
	}
	
	public ComboBoxModel getModel() {
		return this.jcombobox.getModel();
	}

	public JComboBox getJComboBox() { return jcombobox; }

	// オーバーライドではない
	
	public void addActionListener(ActionListener l) {
		this.jcombobox.addActionListener(l);
	}
	
	/*
	public ActionListener[] getActionListeners() {
		return this.jcombobox.getActionListeners();
	}
	*/
	
	public void removeActionListener(ActionListener l) {
		this.jcombobox.removeActionListener(l);
	}
	
	// オーバーライド

	@Override
	public void addPopupWidth(int w) {
		this.jcombobox.addPopupWidth(w);
	}
	
	@Override
	public void addItemListener(ItemListener l) {
		this.jcombobox.addItemListener(l);
	}

	@Override
	public Object[] getSelectedObjects() {
		return this.jcombobox.getSelectedObjects();
	}

	@Override
	public void removeItemListener(ItemListener l) {
		this.jcombobox.removeItemListener(l);
	}
}
