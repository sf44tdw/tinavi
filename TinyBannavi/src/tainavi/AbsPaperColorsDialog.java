package tainavi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import tainavi.Env.AAMode;
import tainavi.JTXTButton.FontStyle;
import tainavi.TVProgram.ProgGenre;


abstract class AbsPaperColorsDialog extends JEscCancelDialog {

	private static final long serialVersionUID = 1L;
	
	private static boolean debug = false;

	
	/*******************************************************************************
	 * 抽象メソッド
	 ******************************************************************************/
	
	protected abstract Env getEnv();
	protected abstract Bounds getBoundsEnv();
	protected abstract PaperColorsMap getPaperColorMap();

	protected abstract VWColorChooserDialog getCCWin();

	protected abstract void updatePaperColors(Env ec, PaperColorsMap pc);
	protected abstract void updatePaperFonts(Env ec);
	protected abstract void updatePaperBounds(Env ec, Bounds bc);
	protected abstract void updatePaperRepaint();

	
	/*******************************************************************************
	 * 呼び出し元から引き継いだもの
	 ******************************************************************************/
	
	private final Env origenv = getEnv();
	private final Bounds origbnd = getBoundsEnv();
	private final PaperColorsMap origpc = getPaperColorMap();
	
	private final VWColorChooserDialog ccwin = getCCWin();
	
	private final Env tmpenv = new Env();
	private final Bounds tmpbnd = new Bounds();
	private final PaperColorsMap tmppc = new PaperColorsMap();
	
	
	/*******************************************************************************
	 * 定数
	 ******************************************************************************/

	private static final int STEPBY = 10;
	
	private static final int SEP_WIDTH = 10;
	private static final int SEP_WIDTH_NARROW = 2;
	private static final int SEP_HEIGHT = 5;
	private static final int SEP_HEIGHT_NARROW = 2;
	
	//private static final int PARTS_WIDTH = 900;
	private static final int PARTS_HEIGHT = 30;
	
	private static final int LABEL_WIDTH = 125;
	private static final int ITEM_WIDTH = 250;
	private static final int TITLE_WIDTH = LABEL_WIDTH+ITEM_WIDTH;
	
	private static final int BUTTON_WIDTH = 100;
	
	private static final int PANEL_WIDTH = LABEL_WIDTH+ITEM_WIDTH+SEP_WIDTH*2;
	private static int PANEL_HEIGHT = 0;
	
	private static final int TABLE_WIDTH = PANEL_WIDTH-SEP_WIDTH*2;
	private static final int TABLE_HEIGHT = 260;
	
	private static final int STYLETABLE_HEIGHT = 80;
	
	private static final int TIMEBAR_WIDTH = TABLE_WIDTH/4;
	
	
	/*******************************************************************************
	 * 部品
	 ******************************************************************************/
	
	private JPanel jPanel = null;
	
	private JTabbedPane jTabbedPane = null;
	
	private JPanel jPanel_buttons = null;
	private JButton jButton_preview = null;
	private JButton jButton_update = null;
	private JButton jButton_cancel = null;
	
	// ジャンル別背景色のタブ
	private JPanel jPanel_pColors = null;
	private JScrollPane jScrollPane_list = null;
	private JNETable jTable_list = null;
	private DefaultTableModel jTableModel_list = null;
	private JCCLabel jLabel_timebar = null;
	private JCCLabel jLabel_timebar2 = null;
	private JCCLabel jLabel_timebar3 = null;
	private JCCLabel jLabel_timebar4 = null;
	private JCheckBoxPanel jCBP_highlight = null;
	private JCCLabel jLabel_highlight = null;
	
