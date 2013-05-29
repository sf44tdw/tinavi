package tainavi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;


public class JDetailPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	JScrollPane jscrollpane = null;
	private JTextAreaWithPopup jta = null;
	private JLabel jlabel_time = null;
	private JLabel jlabel_title = null;
	
	private final int titleFontSize = 20;
	//private int textAreaRows = 4;
	
	public JDetailPanel() {
		
		this.setLayout(new BorderLayout());
		
		Font f = null;
		
		jlabel_time = new JLabel();
		f = jlabel_time.getFont();
		jlabel_time.setFont(f.deriveFont(f.getStyle() | Font.BOLD, titleFontSize));
		this.add(jlabel_time,BorderLayout.LINE_START);
		
		jlabel_title = new JLabel();
		f = jlabel_title.getFont();
		jlabel_title.setFont(f.deriveFont(f.getStyle() | Font.BOLD, titleFontSize));
		jlabel_title.setForeground(Color.BLUE);
		this.add(jlabel_title,BorderLayout.CENTER);
		jlabel_title.setText(" ");
		
		jta = CommonSwingUtils.getJta(this,4,0);
		
		jscrollpane = new JScrollPane(jta,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jscrollpane.setBorder(new EmptyBorder(0,0,0,0));
		this.add(jscrollpane,BorderLayout.PAGE_END);
	}
	
	public void setLabel(String s, String e, String t) {
		if (s == null || s.length() == 0 || e == null || e.length() == 0) {
			jlabel_time.setText(" ");
		}
		else {
			jlabel_time.setText(s+"～"+e+"　");
		}
		jlabel_title.setText(t);
	}
	public String getText() {
		return jta.getText();
	}
	public void setText(String s) {
		jta.setText(s);
		jta.setCaretPosition(0);
	}
	
	public int getRows() {
		return jta.getRows();
	}
	
	public void setRows(int rows) {
		jta.setRows(rows);
	}
}
