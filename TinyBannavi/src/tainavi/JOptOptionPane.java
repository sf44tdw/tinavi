package tainavi;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class JOptOptionPane extends JOptionPane {
	
	private static final long serialVersionUID = 1L;
	
	private static JCheckBoxPanel jcheckbox = null;

	public static int showConfirmDialog(Component parentComponent, String message, String notice, String title, int optionType) {
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel,BoxLayout.Y_AXIS);
		panel.setLayout(layout);
		JLabel jlabel = new JLabel(message,JLabel.CENTER);
		jlabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(jlabel);
		panel.add(new JLabel(" "));	// Vgap
		jcheckbox = new JCheckBoxPanel(notice, 0, true);
		jcheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(jcheckbox);
		return showConfirmDialog(parentComponent, panel, title, optionType);
	}
	
	public static boolean isSelected() {
		return (jcheckbox != null && jcheckbox.isSelected());
	}

}