	// フォント設定のタブ
	private JPanel jPanel_fonts = null;
	private JCheckBoxPanel jCBP_showStart = null;
	private JComboBoxPanel jCBX_titleFont = null;
	private JSliderPanel jSP_titleFontSize = null;
	private JCCLabel jLabel_titleFontColor = null;
	private JScrollPane jScrollPane_titleFontStyle = null;
	private JCheckBoxPanel jCBP_showDetail = null;
	private JComboBoxPanel jCBX_detailFont = null;
	private JSliderPanel jSP_detailFontSize = null;
	private JCCLabel jLabel_detailFontColor = null;
	private JScrollPane jScrollPane_detailFontStyle = null;
	private JSliderPanel jSP_detailTab = null;
	private JComboBoxPanel jCBX_aaMode = null;
	
	// サイズのタブ
	private JPanel jPanel_bounds = null;
	private JSliderPanel jSP_width = null;
	private JSliderPanel jSP_height = null;
	private JSliderPanel jSP_timebarPosition = null;
	private JCCLabel jLabel_execon = null;
	private JCCLabel jLabel_execoff = null;
	private JCCLabel jLabel_pickup = null;
	private JCCLabel jLabel_pickupFont = null;
	private JCCLabel jLabel_matchedBorderColor = null;
	private JSliderPanel jSP_matchedBorderThickness = null;
	//private JCheckBoxPanel jCBP_lightProgramView = null;
	
	
	/*******************************************************************************
	 * コンストラクタ
	 ******************************************************************************/
	
