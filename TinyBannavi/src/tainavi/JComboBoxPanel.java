package tainavi;

import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class JComboBoxPanel extends JPanel implements ItemSelectable,WideComponent {

	private static final long serialVersionUID = 1L;

	private JComboBoxWithPopup jcombobox = null;
	private JLabel jlabel = null;
	
	private final int h = 25;

	// 旧版
	public JComboBoxPanel(String s, int labelWidth, int comboboxWidth) {
		makeComboBoxPanel(s, labelWidth, comboboxWidth, false);
	}

	// 新版
	public JComboBoxPanel(String s, int labelWidth, int comboboxWidth, boolean horizontal) {
		makeComboBoxPanel(s, labelWidth, comboboxWidth,  horizontal);
	}
	
	private void makeComboBoxPanel(String s, int labelWidth, int comboboxWidth, boolean horizontal) {
		if ( horizontal == true ) {
			// 左・右
			this.setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
			
			jlabel = new JLabel(s);
			Dimension d = jlabel.getPreferredSize();
			d.width = labelWidth;
			d.height = 100;
			jlabel.setMaximumSize(d);
			this.add(jlabel);
			
			jcombobox = new JComboBoxWithPopup();
			d = jcombobox.getPreferredSize();
			d.width = comboboxWidth;
			d.height = 100;
			jcombobox.setMaximumSize(d);
			this.add(jcombobox);
		}
		else {
			// 上・下
			this.setLayout(null);
			
			this.add(jlabel = new JLabel(s));
			//Dimension d1 = jlabel.getPreferredSize();
			jlabel.setBounds(new Rectangle(0,0,labelWidth,h));
			
			this.add(jcombobox = new JComboBoxWithPopup());
			//Dimension d2 = jcombobox.getPreferredSize();
			jcombobox.setBounds(new Rectangle(5,25,comboboxWidth,h));
			
			this.setPreferredSize(new Dimension(comboboxWidth+5,h*2+5));
		}
	}

	public void removeAllItems() {
		this.jcombobox.removeAllItems();
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
