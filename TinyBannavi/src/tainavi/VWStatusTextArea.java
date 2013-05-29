package tainavi;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class VWStatusTextArea extends JPanel implements StatusTextArea {

	private static final long serialVersionUID = 1L;

	/*
	 * 部品
	 */
	
	private JScrollPane jsp = null;
	private JTextArea jta = null;
	
	/*
	 * コンストラクタ
	 */
	
	public VWStatusTextArea() {
		
		super();
		
		this.setLayout(new BorderLayout());
		
		this.add(getJScrollPane_statusarea());
		
	}

	private JScrollPane getJScrollPane_statusarea() {
		if (jsp == null) {
			jsp = new JScrollPane(getJTextArea_statusarea(),JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return(jsp);
	}
	
	private JTextArea getJTextArea_statusarea() {
		if (jta == null) {
			jta = new JTextAreaWithPopup(4,0);
			jta.setLineWrap(true);			// 改行、する！
			jta.setWrapStyleWord(false);		// 画面端で折り返し、する！
			jta.setEditable(false);			// 編集、させない！
		}
		return jta;
	}
	
	/*
	 * StatusTextArea用のメソッド(non-Javadoc)
	 */

	@Override
	public void clear() {
		jta.setText("");
	}

	private void append(String message) {
		jta.append("\n"+message);
		jta.setCaretPosition(jta.getText().length());
	}

	@Override
	public void appendMessage(String message) {
		this.append(message);
		System.out.println(message);
	}

	@Override
	public void appendError(String message) {
		this.append(message);
		System.err.println(message);
	}

	@Override
	public int getRows() {
		return jta.getRows();
	}
	
	@Override
	public void setRows(int rows) {
		jta.setRows(rows);
	}
}
