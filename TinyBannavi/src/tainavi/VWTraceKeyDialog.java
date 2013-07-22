package tainavi;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/***
 * 
 * 番組追跡検索の設定のクラス
 * 
 */

public class VWTraceKeyDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private TraceProgram xKeys = null;
	private TraceKey xKey = null;
	private ProgDetailList xTvd = null;
	
	private boolean reg = false;
	
	public String getNewLabel() { return getNewLabel(jTextField_title.getText(),jTextField_channel.getText()); }
	
	public static String getNewLabel(String title, String center) { return title+" ("+center+")"; }
	
	private ArrayList<String> okiniiri_items = new ArrayList<String>(); 
	public void clean_okiniiri_items() { okiniiri_items.clear(); }
	public void add_okiniiri_item(String s) { okiniiri_items.add(s); }

	// キーワード検索の設定ウィンドウのコンポーネント
	
	private JPanel jPanel = null;
	
	private JButton jButton_title = null;
	private JTextField jTextField_title = null;
	private JLabel jLabel_channel = null;
	private JTextField jTextField_channel = null;
	private JLabel jLabel_fazzyThreshold = null;
	private JSlider jSlider_fazzyThreshold = null;
	private JLabel jLabel_okiniiri = null;
	private JComboBox jComboBox_okiniiri = null;
	private JLabel jLabel_disableRepeat = null;
	private JCheckBox jCheckBox_disableRepeat = null;
	private JLabel jLabel_showLatestOnly = null;
	private JCheckBox jCheckBox_showLatestOnly = null;
	private JButton jButton_label = null;
	private JButton jButton_cancel = null;

	// ほげほげ

	public boolean isRegistered() { return reg; }
	
	public void open(TraceProgram sKeys, ProgDetailList tvd, int threshold) {
		
		xKeys = sKeys;
		xKey = null;
		xTvd = tvd;
		
		jTextField_title.setText(tvd.title);
		jTextField_title.setCaretPosition(0);
		jTextField_channel.setText(tvd.center);
		jSlider_fazzyThreshold.setValue(threshold);
		jComboBox_okiniiri.setSelectedItem(TVProgram.OKINIIRI[0]);
		jCheckBox_disableRepeat.setSelected(false);
		jCheckBox_showLatestOnly.setSelected(false);
		
		jButton_title.setEnabled(true);
	}
	
	public void reopen(String label, TraceProgram sKeys) {
		
		xKeys = sKeys;
		xKey = null;
		xTvd = null;
		
		for (TraceKey k : xKeys.getTraceKeys()) {
			if (k.getLabel().equals(label)) {
				// 操作対象をみつけた
				xKey = k;
				break;
			}
		}
		
		int index = xKey.getLabel().indexOf("("+xKey.getCenter()+")");
		if ( index > 0 ) {
			jTextField_title.setText(xKey.getLabel().substring(0,index));
		}
		else {
			jTextField_title.setText(xKey.getLabel());
		}
		jTextField_title.setCaretPosition(0);
		jTextField_channel.setText(xKey.getCenter());
		jSlider_fazzyThreshold.setValue(xKey.getFazzyThreshold());
		jComboBox_okiniiri.setSelectedItem(xKey.getOkiniiri());
		jCheckBox_disableRepeat.setSelected(xKey.getDisableRepeat());
		jCheckBox_showLatestOnly.setSelected(xKey.getShowLatestOnly());
		
		jButton_title.setEnabled(false);
	}
	
	//
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();

			jPanel.setLayout(new SpringLayout());
			
			int lw = 90;
			int iw = 200;
			int ix = 10+lw+10;
			int y = 10;
			_getJComponent(jPanel, getJButton_title("番組タイトル"), lw, 25, 10, y);
			_getJComponent(jPanel, getJTextField_title(), iw, 25, ix, y);
			
			y += 30;
			_getJComponent(jPanel, getJLabel_channel("チャンネル名"), lw, 25, 10, y);
			_getJComponent(jPanel, getJTextField_channel(), iw, 25, ix, y);
			
			y += 30;
			_getJComponent(jPanel, getJLabel_fazzyThreshold("あいまい閾値"), lw, 25, 10, y);
			_getJComponent(jPanel, getJSlider_fazzyThreshold(), iw, 25, ix, y);
			
			y += 30;
			_getJComponent(jPanel, getJLabel_okiniiri("お気に入り度"), lw, 25, 10, y);
			_getJComponent(jPanel, getJComboBox_okiniiri(), iw, 25, ix, y);
			
			y += 30;
			_getJComponent(jPanel, getJLabel_disableRepeat("再放送を除く"), lw, 25, 10, y);
			_getJComponent(jPanel, getJCheckBox_disableRepeat(), iw, 25, ix, y);
			
			y += 30;
			_getJComponent(jPanel, getJLabel_showLatestOnly("ﾘﾋﾟｰﾄ放送を検出"), lw, 25, 10, y);
			_getJComponent(jPanel, getJCheckBox_showLatestOnly(), iw, 25, ix, y);
			
			int wd = 10+lw+10+iw+20;
			
			y += 50;
			int bw = 75;
			_getJComponent(jPanel, getJButton_label("登録"), bw, 25, (wd/2)-bw-5, y);
			_getJComponent(jPanel, getJButton_cancel("ｷｬﾝｾﾙ"), bw, 25, (wd/2)+5, y);
			
			y += 30;
			
			Dimension d = new Dimension(wd,y+10);
			jPanel.setPreferredSize(d);
		}
		return jPanel;
	}
	
	private void _getJComponent(JPanel p, JComponent c, int width, int height, int x, int y) {
	    c.setPreferredSize(new Dimension(width, height));
	    ((SpringLayout)p.getLayout()).putConstraint(SpringLayout.NORTH, c, y, SpringLayout.NORTH, p);
	    ((SpringLayout)p.getLayout()).putConstraint(SpringLayout.WEST, c, x, SpringLayout.WEST, p);
	    p.add(c);
	}
	
	
	
	//
	private JButton getJButton_title(String s) {
		if (jButton_title == null) {
			jButton_title = new JButton(s);
			
			jButton_title.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					jTextField_title.setText(jTextField_title.getText().equals(xTvd.title) ? xTvd.splitted_title : xTvd.title);
					jTextField_title.setCaretPosition(jTextField_title.getText().length());
					jTextField_title.requestFocusInWindow();
				}
			});
		}
		return(jButton_title);
	}
	
	//
	private JTextField getJTextField_title() {
		if (jTextField_title == null) {
			jTextField_title = new JTextField();
		}
		return(jTextField_title);
	}
	
	//
	private JLabel getJLabel_channel(String s) {
		if (jLabel_channel == null) {
			jLabel_channel = new JLabel(s);
		}
		return(jLabel_channel);
	}
	private JTextField getJTextField_channel() {
		if (jTextField_channel == null) {
			jTextField_channel = new JTextField();
			jTextField_channel.setEnabled(false);
		}
		return(jTextField_channel);
	}
	
	//
	private JLabel getJLabel_fazzyThreshold(String s) {
		if (jLabel_fazzyThreshold == null) {
			jLabel_fazzyThreshold = new JLabel(s);
		}
		return(jLabel_fazzyThreshold);
	}
	private JSlider getJSlider_fazzyThreshold() {
		if (jSlider_fazzyThreshold == null) {
			jSlider_fazzyThreshold = new JSlider(1,99);
			
			jSlider_fazzyThreshold.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					
					jLabel_fazzyThreshold.setText("あいまい閾値"+jSlider_fazzyThreshold.getValue());
				}
			});
		}
		return(jSlider_fazzyThreshold);
	}
	
	//
	private JLabel getJLabel_okiniiri(String s) {
		if (jLabel_okiniiri == null) {
			jLabel_okiniiri = new JLabel(s);
		}
		return(jLabel_okiniiri);
	}
	private JComboBox getJComboBox_okiniiri() {
		if (jComboBox_okiniiri == null) {
			jComboBox_okiniiri = new JComboBox();
			jComboBox_okiniiri.setEditable(false);
			
			DefaultComboBoxModel aModel = new DefaultComboBoxModel();
			jComboBox_okiniiri.setModel(aModel);
			for (String k : okiniiri_items) {
				aModel.addElement(k);
			}
		}
		return(jComboBox_okiniiri);
	}
	
	//
	private JLabel getJLabel_disableRepeat(String s) {
		if (jLabel_disableRepeat == null) {
			jLabel_disableRepeat = new JLabel(s);
		}
		return(jLabel_disableRepeat);
	}
	private JCheckBox getJCheckBox_disableRepeat() {
		if (jCheckBox_disableRepeat == null) {
			jCheckBox_disableRepeat = new JCheckBox();
		}
		return(jCheckBox_disableRepeat);
	}
	
	//
	private JLabel getJLabel_showLatestOnly(String s) {
		if (jLabel_showLatestOnly == null) {
			jLabel_showLatestOnly = new JLabel(s);
		}
		return(jLabel_showLatestOnly);
	}
	private JCheckBox getJCheckBox_showLatestOnly() {
		if (jCheckBox_showLatestOnly == null) {
			jCheckBox_showLatestOnly = new JCheckBox();
		}
		return(jCheckBox_showLatestOnly);
	}
	
	//
	private JButton getJButton_label(String s) {
		if (jButton_label == null) {
			jButton_label = new JButton();
			jButton_label.setText(s);
			
			jButton_label.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if ( addToTraceKeyList() ) {
						// ウィンドウを閉じる
						dispose();
					}
				}
			});
		}
		return(jButton_label);
	}
	
	private boolean addToTraceKeyList() {
		if (jTextField_title.getText().equals("")) {
			return false;
		}
		
		// 重複登録を許さない
		for (TraceKey k : xKeys.getTraceKeys()) {
			if ( k != xKey && k.getLabel().equals(getNewLabel()) ) {
				JOptionPane.showConfirmDialog(this, "既に登録されています:"+getNewLabel(), "警告", JOptionPane.CLOSED_OPTION);							// キーワード検索の追加ではダイアログで修正できるので止めない
				return false;
			}
		}
		
		if ( xKey == null ) {
			// 新規登録の場合はエントリがないので作成する
			xKey = new TraceKey();
			xKeys.add(xKey);
		}
		
		xKey.setLabel(getNewLabel());
		xKey.setCenter(jTextField_channel.getText());
		xKey.setTitlePop(TraceProgram.replacePop(jTextField_title.getText()));
		xKey.setSearchStrKeys(TraceProgram.splitKeys(xKey.getTitlePop()));
		xKey.setFazzyThreshold(jSlider_fazzyThreshold.getValue());
		xKey.setOkiniiri((String) jComboBox_okiniiri.getSelectedItem());
		xKey.setDisableRepeat(jCheckBox_disableRepeat.isSelected());
		xKey.setShowLatestOnly(jCheckBox_showLatestOnly.isSelected());
		
		reg = xKeys.save();
		
		return reg;
	}
	
	//
	private JButton getJButton_cancel(String s) {
		if (jButton_cancel == null) {
			jButton_cancel = new JButton();
			jButton_cancel.setText(s);
			
			jButton_cancel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
		}
		return jButton_cancel;
	}
	
	
	// コンストラクタ
	public VWTraceKeyDialog(int x, int y) {
		
		super();

		//
		reg = false;
		
		// 初期設定
		clean_okiniiri_items();
		for (String okini : TVProgram.OKINIIRI) {
			add_okiniiri_item(okini);
		}
		
		//
		this.setModal(true);
		this.setContentPane(getJPanel());
		// タイトルバーの高さも考慮する必要がある
		Dimension d = getJPanel().getPreferredSize();
		this.pack();
		this.setBounds(
				x,
				y, 
				d.width+(this.getInsets().left+this.getInsets().right),
				d.height+(this.getInsets().top+this.getInsets().bottom));
		this.setResizable(false);
		//
		this.setTitle("番組追跡の設定");
	}
}
