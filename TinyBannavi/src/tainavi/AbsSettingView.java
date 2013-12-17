package tainavi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import tainavi.Env.DblClkCmd;
import tainavi.Env.SnapshotFmt;
import tainavi.Env.UpdateOn;
import tainavi.TVProgram.ProgOption;


/**
 * 各種設定タブのクラス
 * @since 3.15.4β　{@link Viewer}から分離
 */
public abstract class AbsSettingView extends JScrollPane {

	private static final long serialVersionUID = 1L;

	public static String getViewName() { return "各種設定"; } 

	public void setDebug(boolean b) { debug = b; }
	private static boolean debug = false;

	/*******************************************************************************
	 * 抽象メソッド
	 ******************************************************************************/
	
	protected abstract Env getEnv();
	protected abstract ClipboardInfoList getCbItemEnv();
	
	protected abstract VWLookAndFeel getLAFEnv();
	protected abstract VWFont getFontEnv();
	
	protected abstract StatusWindow getStWin(); 
	protected abstract StatusTextArea getMWin();
	
	protected abstract Component getParentComponent();
	protected abstract VWColorChooserDialog getCcWin(); 
	
	protected abstract void lafChanged(String lafname);
	protected abstract void fontChanged(String fn, int fontSize);
	protected abstract void setEnv(boolean reload_prog);

	/*******************************************************************************
	 * 呼び出し元から引き継いだもの
	 ******************************************************************************/
	
	// オブジェクト
	private final Env env = getEnv();
	private final ClipboardInfoList cbitems = getCbItemEnv();
	
	private final StatusWindow StWin = getStWin();			// これは起動時に作成されたまま変更されないオブジェクト
	private final StatusTextArea MWin = getMWin();			// これは起動時に作成されたまま変更されないオブジェクト
	
	private final Component parent = getParentComponent();	// これは起動時に作成されたまま変更されないオブジェクト
	private final VWColorChooserDialog ccwin = getCcWin();	// これは起動時に作成されたまま変更されないオブジェクト
	
	// 雑多なメソッド
	private void StdAppendMessage(String message) { System.out.println(message); }
	//private void StdAppendError(String message) { System.err.println(message); }
	private void StWinSetVisible(boolean b) { StWin.setVisible(b); }
	private void StWinSetLocationCenter(Component frame) { CommonSwingUtils.setLocationCenter(frame, (VWStatusWindow)StWin); }
	
	/*******************************************************************************
	 * 定数
	 ******************************************************************************/

	// レイアウト関連

	private static final int PARTS_WIDTH = 900;
	private static final int PARTS_HEIGHT = 30;
	private static final int SEP_WIDTH = 10;
	private static final int SEP_HEIGHT = 10;
	private static final int SEP_HEIGHT_NALLOW = 5;
	private static final int BLOCK_SEP_HEIGHT = 75;
	
	private static final int LABEL_WIDTH = 350;
	private static final int CCLABEL_WIDTH = 250;
	private static final int DESCRIPTION_WIDTH = LABEL_WIDTH+PARTS_WIDTH;

	private static final int UPDATE_WIDTH = 250;
	private static final int HINT_WIDTH = 700;
	
	private static final int PANEL_WIDTH = LABEL_WIDTH+PARTS_WIDTH+SEP_WIDTH*3;
	
	//
	
	private static final Color NOTICEMSG_COLOR = new Color(0,153,153);
	// テキスト
	
	private static final String TEXT_HINT =
			"各項目の詳細はプロジェクトWikiに説明があります（http://sourceforge.jp/projects/tainavi/wiki/）。"+
			"ツールバーのヘルプアイコンをクリックするとブラウザで開きます。";

	private static final String PARER_REDRAW_NORMAL = "通常";
	private static final String PARER_REDRAW_CACHE = "再描画時に一週間分をまとめて描画して日付切り替えを高速化する（メモリ消費大）";
	private static final String PARER_REDRAW_PAGER = "ページャーを有効して一度に描画する放送局数を抑える（メモリ消費抑制、切替時間短縮）";

	// ログ関連

	private static final String MSGID = "["+getViewName()+"] ";
	private static final String ERRID = "[ERROR]"+MSGID;
	private static final String DBGID = "[DEBUG]"+MSGID;
	
	/*******************************************************************************
	 * 部品
	 ******************************************************************************/
	
	// コンポーネント
	
	private JPanel jPanel_setting = null;
	
	private JPanel jPanel_update = null;
	private JButton jButton_update = null;
	
	// リスト形式
	private JCheckBoxPanel jCBP_disableFazzySearch = null;
	private JCheckBoxPanel jCBP_disableFazzySearchReverse = null;
	private JSliderPanel jSP_defaultFazzyThreshold = null;
	private JCheckBoxPanel jCBP_syoboFilterByCenters = null;
	private JCheckBoxPanel jCBP_displayPassedEntry = null;
	private JCheckBoxPanel jCBP_showRsvPickup = null;
	private JCheckBoxPanel jCBP_showRsvUra = null;
	private JCheckBoxPanel jCBP_rsvdLineEnhance = null;
	private JLabel jLabel_rsvdLineColor = null;
	private JCCLabel jCCL_rsvdLineColor = null;
	private JLabel jLabel_pickedLineColor = null;
	private JCCLabel jCCL_pickedLineColor = null;
	private JCCLabel jCCL_matchedKeywordColor = null;
	private JCheckBoxPanel jCBP_currentLineEnhance = null;
	private JLabel jLabel_currentLineColor = null;
	private JCCLabel jCCL_currentLineColor = null;
	private JSliderPanel jSP_currentAfter = null;
	private JSliderPanel jSP_currentBefore = null;
	private JCheckBoxPanel jCBP_showWarnDialog = null;
	private JCheckBoxPanel jCBP_splitMarkAndTitle = null;
	private JCheckBoxPanel jCBP_showDetailOnList = null;
	private JSliderPanel jSP_rsvTargets = null;
	private JCheckBoxPanel jCBP_rowHeaderVisible = null;
	private JComboBoxPanel jCBX_dblClkCmd = null;
	private JSliderPanel jSP_searchResultMax = null;
	private JSliderPanel jSP_searchResultBufferMax = null;
	
	// 新聞形式
	private JRadioButtonPanel jRBP_getPaperRedrawType = null;
	private JSliderPanel jSP_centerPerPage = null;
	private JCheckBoxPanel jCBP_allPageSnapshot = null;
	private JCheckBoxPanel jCBP_tooltipEnable = null;
	private JSliderPanel jSP_tooltipInitialDelay = null;
	private JSliderPanel jSP_tooltipDismissDelay = null;
	private JCheckBoxPanel jCBP_timerbarScrollEnable = null;
	private JSliderPanel jSP_passedLogLimit = null;
	private JCheckBoxPanel jCBP_effectComboToPaper = null;
	private JComboBoxPanel jCBX_snapshotFmt = null;
	private JCheckBoxPanel jCBP_printSnapshot = null;
	
	// リスト・新聞共通
	private JCheckBoxPanel jCBP_displayOnlyExecOnEntry = null;
	private JCheckBoxPanel jCBP_displayPassedReserve = null;
	private JCheckBoxPanel jCBP_showOnlyNonrepeated = null;
	private JCheckBoxPanel jCBP_adjLateNight = null;
	private JCheckBoxPanel jCBP_rootNodeVisible = null;
	private JCheckBoxPanel jCBP_syncTreeWidth = null;
	private JCheckBoxPanel jCBP_shortExtMark = null;
	private JLabel jLabel_showmarks = null;
	private JScrollPane jScrollPane_showmarks = null;
	private JNETable jTable_showmarks = null;
	private JLabel jLabel_clipboard = null;
	private JScrollPane jScrollPane_clipboard = null;
	private JNETable jTable_clipboard = null;
	private JButton jButton_clipboard_up = null;
	private JButton jButton_clipboard_down = null;
	private JLabel jLabel_menuitem = null;
	private JTextFieldWithPopup jTextField_mikey = null;
	private JTextFieldWithPopup jTextField_mival = null;
	private JScrollPane jScrollPane_mitable = null;
	private JNETable jTable_mitable = null;
	private JButton jButton_miadd = null;
	private JButton jButton_midel = null;
	private JButton jButton_miup = null;
	private JButton jButton_midown = null;
	
	// Web番組表対応
	private JCheckBoxPanel jCBP_continueTomorrow = null;
	private JSliderPanel jSP_cacheTimeLimit = null;
	private JComboBoxPanel jCBX_shutdownCmd = null;
	private JCheckBoxPanel jCBP_expandTo8 = null;
	//private JCheckBoxPanel jCBP_useDetailCache = null;
	private JCheckBoxPanel jCBP_autoEventIdComplete = null;
	private JCheckBoxPanel jCBP_splitEpno = null;
	private JCheckBoxPanel jCBP_traceOnlyTitle = null;
	private JCheckBoxPanel jCBP_fixTitle = null;
	private JLabel jLabel_ngword = null;
	private JTextFieldWithPopup jTextField_ngword = null;
	private JLabel jLabel_userAgent = null;
	private JTextFieldWithPopup jTextField_userAgent = null;
	private JCheckBoxPanel jCBP_useProxy = null;
	private JLabel jLabel_proxy = null;
	private JTextFieldWithPopup jTextField_proxyAddr = null;
	private JTextFieldWithPopup jTextField_proxyPort = null;
	private JCheckBoxPanel jCBP_useSyobocal = null;
	private JCheckBoxPanel jCBP_historyOnlyUpdateOnce = null;
	private JCheckBoxPanel jCBP_usePassedProgram = null;
	private JSliderPanel jSP_prepPassedProgramCount = null;
	
	// レコーダ対応
	private JRadioButtonPanel jRBP_getRdReserveDetails = null;
	private JRadioButtonPanel jRBP_getRdAutoReserves = null;
	private JRadioButtonPanel jRBP_getRdRecorded = null;
	private JComboBoxPanel jCBX_recordedSaveScope = null;
	
	// 予約
	private JSliderPanel jSP_spoex_extend = null;
	private JRadioButtonPanel jRBP_overlapUp = null;
	private JRadioButtonPanel jRBP_overlapDown = null;
	private JLabel jLabel_autoFolderSelect = null;
	private JCheckBox jCheckBox_autoFolderSelect = null;
	private JLabel jLabel_enableCHAVsetting = null;
	private JCheckBox jCheckBox_enableCHAVsetting = null;
	private JSliderPanel jSP_rangeLikeRsv = null;
	private JCheckBoxPanel jCBP_givePriorityToReserved = null;
	private JCheckBoxPanel jCBP_givePriorityToReservedTitle = null;
	private JCheckBoxPanel jCBP_adjoiningNotRepetition = null;
	private JCheckBoxPanel jCBP_rsv_showallite = null;
	private JLabel jLabel_rsv_itecolor = null;
	private JCCLabel jCCL_rsv_itecolor = null;
	private JLabel jLabel_rsv_tunshortcolor = null;
	private JCCLabel jCCL_rsv_tunshortcolor = null;
	private JLabel jLabel_rsv_recedcolor = null;
	private JCCLabel jCCL_rsv_recedcolor = null;
	private JCheckBoxPanel jCBP_useAutocomplete = null;
	