	public AbsPaperColorsDialog() {
		//
		super();
		//
		this.setModal(true);
		//
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				doCancel();
			}
		});

		this.setContentPane(getJPanel());
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);	// 閉じるときはキャンセルボタンを使ってクレ
		
		this.pack();
		this.setResizable(false);
		
		this.setTitle("新聞形式の表示設定");
	}

	
	/*******************************************************************************
	 * アクション
	 ******************************************************************************/
	
	private void doPreview() {
		getColors(tmpenv,tmppc);
		getFonts(tmpenv);
		getBounds(tmpenv,tmpbnd);
		
		updatePaperColors(tmpenv,tmppc);
		updatePaperFonts(tmpenv);
		updatePaperBounds(tmpenv,tmpbnd);
		
		updatePaperRepaint();
	}
	
	private void doUpdate() {
		getColors(origenv,origpc);
		getFonts(origenv);
		getBounds(origenv,origbnd);
		
		updatePaperFonts(origenv);
		updatePaperColors(origenv,origpc);
		updatePaperBounds(origenv,origbnd);
		
		updatePaperRepaint();
		
		origpc.save();
		origenv.save();
		origbnd.save();

		setVisible(false);
	}
	
	@Override
	protected void doCancel() {
		updatePaperColors(origenv,origpc);
		updatePaperFonts(origenv);
		updatePaperBounds(origenv,origbnd);
		
		updatePaperRepaint();
		
		setVisible(false);
	}

	/*
	 * メソッド
	 */

	//
	@Override
	public void setVisible(boolean b) {
		if (b) {
			if (debug) {
				for ( ProgGenre key : origpc.keySet() ) {
					System.err.println("[DEBUG] before orig papercolorsmap "+key+"="+origpc.get(key));
				}
			}
			FieldUtils.deepCopy(tmpenv, origenv);
			FieldUtils.deepCopy(tmpbnd, origbnd);
			FieldUtils.deepCopy(tmppc, origpc);
			setColors();
			setFonts();
			setBounds();
		}
		else {
			if (debug) {
				for ( ProgGenre key : origpc.keySet() ) {
					System.err.println("[DEBUG] after orig papercolorsmap "+key+"="+origpc.get(key));
				}
			}
		}
		super.setVisible(b);
	}
	
	//
	private void getColors(Env toe, PaperColorsMap top) {
		for ( int row=0; row<jTable_list.getRowCount(); row++ ) {
			TVProgram.ProgGenre g = (ProgGenre) jTable_list.getValueAt(row, 0);
			Color c = CommonUtils.str2color((String) jTable_list.getValueAt(row, 1));
			top.put(g, c);
		}
		toe.setTimebarColor(jLabel_timebar.getChoosed());
		toe.setTimebarColor2(jLabel_timebar2.getChoosed());
		toe.setTimebarColor3(jLabel_timebar3.getChoosed());
		toe.setTimebarColor4(jLabel_timebar4.getChoosed());
		toe.setEnableHighlight(jCBP_highlight.isSelected());
		toe.setHighlightColor(jLabel_highlight.getChoosed());
	}
	
	//
	private void getFonts(Env to) {
		to.setShowStart(jCBP_showStart.isSelected());
		to.setTitleFont((String) jCBX_titleFont.getSelectedItem());
		to.setTitleFontSize(jSP_titleFontSize.getValue());
		to.setTitleFontColor(jLabel_titleFontColor.getChoosed());
		to.setTitleFontStyle(getFontStyles((JNETable) jScrollPane_titleFontStyle.getViewport().getView()));
		to.setShowDetail(jCBP_showDetail.isSelected());
		to.setDetailFont((String) jCBX_detailFont.getSelectedItem());
		to.setDetailFontSize(jSP_detailFontSize.getValue());
		to.setDetailFontColor(jLabel_detailFontColor.getChoosed());
		to.setDetailFontStyle(getFontStyles((JNETable) jScrollPane_detailFontStyle.getViewport().getView()));
		to.setDetailTab(jSP_detailTab.getValue());
		to.setPaperAAMode((AAMode) jCBX_aaMode.getSelectedItem());
	}
	private ArrayList<JTXTButton.FontStyle> getFontStyles(JNETable jt) {
		ArrayList<JTXTButton.FontStyle> fsa = new ArrayList<JTXTButton.FontStyle>();
		for ( int row=0; row<jt.getRowCount(); row++ ) {
			if ( (Boolean)jt.getValueAt(row, 0) ) {
				fsa.add((FontStyle) jt.getValueAt(row, 1));
			}
		}
		return fsa;
	}
	
	//
	private void getBounds(Env toe, Bounds tob) {
		tob.setBangumiColumnWidth(jSP_width.getValue());
		tob.setPaperHeightMultiplier(jSP_height.getValue()*(float)STEPBY/(float)60);
		tob.setTimelinePosition(jSP_timebarPosition.getValue());
		toe.setExecOnFontColor(jLabel_execon.getChoosed());
		toe.setExecOffFontColor(jLabel_execoff.getChoosed());
		toe.setPickedColor(jLabel_pickup.getChoosed());
		toe.setPickedFontColor(jLabel_pickupFont.getChoosed());
		toe.setMatchedBorderColor(jLabel_matchedBorderColor.getChoosed());
		toe.setMatchedBorderThickness(jSP_matchedBorderThickness.getValue());
		//
		tob.setShowMatchedBorder(origbnd.getShowMatchedBorder());
	}
	
	
	
	/*******************************************************************************
	 * コンポーネント
	 ******************************************************************************/

	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new BorderLayout());
			jPanel.add(getJTabbedPane(), BorderLayout.CENTER);
			jPanel.add(getJPanel_buttons(), BorderLayout.PAGE_END);
		}
		return jPanel;
	}
	
	//
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.add(getJPanel_pColors(),"背景色",0);
			jTabbedPane.add(getJPanel_fonts(),"テキスト",1);
			jTabbedPane.add(getJPanel_bounds(),"その他",2);
		}
		return jTabbedPane;
	}
	
	//
	private JPanel getJPanel_buttons() {
		if (jPanel_buttons == null) {
			jPanel_buttons = new JPanel();

			jPanel_buttons.setLayout(new SpringLayout());
			
			int y = SEP_HEIGHT;
			int x = (PANEL_WIDTH - (BUTTON_WIDTH*3+SEP_WIDTH*2))/2;
			CommonSwingUtils.putComponentOn(jPanel_buttons, getJButton_preview("ﾌﾟﾚﾋﾞｭｰ"), BUTTON_WIDTH, PARTS_HEIGHT, x, y);
			CommonSwingUtils.putComponentOn(jPanel_buttons, getJButton_update("登録"), BUTTON_WIDTH, PARTS_HEIGHT, x+=BUTTON_WIDTH+SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_buttons, getJButton_cancel("ｷｬﾝｾﾙ"), BUTTON_WIDTH, PARTS_HEIGHT, x+=BUTTON_WIDTH+SEP_WIDTH, y);
			
			y += PARTS_HEIGHT+SEP_HEIGHT;
			
			jPanel_buttons.setPreferredSize(new Dimension(PANEL_WIDTH, y));
		}
		return jPanel_buttons;
	}
	private JButton getJButton_preview(String s) {
		if (jButton_preview == null) {
			jButton_preview = new JButton();
			jButton_preview.setText(s);
			
			jButton_preview.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doPreview();
				}
			});
		}
		return jButton_preview;
	}
	private JButton getJButton_update(String s) {
		if (jButton_update == null) {
			jButton_update = new JButton();
			jButton_update.setText(s);
			
			jButton_update.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doUpdate();
				}
			});
		}
		return jButton_update;
	}
	private JButton getJButton_cancel(String s) {
		if (jButton_cancel == null) {
			jButton_cancel = new JButton();
			jButton_cancel.setText(s);
			
			jButton_cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doCancel();
				}
			});
		}
		return jButton_cancel;
	}
	
	
	/*
	 * ジャンル別背景色のタブ 
	 */
	
	private JPanel getJPanel_pColors() {
		if (jPanel_pColors == null) {
			jPanel_pColors = new JPanel();

			jPanel_pColors.setLayout(new SpringLayout());
			
			int y = SEP_HEIGHT_NARROW;
			int x = SEP_WIDTH;
			
			CommonSwingUtils.putComponentOn(jPanel_pColors, new JTitleLabel("ジャンル別背景色"), TITLE_WIDTH, PARTS_HEIGHT, SEP_WIDTH_NARROW, y);
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_pColors, getJScrollPane_list(), TABLE_WIDTH, TABLE_HEIGHT, SEP_WIDTH, y);
			
			y += (TABLE_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_pColors, new JTitleLabel("タイムバーの色"), TITLE_WIDTH, PARTS_HEIGHT, SEP_WIDTH_NARROW, y);
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_pColors, jLabel_timebar = new JCCLabel("6～11", origenv.getTimebarColor(),true,this,ccwin), TIMEBAR_WIDTH, PARTS_HEIGHT, x, y);
			CommonSwingUtils.putComponentOn(jPanel_pColors, jLabel_timebar2 = new JCCLabel("12～17", origenv.getTimebarColor2(),true,this,ccwin), TIMEBAR_WIDTH, PARTS_HEIGHT, x+=TIMEBAR_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_pColors, jLabel_timebar3 = new JCCLabel("18～23", origenv.getTimebarColor3(),true,this,ccwin), TIMEBAR_WIDTH, PARTS_HEIGHT, x+=TIMEBAR_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_pColors, jLabel_timebar4 = new JCCLabel("24～5", origenv.getTimebarColor4(),true,this,ccwin), TIMEBAR_WIDTH, PARTS_HEIGHT, x+=TIMEBAR_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_pColors, new JTitleLabel("マウスオーバー時のハイライト色"), TITLE_WIDTH, PARTS_HEIGHT, SEP_WIDTH_NARROW, y);
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_pColors, jCBP_highlight = new JCheckBoxPanel("有効",LABEL_WIDTH/2), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_pColors, jLabel_highlight = new JCCLabel("ハイライト",origenv.getHighlightColor(),true,this,ccwin), ITEM_WIDTH, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT*2);
			
			if (PANEL_HEIGHT < y) PANEL_HEIGHT = y;
			
			jPanel_pColors.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		}
		return jPanel_pColors;
	}
	private void setColors() {
		//
		for (int i=jTableModel_list.getRowCount()-1; i>=0; i--) {
			jTableModel_list.removeRow(i);
		}
		for (TVProgram.ProgGenre g : TVProgram.ProgGenre.values()) {
			Object[] data = {
					g,
					CommonSwingUtils.getColoredString(origpc.get(g),"色見本")
			};
			jTableModel_list.addRow(data);
		}
		jTable_list.updateUI();
		//
		jLabel_timebar.setChoosed(origenv.getTimebarColor());
		jLabel_timebar2.setChoosed(origenv.getTimebarColor2());
		jLabel_timebar3.setChoosed(origenv.getTimebarColor3());
		jLabel_timebar4.setChoosed(origenv.getTimebarColor4());
		jCBP_highlight.setSelected(origenv.getEnableHighlight());
		jLabel_highlight.setChoosed(origenv.getHighlightColor());
	}
	
	private JScrollPane getJScrollPane_list() {
		if (jScrollPane_list == null) {
			jScrollPane_list = new JScrollPane();
			jScrollPane_list.setViewportView(getJTable_list());
			jScrollPane_list.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			jScrollPane_list.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return(jScrollPane_list);
	}
	private JNETable getJTable_list() {
		if (jTable_list == null) {
			//
			String[] colname = {"ジャンル", "色"};
			int[] colwidth = {TABLE_WIDTH-100,100};
			//
			jTableModel_list = new DefaultTableModel(colname, 0);
			jTable_list = new JNETable(jTableModel_list, false);
			jTable_list.setAutoResizeMode(JNETable.AUTO_RESIZE_OFF);
			DefaultTableColumnModel columnModel = (DefaultTableColumnModel)jTable_list.getColumnModel();
			TableColumn column = null;
			for (int i = 0 ; i < columnModel.getColumnCount() ; i++){
				column = columnModel.getColumn(i);
				column.setPreferredWidth(colwidth[i]);
			}
			//
			TableCellRenderer colorCellRenderer = new VWColorCellRenderer();
			jTable_list.getColumn("色").setCellRenderer(colorCellRenderer);
			//
			final JDialog jd = this;
			jTable_list.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isLeftMouseButton(e)) {
						//
						JTable t = (JTable) e.getSource();
						Point p = e.getPoint();
						
						int col = t.convertColumnIndexToModel(t.columnAtPoint(p));
						if (col == 1) {
							int row = t.convertRowIndexToModel(t.rowAtPoint(p));
							
							ccwin.setColor(CommonUtils.str2color((String) t.getValueAt(row,1)));
							CommonSwingUtils.setLocationCenter(jd,ccwin);
							ccwin.setVisible(true);
							
							if (ccwin.getSelectedColor() != null ) {
								//
								tmppc.put((TVProgram.ProgGenre) t.getValueAt(row,0), ccwin.getSelectedColor());
								//
								t.setValueAt(CommonSwingUtils.getColoredString(ccwin.getSelectedColor(),"色見本"), row, 1);
							}
						}
					}
				}
			});
		}
		return(jTable_list);
	}
	
	
	
	/*
	 * フォントのタブ 
	 */
	
	/**
	 * フォントの選択肢を設定
	 */
	public void setFontList(VWFont vwfont) {
		jCBX_titleFont.removeAllItems();
		jCBX_detailFont.removeAllItems();
		for ( String fn : vwfont.getNames() ) {
			jCBX_titleFont.addItem(fn);
			jCBX_detailFont.addItem(fn);
			
			//if (debug) System.err.println("[DEBUG] font name="+fn);
		}
	}
	private JPanel getJPanel_fonts() {
		if (jPanel_fonts == null) {
			jPanel_fonts = new JPanel();

			jPanel_fonts.setLayout(new SpringLayout());
			
			int y = SEP_HEIGHT_NARROW;
			
			CommonSwingUtils.putComponentOn(jPanel_fonts, new JTitleLabel("開始時刻欄の設定"), TITLE_WIDTH, PARTS_HEIGHT, SEP_HEIGHT_NARROW, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_fonts, jCBP_showStart = new JCheckBoxPanel("表示する",LABEL_WIDTH), TITLE_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			//jCBP_showStart.addActionListener(fal);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_fonts, new JTitleLabel("番組名のフォント設定"), TITLE_WIDTH, PARTS_HEIGHT, SEP_WIDTH_NARROW, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_fonts, jCBX_titleFont = new JComboBoxPanel("フォント",LABEL_WIDTH,ITEM_WIDTH,true), LABEL_WIDTH+ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_fonts, jSP_titleFontSize = new JSliderPanel("サイズ",LABEL_WIDTH,6,24,ITEM_WIDTH), LABEL_WIDTH+ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_fonts, new JLabel("文字色"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_fonts, jLabel_titleFontColor = new JCCLabel("番組名", origenv.getTitleFontColor(),false,this,ccwin), ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH+LABEL_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_fonts, new JLabel("スタイル"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_fonts, jScrollPane_titleFontStyle = getJScrollPane_fontstyle(), ITEM_WIDTH, STYLETABLE_HEIGHT, SEP_WIDTH+LABEL_WIDTH, y);
			
			y += (STYLETABLE_HEIGHT+10);
			CommonSwingUtils.putComponentOn(jPanel_fonts, new JTitleLabel("番組詳細のフォント設定"), TITLE_WIDTH, PARTS_HEIGHT, SEP_WIDTH_NARROW, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_fonts, jCBP_showDetail = new JCheckBoxPanel("表示する",LABEL_WIDTH), TITLE_WIDTH+ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_fonts, jCBX_detailFont = new JComboBoxPanel("フォント",LABEL_WIDTH,ITEM_WIDTH,true), LABEL_WIDTH+ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_fonts, jSP_detailFontSize = new JSliderPanel("サイズ",LABEL_WIDTH,6,24,ITEM_WIDTH), LABEL_WIDTH+ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_fonts, new JLabel("文字色"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_fonts, jLabel_detailFontColor = new JCCLabel("番組詳細", origenv.getDetailFontColor(),false,this,ccwin), ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH+LABEL_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_fonts, new JLabel("スタイル"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_fonts, jScrollPane_detailFontStyle = getJScrollPane_fontstyle(), ITEM_WIDTH, STYLETABLE_HEIGHT, SEP_WIDTH+LABEL_WIDTH, y);
			
			y += (STYLETABLE_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_fonts, jSP_detailTab = new JSliderPanel("左余白",LABEL_WIDTH,0,24,ITEM_WIDTH), LABEL_WIDTH+ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT*3);
			
			if (PANEL_HEIGHT < y) PANEL_HEIGHT = y;
			
			jPanel_fonts.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		}
		return jPanel_fonts;
	}
	private void setFonts() {
		//
		jCBP_showStart.setSelected(origenv.getShowStart());
		//
		if ( ! origenv.getTitleFont().equals("") ) {
			jCBX_titleFont.setSelectedItem(origenv.getTitleFont());
		}
		else if ( ! origenv.getFontName().equals("") ) {
			jCBX_titleFont.setSelectedItem(origenv.getFontName());
		}
		jSP_titleFontSize.setValue(origenv.getTitleFontSize());
		jLabel_titleFontColor.setChoosed(origenv.getTitleFontColor());
		setFontStyles((JNETable) jScrollPane_titleFontStyle.getViewport().getView(), origenv.getTitleFontStyle());
		//
		jCBP_showDetail.setSelected(origenv.getShowDetail());
		if ( ! origenv.getDetailFont().equals("") ) {
			jCBX_detailFont.setSelectedItem(origenv.getDetailFont());
		}
		else if ( ! origenv.getFontName().equals("") ) {
			jCBX_detailFont.setSelectedItem(origenv.getFontName());
		}
		jSP_detailFontSize.setValue(origenv.getDetailFontSize());
		jLabel_detailFontColor.setChoosed(origenv.getDetailFontColor());
		setFontStyles((JNETable) jScrollPane_detailFontStyle.getViewport().getView(), origenv.getDetailFontStyle());
		jSP_detailTab.setValue(origenv.getDetailTab());
		jCBX_aaMode.setSelectedItem(origenv.getPaperAAMode());
	}
	private void setFontStyles(JNETable jt, ArrayList<JTXTButton.FontStyle> fsa) {
		for ( int row=0; row<jt.getRowCount(); row++ ) {
			jt.setValueAt(false, row, 0);
			for ( JTXTButton.FontStyle fs : fsa ) {
				if ( fs == jt.getValueAt(row, 1) ) {
					jt.setValueAt(true, row, 0);
					break;
				}
			}
		}
	}
	
	private JScrollPane getJScrollPane_fontstyle() {
		JScrollPane jScrollPane = new JScrollPane();
		jScrollPane.setViewportView(getJTable_fontstyle());
		jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		return jScrollPane;
	}
	private JNETable getJTable_fontstyle() {
		
		// ヘッダの設定
		String[] colname = {"ﾁｪｯｸ", "スタイル"};
		int[] colwidth = {50,ITEM_WIDTH-50};
			
		//
		DefaultTableModel model = new DefaultTableModel(colname, 0);
		JNETable jTable = new JNETable(model, false) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
					return (column == 0);
			}
		};
		jTable.setAutoResizeMode(JNETable.AUTO_RESIZE_OFF);
		DefaultTableColumnModel columnModel = (DefaultTableColumnModel)jTable.getColumnModel();
		TableColumn column = null;
		for (int i = 0 ; i < columnModel.getColumnCount() ; i++){
			column = columnModel.getColumn(i);
			column.setPreferredWidth(colwidth[i]);
		}
		
		// にゃーん
		jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
   
		// エディタに手を入れる
		DefaultCellEditor editor = new DefaultCellEditor(new JCheckBox() {

			private static final long serialVersionUID = 1L;

			@Override
			public int getHorizontalAlignment() {
				return JCheckBox.CENTER;
			}
		});
		jTable.getColumn("ﾁｪｯｸ").setCellEditor(editor);
		// レンダラに手を入れる
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				//
				JCheckBox cBox = new JCheckBox();
				cBox.setHorizontalAlignment(JCheckBox.CENTER);
				//
				Boolean b = (Boolean)value;
				cBox.setSelected(b.booleanValue());
				//
				if (isSelected) {
					cBox.setBackground(table.getSelectionBackground());
				}
				else {
					cBox.setBackground(table.getBackground());
				}
				return cBox;
			}
		};
		jTable.getColumn("ﾁｪｯｸ").setCellRenderer(renderer);
		
		//
		for ( JTXTButton.FontStyle fs : JTXTButton.FontStyle.values() ) {
			Object[] data = { false,fs };
			model.addRow(data);
		}
		return jTable;
	}
	
	/*
	 * サイズのタブ 
	 */
	
	private JPanel getJPanel_bounds() {
		if (jPanel_bounds == null) {
			jPanel_bounds = new JPanel();

			jPanel_bounds.setLayout(new SpringLayout());
			
			int y = SEP_HEIGHT_NARROW;
			
			CommonSwingUtils.putComponentOn(jPanel_bounds, new JTitleLabel("サイズの設定"), TITLE_WIDTH, PARTS_HEIGHT, SEP_WIDTH_NARROW, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_bounds, jSP_width = new JSliderPanel("幅",LABEL_WIDTH,50,300,ITEM_WIDTH), LABEL_WIDTH+ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_bounds, jSP_height = new JSliderPanel("高さ(pt/H)",LABEL_WIDTH,30,600,STEPBY,ITEM_WIDTH), LABEL_WIDTH+ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_bounds, jSP_timebarPosition = new JSliderPanel("現在時刻線(分)",LABEL_WIDTH,1,180,ITEM_WIDTH), LABEL_WIDTH+ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);

			y += (PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_bounds, new JTitleLabel("予約枠の設定"), TITLE_WIDTH, PARTS_HEIGHT, SEP_WIDTH_NARROW, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_bounds, jLabel_execon = new JCCLabel("実行ONの文字色",origenv.getExecOnFontColor(),false,this,ccwin), ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH+LABEL_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_bounds, jLabel_execoff = new JCCLabel("実行OFFの文字色",origenv.getExecOffFontColor(),false,this,ccwin), ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH+LABEL_WIDTH, y);

			y += (PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_bounds, new JTitleLabel("ピックアップ枠の設定"), TITLE_WIDTH, PARTS_HEIGHT, SEP_WIDTH_NARROW, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_bounds, jLabel_pickup = new JCCLabel("ピックアップの枠色",origenv.getPickedColor(),true,this,ccwin), ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH+LABEL_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_bounds, jLabel_pickupFont = new JCCLabel("ピックアップの文字色",origenv.getPickedFontColor(),false,this,ccwin), ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH+LABEL_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_bounds, new JTitleLabel("予約待機枠の設定"), TITLE_WIDTH, PARTS_HEIGHT, SEP_WIDTH_NARROW, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_bounds, jSP_matchedBorderThickness = new JSliderPanel("太さ",LABEL_WIDTH,1,16,ITEM_WIDTH), LABEL_WIDTH+ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_bounds, jLabel_matchedBorderColor = new JCCLabel("予約待機の枠色",origenv.getMatchedBorderColor(),true,this,ccwin), ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH+LABEL_WIDTH, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_bounds, new JTitleLabel("フォントのアンチエイリアス設定"), TITLE_WIDTH, PARTS_HEIGHT, SEP_WIDTH_NARROW, y);
			
			y += (PARTS_HEIGHT+SEP_HEIGHT_NARROW);
			CommonSwingUtils.putComponentOn(jPanel_bounds, jCBX_aaMode = new JComboBoxPanel("アンチエイリアス",LABEL_WIDTH,ITEM_WIDTH,true), LABEL_WIDTH+ITEM_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			for ( AAMode aam : AAMode.values() ) {
				jCBX_aaMode.addItem(aam);
			}
			
			y += (PARTS_HEIGHT+SEP_HEIGHT*2);
			
			if (PANEL_HEIGHT < y) PANEL_HEIGHT = y;
			
			jPanel_bounds.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		}
		return jPanel_bounds;
	}

	private void setBounds() {
		jSP_width.setValue(origbnd.getBangumiColumnWidth());
		jSP_height.setValue(Math.round(origbnd.getPaperHeightMultiplier()*(float)60/(float)STEPBY));
		jSP_timebarPosition.setValue(origbnd.getTimelinePosition());
		jLabel_execon.setChoosed(origenv.getExecOnFontColor());
		jLabel_execon.setBackground(Color.RED);
		jLabel_execoff.setChoosed(origenv.getExecOffFontColor());
		jLabel_execoff.setBackground(Color.RED);
		jLabel_pickup.setChoosed(origenv.getPickedColor());
		jLabel_pickupFont.setChoosed(origenv.getPickedFontColor());
		jLabel_pickupFont.setBackground(Color.RED);
		jLabel_matchedBorderColor.setChoosed(origenv.getMatchedBorderColor());
		jSP_matchedBorderThickness.setValue(origenv.getMatchedBorderThickness());
		/*
		if ( ! origenv.getShowStart() && ! origenv.getShowDetail() ) {
			jCBP_lightProgramView.setSelected(true);
		}
		else {
			jCBP_lightProgramView.setSelected(false);
		}
		*/
	}
	
	/*******************************************************************************
	 * 独自部品
	 ******************************************************************************/
	
	private class JTitleLabel extends JLabel {
		
		private static final long serialVersionUID = 1L;

		public JTitleLabel(String s) {
			super(s);
			this.setForeground(Color.RED);
			//this.setFont(this.getFont().deriveFont(this.getFont().getStyle()|Font.BOLD));
		}
	}
	
}
