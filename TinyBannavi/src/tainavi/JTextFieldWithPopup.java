package tainavi;

import javax.swing.JTextField;


public class JTextFieldWithPopup extends JTextField {

	private static final long serialVersionUID = 1L;

	public JTextFieldWithPopup() {
		super();
		this.addMouseListener(new TextEditPopupMenu());
	}

	public JTextFieldWithPopup(int col) {
		super(col);
		this.addMouseListener(new TextEditPopupMenu());
	}
}