	// その他
	private JComboBoxPanel jCBX_updateMethod = null;
	private JCheckBoxPanel jCBP_disableBeep = null;
	private JCheckBoxPanel jCBP_showSysTray = null;
	private JCheckBoxPanel jCBP_hideToTray = null;
	private JCheckBoxPanel jCBP_onlyOneInstance = null;
	private JLabel jLabel_lookAndFeel = null;
	private JComboBox jComboBox_lookAndFeel = null;
	private JLabel jLabel_font = null;
	private JComboBox jComboBox_font = null;
	private JComboBox jComboBox_fontSize = null;
	private JLabel jLabel_fontSample = null;
	private JCheckBoxPanel jCBP_useGTKRC = null;
	private JCheckBoxPanel jCBP_useRundll32 = null;
	private JCheckBoxPanel jCBP_debug = null;

	private JTextAreaWithPopup jta_help = null;

	// コンポーネント以外
	
	/**
	 * 特定のコンポーネントを操作した時だけ番組表を再取得してほしい
	 */
	private boolean reload_prog_needed = false;
	
	/*******************************************************************************
	 * コンストラクタ
	 ******************************************************************************/
	
	public AbsSettingView() {
		
		super();
		
		this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.getVerticalScrollBar().setUnitIncrement(25);
		this.setColumnHeaderView(getJPanel_update());
		this.setViewportView(getJPanel_setting());
		
		setUpdateButtonEnhanced(false);
	}
	
	private JPanel getJPanel_update() {
		if (jPanel_update == null)
		{
			jPanel_update = new JPanel();
			jPanel_update.setLayout(new SpringLayout());
			
			jPanel_update.setBorder(new LineBorder(Color.GRAY));
			
			int y = SEP_HEIGHT;
			CommonSwingUtils.putComponentOn(jPanel_update, getJButton_update("更新を確定する"), UPDATE_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			
			int yz = SEP_HEIGHT/2;
			int x = UPDATE_WIDTH+50;
			CommonSwingUtils.putComponentOn(jPanel_update, getJta_help(), HINT_WIDTH, PARTS_HEIGHT+SEP_HEIGHT, x, yz);
			
			y += (PARTS_HEIGHT + SEP_HEIGHT);
			
			// 画面の全体サイズを決める
			Dimension d = new Dimension(PANEL_WIDTH,y);
			jPanel_update.setPreferredSize(d);
		}
		return jPanel_update;
	}

	/**
	 * ActionListenerはGUIの操作では動くがsetSelected()での変更では動かない<BR>
	 * ChangeListenerもItemListenerも同じ値のセットしなおしだと動作しない<BR>
	 * 以下ではFire!するために涙ぐましい努力を行っている<BR>
	 * <BR>
	 * ex.<BR>
	 * jcheckbox.setSelected( ! env.isSelected())<BR>
	 * jchackbox.addItemListener()<BR>
	 * jcheckbox.setSelected( ! jcheckbox.isSelected())<BR>
	 * <BR>
	 * …あほか！
	 */
	private JPanel getJPanel_setting() {
		if (jPanel_setting == null)
		{
			jPanel_setting = new JPanel();
			jPanel_setting.setLayout(new SpringLayout());
			
			//
			int y = SEP_HEIGHT;
			
			/*
			 * リスト形式 
			 */
			
			y+=0;
			CommonSwingUtils.putComponentOn(jPanel_setting, new JLabel("＜＜＜リスト形式＞＞＞"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);

			{
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_disableFazzySearch = new JCheckBoxPanel("番組追跡であいまい検索をしない",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jCBP_disableFazzySearch.setSelected( ! env.getDisableFazzySearch());
				// RELOADリスナー不要
				
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_disableFazzySearchReverse = new JCheckBoxPanel("┗　あいまい検索の逆引きを省略(非推奨)",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jCBP_disableFazzySearchReverse.setSelected(env.getDisableFazzySearchReverse());
				// RELOADリスナー不要

				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jSP_defaultFazzyThreshold = new JSliderPanel("┗　あいまい検索のデフォルト閾値",LABEL_WIDTH,1,99,200), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jSP_defaultFazzyThreshold.setValue(env.getDefaultFazzyThreshold());
				// RELOADリスナー不要
				
				// 連動設定（Fire!がキモイ…）
				
				jCBP_disableFazzySearch.addItemListener(al_fazzysearch);
				
				jCBP_disableFazzySearch.setSelected( ! jCBP_disableFazzySearch.isSelected());
			}

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getNoticeMsg("※あいまい検索のアルゴリズムにはバイグラムを使用しています(TraceProgram.java参照)"), DESCRIPTION_WIDTH, PARTS_HEIGHT, SEP_WIDTH*2, y);
			y+=(PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getNoticeMsg("　正順では「検索キーワード」の成分が「番組表のタイトル」にどれくらい含まれているかを判定しています。"), DESCRIPTION_WIDTH, PARTS_HEIGHT, SEP_WIDTH*2, y);
			y+=(PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getNoticeMsg("　逆順を有効にすると、正順でNG判定になった場合に前者と後者を入れ替えて再判定を行います。取りこぼしが減る場合がある反面、検索ノイズが増える場合もあります。"), DESCRIPTION_WIDTH, PARTS_HEIGHT, SEP_WIDTH*2, y);
			y+=(PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getNoticeMsg("　閾値を大きくすると判定が厳しくなります。キーワードが短いためにヒットしまくりで検索ノイズが多くなった場合に、値を大きくしてみてください。"), DESCRIPTION_WIDTH, PARTS_HEIGHT, SEP_WIDTH*2, y);
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_traceOnlyTitle = new JCheckBoxPanel("タイトル中に含まれるサブタイトルは番組追跡の対象にしない",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_traceOnlyTitle.setSelected(env.getTraceOnlyTitle());
			jCBP_traceOnlyTitle.addItemListener(IL_RELOAD_PROG_NEEDED);
		
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_syoboFilterByCenters = new JCheckBoxPanel("しょぼかるの検索結果も有効な放送局のみに絞る",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_syoboFilterByCenters.setSelected(env.getSyoboFilterByCenters());
			// RELOADリスナー不要

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_displayPassedEntry = new JCheckBoxPanel("当日の終了済み番組も表示する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_displayPassedEntry.setSelected(env.getDisplayPassedEntry());
			// RELOADリスナー不要

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_showRsvPickup = new JCheckBoxPanel("ピックアップマーク(★)を表示する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_showRsvPickup.setSelected(env.getShowRsvPickup());
			// RELOADリスナー不要

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_showRsvUra = new JCheckBoxPanel("裏番組予約マーク(■)を表示する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_showRsvUra.setSelected(env.getShowRsvUra());
			// RELOADリスナー不要

			{
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_rsvdLineEnhance = new JCheckBoxPanel("予約行の背景色を変えて強調する",LABEL_WIDTH), LABEL_WIDTH+PARTS_HEIGHT, PARTS_HEIGHT, SEP_WIDTH, y);
				jCBP_rsvdLineEnhance.setSelected( ! env.getRsvdLineEnhance());
				// RELOADリスナー不要

				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jLabel_rsvdLineColor = new JLabel("┗　予約行の背景色"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCCL_rsvdLineColor = new JCCLabel("予約行の背景色",env.getRsvdLineColor(),true,parent,ccwin), CCLABEL_WIDTH, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
				// RELOADリスナー不要

				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jLabel_pickedLineColor = new JLabel("┗　ピックアップ行の背景色"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCCL_pickedLineColor = new JCCLabel("ピックアップ行の背景色",env.getPickedLineColor(),true,parent,ccwin), CCLABEL_WIDTH, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
				// RELOADリスナー不要
				
				// 連動設定
				
				jCBP_rsvdLineEnhance.addItemListener(al_rsvdlineenhance);
				
				jCBP_rsvdLineEnhance.setSelected( ! jCBP_rsvdLineEnhance.isSelected());
			}
			
			{
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_currentLineEnhance = new JCheckBoxPanel("現在放送中の行の背景色を変えて強調する",LABEL_WIDTH), LABEL_WIDTH+PARTS_HEIGHT, PARTS_HEIGHT, SEP_WIDTH, y);
				jCBP_currentLineEnhance.setSelected( ! env.getCurrentLineEnhance());
				// RELOADリスナー不要

				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jLabel_currentLineColor = new JLabel("┗　現在放送中行の背景色"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCCL_currentLineColor = new JCCLabel("現在放送中行の背景色",env.getCurrentLineColor(),true,parent,ccwin), CCLABEL_WIDTH, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
				// RELOADリスナー不要
				
				// 連動設定
				
				jCBP_currentLineEnhance.addItemListener(al_currentlineenhance);
				
				jCBP_currentLineEnhance.setSelected( ! jCBP_currentLineEnhance.isSelected());
			}
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jSP_currentAfter = new JSliderPanel("現在放送中ノードに終了後何分までの番組を表示するか",LABEL_WIDTH,0,60,200), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jSP_currentAfter.setValue(env.getCurrentAfter()/60);
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jSP_currentBefore = new JSliderPanel("現在放送中ノードに開始前何分までの番組を表示するか",LABEL_WIDTH,0,120,200), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jSP_currentBefore.setValue(env.getCurrentBefore()/60);
			// RELOADリスナー不要
			
			//
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, new JLabel("タイトル中のキーワードにマッチした箇所の強調色"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCCL_matchedKeywordColor = new JCCLabel("強調色",env.getMatchedKeywordColor(),false,parent,ccwin), CCLABEL_WIDTH, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_showWarnDialog = new JCheckBoxPanel("キーワード削除時に確認ダイアログを表示",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_showWarnDialog.setSelected(env.getShowWarnDialog());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_splitMarkAndTitle = new JCheckBoxPanel("オプション表示を個別欄に分離表示",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_splitMarkAndTitle.setSelected(env.getSplitMarkAndTitle());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_showDetailOnList = new JCheckBoxPanel("番組詳細列を表示",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_showDetailOnList.setSelected(env.getShowDetailOnList());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_rowHeaderVisible = new JCheckBoxPanel("行番号を表示",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_rowHeaderVisible.setSelected(env.getRowHeaderVisible());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jSP_rsvTargets = new JSliderPanel("予約待機の予約番組自動選択数",LABEL_WIDTH,1,99,200), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jSP_rsvTargets.setValue(env.getRsvTargets());
			// RELOADリスナー不要

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBX_dblClkCmd = new JComboBoxPanel("ダブルクリック時の動作",LABEL_WIDTH,250,true), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			for ( DblClkCmd c : DblClkCmd.values() ) {
				jCBX_dblClkCmd.addItem(c);
			}
			jCBX_dblClkCmd.setSelectedItem(env.getDblClkCmd());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jSP_searchResultMax = new JSliderPanel("過去ログ検索件数の上限",LABEL_WIDTH,10,500,200), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jSP_searchResultMax.setValue(env.getSearchResultMax());
			// RELOADリスナー不要

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jSP_searchResultBufferMax = new JSliderPanel("過去ログ検索履歴の上限",LABEL_WIDTH,1,10,200), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jSP_searchResultBufferMax.setValue(env.getSearchResultBufferMax());
			// RELOADリスナー不要

			/*
			 * 新聞形式 
			 */
			
			y+=(PARTS_HEIGHT+BLOCK_SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, new JLabel("＜＜＜新聞形式＞＞＞"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			
			{
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				int paperredraw_h = PARTS_HEIGHT*3+SEP_HEIGHT_NALLOW*2;
				CommonSwingUtils.putComponentOn(jPanel_setting, jRBP_getPaperRedrawType = new JRadioButtonPanel("描画方式",LABEL_WIDTH), PARTS_WIDTH, paperredraw_h, SEP_WIDTH, y);
				jRBP_getPaperRedrawType.add(PARER_REDRAW_NORMAL, false);
				jRBP_getPaperRedrawType.add(PARER_REDRAW_CACHE, false);
				jRBP_getPaperRedrawType.add(PARER_REDRAW_PAGER, false);
				// RELOADリスナー不要
				
				y+=(paperredraw_h+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jSP_centerPerPage = new JSliderPanel("┗　ページあたりの放送局数",LABEL_WIDTH,1,99,200), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jSP_centerPerPage.setValue(env.getCenterPerPage());
				// RELOADリスナー不要
				
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_allPageSnapshot = new JCheckBoxPanel("┗　スナップショットを全ページ連続で実行",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jCBP_allPageSnapshot.setSelected(env.getAllPageSnapshot());
				// RELOADリスナー不要
				
				// 連動設定(1)
				jRBP_getPaperRedrawType.addItemListener(il_paperredrawtype);
				
				if ( env.isPagerEnabled() ) {
					jRBP_getPaperRedrawType.setSelectedItem(PARER_REDRAW_PAGER);
				}
				else if ( env.getDrawcacheEnable() ) {
					jRBP_getPaperRedrawType.setSelectedItem(PARER_REDRAW_CACHE);
				}
				else {
					jRBP_getPaperRedrawType.setSelectedItem(PARER_REDRAW_NORMAL);
				}
			}

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getNoticeMsg("※切り替えがおっせーよ！忍耐の限界だよ！という場合は新聞形式の設定変更（ツールバーのパレットアイコン）で「番組詳細のフォント設定＞表示する」のチェックを外してください。"), DESCRIPTION_WIDTH, PARTS_HEIGHT, SEP_WIDTH*2, y);

			{
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_tooltipEnable = new JCheckBoxPanel("番組表でツールチップを表示する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jCBP_tooltipEnable.setSelected( ! env.getTooltipEnable());
				// RELOADリスナー不要

				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jSP_tooltipInitialDelay = new JSliderPanel("┗　表示までの遅延(ミリ秒)",LABEL_WIDTH,0,3000,100,200), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jSP_tooltipInitialDelay.setValue(env.getTooltipInitialDelay());
				// RELOADリスナー不要

				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jSP_tooltipDismissDelay = new JSliderPanel("┗　消去までの遅延(ミリ秒)",LABEL_WIDTH,1000,60000,100,200), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jSP_tooltipDismissDelay.setValue(env.getTooltipDismissDelay());
				// RELOADリスナー不要
				
				// 連動設定

				jCBP_tooltipEnable.addItemListener(al_tooltipenable);
				
				jCBP_tooltipEnable.setSelected( ! jCBP_tooltipEnable.isSelected());
			}
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_timerbarScrollEnable = new JCheckBoxPanel("現在時刻線を固定し番組表側をスクロール",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_timerbarScrollEnable.setSelected(env.getTimerbarScrollEnable());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jSP_passedLogLimit = new JSliderPanel("過去ログの日付ノードの表示数",LABEL_WIDTH,7,180,200), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jSP_passedLogLimit.setValue(env.getPassedLogLimit());
			// RELOADリスナー不要

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_effectComboToPaper = new JCheckBoxPanel("レコーダコンボボックスを新聞形式でも有効に",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_effectComboToPaper.setSelected(env.getEffectComboToPaper());
			// RELOADリスナー不要
			
			
			/*
			 * リスト・新聞形式共通 
			 */
			
			y+=(PARTS_HEIGHT+BLOCK_SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, new JLabel("＜＜＜リスト・新聞形式共通＞＞＞"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_displayOnlyExecOnEntry = new JCheckBoxPanel("実行ONの予約のみ予約マークを表示する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_displayOnlyExecOnEntry.setSelected(env.getDisplayOnlyExecOnEntry());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_displayPassedReserve = new JCheckBoxPanel("当日の終了済み予約も表示する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_displayPassedReserve.setSelected(env.getDisplayPassedReserve());
			// RELOADリスナー不要

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_showOnlyNonrepeated = new JCheckBoxPanel("リピート放送と判定されたら番組追跡に表示しない",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_showOnlyNonrepeated.setSelected(env.getShowOnlyNonrepeated());
			// RELOADリスナー不要

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_adjLateNight = new JCheckBoxPanel("【RD】深夜の帯予約を前にずらす(火～土→月～金)",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_adjLateNight.setSelected(env.getAdjLateNight());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getNoticeMsg("※月～金26:00の帯番組は、実際には火～土AM2:00に放送されますので鯛ナビでもそのように帯予約を処理しています。"), DESCRIPTION_WIDTH, PARTS_HEIGHT, SEP_WIDTH*2, y);
			y+=(PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getNoticeMsg("　しかし、RDのように月～金AM2:00で録画が実行されてしまうような場合にはこれをチェックしてください。帯予約の予約枠を月～金AM2:00で表示するようにします。"), DESCRIPTION_WIDTH, PARTS_HEIGHT, SEP_WIDTH*2, y);

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_rootNodeVisible = new JCheckBoxPanel("ツリーペーンにrootノードを表示させる",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_rootNodeVisible.setSelected(env.getRootNodeVisible());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_syncTreeWidth = new JCheckBoxPanel("リスト形式と新聞形式のツリーペーンの幅を同期する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_syncTreeWidth.setSelected(env.getSyncTreeWidth());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_shortExtMark = new JCheckBoxPanel("「★延長注意★」を「(延)」に短縮表示",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_shortExtMark.setSelected(env.getShortExtMark());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBX_snapshotFmt = new JComboBoxPanel("スナップショットの画像形式",LABEL_WIDTH,250,true), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			for ( SnapshotFmt s : SnapshotFmt.values() ) {
				jCBX_snapshotFmt.addItem(s);
			}
			jCBX_snapshotFmt.setSelectedItem(env.getSnapshotFmt());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_printSnapshot = new JCheckBoxPanel("スナップショットボタンで印刷を実行する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_printSnapshot.setSelected(env.getPrintSnapshot());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			int marks_h = PARTS_HEIGHT*12; 
			CommonSwingUtils.putComponentOn(jPanel_setting, getJLabel_showmarks("表示マークの選択"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJScrollPane_showmarks(), 320, marks_h, LABEL_WIDTH+SEP_WIDTH, y);
			// ★★★ RELOADリスナーは getJScrollPane_showmarks()内でつける
			
			y+=(marks_h+SEP_HEIGHT);
			int cbitems_w = 320;
			int cbitems_h = PARTS_HEIGHT*8;
			CommonSwingUtils.putComponentOn(jPanel_setting, getJLabel_clipboard("クリップボードアイテムの選択"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJButton_clipboard_up("↑"), 50, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH+10+cbitems_w, y);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJButton_clipboard_down("↓"), 50, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH+10+cbitems_w, y+PARTS_HEIGHT+SEP_WIDTH);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJScrollPane_clipboard(), cbitems_w, cbitems_h, LABEL_WIDTH+SEP_WIDTH, y);
			// RELOADリスナー不要
  
			y += (cbitems_h + SEP_HEIGHT);
			int mitable_h = PARTS_HEIGHT*8;
			{
				int col1_w = 150;
				int col2_w = 400;
				int mitable_w = col1_w+col2_w;
				CommonSwingUtils.putComponentOn(jPanel_setting, getJLabel_menuitem("右クリックメニューの実行アイテム"), LABEL_WIDTH,PARTS_HEIGHT, SEP_WIDTH, y);
				CommonSwingUtils.putComponentOn(jPanel_setting, getJTextField_mikey(),col1_w, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
				CommonSwingUtils.putComponentOn(jPanel_setting, getJTextField_mival(),col2_w, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH+col1_w, y);
				int yz = y;
				CommonSwingUtils.putComponentOn(jPanel_setting, getJButton_miadd("登録"),75, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH+mitable_w+SEP_WIDTH, yz);
				CommonSwingUtils.putComponentOn(jPanel_setting, getJButton_midel("削除"),75, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH+mitable_w+SEP_WIDTH, yz += PARTS_HEIGHT+SEP_HEIGHT_NALLOW);
				CommonSwingUtils.putComponentOn(jPanel_setting, getJButton_miup("↑"), 50, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH+mitable_w+SEP_WIDTH, yz += PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, getJButton_midown("↓"), 50, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH+mitable_w+SEP_WIDTH, yz += PARTS_HEIGHT+SEP_HEIGHT_NALLOW);
				y += (PARTS_HEIGHT + SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting,getJScrollPane_mitable(col1_w,col2_w), mitable_w, mitable_h, LABEL_WIDTH+SEP_WIDTH, y);
				// RELOADリスナー不要
			}

			/*
			 * Web番組表対応 
			 */
			
			y+=(mitable_h+BLOCK_SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, new JLabel("＜＜＜Web番組表対応＞＞＞"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_continueTomorrow = new JCheckBoxPanel("29時をまたぐ番組を検出し同一視する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_continueTomorrow.setSelected(env.getContinueTomorrow());
			jCBP_continueTomorrow.addItemListener(IL_RELOAD_PROG_NEEDED);
			
			{
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jSP_cacheTimeLimit = new JSliderPanel("番組表キャッシュの有効時間(0:無制限)",LABEL_WIDTH,0,72,200), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jSP_cacheTimeLimit.setValue((env.getCacheTimeLimit()+1)%73);
				// RELOADリスナー不要

				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCBX_shutdownCmd = new JComboBoxPanel("┗　シャットダウンコマンド",LABEL_WIDTH,250,true), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jCBX_shutdownCmd.setEditable(true);
				jCBX_shutdownCmd.addItem(env.getShutdownCmd());
				for ( String cmd : Env.SHUTDOWN_COMMANDS ) {
					jCBX_shutdownCmd.addItem(cmd);
				}
				// RELOADリスナー不要

				// 連動設定
				
				jSP_cacheTimeLimit.addChangeListener(cl_cachetimelimit);
				
				jSP_cacheTimeLimit.setValue(env.getCacheTimeLimit());
			}
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getNoticeMsg("※起動時に、Web番組表の再取得を自動で「実行させたくない」場合は０にしてください。"), DESCRIPTION_WIDTH, PARTS_HEIGHT, SEP_WIDTH*2, y);
			y+=(PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getNoticeMsg("※シャットダウンコマンドを設定すると、Web番組表取得メニューに「CSのみ取得(取得後シャットダウン)」が追加されます。"), DESCRIPTION_WIDTH, PARTS_HEIGHT, SEP_WIDTH*2, y);
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_expandTo8 = new JCheckBoxPanel("可能なら番組表を８日分取得する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_expandTo8.setSelected(env.getExpandTo8());
			jCBP_expandTo8.addItemListener(IL_RELOAD_PROG_NEEDED);

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_autoEventIdComplete = new JCheckBoxPanel("予約ダイアログを開いたときに自動で番組IDを取得する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_autoEventIdComplete.setSelected(env.getAutoEventIdComplete());
			// RELOADリスナー不要

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_splitEpno = new JCheckBoxPanel("タイトルに話数が含まれる場合に以降を分離する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_splitEpno.setSelected( ! env.getSplitEpno());
			jCBP_splitEpno.addItemListener(IL_RELOAD_PROG_NEEDED);
				
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_fixTitle = new JCheckBoxPanel("タイトル先頭の「アニメ 」を削除(NHKのみ)",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_fixTitle.setSelected(env.getFixTitle());
			jCBP_fixTitle.addItemListener(IL_RELOAD_PROG_NEEDED);
			
			y +=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJLabel_ngword("NGワード(;区切りで複数指定可)"), LABEL_WIDTH,PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJTextField_ngword(CommonUtils.joinStr(";", env.getNgword())),600, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
			jTextField_ngword.getDocument().addDocumentListener(DL_RELOAD_PROG_NEEDED);
			
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJLabel_userAgent("User-Agent"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJTextField_userAgent(env.getUserAgent()), 600, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
			// RELOADリスナー不要
			
			{
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_useProxy = new JCheckBoxPanel("HTTPプロキシを有効にする",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jCBP_useProxy.setSelected( ! env.getUseProxy());
				// RELOADリスナー不要
			
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, getJLabel_proxy("┗　HTTPプロキシ/ポート"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				CommonSwingUtils.putComponentOn(jPanel_setting, getJTextField_proxyAddr(env.getProxyAddr()), 200, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
				CommonSwingUtils.putComponentOn(jPanel_setting, getJTextField_proxyPort(env.getProxyPort()), 100, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH+210, y);
				// RELOADリスナー不要
				
				// 連動設定

				jCBP_useProxy.addItemListener(al_useproxy);

				jCBP_useProxy.setSelected( ! jCBP_useProxy.isSelected());
			}
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_useSyobocal = new JCheckBoxPanel("【アニメ】しょぼいカレンダーを利用する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_useSyobocal.setSelected(env.getUseSyobocal());
			jCBP_useSyobocal.addItemListener(IL_RELOAD_PROG_NEEDED);
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getNoticeMsg("※アニメなんか興味ないよ！という場合はチェックを外して再起動してください。"), DESCRIPTION_WIDTH, PARTS_HEIGHT, SEP_WIDTH*2, y);
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_historyOnlyUpdateOnce = new JCheckBoxPanel("日に一回しか新着履歴を更新しない",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_historyOnlyUpdateOnce.setSelected(env.getHistoryOnlyUpdateOnce());
			// RELOADリスナー不要
			
			{
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_usePassedProgram = new JCheckBoxPanel("過去ログを記録する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jCBP_usePassedProgram.setSelected( ! env.getUsePassedProgram());
				// RELOADリスナー不要
				
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jSP_prepPassedProgramCount = new JSliderPanel("┗　何日先のログまで過去ログ用に保存するか",LABEL_WIDTH,1,8,200), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jSP_prepPassedProgramCount.setValue(env.getPrepPassedProgramCount());
				// RELOADリスナー不要
				
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, getNoticeMsg("※保存期間を４日先までにして１週間旅行に出かけると７日－４日＝３日分の過去ログがロストすることになります。"), DESCRIPTION_WIDTH, PARTS_HEIGHT, SEP_WIDTH*2, y);
				
				// 連動設定
				
				jCBP_usePassedProgram.addItemListener(al_usepassedprogram);
				
				jCBP_usePassedProgram.setSelected( ! jCBP_usePassedProgram.isSelected());
			}
			
			/*
			 * レコーダ対応 
			 */

			y+=(PARTS_HEIGHT+BLOCK_SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, new JLabel("＜＜＜レコーダ対応＞＞＞"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			
			int getdetail_h = PARTS_HEIGHT*3+SEP_HEIGHT_NALLOW*2;
			
			{
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jRBP_getRdReserveDetails = new JRadioButtonPanel("予約一覧取得時に詳細情報も取得する",LABEL_WIDTH), PARTS_WIDTH, getdetail_h, SEP_WIDTH, y);
				jRBP_getRdReserveDetails.add("毎回確認する",true);
				jRBP_getRdReserveDetails.add("常に取得する",false);
				jRBP_getRdReserveDetails.add("常に取得しない",false);
				// RELOADリスナー不要
				
				y+=(getdetail_h+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jRBP_getRdAutoReserves = new JRadioButtonPanel("予約一覧取得時に自動予約一覧も取得する",LABEL_WIDTH), PARTS_WIDTH, getdetail_h, SEP_WIDTH, y);
				jRBP_getRdAutoReserves.add("毎回確認する",true);
				jRBP_getRdAutoReserves.add("常に取得する",false);
				jRBP_getRdAutoReserves.add("常に取得しない",false);
				// RELOADリスナー不要
				
				y+=(getdetail_h+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jRBP_getRdRecorded = new JRadioButtonPanel("予約一覧取得時に録画結果一覧も取得する",LABEL_WIDTH), PARTS_WIDTH, getdetail_h, SEP_WIDTH, y);
				jRBP_getRdRecorded.add("毎回確認する",true);
				jRBP_getRdRecorded.add("常に取得する",false);
				jRBP_getRdRecorded.add("常に取得しない",false);
				// RELOADリスナー不要
				
				// 選択肢
				updateSelections();
				
				y+=(getdetail_h+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, getNoticeMsg("※「常に取得しない」を選択した場合でも、ツールバーのプルダウンメニューから強制的に取得を実行できます。"), DESCRIPTION_WIDTH, PARTS_HEIGHT, SEP_WIDTH*2, y);
			}
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBX_recordedSaveScope = new JComboBoxPanel("録画結果一覧の保存期間",LABEL_WIDTH,250,true), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBX_recordedSaveScope.addItem("保存しない");
			for ( int n=1; n<=HDDRecorder.SCOPEMAX; n++ ) {
				jCBX_recordedSaveScope.addItem(String.format("%d日 (%d週)",n,(n/7)+1));
			}
			jCBX_recordedSaveScope.setSelectedIndex(env.getRecordedSaveScope());
			// RELOADリスナー不要
			
			y+=(getdetail_h+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, new JLabel("NULLプラグインでのカレンダ連携設定はレコーダ設定に移動しました"), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			
			/*
			 * 予約
			 */

			y+=(75+BLOCK_SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, new JLabel("＜＜＜予約＞＞＞"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jSP_spoex_extend = new JSliderPanel("延長警告の録画時間延長幅(分)",LABEL_WIDTH,0,180,200), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jSP_spoex_extend.setValue(Integer.valueOf(env.getSpoexLength()));
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			int ovarlap_h = PARTS_HEIGHT*2+SEP_HEIGHT_NALLOW*1;
			CommonSwingUtils.putComponentOn(jPanel_setting, jRBP_overlapUp = new JRadioButtonPanel("録画時間の前",LABEL_WIDTH), PARTS_WIDTH, ovarlap_h, SEP_WIDTH, y);
			jRBP_overlapUp.add("なにもしない",! (env.getOverlapUp()));
			jRBP_overlapUp.add("１分前倒し",(env.getOverlapUp()));
			// RELOADリスナー不要

			y+=(ovarlap_h+SEP_HEIGHT);
			int ovarlap2_h = PARTS_HEIGHT*3+SEP_HEIGHT_NALLOW*2;
			CommonSwingUtils.putComponentOn(jPanel_setting, jRBP_overlapDown = new JRadioButtonPanel("録画時間の後ろ",LABEL_WIDTH), PARTS_WIDTH, ovarlap2_h, SEP_WIDTH, y);
			jRBP_overlapDown.add("なにもしない",! (env.getOverlapDown() || env.getOverlapDown2()));
			jRBP_overlapDown.add("１分延ばす",(env.getOverlapDown()));
			jRBP_overlapDown.add("１分短縮(NHK以外)",(env.getOverlapDown2()));
			// RELOADリスナー不要

			y+=(ovarlap2_h+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJLabel_autoFolderSelect("自動フォルダ選択を有効にする"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJCheckBox_autoFolderSelect(env.getAutoFolderSelect()), 100, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
			// RELOADリスナー不要

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJLabel_enableCHAVsetting("AV自動設定キーをジャンルからCHに"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJCheckBox_enableCHAVsetting(env.getEnableCHAVsetting()), 100, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jSP_rangeLikeRsv = new JSliderPanel("類似予約の検索時間範囲(0:無制限)",LABEL_WIDTH,0,24,200), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jSP_rangeLikeRsv.setValue(env.getRangeLikeRsv());
			// RELOADリスナー不要
			
			{
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_givePriorityToReserved = new JCheckBoxPanel("類似予約がある場合は情報を引き継ぐ",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jCBP_givePriorityToReserved.setSelected( ! env.getGivePriorityToReserved());
				// RELOADリスナー不要

				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_givePriorityToReservedTitle = new JCheckBoxPanel("┗　類似予約のタイトルを引き継ぐ",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jCBP_givePriorityToReservedTitle.setSelected(env.getGivePriorityToReservedTitle());
				// RELOADリスナー不要
				
				// 連動設定

				jCBP_givePriorityToReserved.addItemListener(al_giveprioritytoreserved);
				
				jCBP_givePriorityToReserved.setSelected( ! jCBP_givePriorityToReserved.isSelected());
			}
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_adjoiningNotRepetition = new JCheckBoxPanel("終了時刻と開始時刻が重なる番組でも重複扱いしない",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_adjoiningNotRepetition.setSelected(env.getAdjoiningNotRepetition());
			// RELOADリスナー不要
			
			{
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_rsv_showallite = new JCheckBoxPanel("予約一覧で繰り返し予約を展開する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jCBP_rsv_showallite.setSelected( ! env.getShowAllIterationItem());
				// RELOADリスナー不要
				
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jLabel_rsv_itecolor = new JLabel("┗　展開した繰り返し予約の２回目以降の文字色"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCCL_rsv_itecolor = new JCCLabel("文字色",env.getIterationItemForeground(),false,parent,ccwin), CCLABEL_WIDTH, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
				// RELOADリスナー不要
				
				jCBP_rsv_showallite.addItemListener(al_showallite);
				
				// Fire!
				jCBP_rsv_showallite.setSelected( ! jCBP_rsv_showallite.isSelected());
			}
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jLabel_rsv_tunshortcolor = new JLabel("チューナー不足警告の背景色"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCCL_rsv_tunshortcolor = new JCCLabel("チューナー不足警告の背景色",env.getTunerShortColor(),true,parent,ccwin), CCLABEL_WIDTH, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getNoticeMsg("※チューナー不足警告は、レコーダの予約一覧上に表示される警告情報を反映しています。"), DESCRIPTION_WIDTH, PARTS_HEIGHT, SEP_WIDTH*2, y);
			y+=(PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getNoticeMsg("※EDCBの場合、チューナー不足警告は鯛ナビからの予約アクションでは更新されませんので、必要に応じて予約一覧の再取得を行って更新してください。"), DESCRIPTION_WIDTH, PARTS_HEIGHT, SEP_WIDTH*2, y);
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jLabel_rsv_recedcolor = new JLabel("正常録画済み(と思われる)予約の背景色"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCCL_rsv_recedcolor = new JCCLabel("正常録画済み(と思われる)予約の背景色",env.getRecordedColor(),true,parent,ccwin), CCLABEL_WIDTH, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_useAutocomplete = new JCheckBoxPanel("【RD】タイトル自動補完機能を使用する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_useAutocomplete.setSelected(env.getUseAutocomplete());
			// RELOADリスナー不要
			
			/*
			 * その他 
			 */

			y+=(PARTS_HEIGHT+BLOCK_SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, new JLabel("＜＜＜その他＞＞＞"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBX_updateMethod = new JComboBoxPanel("起動時にアップデートを確認する",LABEL_WIDTH,250,true), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			for ( UpdateOn u : UpdateOn.values() ) {
				jCBX_updateMethod.addItem(u);
			}
			jCBX_updateMethod.setSelectedItem(env.getUpdateMethod());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_disableBeep = new JCheckBoxPanel("beep禁止",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_disableBeep.setSelected(env.getDisableBeep());
			// RELOADリスナー不要

			{
				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_showSysTray = new JCheckBoxPanel("システムトレイにアイコンを表示",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jCBP_showSysTray.setSelected( ! env.getShowSysTray());
				// RELOADリスナー不要

				y+=(PARTS_HEIGHT+SEP_HEIGHT);
				CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_hideToTray = new JCheckBoxPanel("┗　最小化時はシステムトレイに隠す",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
				jCBP_hideToTray.setSelected(env.getHideToTray());
				// RELOADリスナー不要
				
				// 連動設定

				jCBP_showSysTray.addItemListener(al_showsystray);
				
				jCBP_showSysTray.setSelected( ! jCBP_showSysTray.isSelected());
			}

			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_onlyOneInstance = new JCheckBoxPanel("多重起動禁止（要再起動）",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_onlyOneInstance.setSelected(env.getOnlyOneInstance());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJLabel_lookAndFeel("ルック＆フィール"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJComboBox_lookAndFeel(), 250, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJLabel_font("表示フォント"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJComboBox_font(), 250, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJComboBox_fontSize(), 100, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH+260, y);
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, new JLabel("┗　表示サンプル"), LABEL_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jPanel_setting, getJLabel_fontSample(""), 360, PARTS_HEIGHT, LABEL_WIDTH+SEP_WIDTH, y);
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_useGTKRC = new JCheckBoxPanel("鯛ナビ専用のgtkrcを使う",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_useGTKRC.setSelected(env.getUseGTKRC());
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, getNoticeMsg("※ルック＆フィールがGTKの場合は再起動するまで表示フォントの設定は反映されません（@see env/_gtkrc-2.0）"), DESCRIPTION_WIDTH, PARTS_HEIGHT, SEP_WIDTH*2, y);
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_useRundll32 = new JCheckBoxPanel("【Win】ファイルオープンにrundll32を使用する",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_useRundll32.setSelected(env.getUseRundll32());
			// RELOADリスナー不要
			
			y+=(PARTS_HEIGHT+SEP_HEIGHT);
			CommonSwingUtils.putComponentOn(jPanel_setting, jCBP_debug = new JCheckBoxPanel("【注意】デバッグログ出力を有効にする",LABEL_WIDTH), PARTS_WIDTH, PARTS_HEIGHT, SEP_WIDTH, y);
			jCBP_debug.setSelected(env.getDebug());
			// RELOADリスナー不要

			y += (PARTS_HEIGHT + 50);
			
			// 画面の全体サイズを決める
			Dimension d = new Dimension(PANEL_WIDTH,y);
			jPanel_setting.setPreferredSize(d);
		}
		
		return jPanel_setting;
	}
	
	/*******************************************************************************
	 * アクション
	 ******************************************************************************/
	
	// 更新確定ボタン押下時の処理
	private void updateEnvs() {
		
		TatCount tc = new TatCount();
		
		StWin.clear();
		
		new SwingBackgroundWorker(false) {
			
			@Override
			protected Object doWorks() throws Exception {
				
				StWin.appendMessage(MSGID+"設定を保存します");
				
				int idx;
	
				// リスト形式
				env.setDisableFazzySearch(jCBP_disableFazzySearch.isSelected());
				env.setDisableFazzySearchReverse(jCBP_disableFazzySearchReverse.isSelected());
				env.setDefaultFazzyThreshold(jSP_defaultFazzyThreshold.getValue());
				env.setSyoboFilterByCenters(jCBP_syoboFilterByCenters.isSelected());
				env.setDisplayPassedEntry(jCBP_displayPassedEntry.isSelected());
				env.setShowRsvPickup(jCBP_showRsvPickup.isSelected());
				env.setShowRsvUra(jCBP_showRsvUra.isSelected());
				env.setRsvdLineEnhance(jCBP_rsvdLineEnhance.isSelected());
				env.setRsvdLineColor(jCCL_rsvdLineColor.getChoosed());
				env.setPickedLineColor(jCCL_pickedLineColor.getChoosed());
				env.setCurrentLineEnhance(jCBP_currentLineEnhance.isSelected());
				env.setCurrentLineColor(jCCL_currentLineColor.getChoosed());
				env.setCurrentAfter(jSP_currentAfter.getValue()*60);
				env.setCurrentBefore(jSP_currentBefore.getValue()*60);
				env.setMatchedKeywordColor(jCCL_matchedKeywordColor.getChoosed());
				env.setShowWarnDialog(jCBP_showWarnDialog.isSelected());
				env.setSplitMarkAndTitle(jCBP_splitMarkAndTitle.isSelected());
				env.setShowDetailOnList(jCBP_showDetailOnList.isSelected());
				env.setRsvTargets(jSP_rsvTargets.getValue());
				env.setRowHeaderVisible(jCBP_rowHeaderVisible.isSelected());
				env.setDblClkCmd((DblClkCmd) jCBX_dblClkCmd.getSelectedItem());
				env.setSearchResultMax(jSP_searchResultMax.getValue());
				env.setSearchResultBufferMax(jSP_searchResultBufferMax.getValue());
				
				// 新聞形式関連
				{
					String selected = jRBP_getPaperRedrawType.getSelectedItem().getText();
					if ( PARER_REDRAW_PAGER.equals(selected) ) {
						env.setDrawcacheEnable(false);
						env.setCenterPerPage(jSP_centerPerPage.getValue());
					}
					else if ( PARER_REDRAW_CACHE.equals(selected) ) {
						env.setDrawcacheEnable(true);
						env.setCenterPerPage(0);
					}
					else if ( PARER_REDRAW_NORMAL.equals(selected) ) {
						env.setDrawcacheEnable(false);
						env.setCenterPerPage(0);
					}
				}
				env.setAllPageSnapshot(jCBP_allPageSnapshot.isSelected());
				env.setTooltipEnable(jCBP_tooltipEnable.isSelected());
				env.setTooltipInitialDelay(jSP_tooltipInitialDelay.getValue());
				env.setTooltipDismissDelay(jSP_tooltipDismissDelay.getValue());
				env.setTimerbarScrollEnable(jCBP_timerbarScrollEnable.isSelected());
				env.setPassedLogLimit(jSP_passedLogLimit.getValue());
				env.setEffectComboToPaper(jCBP_effectComboToPaper.isSelected());
				env.setSnapshotFmt((SnapshotFmt) jCBX_snapshotFmt.getSelectedItem());
				env.setPrintSnapshot(jCBP_printSnapshot.isSelected());
				
				// リスト・新聞形式共通
				env.setDisplayOnlyExecOnEntry(jCBP_displayOnlyExecOnEntry.isSelected());
				env.setDisplayPassedReserve(jCBP_displayPassedReserve.isSelected());
				env.setShowOnlyNonrepeated(jCBP_showOnlyNonrepeated.isSelected());
				env.setAdjLateNight(jCBP_adjLateNight.isSelected());
				env.setRootNodeVisible(jCBP_rootNodeVisible.isSelected());
				env.setSyncTreeWidth(jCBP_syncTreeWidth.isSelected());
				env.setShortExtMark(jCBP_shortExtMark.isSelected());
				for (int row=0; row<jTable_showmarks.getRowCount(); row++) {
					env.getOptMarks().put((TVProgram.ProgOption) jTable_showmarks.getValueAt(row, 2), (Boolean) jTable_showmarks.getValueAt(row, 0));
				}
				for (int row=0; row<jTable_clipboard.getRowCount(); row++) {
					cbitems.get(row).setB((Boolean)jTable_clipboard.getValueAt(row, 0));
					cbitems.get(row).setItem((String)jTable_clipboard.getValueAt(row, 1));
					cbitems.get(row).setId((Integer)jTable_clipboard.getValueAt(row, 2));
				}
				env.getTvCommand().removeAll(env.getTvCommand());
				for (int row = 0; row < jTable_mitable.getRowCount(); row++) {
					TextValueSet tv = new TextValueSet();
					tv.setText((String) jTable_mitable.getValueAt(row, 0));
					tv.setValue((String) jTable_mitable.getValueAt(row, 1));
					env.getTvCommand().add(tv);
				}
				
				// Web番組表対応
				env.setContinueTomorrow(jCBP_continueTomorrow.isSelected());
				env.setCacheTimeLimit(jSP_cacheTimeLimit.getValue());
				if ( env.getCacheTimeLimit() == 0 ) {
					env.setShutdownCmd((String) jCBX_shutdownCmd.getSelectedItem());
				}
				else {
					env.setShutdownCmd("");
				}
				env.setExpandTo8(jCBP_expandTo8.isSelected());
				//env.setUseDetailCache(jCBP_useDetailCache.isSelected());
				env.setUseDetailCache(false);
				env.setAutoEventIdComplete(jCBP_autoEventIdComplete.isSelected());
				env.setSplitEpno(jCBP_splitEpno.isSelected());
				env.setTraceOnlyTitle(jCBP_traceOnlyTitle.isSelected());
				env.setFixTitle(jCBP_fixTitle.isSelected());
				env.setNgword(jTextField_ngword.getText());
				env.setUserAgent(jTextField_userAgent.getText());
				env.setUseProxy(jCBP_useProxy.isSelected());
				env.setProxyAddr((String)jTextField_proxyAddr.getText());
				env.setProxyPort((String)jTextField_proxyPort.getText());
				env.setUseSyobocal(jCBP_useSyobocal.isSelected());
				env.setHistoryOnlyUpdateOnce(jCBP_historyOnlyUpdateOnce.isSelected());
				env.setUsePassedProgram(jCBP_usePassedProgram.isSelected());
				env.setPrepPassedProgramCount(jSP_prepPassedProgramCount.getValue());
				
				// レコーダ対応
				env.setForceLoadReserveDetails(jRBP_getRdReserveDetails.getSelectedIndex());
				env.setForceLoadAutoReserves(jRBP_getRdAutoReserves.getSelectedIndex());
				env.setForceLoadRecorded(jRBP_getRdRecorded.getSelectedIndex());
				env.setRecordedSaveScope(jCBX_recordedSaveScope.getSelectedIndex());
	
				// 予約
				env.setSpoexLength(String.format("%d",jSP_spoex_extend.getValue()));
				idx = jRBP_overlapUp.getSelectedIndex();
				switch (idx) {
				case 1:
					env.setOverlapUp(true);
					break;
				default:
					env.setOverlapUp(false);
					break;
				}
				idx = jRBP_overlapDown.getSelectedIndex();
				switch (idx) {
				case 1:
					env.setOverlapDown(true);
					env.setOverlapDown2(false);
					break;
				case 2:
					env.setOverlapDown(false);
					env.setOverlapDown2(true);
					break;
				default:
					env.setOverlapDown(false);
					env.setOverlapDown2(false);
					break;
				}
				env.setAutoFolderSelect(jCheckBox_autoFolderSelect.isSelected());
				env.setEnableCHAVsetting(jCheckBox_enableCHAVsetting.isSelected());
				env.setRangeLikeRsv(jSP_rangeLikeRsv.getValue());
				env.setGivePriorityToReserved(jCBP_givePriorityToReserved.isSelected());
				env.setGivePriorityToReservedTitle(jCBP_givePriorityToReservedTitle.isSelected());
				env.setAdjoiningNotRepetition(jCBP_adjoiningNotRepetition.isSelected());
				env.setShowAllIterationItem(jCBP_rsv_showallite.isSelected());
				env.setIterationItemForeground(jCCL_rsv_itecolor.getChoosed());
				env.setTunerShortColor(jCCL_rsv_tunshortcolor.getChoosed());
				env.setRecordedColor(jCCL_rsv_recedcolor.getChoosed());
				env.setUseAutocomplete(jCBP_useAutocomplete.isSelected());
				
				// その他の設定
				env.setUpdateMethod((UpdateOn) jCBX_updateMethod.getSelectedItem());
				env.setDisableBeep(jCBP_disableBeep.isSelected());
				env.setShowSysTray(jCBP_showSysTray.isSelected());
				env.setHideToTray(jCBP_hideToTray.isSelected());
				env.setOnlyOneInstance(jCBP_onlyOneInstance.isSelected());
				env.setLookAndFeel((String) jComboBox_lookAndFeel.getSelectedItem());
				env.setFontName((String) jComboBox_font.getSelectedItem());
				env.setFontSize(Integer.valueOf((String) jComboBox_fontSize.getSelectedItem()));
				env.setUseGTKRC(jCBP_useGTKRC.isSelected());
				env.setUseRundll32(jCBP_useRundll32.isSelected());
				env.setDebug(jCBP_debug.isSelected());
	
				// 設定保存
				setEnv(reload_prog_needed);
				setUpdateButtonEnhanced(false);
	
				return null;
			}
			
			@Override
			protected void doFinally() {
				StWinSetVisible(false);
			}
		}.execute();

		StWinSetLocationCenter(parent);
		StWinSetVisible(true);
		
		MWin.appendMessage(String.format(MSGID+"更新が完了しました。所要時間： %.2f秒",tc.end()));
	}
	
	/**
	 * 各種設定タブ以外で変更したenvの内容をタブに反映する
	 */
	public void updateSelections() {
		jRBP_getRdReserveDetails.setSelectedIndex(env.getForceLoadReserveDetails());
		jRBP_getRdAutoReserves.setSelectedIndex(env.getForceLoadAutoReserves());
		jRBP_getRdRecorded.setSelectedIndex(env.getForceLoadRecorded());
	}
	
	/*******************************************************************************
	 * リスナー
	 ******************************************************************************/
	
	/*
	 * 連動
	 */
	
	/**
	 * 変更があった場合に番組表のリロードを要求するコンポーネントにつけるリスナー
	 */
	
	private void setUpdateButtonEnhanced(boolean b) {
		if (b) {
			jButton_update.setText("更新時番組表再取得あり");
			jButton_update.setForeground(Color.RED);
		}
		else {
			jButton_update.setText("更新を確定する");
			jButton_update.setForeground(Color.BLACK);
		}
		reload_prog_needed = b;
	}
	
	private final ItemListener IL_RELOAD_PROG_NEEDED = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (debug) System.err.println(DBGID+"MODIFIED");
			setUpdateButtonEnhanced(true);
		}
	};
	private final DocumentListener DL_RELOAD_PROG_NEEDED = new DocumentListener() {
		@Override
		public void removeUpdate(DocumentEvent e) {
			if (debug) System.err.println(DBGID+"MODIFIED");
			setUpdateButtonEnhanced(true);
		}
		@Override
		public void insertUpdate(DocumentEvent e) {
			if (debug) System.err.println(DBGID+"MODIFIED");
			setUpdateButtonEnhanced(true);
		}
		@Override
		public void changedUpdate(DocumentEvent e) {
			if (debug) System.err.println(DBGID+"MODIFIED");
			setUpdateButtonEnhanced(true);
		}
	};
	private final CellEditorListener CEL_RELOAD_PROG_NEEDED = new CellEditorListener() {
		@Override
		public void editingStopped(ChangeEvent e) {
			if (debug) System.err.println(DBGID+"MODIFIED");
			setUpdateButtonEnhanced(true);
		}
		
		@Override
		public void editingCanceled(ChangeEvent e) {
		}
	};

	// あいまい検索
	ItemListener al_fazzysearch = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (debug) System.out.println("Fire! al_fazzysearch");
			if (jCBP_disableFazzySearch.isSelected()) {
				jCBP_disableFazzySearchReverse.setEnabled(false);
				jSP_defaultFazzyThreshold.setEnabled(false);
			}
			else {
				jCBP_disableFazzySearchReverse.setEnabled(true);
				jSP_defaultFazzyThreshold.setEnabled(true);
			}
		}
	};

	// 予約行の背景色
	ItemListener al_rsvdlineenhance = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (debug) System.out.println("Fire! al_rsvdlineenhance");
			if (jCBP_rsvdLineEnhance.isSelected()) {
				jLabel_rsvdLineColor.setEnabled(true);
				jCCL_rsvdLineColor.setEnabled(true);
				jLabel_pickedLineColor.setEnabled(true);
				jCCL_pickedLineColor.setEnabled(true);
				
			}
			else {
				jLabel_rsvdLineColor.setEnabled(false);
				jCCL_rsvdLineColor.setEnabled(false);
				jLabel_pickedLineColor.setEnabled(false);
				jCCL_pickedLineColor.setEnabled(false);
			}
		}
	};
	
	// 現在放送中行の背景色
	ItemListener al_currentlineenhance = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (debug) System.out.println("Fire! al_currentlineenhance");
			if (jCBP_currentLineEnhance.isSelected()) {
				jLabel_currentLineColor.setEnabled(true);
				jCCL_currentLineColor.setEnabled(true);
				
			}
			else {
				jLabel_currentLineColor.setEnabled(false);
				jCCL_currentLineColor.setEnabled(false);
			}
		}
	};

	ItemListener il_paperredrawtype = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (debug) System.out.println("Fire! il_paperredrawtype "+e.toString());
			if ( e.getStateChange() == ItemEvent.SELECTED ) {
				String selected = ((JRadioButton)e.getItem()).getText();
				if ( selected.equals(PARER_REDRAW_NORMAL) ) {
					jSP_centerPerPage.setEnabled(false);
					jCBP_allPageSnapshot.setEnabled(false);
				}
				else if ( selected.equals(PARER_REDRAW_CACHE) ) {
					jSP_centerPerPage.setEnabled(false);
					jCBP_allPageSnapshot.setEnabled(false);
				}
				else if ( selected.equals(PARER_REDRAW_PAGER) ) {
					jSP_centerPerPage.setEnabled(true);
					jCBP_allPageSnapshot.setEnabled(true);
					jSP_centerPerPage.setValue(env.getCenterPerPage()>0?env.getCenterPerPage():7);
				}
			}
		}
	};

	/*
	ItemListener al_drawcacheenable = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (debug) System.out.println("Fire! al_drawcacheenable");
			if (jCBP_drawcacheEnable.isSelected()) {
				jCBP_pagerEnable.removeItemListener(il_pagerenable);
				jCBP_pagerEnable.setEnabled(false);
				jCBP_pagerEnable.addItemListener(il_pagerenable);
				
				jSP_centerPerPage.setEnabled(false);
				jCBP_allPageSnapshot.setEnabled(false);
			}
			else {
				jCBP_pagerEnable.removeItemListener(il_pagerenable);
				jCBP_pagerEnable.setEnabled(true);
				jCBP_pagerEnable.addItemListener(il_pagerenable);
				
				if (jCBP_pagerEnable.isSelected()) {
					jSP_centerPerPage.setEnabled(true);
					jCBP_allPageSnapshot.setEnabled(true);
				}
				else {
					jSP_centerPerPage.setEnabled(false);
					jCBP_allPageSnapshot.setEnabled(false);
				}
			}
		}
	};

	ChangeListener cl_centerperpage = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			if (debug) System.out.println("Fire! cl_centerperpage");
			if (jSP_centerPerPage.getValue() == 0) {
				jCBP_drawcacheEnable.removeItemListener(al_drawcacheenable);
				jCBP_drawcacheEnable.setEnabled(true);
				jCBP_allPageSnapshot.setEnabled(false);
				jCBP_drawcacheEnable.addItemListener(al_drawcacheenable);
			}
			else {
				jCBP_drawcacheEnable.removeItemListener(al_drawcacheenable);
				jCBP_drawcacheEnable.setSelected(false);
				jCBP_drawcacheEnable.setEnabled(false);
				jCBP_allPageSnapshot.setEnabled(true);
				jCBP_drawcacheEnable.addItemListener(al_drawcacheenable);
			}
		}
	};
	*/
	
	ItemListener al_tooltipenable = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (debug) System.out.println("Fire! al_tooltipenable");
			if (jCBP_tooltipEnable.isSelected()) {
				jSP_tooltipInitialDelay.setEnabled(true);
				jSP_tooltipDismissDelay.setEnabled(true);
			}
			else {
				jSP_tooltipInitialDelay.setEnabled(false);
				jSP_tooltipDismissDelay.setEnabled(false);
			}
		}
	};

	ChangeListener cl_cachetimelimit = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			if (debug) System.out.println("Fire! cl_cachetimelimit");
			if (jSP_cacheTimeLimit.getValue() == 0) {
				jCBX_shutdownCmd.setEnabled(true);
			}
			else {
				jCBX_shutdownCmd.setEnabled(false);
			}
		}
	};

	ItemListener al_splitepno = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (debug) System.out.println("Fire! al_splitepno");
			if (jCBP_splitEpno.isSelected()) {
				jCBP_traceOnlyTitle.setEnabled(false);
			}
			else {
				jCBP_traceOnlyTitle.setEnabled(true);
			}
		}
	};

	ItemListener al_useproxy = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (debug) System.out.println("Fire! al_useproxy");
			if (jCBP_useProxy.isSelected()) {
				jLabel_proxy.setEnabled(true);
				jTextField_proxyAddr.setEnabled(true);
				jTextField_proxyPort.setEnabled(true);
			}
			else {
				jLabel_proxy.setEnabled(false);
				jTextField_proxyAddr.setEnabled(false);
				jTextField_proxyPort.setEnabled(false);
			}
		}
	};

	ItemListener al_usepassedprogram = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (debug) System.out.println("Fire! al_usepassedprogram");
			if (jCBP_usePassedProgram.isSelected()) {
				jSP_prepPassedProgramCount.setEnabled(true);
			}
			else {
				jSP_prepPassedProgramCount.setEnabled(false);
			}
		}
	};

	ItemListener al_giveprioritytoreserved = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (debug) System.out.println("Fire! al_giveprioritytoreserved");
			if (jCBP_givePriorityToReserved.isSelected()) {
				jCBP_givePriorityToReservedTitle.setEnabled(true);
				jCBP_givePriorityToReservedTitle.setSelected(true);
			}
			else {
				jCBP_givePriorityToReservedTitle.setEnabled(false);
				jCBP_givePriorityToReservedTitle.setSelected(false);
			}
		}
	};

	ItemListener al_showallite = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if ( jCBP_rsv_showallite.isSelected() ) {
				jLabel_rsv_itecolor.setEnabled(true);
				jCCL_rsv_itecolor.setEnabled(true);
			}
			else {
				jLabel_rsv_itecolor.setEnabled(false);
				jCCL_rsv_itecolor.setEnabled(false);
			}
		}
	};
	
	ItemListener al_showsystray = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (debug) System.out.println(DBGID+"Fire! al_showsystray");
			if (jCBP_showSysTray.isSelected()) {
				jCBP_hideToTray.setEnabled(true);
			}
			else {
				jCBP_hideToTray.setEnabled(false);
			}
		}
	};
	
	ActionListener al_fontChanged = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			String fn = (String) jComboBox_font.getSelectedItem();
			int fs = Integer.valueOf((String) jComboBox_fontSize.getSelectedItem());
			
			fontChanged(fn, fs);
			
			if ( jLabel_fontSample != null ) {
				Font f = jLabel_fontSample.getFont();
				jLabel_fontSample.setFont(new Font(fn,f.getStyle(),fs));
			}
		}
	};
	
	
	/*******************************************************************************
	 * コンポーネント
	 ******************************************************************************/
	
	private JLabel getNoticeMsg(String text) {
		JLabel l = new JLabel(text);
		l.setForeground(NOTICEMSG_COLOR);
		return l;
	}
	
	// 更新確定ボタン
	private JButton getJButton_update(String s) {
		if (jButton_update == null) {
			jButton_update = new JButton(s);
			
			jButton_update.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateEnvs();
				}
			});
		}
		return(jButton_update);
	}
	
	//
	private JLabel getJLabel_userAgent(String s)
	{
		if (jLabel_userAgent == null) {
			jLabel_userAgent = new JLabel(s);
		}
		return(jLabel_userAgent);
	}
	private JTextField getJTextField_userAgent(String s) {
		if (jTextField_userAgent == null) {
			jTextField_userAgent = new JTextFieldWithPopup();
			jTextField_userAgent.setText(s);
			jTextField_userAgent.setCaretPosition(0);
		}
		return jTextField_userAgent;
	}
	
	//
	private JLabel getJLabel_proxy(String s)
	{
		if (jLabel_proxy == null) {
			jLabel_proxy = new JLabel(s);
		}
		return(jLabel_proxy);
	}
	private JTextField getJTextField_proxyAddr(String s) {
		if (jTextField_proxyAddr == null) {
			jTextField_proxyAddr = new JTextFieldWithPopup();
			jTextField_proxyAddr.setText(s);
			jTextField_proxyAddr.setCaretPosition(0);
		}
		return jTextField_proxyAddr;
	}
	private JTextField getJTextField_proxyPort(String s) {
		if (jTextField_proxyPort == null) {
			jTextField_proxyPort = new JTextFieldWithPopup();
			jTextField_proxyPort.setText(s);
			jTextField_proxyPort.setCaretPosition(0);
		}
		return jTextField_proxyPort;
	}
	
	// 表示マークの選択
	private JLabel getJLabel_showmarks(String s) {
		if (jLabel_showmarks == null) {
			jLabel_showmarks = new JLabel(s);
		}
		return jLabel_showmarks;
	}
	private JScrollPane getJScrollPane_showmarks() {
		if (jScrollPane_showmarks == null) {
			jScrollPane_showmarks = new JScrollPane();
			jScrollPane_showmarks.setViewportView(getJTable_showmarks());
			jScrollPane_showmarks.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		}
		return(jScrollPane_showmarks);
	}
	private JNETable getJTable_showmarks() {
		if (jTable_showmarks == null) {
			// ヘッダの設定
			String[] colname = {"ﾁｪｯｸ", "マーク", "ID"};
			int[] colwidth = {50,250,0};
			
			//
			DefaultTableModel model = new DefaultTableModel(colname, 0);
			jTable_showmarks = new JNETable(model, false) {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int row, int column) {
					return (column == 0);
				}
			};
			jTable_showmarks.setAutoResizeMode(JNETable.AUTO_RESIZE_OFF);
			DefaultTableColumnModel columnModel = (DefaultTableColumnModel)jTable_showmarks.getColumnModel();
			TableColumn column = null;
			for (int i = 0 ; i < columnModel.getColumnCount() ; i++){
				column = columnModel.getColumn(i);
				column.setPreferredWidth(colwidth[i]);
			}
			
			// にゃーん
			jTable_showmarks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
   
			// エディタに手を入れる
			DefaultCellEditor editor = new DefaultCellEditor(new JCheckBox() {

				private static final long serialVersionUID = 1L;

				@Override
				public int getHorizontalAlignment() {
					return JCheckBox.CENTER;
				}
			});
			
			editor.addCellEditorListener(CEL_RELOAD_PROG_NEEDED);
			
			//
			jTable_showmarks.getColumn("ﾁｪｯｸ").setCellEditor(editor);
			
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
			jTable_showmarks.getColumn("ﾁｪｯｸ").setCellRenderer(renderer);
			
			//
			for (Object[] obj : TVProgram.optMarks) {
				Entry<ProgOption,Boolean> entry = null;
				for (Entry<ProgOption,Boolean> e : env.getOptMarks().entrySet()) {
					if (e.getKey() == obj[0]) {
						entry = e;
						break;
					}
				}
				if ( entry != null ) {
					Object[] data = { entry.getValue(),obj[1],obj[0] };
					model.addRow(data);
				}
				else {
					Object[] data = { Boolean.TRUE,obj[1],obj[0] };
					model.addRow(data);
				}
			}
		}
		return(jTable_showmarks);
	}

	// クリップボードアイテムの選択
	private JLabel getJLabel_clipboard(String s) {
		if (jLabel_clipboard == null) {
			jLabel_clipboard = new JLabel(s);
		}
		return jLabel_clipboard;
	}
	private JButton getJButton_clipboard_up(String s) {
		if (jButton_clipboard_up == null) {
			jButton_clipboard_up = new JButton(s);
			jButton_clipboard_up.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int row = 0;
					if ((row = jTable_clipboard.getSelectedRow()) <= 0) {
						return;
					}
					Object b = jTable_clipboard.getValueAt(row, 0);
					Object item = jTable_clipboard.getValueAt(row, 1);
					Object id = jTable_clipboard.getValueAt(row, 2);
					
					jTable_clipboard.setValueAt(jTable_clipboard.getValueAt(row-1, 0), row, 0);
					jTable_clipboard.setValueAt(jTable_clipboard.getValueAt(row-1, 1), row, 1);
					jTable_clipboard.setValueAt(jTable_clipboard.getValueAt(row-1, 2), row, 2);
					
					jTable_clipboard.setValueAt(b, row-1, 0);
					jTable_clipboard.setValueAt(item, row-1, 1);
					jTable_clipboard.setValueAt(id, row-1, 2);
					
					jTable_clipboard.setRowSelectionInterval(row-1, row-1);
				}
			});
		}
		return jButton_clipboard_up;
	}
	private JButton getJButton_clipboard_down(String s) {
		if (jButton_clipboard_down == null) {
			jButton_clipboard_down = new JButton(s);
			jButton_clipboard_down.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int row = 0;
					if ((row = jTable_clipboard.getSelectedRow()) >= jTable_clipboard.getRowCount()-1) {
						return;
					}
					Object b = jTable_clipboard.getValueAt(row, 0);
					Object item = jTable_clipboard.getValueAt(row, 1);
					Object id = jTable_clipboard.getValueAt(row, 2);
					
					jTable_clipboard.setValueAt(jTable_clipboard.getValueAt(row+1, 0), row, 0);
					jTable_clipboard.setValueAt(jTable_clipboard.getValueAt(row+1, 1), row, 1);
					jTable_clipboard.setValueAt(jTable_clipboard.getValueAt(row+1, 2), row, 2);
					
					jTable_clipboard.setValueAt(b, row+1, 0);
					jTable_clipboard.setValueAt(item, row+1, 1);
					jTable_clipboard.setValueAt(id, row+1, 2);
					
					jTable_clipboard.setRowSelectionInterval(row+1, row+1);
				}
			});
		}
		return jButton_clipboard_down;
	}
	private JScrollPane getJScrollPane_clipboard() {
		if (jScrollPane_clipboard == null) {
			jScrollPane_clipboard = new JScrollPane();
			jScrollPane_clipboard.setViewportView(getJTable_clipboard());
			jScrollPane_clipboard.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		}
		return(jScrollPane_clipboard);
	}
	private JNETable getJTable_clipboard() {
		if (jTable_clipboard == null) {
			// ヘッダの設定
			String[] colname = {"ﾁｪｯｸ", "アイテム", "ID"};
			int[] colwidth = {50,250,0};
			
			//
			DefaultTableModel model = new DefaultTableModel(colname, 0);
			jTable_clipboard = new JNETable(model, false) {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int row, int column) {
					return (column == 0);
				}
			};
			jTable_clipboard.setAutoResizeMode(JNETable.AUTO_RESIZE_OFF);
			DefaultTableColumnModel columnModel = (DefaultTableColumnModel)jTable_clipboard.getColumnModel();
			TableColumn column = null;
			for (int i = 0 ; i < columnModel.getColumnCount() ; i++){
				column = columnModel.getColumn(i);
				column.setPreferredWidth(colwidth[i]);
			}
			
			// にゃーん
			jTable_clipboard.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
   
			// エディタに手を入れる
			DefaultCellEditor editor = new DefaultCellEditor(new JCheckBox() {

				private static final long serialVersionUID = 1L;

				@Override
				public int getHorizontalAlignment() {
					return JCheckBox.CENTER;
				}
			});
			jTable_clipboard.getColumn("ﾁｪｯｸ").setCellEditor(editor);
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
			jTable_clipboard.getColumn("ﾁｪｯｸ").setCellRenderer(renderer);
			
			//
			for (ClipboardInfo cb : getCbItemEnv()) {
				Object[] data = { cb.getB(), cb.getItem(), cb.getId() };
				model.addRow(data);
			}
		}
		return(jTable_clipboard);
	}
	
	// 右クリックメニューの実行コマンドの追加
	private JLabel getJLabel_menuitem(String s) {
		if (jLabel_menuitem == null) {
			jLabel_menuitem = new JLabel(s);
		}
		return jLabel_menuitem;
	}
	private JTextField getJTextField_mikey() {
		if (jTextField_mikey == null) {
			jTextField_mikey = new JTextFieldWithPopup();
		}
		return (jTextField_mikey);
	}
	private JTextField getJTextField_mival() {
		if (jTextField_mival == null) {
			jTextField_mival = new JTextFieldWithPopup();
		}
		return (jTextField_mival);
	}
	private JComponent getJButton_miadd(String s) {
		if (jButton_miadd == null) {
			jButton_miadd = new JButton(s);
			jButton_miadd.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (jTextField_mikey.getText().length() > 0 && jTextField_mival.getText().length() > 0) {
						DefaultTableModel model = (DefaultTableModel) jTable_mitable.getModel();
						Object[] data = { jTextField_mikey.getText(),jTextField_mival.getText() };
						model.addRow(data);
						jTextField_mikey.setText("");
						jTextField_mival.setText("");
					}
				}
			});
		}
		return (jButton_miadd);
	}
	private JComponent getJButton_midel(String s) {
		if (jButton_midel == null) {
			jButton_midel = new JButton(s);
			jButton_midel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					DefaultTableModel model = (DefaultTableModel) jTable_mitable.getModel();
					int row = 0;
					if ((row = jTable_mitable.getSelectedRow()) >= 0) {
						jTextField_mikey.setText((String) model.getValueAt(row, 0));
						jTextField_mival.setText((String) model.getValueAt(row, 1));
						model.removeRow(row);
					}
				}
			});
		}
		return (jButton_midel);
	}
	private JButton getJButton_miup(String s) {
		if (jButton_miup == null) {
			jButton_miup = new JButton(s);
			jButton_miup.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int row = 0;
					if ((row = jTable_mitable.getSelectedRow()) <= 0) {
						return;
					}
					Object name = jTable_mitable.getValueAt(row, 0);
					Object cmd = jTable_mitable.getValueAt(row, 1);
					
					jTable_mitable.setValueAt(jTable_mitable.getValueAt(row-1, 0), row, 0);
					jTable_mitable.setValueAt(jTable_mitable.getValueAt(row-1, 1), row, 1);
					
					jTable_mitable.setValueAt(name, row-1, 0);
					jTable_mitable.setValueAt(cmd, row-1, 1);
					
					jTable_mitable.setRowSelectionInterval(row-1, row-1);
				}
			});
		}
		return jButton_miup;
	}
	private JButton getJButton_midown(String s) {
		if (jButton_midown == null) {
			jButton_midown = new JButton(s);
			jButton_midown.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int row = 0;
					if ((row = jTable_mitable.getSelectedRow()) >= jTable_mitable.getRowCount()-1) {
						return;
					}
					Object name = jTable_mitable.getValueAt(row, 0);
					Object cmd = jTable_mitable.getValueAt(row, 1);
					
					jTable_mitable.setValueAt(jTable_mitable.getValueAt(row+1, 0), row, 0);
					jTable_mitable.setValueAt(jTable_mitable.getValueAt(row+1, 1), row, 1);
					
					jTable_mitable.setValueAt(name, row+1, 0);
					jTable_mitable.setValueAt(cmd, row+1, 1);
					
					jTable_mitable.setRowSelectionInterval(row+1, row+1);
				}
			});
		}
		return jButton_midown;
	}
	private JScrollPane getJScrollPane_mitable(int col1_w, int col2_w) {
		if (jScrollPane_mitable == null) {
			jScrollPane_mitable = new JScrollPane();
			jScrollPane_mitable.setViewportView(getJTable_mitable(col1_w, col2_w));
			jScrollPane_mitable.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		}
		return (jScrollPane_mitable);
	}
	private JNETable getJTable_mitable(int col1_w, int col2_w) {
		if (jTable_mitable == null) {
			// ヘッダの設定
			String[] colname = {"コマンド名", "実行するコマンド"};
			int[] colwidth = {col1_w,col2_w};
			
			//
			DefaultTableModel model = new DefaultTableModel(colname, 0);
			jTable_mitable = new JNETable(model, false) {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int row, int column) {
					return (column == 0);
				}
			};
			jTable_mitable.setAutoResizeMode(JNETable.AUTO_RESIZE_OFF);
			DefaultTableColumnModel columnModel = (DefaultTableColumnModel)jTable_mitable.getColumnModel();
			TableColumn column = null;
			for (int i = 0 ; i < columnModel.getColumnCount() ; i++){
				column = columnModel.getColumn(i);
				column.setPreferredWidth(colwidth[i]);
			}
			
			// にゃーん
			jTable_mitable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
   
			//
			for (TextValueSet tv : env.getTvCommand()) {
				Object[] data = { tv.getText(), tv.getValue() };
				model.addRow(data);
			}
		}
		return(jTable_mitable);
	}
	
	//
	private JLabel getJLabel_ngword(String s) {
		if (jLabel_ngword == null) {
			jLabel_ngword = new JLabel(s);
		}
		return(jLabel_ngword);
	}
	private JTextField getJTextField_ngword(String s) {
		if (jTextField_ngword == null) {
			jTextField_ngword = new JTextFieldWithPopup();
			jTextField_ngword.setText(s);
			jTextField_ngword.setCaretPosition(0);
		}
		return jTextField_ngword;
	}

	private JLabel getJLabel_lookAndFeel(String s) {
		if (jLabel_lookAndFeel == null) {
			jLabel_lookAndFeel = new JLabel();
			jLabel_lookAndFeel.setText(s);
		}
		return jLabel_lookAndFeel;
	}
	private JComboBox getJComboBox_lookAndFeel() {
		if (jComboBox_lookAndFeel == null) {
			jComboBox_lookAndFeel = new JComboBox();

			// 初期値を設定
			DefaultComboBoxModel model = new DefaultComboBoxModel();
			jComboBox_lookAndFeel.setModel(model);
			for ( String className : getLAFEnv().getNames() ) {
				Matcher ma = Pattern.compile("\\.([^\\.]+?)LookAndFeel$").matcher(className);
				if ( ma.find() ) {
					model.addElement(ma.group(1));
				}
			}
			if ( ! env.getLookAndFeel().equals("")) {
				model.setSelectedItem(env.getLookAndFeel());
				//updateFont(env.getFontName(), env.getFontSize());
				StdAppendMessage("Set lookandfeel="+env.getLookAndFeel());
			}
			else {
				model.setSelectedItem(UIManager.getLookAndFeel().getName());
			}
			
			//
			jComboBox_lookAndFeel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DefaultComboBoxModel model = (DefaultComboBoxModel)((JComboBox)e.getSource()).getModel();
					lafChanged((String)model.getSelectedItem());
				}
			});

		}
		return jComboBox_lookAndFeel;
	}
	
	private JLabel getJLabel_font(String s) {
		if (jLabel_font == null) {
			jLabel_font = new JLabel();
			jLabel_font.setText(s);
		}
		return jLabel_font;
	}
	private JComboBox getJComboBox_font() {
		if (jComboBox_font == null) {
			jComboBox_font = new JComboBox();

			// 初期値を設定
			DefaultComboBoxModel model = new DefaultComboBoxModel();
			jComboBox_font.setModel(model);
			for ( String f : getFontEnv().getNames() ) {
				model.addElement(f);
			}
			if ( ! env.getFontName().equals("")) {
				model.setSelectedItem(env.getFontName());
				//updateFont(env.getFontName(), env.getFontSize());
				//StdAppendMessage("システムのフォント： "+env.getFontName());
			}
			else {
				model.setSelectedItem(jComboBox_font.getFont().getFontName());
			}
			
			//
			jComboBox_font.addActionListener(al_fontChanged);

		}
		return jComboBox_font;
	}
	private JComboBox getJComboBox_fontSize() {
		if (jComboBox_fontSize == null) {
			jComboBox_fontSize = new JComboBox();
			DefaultComboBoxModel model = new DefaultComboBoxModel();
			jComboBox_fontSize.setModel(model);
			for ( int i=6; i<=20; i++ ) {
				model.addElement(String.valueOf(i));
			}
			if ( env.getFontSize() > 0) {
				jComboBox_fontSize.setSelectedItem(String.valueOf(env.getFontSize()));
			}
			
			jComboBox_fontSize.addActionListener(al_fontChanged);
		}
		return(jComboBox_fontSize);
	}
	private JLabel getJLabel_fontSample(String s) {
		if (jLabel_fontSample == null) {
			jLabel_fontSample = new JLabel();
			jLabel_fontSample.setText("012０１２３abcＡＢＣあいうアイウ阿伊宇○×？");
			jLabel_fontSample.setBackground(Color.WHITE);
			jLabel_fontSample.setBorder(new LineBorder(Color.BLACK));
			jLabel_fontSample.setOpaque(true);
			Font f = jLabel_fontSample.getFont();
			jLabel_fontSample.setFont(new Font(env.getFontName(),f.getStyle(),env.getFontSize()));
		}
		return jLabel_fontSample;
	}
	
	private JLabel getJLabel_enableCHAVsetting(String s)
	{
		if (jLabel_enableCHAVsetting == null) {
			jLabel_enableCHAVsetting = new JLabel();
			jLabel_enableCHAVsetting.setText(s);
		}
		return(jLabel_enableCHAVsetting);
	}
	private JCheckBox getJCheckBox_enableCHAVsetting(boolean b) {
		if (jCheckBox_enableCHAVsetting == null) {
			jCheckBox_enableCHAVsetting = new JCheckBox();
			jCheckBox_enableCHAVsetting.setSelected(b);
		}
		return(jCheckBox_enableCHAVsetting);
	}
	
	private JLabel getJLabel_autoFolderSelect(String s)
	{
		if (jLabel_autoFolderSelect == null) {
			jLabel_autoFolderSelect = new JLabel();
			jLabel_autoFolderSelect.setText(s);
		}
		return(jLabel_autoFolderSelect);
	}
	private JCheckBox getJCheckBox_autoFolderSelect(boolean b) {
		if (jCheckBox_autoFolderSelect == null) {
			jCheckBox_autoFolderSelect = new JCheckBox();
			jCheckBox_autoFolderSelect.setSelected(b);
		}
		return(jCheckBox_autoFolderSelect);
	}


	//
	private JTextAreaWithPopup getJta_help() {
		if ( jta_help == null ) {
			jta_help = CommonSwingUtils.getJta(this,2,0);
			jta_help.setText(TEXT_HINT);
		}
		return jta_help;
	}
}
