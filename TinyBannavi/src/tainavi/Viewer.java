package tainavi;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import taiSync.ReserveInfo;
import tainavi.HDDRecorder.RecType;
import tainavi.SearchKey.TargetId;
import tainavi.TVProgram.ProgFlags;
import tainavi.TVProgram.ProgGenre;
import tainavi.TVProgram.ProgOption;
import tainavi.TVProgram.ProgSubgenre;
import tainavi.TVProgram.ProgSubtype;
import tainavi.TVProgram.ProgType;
import tainavi.VWMainWindow.MWinTab;
import tainavi.VWUpdate.UpdateResult;


/**
 * メインな感じ
 */
public class Viewer extends JFrame implements ChangeListener,VWTimerRiseListener {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void stateChanged(ChangeEvent e){
		StdAppendMessage("イベント発生");
	}

	
	/*
	 * メソッド的な
	 */
	
	private void StdAppendMessage(String message)	{ System.out.println(message); }
	private void StdAppendError(String message)		{ System.err.println(message); }
	// 
	private void MWinSetVisible(boolean b)			{ mwin.setVisible(b); }
	// 
	private void StWinClear()						{ stwin.clear(); }
	private void StWinSetVisible(boolean b)			{ stwin.setVisible(b); }
	private void StWinSetLocationCenter(Component frame) { CommonSwingUtils.setLocationCenter(frame, (VWStatusWindow)stwin); }
	private void StWinSetLocationUnder(Component frame)  { CommonSwingUtils.setLocationUnder(frame, (VWStatusWindow)stwin); }
	
	private void ringBeep() { if (env!=null && ! env.getDisableBeep()) { Toolkit.getDefaultToolkit().beep(); if ( env.getDebug() ) CommonUtils.printStackTrace(); } }
	
	
	/*
	 * オブジェクト的な
	 */
	
	// 設定値をいれるところ
	private final Env env = new Env();											// 主要な設定
	private final Bounds bounds = new Bounds();									// ウィンドウサイズとか動的に変化するもの
	private final ClipboardInfoList cbitems = new ClipboardInfoList();			// クリップボード対応機能でどの項目をコピーするかとかの設定 
	private final PaperColorsMap pColors = new PaperColorsMap();				// 新聞形式のジャンル別背景色の設定
	private final AVSetting avs = new AVSetting();								// ジャンル別録画画質・音質等設定
	private final CHAVSetting chavs = new CHAVSetting();						// CH別録画画質・音質等設定
	private final ChannelSort chsort = new ChannelSort();						// CHソート設定
	private final ChannelConvert chconv = new ChannelConvert();					// ChannelConvert.dat
	private final MarkChar markchar = new MarkChar(env);						// タイトルにつけるマークを操作する
	
	private final MarkedProgramList mpList = new MarkedProgramList();			// 検索結果のキャッシュ（表示高速化用）
	private final TraceProgram trKeys = new TraceProgram();						// 番組追跡の設定
	private final SearchProgram srKeys = new SearchProgram();					// キーワード検索の設定
	private final SearchGroupList srGrps = new SearchGroupList();				// キーワード検索グループの設定
	private final ExtProgram extKeys = new ExtProgram();						// 延長警告管理の設定
	
	private final RecorderInfoList recInfoList = new RecorderInfoList();		// レコーダ一覧の設定
	
	private final HDDRecorderList recPlugins = new HDDRecorderList();			// レコーダプラグイン（テンプレート）
	private final HDDRecorderList recorders = new HDDRecorderList();			// レコーダプラグイン（実際に利用するもの）
	
	private final TVProgramList progPlugins = new TVProgramList();				// Web番組表プラグイン（テンプレート）
	private final TVProgramList tvprograms = new TVProgramList();				// Web番組表プラグイン（実際に利用するもの）
	
	private final VWTimer timer_now = new VWTimer();							// 毎分00秒に起動して処理をキックするタイマー
	
	// 初期化的な
	private boolean logging = true;											// ログ出力する
	private boolean runRecWakeup = false;										// 起動時にレコーダを起こす
	private boolean runRecLoad = false;											// 起動時にレコーダから予約一覧を取得する
	private boolean enableWebAccess = true;										// 起動時のWeb番組表へのアクセスを禁止する
	private boolean onlyLoadProgram = false;
	private String pxaddr = null;												// ProxyAddress指定
	private String pxport = null;												// ProxtPort指定
	
	
	/*******************************************************************************
	 * 定数
	 ******************************************************************************/
	
	public static final String LOG_FILE = "log.txt";							// ログファイル名
	public static final String HISTORY_FILE = "05_history.txt";				// 更新履歴だよ
	
	private static final String ICONFILE_SYSTRAY = "icon"+File.separator+"tainavi16.png";
	private static final String ICONFILE_TAINAVI = "icon"+File.separator+"tainavi.png";
	
	public static final int TIMEBAR_START = 5;					// 新聞形式の開始時刻
	private static final int OPENING_WIAT = 500;				// まあ起動時しか使わないんですけども

	private static final String MSGID = "[鯛ナビ] ";
	private static final String ERRID = "[ERROR]"+MSGID;
	private static final String DBGID = "[DEBUG]"+MSGID;
	
	/**
	 * Web番組表のどれとどれを読めばいいのか
	 */
	public static enum LoadFor {
		TERRA	("地上波&BSのみ取得"),
		CS		("CSのみ取得"),
		CSo1	("CS[プライマリ]のみ取得"),
		CSo2	("CS[セカンダリ]のみ取得"),
		CSwSD	("CSのみ取得（取得後シャットダウン）"),
		RADIO	("ラジオのみ取得"),
		SYOBO	("しょぼかるのみ取得"),
		ALL		("すべて取得");
		
		private String name;
		
		private LoadFor(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
		
		public static LoadFor get(String s) {
			for ( LoadFor lf : LoadFor.values() ) {
				if ( lf.name.equals(s) ) {
					return lf;
				}
			}
			return null;
		}
	};

	/**
	 * レコーダ情報のどれとどれを読めばいいのか
	 */
	public static enum LoadRsvedFor {
		SETTING		( "設定情報のみ取得(future use.)" ),
		RECORDED	( "録画結果のみ取得" ),
		;
		
		private String name;
		
		private LoadRsvedFor(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public static LoadRsvedFor get(String s) {
			for ( LoadRsvedFor lrf : LoadRsvedFor.values() ) {
				if ( lrf.name.equals(s) ) {
					return lrf;
				}
			}
			return null;
		}
	}
	
	/**
	 *  リスト形式のカラム定義
	 * @deprecated しっぱいした 半年くらいしたら削除する
	 */
	public static enum ListedColumn {
		RSVMARK		("予約",			35),
		DUPMARK		("重複",			35),
		CHNAME		("チャンネル名",	100),
		TITLE		("番組タイトル",	300),
		DETAIL		("番組詳細",		200),
		START		("開始時刻",		150),
		END			("終了",			50),
		LENGTH		("長さ",			50),
		GENRE		("ジャンル",		85),
		SITEM		("検索アイテム名",	100),
		STAR		("お気に入り度",	100),
		SCORE		("ｽｺｱ",			35),
		THRESHOLD	("閾値",			35),
		HID_PRGID	("PRGID",		-1),
		HID_STIME	("STIME",		-1),
		HID_ETIME	("ETIME",		-1),
		HID_EXFLG	("EXFLG",		-1),
		HID_TITLE	("TITLE",		-1),
		;

		@SuppressWarnings("unused")
		private String name;
		private int iniWidth;

		private ListedColumn(String name, int iniWidth) {
			this.name = name;
			this.iniWidth = iniWidth;
		}

		/* なんだかなー
		@Override
		public String toString() {
			return name;
		}
		*/

		public int getIniWidth() {
			return iniWidth;
		}
		
		public int getColumn() {
			return ordinal();
		}
	};
	
	/**
	 *  本体予約一覧のカラム定義
	 * @deprecated しっぱいした 半年くらいしたら削除する
	 */
	public static enum RsvedColumn {
		PATTERN		("パタン",			110),
		DUPMARK		("重複",			35),
		EXEC		("実行",			35),
		TRACE		("追跡",			35),
		NEXTSTART	("次回実行予定",	150),
		END			("終了",			50),
		LENGTH		("長さ",			50),
		ENCODER		("ｴﾝｺｰﾀﾞ",		50),
		VRATE		("画質",			100),
		ARATE		("音質",			50),
		TITLE		("番組タイトル",	300),
		CHNAME		("チャンネル名",	150),
		RECORDER	("レコーダ",		200),
		HID_INDEX	("INDEX",		-1),
		HID_RSVID	("RSVID",		-1),
		;

		@SuppressWarnings("unused")
		private String name;
		private int iniWidth;

		private RsvedColumn(String name, int iniWidth) {
			this.name = name;
			this.iniWidth = iniWidth;
		}

		/*
		@Override
		public String toString() {
			return name;
		}
		*/

		public int getIniWidth() {
			return iniWidth;
		}
		
		public int getColumn() {
			return ordinal();
		}
	};
	
	
	
	/*
	 * コンポーネント
	 */
	
	// 起動時に固定で用意しておくもの
	private final VWStatusWindow stwin = new VWStatusWindow();
	private final VWStatusTextArea mwin = new VWStatusTextArea();
	private final VWColorChooserDialog ccwin = new VWColorChooserDialog();
	private final VWPaperColorsDialog pcwin = new VWPaperColorsDialog();
	private final VWReserveDialog rdialog = new VWReserveDialog(0, 0);
	
	// 初期化処理の中で生成していくもの
	private VWMainWindow mainWindow = null;
	private VWToolBar toolBar = null;
	private VWListedView listed = null;
	private VWPaperView paper = null;
	private VWReserveListView reserved = null;
	private VWRecordedListView recorded = null;
	private VWAutoReserveListView autores = null;
	private VWSettingView setting = null;
	private VWRecorderSettingView recsetting = null;
	private VWChannelSettingView chsetting = null;
	private VWChannelDatSettingView chdatsetting = null;
	private VWChannelSortView chsortsetting = null;
	private VWChannelConvertView chconvsetting = null;
	private VWLookAndFeel vwlaf = null;
	private VWFont vwfont = null;
	
	private TrayIcon trayicon = null;
	
	
	
	/*******************************************************************************
	 * タブやダイアログのインスタンス作成用クラス定義
	 ******************************************************************************/
	
	/***
	 * リスト形式の内部クラス
	 */
	private class VWListedView extends AbsListedView {

		private static final long serialVersionUID = 1L;

		// 環境設定の入れ物を渡す
		@Override
		protected Env getEnv() { return env; }
		@Override
		protected Bounds getBoundsEnv() { return bounds; }
		@Override
		protected ChannelSort getChannelSort() { return chsort; }

		@Override
		protected MarkedProgramList getMarkedProgramList() { return mpList; }
		@Override
		protected TraceProgram getTraceProgram() { return trKeys; }
		@Override
		protected SearchProgram getSearchProgram() { return srKeys; }
		@Override
		protected SearchGroupList getSearchGroupList() { return srGrps; }
		@Override
		protected ExtProgram getExtProgram() { return extKeys; }

		@Override
		protected TVProgramList getTVProgramList() { return tvprograms; }
		@Override
		protected HDDRecorderList getRecorderList() { return recorders; }

		// メッセージ出力関連
		@Override
		protected StatusWindow getStWin() { return stwin; }
		@Override
		protected StatusTextArea getMWin() { return mwin; }
		
		// コンポーネントを渡す
		@Override
		protected AbsReserveDialog getReserveDialog() { return rdialog; }
		@Override
		protected Component getParentComponent() { return Viewer.this; }

		@Override
		protected void ringBeep() { Viewer.this.ringBeep(); }
		
		/*
		 * AbsListedView内でのイベントから呼び出されるメソッド群
		 */

		@Override
		protected void onShown() {
			// キーワード登録ボタンはリスト形式のみ
			toolBar.setAddkeywordEnabled(true);
			// 一括予約はリスト形式のみ
			toolBar.setBatchReservationEnabled(true);
			// スナップショットを有効にする
			toolBar.setSnapShotEnabled(true);
		}

		@Override
		protected void onHidden() {
			// キーワード登録ボタンはリスト形式のみ
			toolBar.setAddkeywordEnabled(false);
			// 一括予約はリスト形式のみ
			toolBar.setBatchReservationEnabled(false);
			// スナップショットを無効にする
			toolBar.setSnapShotEnabled(false);
		}

		@Override
		protected void showPopupForTraceProgram(
				final JComponent comp,
				final ProgDetailList tvd, final String keyword, final int threshold,
				final int x, final int y, final int h) {
			
			timer_now.pause();
			Viewer.this.showPopupForTraceProgram(comp, tvd, keyword, threshold, x, y, h);
			timer_now.start();
		}

		@Override
		protected void updateReserveDisplay(String chname) {
			timer_now.pause();
			paper.updateReserveBorder(chname);
			reserved.redrawReservedList();
			timer_now.start();
		}

		@Override
		protected void updateBangumiColumns() {
			timer_now.pause();
			paper.updateBangumiColumns();
			timer_now.start();
		}

		@Override
		protected void clearPaper() {
			timer_now.pause();
			paper.clearPanel();
			timer_now.start();
		}

		@Override
		protected void previewKeywordSearch(SearchKey search) {
			//timer_now.pause();
			if (search.alTarget.size() > 0) {
				mainWindow.setSelectedTab(MWinTab.LISTED);
				listed.redrawListByPreview(search);
			}
			//timer_now.start();
		}

		@Override
		protected void jumpToPaper(String Center, String StartDateTime) {
			//timer_now.pause();
			paper.jumpToBangumi(Center,StartDateTime);
			//timer_now.start();
		}

		@Override
		protected boolean addToPickup(ProgDetailList tvd) { return Viewer.this.addToPickup(tvd); }

		@Override
		protected boolean isTabSelected(MWinTab tab) { return mainWindow.isTabSelected(tab); }
		@Override
		protected void setSelectedTab(MWinTab tab) { mainWindow.setSelectedTab(tab); }

		@Override
		protected String getSelectedRecorderOnToolbar() { return toolBar.getSelectedRecorder(); }
		@Override
		protected boolean isFullScreen() { return toolBar.isFullScreen(); }
		@Override
		protected void setPagerEnabled(boolean b) { toolBar.setPagerEnabled(b); }
		@Override
		protected int getPagerCount() { return toolBar.getPagerCount(); }
		@Override
		protected int getSelectedPagerIndex() { return toolBar.getSelectedPagerIndex(); }

		@Override
		protected void setDividerEnvs(int loc) {
			if ( ! toolBar.isFullScreen() && mainWindow.isTabSelected(MWinTab.LISTED) ) {
				if (env.getSyncTreeWidth()) {
					bounds.setTreeWidth(loc);
					bounds.setTreeWidthPaper(loc);
				}
				else {
					bounds.setTreeWidth(loc);
				}
			}
		}
	}
	
	
	
	/**
	 * 新聞形式の内部クラス
	 */
	private class VWPaperView extends AbsPaperView {

		private static final long serialVersionUID = 1L;

		// 環境設定の入れ物を渡す
		@Override
		protected Env getEnv() { return env; }
		@Override
		protected Bounds getBoundsEnv() { return bounds; }
		@Override
		protected PaperColorsMap getPaperColorMap() { return pColors; }
		@Override
		protected ChannelSort getChannelSort() { return chsort; }
		
		@Override
		protected TVProgramList getTVProgramList() { return tvprograms; }
		@Override
		protected HDDRecorderList getRecorderList() { return recorders; }

		// メッセージ出力関連
		@Override
		protected StatusWindow getStWin() { return stwin; }
		@Override
		protected StatusTextArea getMWin() { return mwin; }
		
		// コンポーネントを渡す
		@Override
		protected AbsReserveDialog getReserveDialog() { return rdialog; }
		@Override
		protected Component getParentComponent() { return Viewer.this; }

		@Override
		protected void ringBeep() { Viewer.this.ringBeep(); }
		
		/*
		 * AbsPaperView内でのイベントから呼び出されるメソッド群
		 */

		@Override
		protected void onShown() {
			// ページャーコンボボックスを有効にする（状況次第で有効にならない場合もある）（ツリーの選択次第で変わるのでもどし）
			//toolBar.setPagerEnabled(true);
			// スナップショットを有効にする
			toolBar.setSnapShotEnabled(true);
			// ジャンル別背景色を有効にする
			toolBar.setPaperColorDialogEnabled(true);
			// マッチ枠を有効にする
			toolBar.setBorderToggleEnabled(true);
		}

		@Override
		protected void onHidden() {
			// 新聞形式以外ではページャーコンボボックスを無効にする（ツリーの選択次第で変わるのでもどし）
			//toolBar.setPagerEnabled(false);
			// 新聞形式以外ではスナップショットを無効にする
			toolBar.setSnapShotEnabled(false);
			// 新聞形式以外ではジャンル別背景色を無効にする
			toolBar.setPaperColorDialogEnabled(false);
			// 新聞形式以外ではマッチ枠を無効にする
			toolBar.setBorderToggleEnabled(false);
		}

		@Override
		protected void showPopupForTraceProgram(
				final JComponent comp,
				final ProgDetailList tvd, final String keyword, final int threshold,
				final int x, final int y, final int h) {
			
			timer_now.pause();
			Viewer.this.showPopupForTraceProgram(comp, tvd, keyword, threshold, x, y, h);
			timer_now.start();
		}

		@Override
		protected void updateReserveDisplay() {
			timer_now.pause();
			listed.updateReserveMark();
			reserved.redrawReservedList();
			timer_now.start();
		}

		@Override
		protected void addToPickup(ProgDetailList tvd) { Viewer.this.addToPickup(tvd); }

		@Override
		protected boolean isTabSelected(MWinTab tab) { return mainWindow.isTabSelected(tab); }
		@Override
		protected void setSelectedTab(MWinTab tab) { mainWindow.setSelectedTab(tab); }

		@Override
		protected String getSelectedRecorderOnToolbar() { return toolBar.getSelectedRecorder(); }
		@Override
		protected boolean isFullScreen() { return toolBar.isFullScreen(); }
		@Override
		protected void setSelectedPagerIndex(int idx) {
			toolBar.setSelectedPagerIndex(idx);
		}
		@Override
		protected void setPagerEnabled(boolean b) { toolBar.setPagerEnabled(b); }
		@Override
		protected int getPagerCount() { return toolBar.getPagerCount(); }
		@Override
		protected int getSelectedPagerIndex() { return toolBar.getSelectedPagerIndex(); }
		@Override
		protected void setPagerItems(TVProgramIterator pli, int curindex) {
			toolBar.setPagerItems(pli,curindex);
		}

		@Override
		protected String getExtensionMark(ProgDetailList tvd) { return markchar.getExtensionMark(tvd); }
		@Override
		protected String getOptionMark(ProgDetailList tvd) { return markchar.getOptionMark(tvd)+markchar.getNewLastMark(tvd); }
		@Override
		protected String getPostfixMark(ProgDetailList tvd) { return markchar.getPostfixMark(tvd); }

		@Override
		protected void setDividerEnvs(int loc) {
			if ( ! toolBar.isFullScreen() && mainWindow.isTabSelected(MWinTab.PAPER) ) {
				if (env.getSyncTreeWidth()) {
					bounds.setTreeWidth(loc);
					bounds.setTreeWidthPaper(loc);
				}
				else {
					bounds.setTreeWidthPaper(loc);
				}
			}
		}
	}
	
	
	
	/**
	 * 
	 * 本体予約一覧の内部クラス
	 * 
	 */
	private class VWReserveListView extends AbsReserveListView {

		private static final long serialVersionUID = 1L;

		// 環境設定の入れ物を渡す
		@Override
		protected Env getEnv() { return env; }
		@Override
		protected Bounds getBoundsEnv() { return bounds; }

		@Override
		protected HDDRecorderList getRecorderList() { return recorders; }

		// ログ関係はないのか
		
		// コンポーネントを渡す
		@Override
		protected AbsReserveDialog getReserveDialog() { return rdialog; }
		@Override
		protected Component getParentComponent() { return Viewer.this; }

		@Override
		protected void ringBeep() { Viewer.this.ringBeep(); }

		/*
		 * AbsReserveListView内でのイベントから呼び出されるメソッド群
		 */
		
		@Override
		protected void updateReserveDisplay(String chname) {
			timer_now.pause();
			listed.updateReserveMark();
			paper.updateReserveBorder(chname);
			timer_now.start();
		}

		@Override
		protected boolean doExecOnOff(boolean fexec, String title, String chnam, String rsvId, String recId) {
			return Viewer.this.doExecOnOff(fexec, title, chnam, rsvId, recId);
		}
		
		@Override
		protected JMenuItem getExecOnOffMenuItem(boolean fexec, String title,
				String chnam, String rsvId, String recId) {

			return Viewer.this.getExecOnOffMenuItem(fexec, title, chnam, rsvId, recId, 0);
		}

		@Override
		protected JMenuItem getRemoveRsvMenuItem(String title, String chnam,
				String rsvId, String recId) {
			
			return Viewer.this.getRemoveRsvMenuItem(title, chnam, rsvId, recId, 0);
		}

		@Override
		protected JMenuItem getJumpMenuItem(String title, String chnam,
				String startDT) {
			
			return Viewer.this.getJumpMenuItem(title, chnam, startDT);
		}

		@Override
		protected JMenuItem getJumpToLastWeekMenuItem(String title,
				String chnam, String startDT) {
			
			return Viewer.this.getJumpToLastWeekMenuItem(title, chnam, startDT);
		}

		@Override
		protected String getSelectedRecorderOnToolbar() { return toolBar.getSelectedRecorder(); }
	}
	
	
	/**
	 * 
	 * 録画結果一覧の内部クラス
	 * 
	 */
	private class VWRecordedListView extends AbsRecordedListView {

		private static final long serialVersionUID = 1L;

		// 環境設定の入れ物を渡す
		@Override
		protected Env getEnv() { return env; }
		@Override
		protected Bounds getBoundsEnv() { return bounds; }

		@Override
		protected HDDRecorderList getRecorderList() { return recorders; }

		// ログ関係はないのか
		
		// コンポーネントを渡す
		@Override
		protected Component getParentComponent() { return Viewer.this; }

		@Override
		protected void ringBeep() { Viewer.this.ringBeep(); }

		/*
		 * AbsReserveListView内でのイベントから呼び出されるメソッド群
		 */
		
		@Override
		protected String getSelectedRecorderOnToolbar() { return toolBar.getSelectedRecorder(); }
	}
	
	
	/**
	 * 
	 * 録画結果一覧の内部クラス
	 * 
	 */
	private class VWAutoReserveListView extends AbsAutoReserveListView {

		private static final long serialVersionUID = 1L;

		// 環境設定の入れ物を渡す
		@Override
		protected Env getEnv() { return env; }
		@Override
		protected Bounds getBoundsEnv() { return bounds; }
		
	}
	
	/***
	 * 各種設定の内部クラス
	 */
	private class VWSettingView extends AbsSettingView {

		private static final long serialVersionUID = 1L;

		// 環境設定の入れ物を渡す
		@Override
		protected Env getEnv() { return env; }
		@Override
		protected ClipboardInfoList getCbItemEnv() { return cbitems; }
		@Override
		protected VWLookAndFeel getLAFEnv() { return vwlaf; }
		@Override
		protected VWFont getFontEnv() { return vwfont; }
		
		// メッセージ出力関連
		@Override
		protected StatusWindow getStWin() { return stwin; }
		@Override
		protected StatusTextArea getMWin() { return mwin; }
		
		// コンポーネントを渡す
		@Override
		protected Component getParentComponent() { return Viewer.this; }
		@Override
		protected VWColorChooserDialog getCcWin() { return ccwin; }

		/*
		 * AbsSettingView内でのイベントから呼び出されるメソッド群
		 */
		
		@Override
		protected void lafChanged(String lafname) {
			vwlaf.update(lafname);
			Viewer.this.updateComponentTreeUI();
			StdAppendMessage("Set LookAndFeel="+lafname);
		}

		@Override
		protected void fontChanged(String fn, int fontSize) {
			vwfont.update(fn, fontSize);
			Viewer.this.updateComponentTreeUI();
			StdAppendMessage("システムのフォントを変更しました： "+fn+", size="+fontSize);
		}

		@Override
		protected void setEnv(final boolean reload_prog) {
			
			//listed.pauseTimer();
			timer_now.pause();
			
			bounds.save();
			cbitems.save();
			env.save();

			// CommonUtilsの設定変更
			CommonUtils.setAdjLateNight(env.getAdjLateNight());
			CommonUtils.setExpandTo8(env.getExpandTo8());
			CommonUtils.setUseRundll32(env.getUseRundll32());
			CommonUtils.setDisplayPassedReserve(env.getDisplayPassedReserve());
			CommonUtils.setDebug(env.getDebug());
			
			SwingBackgroundWorker.setDebug(env.getDebug());

			// ほにゃらら
			toolBar.setDebug(env.getDebug());
			autores.setDebug(env.getDebug());

			// PassedProgramListの設定変更
			tvprograms.getPassed().setPassedDir(env.getPassedDir());

			// レコーダプラグインの設定変更
			for ( HDDRecorder rec : recorders ) {
				// 拡張設定だけ
				setSettingRecPluginExt(rec, env);
			}

			// Web番組表共通設定
			setSettingProgPluginCommon(env);
			
			// web番組表のリフレッシュ
			setSettingProgPluginAll(env);
			
			// リロードメニューの書き換え
			toolBar.updateReloadProgramExtention();
			
			// ページャーコンボボックスの書き換え
			toolBar.setPagerItems();
			
			// 列の表示・非表示
			listed.setMarkColumnVisible(env.getSplitMarkAndTitle());
			listed.setDetailColumnVisible(env.getShowDetailOnList());
			listed.setRowHeaderVisible(env.getRowHeaderVisible());
			reserved.setRowHeaderVisible(env.getRowHeaderVisible());
			
			// 強調色
			listed.setMatchedKeywordColor(env.getMatchedKeywordColor());
			listed.setRsvdLineColor((env.getRsvdLineEnhance())?(env.getRsvdLineColor()):(null));
			listed.setPickedLineColor((env.getRsvdLineEnhance())?(env.getPickedLineColor()):(null));
			listed.setCurrentLineColor((env.getCurrentLineEnhance())?(env.getCurrentLineColor()):(null));
			
			// システムトレイアイコン
			setTrayIconVisible(env.getShowSysTray());
			setXButtonAction(env.getShowSysTray() && env.getHideToTray());
			
			// 新聞形式のツールチップの表示時間を変更する
			setTooltipDelay();
			
			// Web番組表の再構築
			mpList.setHistoryOnlyUpdateOnce(env.getHistoryOnlyUpdateOnce());
			mpList.setShowOnlyNonrepeated(env.getShowOnlyNonrepeated());
			
			// 番組情報の再取得
			if ( reload_prog ) {
				loadTVProgram(false,LoadFor.ALL);	// 部品呼び出し
			}
			
			// 新聞描画枠のリセット
			paper.clearPanel();
			paper.buildMainViewByDate();
			
			// 再度ツリーの再構築
			paper.redrawTreeByDate();
			paper.redrawTreeByPassed();
			
			listed.redrawTreeByHistory();
			listed.redrawTreeByCenter();
			
			// 再描画
			paper.reselectTree();
			listed.reselectTree();

			//listed.continueTimer();	// まあreselectTree()で再開しているはずだが
			timer_now.start();
		}
	}
	
	/**
	 * レコーダ設定タブの内部クラス
	 * @see AbsRecorderSettingView
	 */
	private class VWRecorderSettingView extends AbsRecorderSettingView {

		private static final long serialVersionUID = 1L;

		// 環境設定の入れ物を渡す
		@Override
		protected Env getEnv() { return env; }
		@Override
		protected RecorderInfoList getRecInfos() { return recInfoList; }
		@Override
		protected HDDRecorderList getRecPlugins() { return recPlugins; }

		// ログ関連
		@Override
		protected VWStatusWindow getStWin() { return stwin; }
		@Override
		protected StatusTextArea getMWin() { return mwin; }

		// コンポーネントを渡す
		@Override
		protected Component getParentComponent() { return Viewer.this; }
		@Override
		protected VWColorChooserDialog getCcWin() { return ccwin; }

		@Override
		protected void ringBeep() { Viewer.this.ringBeep(); }

		/*
		 * AbsRecorderSettingView内でのイベントから呼び出されるメソッド群
		 */
		
		@Override
		protected void setRecInfos() {
			
			timer_now.pause();
			
			// 設定を保存
			recInfoList.save();
			
			// レコーダプラグインのリフレッシュ
			initRecPluginAll();
			
			// レコーダ一覧をツールバーに設定
			toolBar.updateRecorderComboBox();
			
			// 予約一覧のリフレッシュ
			loadRdReserve(false, null);		// toolBarの内容がリセットされているので recId = null で
			
			// レコーダのエンコーダ表示の更新
			this.redrawRecorderEncoderEntry();

			// レコーダ一覧をCHコード設定のコンボボックスに設定 
			chdatsetting.updateRecorderComboBox();
			
			// Web番組表の再構築（予約マークのリフレッシュ）
			paper.updateReserveBorder(null);
			listed.updateReserveMark();
			
			timer_now.start();
		}

	}

	
	/***
	 * CH設定の内部クラス
	 */
	private class VWChannelSettingView extends AbsChannelSettingView {

		private static final long serialVersionUID = 1L;
		
		// 環境設定の入れ物を渡す
		@Override
		protected Env getEnv() { return Viewer.this.env; }
		@Override
		protected TVProgramList getProgPlugins() { return progPlugins; }

		// ログ関連
		@Override
		protected StatusWindow getStWin() { return stwin; }
		@Override
		protected StatusTextArea getMWin() { return mwin; }

		// コンポーネントを渡す
		@Override
		protected Component getParentComponent() { return Viewer.this; }
		@Override
		protected VWColorChooserDialog getCcWin() { return ccwin; }

		@Override
		protected void ringBeep() {
			Viewer.this.ringBeep();
		}
		@Override
		protected void updateProgPlugin() {
			
			timer_now.pause();
			
			// 設定を保存（プラグイン内部の設定はChannelSettingPanel内で実施）
			env.save();
			
			// Web番組表プラグインのリフレッシュ
			setSelectedProgPlugin();
			initProgPluginAll();
			
			// CHソート設定に反映
			chsortsetting.updateChannelSortTable();
			
			// CHコンバート設定をリフレッシュ
			chconvsetting.updateChannelConvertTable();
			
			// CHコード設定にも反映
			chdatsetting.updateChannelDatTable();

			// 番組情報の再取得
			loadTVProgram(false,LoadFor.ALL);	// 部品呼び出し
			
			// ツールバーに反映
			toolBar.setPagerItems();
			
			// 新聞描画枠のリセット
			paper.clearPanel();
			paper.buildMainViewByDate();
			
			// サイドツリーの再構築
			paper.redrawTreeByCenter();
			
			listed.redrawTreeByCenter();
			
			// 再構築
			paper.reselectTree();
			listed.reselectTree();
			
			timer_now.start();
		}
		
	}
	
	/***
	 * CHコード設定の内部クラス
	 */
	private class VWChannelDatSettingView extends AbsChannelDatSettingView {

		private static final long serialVersionUID = 1L;

		// 環境設定の入れ物を渡す
		@Override
		protected Env getEnv() { return Viewer.this.env; }
		@Override
		protected TVProgramList getTVProgramList() { return tvprograms; }
		@Override
		protected ChannelSort getChannelSort() { return chsort; }
		@Override
		protected HDDRecorderList getHDDRecorderList() { return recorders; }

		// ログ関連
		@Override
		protected StatusWindow getStWin() { return stwin; }
		@Override
		protected StatusTextArea getMWin() { return mwin; }

		// コンポーネントを渡す
		@Override
		protected Component getParentComponent() { return Viewer.this; }
		
		@Override
		protected void ringBeep() {
			Viewer.this.ringBeep();
		}
		
	}

	/**
	 * CHソート設定タブの内部クラス
	 */
	private class VWChannelSortView extends AbsChannelSortView {

		private static final long serialVersionUID = 1L;

		@Override
		protected Env getEnv() { return Viewer.this.env; }
		@Override
		protected TVProgramList getTVProgramList() { return tvprograms; }
		@Override
		protected ChannelSort getChannelSort() { return chsort; }
		
		// ログ関連
		@Override
		protected StatusTextArea getMWin() { return mwin; }
		
		@Override
		protected void updProc() {
			
			timer_now.pause();
			
			env.save();
			
			toolBar.setPagerItems();
			toolBar.setSelectedPagerIndex(toolBar.getSelectedPagerIndex());
			
			// 新聞描画枠のリセット
			paper.clearPanel();
			paper.buildMainViewByDate();
			
			// サイドツリーの再構築
			paper.redrawTreeByCenter();
			
			listed.redrawTreeByCenter();
			
			// 再描画 
			paper.reselectTree();
			listed.reselectTree();
			
			timer_now.start();
		}
	}
	
	/**
	 * CHｺﾝﾊﾞｰﾄ設定タブの内部クラス
	 */
	private class VWChannelConvertView extends AbsChannelConvertView {

		private static final long serialVersionUID = 1L;

		// 環境設定の入れ物を渡す
		@Override
		protected Env getEnv() { return env; }
		@Override
		protected TVProgramList getProgPlugins() { return progPlugins; }
		@Override
		protected ChannelConvert getChannelConvert() { return chconv; }
		
	}
	
	/***
	 * 予約ウィンドウの内部クラス
	 */
	private class VWReserveDialog extends AbsReserveDialog {

		private static final long serialVersionUID = 1L;

		// コンストラクタ
		public VWReserveDialog(int x, int y) {
			super(x, y);
		}

		// 環境設定の入れ物を渡す
		@Override
		protected Env getEnv() { return env; }
		@Override
		protected TVProgramList getTVProgramList() { return tvprograms; }
		@Override
		protected HDDRecorderList getRecorderList() { return recorders; }
		@Override
		protected AVSetting getAVSetting() { return avs; }
		@Override
		protected CHAVSetting getCHAVSetting() { return chavs; }

		// ログ関連
		@Override
		protected StatusWindow getStWin() { return stwin; }
		@Override
		protected StatusTextArea getMWin() { return mwin; }
		
		// コンポーネントを渡す
		@Override
		protected Component getParentComponent() { return Viewer.this; }

		@Override
		protected void ringBeep() { Viewer.this.ringBeep(); }

		/*
		 * ReserveDialog内でのイベントから呼び出されるメソッド群
		 */
		
		@Override
		protected LikeReserveList findLikeReserves(ProgDetailList tvd, String keyword, int threshold) {
			return Viewer.this.findLikeReserves(tvd, keyword, threshold);
		}
	}
	
	/**
	 * 新聞の表示形式を操作するダイアログ
	 */
	private class VWPaperColorsDialog extends AbsPaperColorsDialog {

		private static final long serialVersionUID = 1L;

		@Override
		protected Env getEnv() { return env; }
		@Override
		protected Bounds getBoundsEnv() { return bounds; }
		@Override
		protected PaperColorsMap getPaperColorMap() { return pColors; }
		
		@Override
		protected VWColorChooserDialog getCCWin() { return ccwin; }
		
		/*
		 * PaperColorsDialog内でのイベントから呼び出されるメソッド群
		 */
		
		// 背景色設定の反映
		@Override
		protected void updatePaperColors(Env ec,PaperColorsMap pc) {
			paper.updateColors(ec,pc);
		}

		// フォント設定の反映
		@Override
		protected void updatePaperFonts(Env ec) {
			paper.updateFonts(ec);
		}

		// サイズ設定の反映
		@Override
		protected void updatePaperBounds(Env ec, Bounds bc) {
			paper.updateBounds(ec,bc);
		}
		
		// 再描画？
		@Override
		protected void updatePaperRepaint() {
			paper.updateRepaint();
		}
	}
	
	/**
	 * キーワード検索ウィンドウの内部クラス
	 */
	private class VWKeywordDialog extends AbsKeywordDialog {

		private static final long serialVersionUID = 1L;

		@Override
		void preview(SearchKey search) {
			// 検索実行
			if (search.alTarget.size() > 0) {
				mainWindow.setSelectedTab(MWinTab.LISTED);
				listed.redrawListByPreview(search);
			}
		}
	}

	/**
	 * 延長警告管理ウィンドウの内部クラス
	 */
	private class VWExtensionDialog extends AbsExtensionDialog {

		private static final long serialVersionUID = 1L;

		@Override
		void preview(SearchKey search) {
			// 検索実行
			if (search.alTarget.size() > 0) {
				mainWindow.setSelectedTab(MWinTab.LISTED);
				listed.redrawListByPreview(search);
			}
		}
	}
	
	/***
	 * 
	 * ツールバーの内部クラス
	 * 
	 */
	private class VWToolBar extends AbsToolBar {

		private static final long serialVersionUID = 1L;

		@Override
		protected Env getEnv() { return env; }
		@Override
		protected Bounds getBoundsEnv() { return bounds; }
		@Override
		protected TVProgramList getTVPrograms() { return tvprograms; }
		@Override
		protected ChannelSort getChannelSort() { return chsort; }
		@Override
		protected HDDRecorderList getHDDRecorders() { return recorders; }

		@Override
		protected StatusWindow getStWin() { return stwin; }
		@Override
		protected StatusTextArea getMWin() { return mwin; }
		@Override
		protected Component getParentComponent() { return Viewer.this; }

		@Override
		protected void ringBeep() { Viewer.this.ringBeep(); }

		@Override
		protected boolean doKeywordSerach(SearchKey search, String kStr, String sStr, boolean doFilter) {
			
			timer_now.pause();
			
			if ( mainWindow.getSelectedTab() == MWinTab.RSVED ) {
				reserved.redrawListByKeywordFilter(search, kStr);
			}
			else if ( mainWindow.getSelectedTab() == MWinTab.RECED ) {
				recorded.redrawListByKeywordFilter(search, kStr);
			}
			else {
				if ( search != null ) {
					mainWindow.setSelectedTab(MWinTab.LISTED);
					if ( doFilter ) {
						// 絞り込み検索
						listed.clearSelection();
						listed.redrawListByKeywordFilter(search, kStr);
					}
					else if (sStr != null) {
						// 過去ログ検索
						searchPassedProgram(search, sStr);
						listed.clearSelection();
						listed.redrawListBySearched(ProgType.PASSED, 0);
						
						listed.redrawTreeByHistory();
					}
					else {
						// キーワード検索
						listed.clearSelection();
						listed.redrawListByKeywordDyn(search, kStr);
					}
				}
			}
			
			timer_now.start();
			
			return true;
		}

		@Override
		protected boolean doBatchReserve() {
			timer_now.pause();
			listed.doBatchReserve();
			timer_now.start();
			return true;
		}

		@Override
		protected boolean jumpToNow() {
			timer_now.pause();
			if ( ! mainWindow.isTabSelected(MWinTab.PAPER) ) {
				mainWindow.setSelectedTab(MWinTab.PAPER);
			}
			paper.jumpToNow();
			timer_now.start();
			return true;
		}

		@Override
		protected boolean jumpToPassed(String passed) {
			timer_now.pause();
			boolean b = paper.jumpToBangumi(null,passed);
			timer_now.start();
			return b;
		}

		@Override
		protected boolean redrawByPager() {
			timer_now.pause();
			boolean b = paper.redrawByPager();
			timer_now.start();
			return b;
		}

		@Override
		protected void toggleMatchBorder() {
			timer_now.pause();
			paper.toggleMatchBorder();
			timer_now.start();
		}

		@Override
		protected void setPaperColorDialogVisible(boolean b) {
			//paper.stopTimer(); xxxx
			timer_now.pause();
			CommonSwingUtils.setLocationCenter(Viewer.this,pcwin);
			pcwin.setVisible(true);
			timer_now.start();
		}

		@Override
		protected void setPaperZoom(int n) {
			timer_now.pause();
			paper.setZoom(n);
			timer_now.start();
		}

		@Override
		protected boolean recorderSelectorChanged() {
			
			timer_now.pause();
			
			if (mainWindow.isTabSelected(MWinTab.LISTED)) {
				listed.updateReserveMark();
				listed.selectBatchTarget();
			}
			else if (mainWindow.isTabSelected(MWinTab.RSVED)) {
				reserved.redrawReservedList();
			}
			else if (mainWindow.isTabSelected(MWinTab.RECED)) {
				recorded.redrawRecordedList();
			}
			
			// 新聞形式の予約枠を書き換えるかもよ？
			if (env.getEffectComboToPaper()) {
				paper.updateReserveBorder(null);
			}
			
			timer_now.start();
			
			return true;
		}

		@Override
		protected void takeSnapShot() {
			
			timer_now.pause();
			
			try {
				String fname;
				if ( mainWindow.isTabSelected(MWinTab.LISTED) ) {
					fname = String.format("snapshot.%s",env.getSnapshotFmt().getExtension());
					CommonSwingUtils.saveComponentAsJPEG(listed.getCurrentView(), listed.getTableHeader(), null, listed.getTableBody(), fname, env.getSnapshotFmt(), Viewer.this);
				}
				else if ( mainWindow.isTabSelected(MWinTab.PAPER) ){
					if ( env.getDrawcacheEnable() || ! env.isPagerEnabled() ) {
						fname = String.format("snapshot.%s",env.getSnapshotFmt().getExtension());
					}
					else {
						int pcur = getSelectedPagerIndex();
						int pmax = getPagerCount();
						if ( env.getAllPageSnapshot() ) {
							for ( int i=0; i<pmax; i++ ) {
								if ( i != pcur ) {
									setSelectedPagerIndex(i);
									fname = String.format("snapshot%02d.%s",i+1,env.getSnapshotFmt().getExtension());
									CommonSwingUtils.saveComponentAsJPEG(paper.getCurrentView(), paper.getCenterPane(), paper.getTimebarPane(), paper.getCurrentPane(), fname, env.getSnapshotFmt(), Viewer.this);
								}
							}
						}
						fname = String.format("snapshot%02d.%s",pcur+1,env.getSnapshotFmt().getExtension());
						setSelectedPagerIndex(pcur);
					}
					CommonSwingUtils.saveComponentAsJPEG(paper.getCurrentView(), paper.getCenterPane(), paper.getTimebarPane(), paper.getCurrentPane(), fname, env.getSnapshotFmt(), Viewer.this);
				}
				else {
					return; // おかしーよ
				}
				Desktop desktop = Desktop.getDesktop();
				if (env.getPrintSnapshot()) {
					desktop.print(new File(fname));
				}
				else {
					String emsg = CommonUtils.openFile(fname);
					if (emsg != null) {
						mwin.appendError(emsg);
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			finally {
				timer_now.start();
			}
		}

		@Override
		protected void setStatusVisible(boolean b) {
			Viewer.this.setStatusVisible(b);
		}

		@Override
		protected void setFullScreen(boolean b) {
			Viewer.this.setFullScreen(b);
		}

		@Override
		protected void toggleSettingTabVisible() {
			mainWindow.toggleShowSettingTabs();
		}

		@Override
		protected boolean isTabSelected(MWinTab tab) {
			return mainWindow.isTabSelected(tab);
		}

		@Override
		protected boolean addKeywordSearch(SearchKey search) {
			
			timer_now.pause();
			
			AbsKeywordDialog kD = new VWKeywordDialog();
			CommonSwingUtils.setLocationCenter(Viewer.this,kD);
			
			kD.open(search.getLabel(), search, srKeys, srGrps);
			kD.setVisible(true);
			
			if (kD.isRegistered()) {
				// 検索結果の再構築
				mpList.clear(env.getDisableFazzySearch(), env.getDisableFazzySearchReverse());
				mpList.build(tvprograms, trKeys.getTraceKeys(), srKeys.getSearchKeys());
				
				// ツリーに反映する
				listed.redrawTreeByKeyword();
				
				mainWindow.setSelectedTab(MWinTab.LISTED);
			}
			
			timer_now.start();
			
			return true;
		}

		@Override
		protected boolean reLoadTVProgram(LoadFor lf) {
			timer_now.pause();
			boolean b = Viewer.this.reLoadTVProgram(lf);
			timer_now.start();
			return b;
		}

		@Override
		protected boolean reLoadRdReserve(String myself) {
			timer_now.pause();
			boolean b = Viewer.this.reLoadRdReserve(myself);
			timer_now.start();
			return b;
		}

		@Override
		protected boolean reLoadRdRecorded(String myself) {
			timer_now.pause();
			boolean b = Viewer.this.reLoadRdRecorded(myself);
			timer_now.start();
			return b;
		}

	}
	
	
	
	/*******************************************************************************
	 * 共通メソッド群
	 ******************************************************************************/
	
	/**
	 * 類似予約をさがす
	 */
	private LikeReserveList findLikeReserves(ProgDetailList tvd, String keyword, int threshold) {
		
		LikeReserveList likeRsvList = new LikeReserveList();
		
		// 曖昧検索のための初期化
		String keywordPop = null;
		int thresholdVal = 0;
		if (threshold > 0) {
			// キーワード指定がある場合
			keywordPop = TraceProgram.replacePop(keyword);
			thresholdVal = threshold;
		}
		else {
			// キーワード指定がない場合
			keywordPop = tvd.titlePop;
			thresholdVal = env.getDefaultFazzyThreshold();
		}

		// 検索範囲
		long rangeLikeRsv = env.getRangeLikeRsv()*3600000;
		
		for ( HDDRecorder recorder : recorders ) {
			
			// 終了した予約を整理する
			recorder.refreshReserves();
			
			for ( ReserveList r : recorder.getReserves() ) {
				
				// タイトルのマッチング
				if ( ! isLikeTitle(tvd, r, keywordPop, thresholdVal) ) {
					continue;
				}
				
				// 放送局のマッチング
				if ( ! isLikeChannel(tvd, r) ) {
					continue;
				}
				
				// 近接時間チェック
				Long d = getLikeDist(tvd, r, rangeLikeRsv);
				if ( d == null ) {
					continue;
				}
				
				// 類似予約あり
				likeRsvList.add(new LikeReserveItem(recorder, r, d));

				for ( LikeReserveItem lr : likeRsvList ) {
					System.out.println(lr.getDist()+", "+lr.getRsv().getTitle());
				}
				System.out.println("********");
			}
			
		}
		
		
		return likeRsvList;
	}
	
	private boolean isLikeTitle(ProgDetailList tvd, ReserveList r, String keywordPop, int thresholdVal) {
		
		if (env.getDisableFazzySearch() == false) {
			// 双方向の比較を行う・正引き
			int fazScore = TraceProgram.sumScore(keywordPop, r.getTitlePop());
			if ( fazScore >= thresholdVal ) {
				return true;
			}
			else if ( ! env.getDisableFazzySearchReverse() ) {
				// 逆引き
				fazScore = TraceProgram.sumScore(r.getTitlePop(), keywordPop);
				if ( fazScore >= thresholdVal) {
					return true;
				}
			}
		}
		else {
			if ( r.getTitlePop().equals(tvd.titlePop )) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isLikeChannel(ProgDetailList tvd, ReserveList r) {
		
		if ( r.getCh_name() == null ) {
			return false;
		}
		if ( ! r.getCh_name().equals(tvd.center) ) {
			return false;
		}
		
		return true;
	}
	
	private Long getLikeDist(ProgDetailList tvd, ReserveList r, long rangeLikeRsv) {
		
		Long d = null;
		
		ArrayList<String> starts = new ArrayList<String>();
		ArrayList<String> ends = new ArrayList<String>();
		CommonUtils.getStartEndList(starts, ends, r);
		 
		for ( int j=0; j<starts.size(); j++ ) {
			long dtmp = CommonUtils.getCompareDateTime(starts.get(j),tvd.startDateTime);
			if ( rangeLikeRsv > 0 && Math.abs(dtmp) >= rangeLikeRsv ) {
				// 範囲指定があって範囲外ならスキップ
				continue;
			}
			else if ( d == null || Math.abs(d) > Math.abs(dtmp) ) {
				// 初値、または一番小さい値を採用
				d = dtmp;
			}
		}
		
		return d;
	}
	
	
	/***
	 * 
	 * リスト・新聞形式共通
	 * 
	 */

	/**
	 *  番組追跡への追加とgoogle検索
	 */
	public void showPopupForTraceProgram(
			final JComponent comp,
			final ProgDetailList tvd, final String keyword, final int threshold,
			final int x, final int y, final int h)
	{
		JPopupMenu pop = new JPopupMenu();
	
		// 予約する
		if ( tvd.type == ProgType.PASSED ||
				(tvd.type == ProgType.PROG && tvd.subtype == ProgSubtype.RADIO) ||
				recorders.size() == 0 ) {
			// 過去ログは処理対象外です
		}
		else {
			JMenuItem menuItem = new JMenuItem("予約する【"+tvd.title+" ("+tvd.center+")】");
			
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					CommonSwingUtils.setLocationCenter(mainWindow,rdialog);

					if ( rdialog.open(tvd) ) {
						rdialog.setVisible(true);
					}
					else {
						rdialog.setVisible(false);
					}
					
					//
					if (rdialog.isReserved()) {
						listed.updateReserveMark();
						paper.updateReserveBorder(tvd.center);
						reserved.redrawReservedList();
					}
				}
			});
			pop.add(menuItem);
		}
		
		pop.addSeparator();
		
		// 類似予約検索
		LikeReserveList likeRsvList = findLikeReserves(tvd, "", 0);
		
		// 重複予約検索
		LikeReserveList overlapRsvList = new LikeReserveList();
		searchOverlapRsv(overlapRsvList, tvd, h);
		
		// 類似と重複で被るものを重複から除外
		for ( LikeReserveItem ll : likeRsvList ) {
			int i=0;
			for ( ; i<overlapRsvList.size(); i++ ) {
				if ( ll.getRsv() == overlapRsvList.getRsv(i) ) {
					break;
				}
			}
			if ( i < overlapRsvList.size() ) {
				overlapRsvList.remove(i);
			}
		}
		
		// 予約実行ON・OFF
		if ( tvd.type != ProgType.PASSED )
		{
			for ( int n=0; n<2; n++ ) {
				
				LikeReserveList rsvList = null;
				if ( n == 0 ) {
					rsvList = likeRsvList;
				}
				else {
					rsvList = overlapRsvList;
				}
				
				for ( int i=0; i<rsvList.size(); i++ ) {
					
					final boolean fexec = rsvList.getRsv(i).getExec();
					final String title = rsvList.getRsv(i).getTitle();
					final String chnam = rsvList.getRsv(i).getCh_name();
					final String rsvId = rsvList.getRsv(i).getId();
					final String recId = rsvList.getRec(i).Myself();
					
					pop.add(getExecOnOffMenuItem(fexec,title,chnam,rsvId,recId,n));
				}
				
				pop.addSeparator();
			}
		}
		
		pop.addSeparator();
		
		// 削除する
		if ( tvd.type != ProgType.PASSED )	// 過去ログは処理対象外です
		{
			for ( int n=0; n<2; n++ ) {
				
				LikeReserveList rsvList = null;
				if ( n == 0 ) {
					rsvList = likeRsvList;
				}
				else {
					rsvList = overlapRsvList;
				}
				
				for (int i=0; i<rsvList.size(); i++) {
					
					final String title = rsvList.getRsv(i).getTitle();
					final String chnam = rsvList.getRsv(i).getCh_name();
					final String rsvId = rsvList.getRsv(i).getId();
					final String recId = rsvList.getRec(i).Myself();
					
					pop.add(getRemoveRsvMenuItem(title,chnam,rsvId,recId,n));
				}
				
				pop.addSeparator();
			}
		}
		else {
			pop.addSeparator();
			pop.addSeparator();
		}
		
		// ジャンプする
		{
			if ( mainWindow.isTabSelected(MWinTab.LISTED) ) {
				pop.add(getJumpMenuItem(tvd.title,tvd.center,tvd.startDateTime));
			}
			if ( mainWindow.isTabSelected(MWinTab.LISTED) || mainWindow.isTabSelected(MWinTab.PAPER) ) {
				JMenuItem mi = getJumpToLastWeekMenuItem(tvd.title,tvd.center,tvd.startDateTime);
				if ( mi != null ) {
					pop.add(mi);
				}
			}
		}
		
		pop.addSeparator();
		
		// 番組追跡へ追加する
		{
			final String label = tvd.title+" ("+tvd.center+")";
			JMenuItem menuItem = new JMenuItem("番組追跡への追加【"+label+"】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//
					for (TraceKey tr : trKeys.getTraceKeys()) {
						if (tr.getLabel().equals(label)) {
							mwin.appendMessage("【警告】すでに番組追跡に登録されています："+label);
							ringBeep();
							return;
						}
					}
					
					//
					trKeys.add(label, tvd.title, tvd.center, env.getDefaultFazzyThreshold());

					VWTraceKeyDialog tD = new VWTraceKeyDialog(0,0);
					CommonSwingUtils.setLocationCenter(mainWindow,tD);
					
					tD.reopen(label, trKeys);
					tD.setVisible(true);
					
					if (tD.isRegistered()) { 
						//
						trKeys.save();
						
						// 検索結果の再構築
						mpList.clear(env.getDisableFazzySearch(), env.getDisableFazzySearchReverse());
						mpList.build(tvprograms, trKeys.getTraceKeys(), srKeys.getSearchKeys());
						
						// ツリーに反映する
						listed.redrawTreeByTrace();

						// 表示を更新する
						paper.updateBangumiColumns();
						listed.reselectTree();
						
						mwin.appendMessage("番組追跡へ追加しました【"+label+"】");
					}
					else {
						trKeys.remove(label);
					}
				}
			});
			pop.add(menuItem);
		}
		
		// キーワード検索へ追加する
		{
			final String label = tvd.title+" ("+tvd.center+")";
			JMenuItem menuItem = new JMenuItem("キーワード検索への追加【"+label+"】");
			menuItem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					//
					for (SearchKey sr : srKeys.getSearchKeys()) {
						if (sr.getLabel().equals(tvd.title)) {
							ringBeep();
							String msg = "すでにキーワード検索に登録されています： "+tvd.title;
							mwin.appendMessage(msg);
							JOptionPane.showConfirmDialog(null, msg, "警告", JOptionPane.CLOSED_OPTION);							// キーワード検索の追加ではダイアログで修正できるので止めない
							//return;
						}
					}
					// 「キーワード検索の設定」ウィンドウを開く
					SearchKey search = new SearchKey();
					{
						search.setCondition("0");
						search.alTarget.add(TargetId.TITLE);
						search.alContain.add("0");
						search.alKeyword.add(tvd.title);
					}
					{
						search.setCondition("0");
						search.alTarget.add(TargetId.CHANNEL);
						search.alContain.add("0");
						search.alKeyword.add(tvd.center);
					}
					{
						AbsKeywordDialog kD = new VWKeywordDialog();
						CommonSwingUtils.setLocationCenter(mainWindow,kD);
						
						kD.open(tvd.title, search, srKeys, srGrps);
						kD.setVisible(true);
						
						if (kD.isRegistered()) {
							// 検索結果の再構築
							mpList.clear(env.getDisableFazzySearch(), env.getDisableFazzySearchReverse());
							mpList.build(tvprograms, trKeys.getTraceKeys(), srKeys.getSearchKeys());
							
							// ツリーに反映する
							listed.redrawTreeByKeyword();

							// 表示を更新する
							paper.updateBangumiColumns();
							listed.reselectTree();
							
							mwin.appendMessage("キーワード検索へ追加しました【"+label+"】");
						}
					}
				}
			});
			pop.add(menuItem);
		}
		
		// ピックアップへ追加する
		{
			boolean isRemoveItem = false;
			if ( mainWindow.isTabSelected(MWinTab.LISTED) && tvd.type == ProgType.PICKED ) {
				isRemoveItem = true;
			}
			else {
				PickedProgram tvp = tvprograms.getPickup();
				if ( tvp != null ) {
					isRemoveItem = tvp.remove(tvd, tvd.center, tvd.accurateDate, false);
				}
			}
			
			if ( ! isRemoveItem )	// 過去ログは処理対象外です
			{
				final String label = tvd.title+" ("+tvd.center+")";
				JMenuItem menuItem = new JMenuItem("ピックアップへの追加【"+label+"】");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						//
						PickedProgram tvp = tvprograms.getPickup();
						if ( tvp != null ) {
							tvp.refresh();
							tvp.add(tvd);
							tvp.save();
							/*
							if ( listed.isNodeSelected(ListedTreeNode.PICKUP) ) {
								// ピックアップノードが選択されていたらリストを更新する
								listed.reselectTree();
							}
							*/
							listed.updateReserveMark();
							listed.refocus();
							paper.updateReserveBorder(tvd.center);
							mwin.appendMessage("【ピックアップ】追加しました： "+tvd.title+" ("+tvd.center+")");
							return;
						}
					}
				});
				pop.add(menuItem);
			}
			else {
				final String label = tvd.title+" ("+tvd.center+")";
				JMenuItem menuItem = new JMenuItem("ピックアップからの削除【"+label+"】");
				menuItem.setForeground(Color.RED);
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						//
						PickedProgram tvp = tvprograms.getPickup();
						if ( tvp != null ) {
							tvp.refresh();
							tvp.remove(tvd, tvd.center, tvd.accurateDate, true);
							tvp.save();
							/*
							if ( listed.isNodeSelected(ListedTreeNode.PICKUP) || listed.isNodeSelected(ListedTreeNode.STANDBY) ) {
								// ピックアップノードが選択されていたらリストを更新する
								listed.reselectTree();
							}
							*/
							listed.updateReserveMark();
							paper.updateReserveBorder(tvd.center);
							mwin.appendMessage("【ピックアップ】削除しました： "+tvd.title+" ("+tvd.center+")");
							return;
						}
					}
				});
				pop.add(menuItem);
			}
		}

		pop.addSeparator();
		
		// googleで検索する
		{
			for (final TextValueSet tv : env.getTvCommand()) {
				JMenuItem menuItem = new JMenuItem(tv.getText());
				String escepedTitle = "";
				String escepedChName = "";
				String escepedDetail = "";
				try {
					escepedTitle = URLEncoder.encode(tvd.title,"UTF-8");
					escepedDetail = URLEncoder.encode(tvd.detail,"UTF-8");
					escepedChName = URLEncoder.encode(tvd.center,"UTF-8");
				} catch (UnsupportedEncodingException e2) {
					//
				}
				
				String cmd = tv.getValue();
				if ( cmd.matches(".*%DETAILURL%.*") ) {
					if ( tvd.link == null || tvd.link.length() == 0 ) {
						// このメニューは利用できません！
						menuItem.setEnabled(false);
						menuItem.setForeground(Color.lightGray);
					}
				}
				cmd = cmd.replaceAll("%ENCTITLE%", escepedTitle);
				cmd = cmd.replaceAll("%ENCDETAIL%", escepedDetail);
				cmd = cmd.replaceAll("%ENCCHNAME%", escepedChName);
				cmd = cmd.replaceAll("%TITLE%", tvd.title);
				cmd = cmd.replaceAll("%DETAIL%", tvd.detail);
				cmd = cmd.replaceAll("%CHNAME%", tvd.center);
				cmd = cmd.replaceAll("%DATE%", tvd.accurateDate);
				cmd = cmd.replaceAll("%START%", tvd.start);
				cmd = cmd.replaceAll("%END%", tvd.end);
				cmd = cmd.replaceAll("%DETAILURL%", tvd.link); 
				
				// CHAN-TORU対応
				if ( cmd.matches(".*%TVKAREACODE%.*") && cmd.matches(".*%TVKPID%.*") ) {
					Center cr = null;
					for ( TVProgram tvp : progPlugins ) {
						if ( tvp.getTVProgramId().startsWith("Gガイド.テレビ王国") ) {
							for ( Center tempcr : tvp.getCRlist() ) {
								// CH設定が完了している必要がある
								if ( tvp.getSubtype() == ProgSubtype.TERRA && tvp.getSelectedCode().equals(TVProgram.allCode) && ! tempcr.getAreaCode().equals(TVProgram.bsCode) ) {
									// 地域が全国の地デジの場合のみ、有効局かどうかを確認する必要がある
									if ( tempcr.getCenter().equals(tvd.center) && tempcr.getOrder() > 0 ) {
										// このメニューは利用できます！
										cr = tempcr;
										break;
									}
								}
								else {
									if ( tempcr.getCenter().equals(tvd.center) ) {
										// このメニューは利用できます！
										cr = tempcr;
										break;
									}
								}
							}
							
							if ( cr != null ) {
								break;
							}
						}
					}
					if ( cr != null ) {
						String areacode = null;
						String centercode = cr.getLink();
						String cat = cr.getLink().substring(0,1);
						if ( cat.equals("1") ) {
							areacode = cr.getAreaCode();
						}
						else {
							if ( cat.equals("4") ) {
								cat = "5";
							}
							else if ( cat.equals("5") ) {
								cat = "4";
							}
							areacode = "10";
						}
						
						cmd = cmd.replaceAll("%TVKAREACODE%", areacode);
						cmd = cmd.replaceAll("%TVKCAT%", cat);
						cmd = cmd.replaceAll("%TVKPID%", centercode+CommonUtils.getDateTimeYMD(CommonUtils.getCalendar(tvd.startDateTime)).replaceFirst("..$", ""));
						System.out.println("[DEBUG] "+cmd);
						
						menuItem.setEnabled(true);
						menuItem.setForeground(Color.BLACK);
					}
					else {
						menuItem.setEnabled(false);
						menuItem.setForeground(Color.lightGray);
					}
				}
				
				final String run = cmd;
				
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							if (run.indexOf("http") == 0) {
								Desktop desktop = Desktop.getDesktop();
								desktop.browse(new URI(run));
							}
							else {
								CommonUtils.executeCommand(run);
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (URISyntaxException e1) {
							e1.printStackTrace();
						}
					}
				});

				pop.add(menuItem);
			}
		}
		
		pop.addSeparator();
		
		// クリップボードへコピーする
		{
			JMenuItem menuItem = new JMenuItem("番組名をコピー【"+tvd.title+"】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String msg = tvd.title;
					Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection s = new StringSelection(msg);
					cb.setContents(s, null);
				}
			});
			pop.add(menuItem);
		}
		{
			JMenuItem menuItem = new JMenuItem("番組名と詳細をコピー【"+tvd.title+"】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String msg = tvd.title+System.getProperty("line.separator")+tvd.detail+"\0"+tvd.getAddedDetail();
					Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection s = new StringSelection(msg);
					cb.setContents(s, null);
				}
			});
			pop.add(menuItem);
		}
		{
			JMenuItem menuItem = new JMenuItem("番組情報をコピー【"+tvd.title+"】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String msg = "";
					int preId = 0;
					for (ClipboardInfo cb : cbitems) {
						if (cb.getB()) {
							switch (cb.getId()) {
							case 1:
								msg += tvd.title+"\t";
								break;
							case 2:
								msg += tvd.center+"\t";
								break;
							case 3:
								msg += tvd.accurateDate+"\t";
								break;
							case 4:
								msg += tvd.start+"\t";
								break;
							case 5:
								if (preId == 4) {
									msg = msg.substring(0,msg.length()-1)+"-";
								}
								msg += tvd.end+"\t";
								break;
							case 6:
								msg += tvd.genre+"\t";
								break;
							case 7:
								msg += tvd.detail+"\0"+tvd.getAddedDetail()+"\t";
								break;
							}
						}
						preId = cb.getId();
					}
					if (msg.length() > 0) {
						msg = msg.substring(0,msg.length()-1);
					}
					Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection s = new StringSelection(msg);
					cb.setContents(s, null);
				}
			});
			pop.add(menuItem);
		}

		pop.addSeparator();
		
		// 延長感染源へ追加する
		if (
				tvd.type == ProgType.SYOBO ||
				tvd.type == ProgType.PASSED ||
				tvd.type == ProgType.PICKED ||
				(tvd.type == ProgType.PROG && tvd.subtype != ProgSubtype.RADIO)	)	// ラジオは処理対象外です
		{
			JMenuItem menuItem = new JMenuItem("延長感染源にしない【"+tvd.title+" ("+tvd.center+")】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//
					mwin.appendMessage("延長感染源を隔離します【"+tvd.title+"("+tvd.center+")】");
					//
					AbsExtensionDialog eD = new VWExtensionDialog();
					CommonSwingUtils.setLocationCenter(mainWindow,eD);
					
					eD.open(tvd.title,tvd.center,false,extKeys);
					eD.setVisible(true);
					
					if (eD.isRegistered()) {
						// 番組表の状態を更新する
						for (TVProgram tvp : tvprograms) {
							if (tvp.getType() == ProgType.PROG) {
								tvp.setExtension(null, null, false, extKeys.getSearchKeys());
							}
						}
						
						// ツリーに反映する
						listed.redrawTreeByExtension();
						
						mainWindow.setSelectedTab(MWinTab.LISTED);
					}
				}
			});
			pop.add(menuItem);
		}
		if ( tvd.type == ProgType.PASSED || (tvd.type == ProgType.PROG && tvd.subtype != ProgSubtype.RADIO) )	// ラジオは処理対象外です
		{
			JMenuItem menuItem = new JMenuItem("延長感染源にする【"+tvd.title+" ("+tvd.center+")】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//
					AbsExtensionDialog eD = new VWExtensionDialog();
					CommonSwingUtils.setLocationCenter(mainWindow,eD);
					
					eD.open(tvd.title,tvd.center,true,extKeys);
					eD.setVisible(true);
					
					if (eD.isRegistered()) {
						// 番組表の状態を更新する
						for (TVProgram tvp : tvprograms) {
							if (tvp.getType() == ProgType.PROG) {
								tvp.setExtension(null, null, false, extKeys.getSearchKeys());
							}
						}
						
						// ツリーに反映する
						listed.redrawTreeByExtension();
						
						mainWindow.setSelectedTab(MWinTab.LISTED);
					}
				}
			});
			pop.add(menuItem);
		}
		
		pop.addSeparator();
		
		// 視聴する
		if ( tvd.type == ProgType.PROG && tvd.subtype != ProgSubtype.RADIO)	// ラジオは処理対象外です
		{
			for (HDDRecorder recorder : recorders ) {
				
				if (recorder.ChangeChannel(null) == false) {
					continue;
				}
				
				final String recorderName = recorder.Myself();
				JMenuItem menuItem = new JMenuItem("【"+recorderName+"】で【"+tvd.center+"】を視聴する");
				
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						for (HDDRecorder recorder : recorders ) {
							if (recorder.isMyself(recorderName)) {
								if (recorder.ChangeChannel(tvd.center) == false) {
									ringBeep();
									mwin.appendError("【警告】チャンネルを変更できませんでした："+recorder.getErrmsg());
								}
								else if (recorder.getErrmsg() !=null && recorder.getErrmsg().length() > 0) {
									mwin.appendError("[追加情報] "+recorder.getErrmsg());
								}
							}
						}
					}
				});
				
				menuItem.setEnabled(recorder.getUseChChange());
				
				pop.add(menuItem);
			}
		}
		
		pop.show(comp, x, y);
	}
	
	// ピックアップへ追加する
	public boolean addToPickup(final ProgDetailList tvd) {
		
		if (tvd.start.equals("")) {
			// 番組情報がありません
			return false;
		}
		
		PickedProgram tvp = tvprograms.getPickup();
		if ( tvp == null ) {
			// ピックアップ先がありません
			return true;
		}
		
		// 削除かな？
		if ( tvp.remove(tvd, tvd.center, tvd.accurateDate, true) ) {
			tvp.save();
			if ( listed.isNodeSelected(JTreeLabel.Nodes.PICKUP) || listed.isNodeSelected(JTreeLabel.Nodes.STANDBY) ) {
				// ピックアップノードor予約待機ノードが選択されていたらリストを更新する
				listed.reselectTree();
				//listed.updateReserveMark();
			}
			else {
				// 予約マークだけ変えておけばいいよね
				listed.updateReserveMark();
				listed.refocus();
			}
			paper.updateReserveBorder(tvd.center);
			mwin.appendMessage("【ピックアップ】削除しました： "+tvd.title+" ("+tvd.center+")");
			return false;
		}
		
		// 追加です
		if ( tvd.endDateTime.compareTo(CommonUtils.getDateTime(0)) > 0 ) {
			tvp.refresh();
			tvp.add(tvd);
			tvp.save();
			if ( listed.isNodeSelected(JTreeLabel.Nodes.PICKUP) ) {
				// ピックアップノードが選択されていたらリストを更新する
				listed.reselectTree();
				//listed.updateReserveMark();
			}
			else {
				listed.updateReserveMark();
				listed.refocus();
			}
			paper.updateReserveBorder(tvd.center);
			mwin.appendMessage("【ピックアップ】追加しました： "+tvd.title+" ("+tvd.center+")");
			return true;
		}

		//　過去ログは登録できないよ
		mwin.appendMessage("【ピックアップ】過去情報はピックアップできません.");
		return false;
	}
	
	/**
	 *  予約を削除するメニューアイテム
	 */
	private JMenuItem getRemoveRsvMenuItem(final String title, final String chnam, final String rsvId, final String recId, int n) {
		//
		JMenuItem menuItem = new JMenuItem(((n==0)?"予約を削除する【":"隣接予約を削除する【")+title+"("+chnam+")/"+recId+"】");
		menuItem.setForeground(Color.RED);
		if ( recId.equals(toolBar.getSelectedRecorder()) ) {
			// 選択中のレコーダのものは太字に
			Font f = menuItem.getFont();
			menuItem.setFont(f.deriveFont(f.getStyle()|Font.BOLD));
		}
		
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (env.getShowWarnDialog()) {
					Container cp = getContentPane();
					int ret = JOptionPane.showConfirmDialog(cp, "削除しますか？【"+title+"("+chnam+")】（"+recId+"）", "確認", JOptionPane.YES_NO_OPTION);
					if (ret != JOptionPane.YES_OPTION) {
						return;
					}
				}
				
				stwin.clear();
				
				// 削除本体
				new SwingBackgroundWorker(false) {
					
					@Override
					protected Object doWorks() throws Exception {
						
						for (HDDRecorder recorder : recorders) {
							if (recorder.isMyself(recId)) {	// IPAddr:PortNo:RecorderIdで比較
								
								String title = "";
								for (ReserveList r : recorder.getReserves()) {
									if (r.getId().equals(rsvId)) {
										title = r.getTitle();
										break;
									}
								}
								
								stwin.appendMessage("予約を削除します："+title+"("+rsvId+")");
								//recorder.setProgressArea(stwin);
								ReserveList r = recorder.RemoveRdEntry(rsvId);	// Noで検索
								if (r != null) {
									mwin.appendMessage("正常に削除できました："+r.getTitle()+"("+r.getCh_name()+")");
									
									if ( ! r.getTitle().equals(title) || ! r.getId().equals(rsvId)) {
										mwin.appendError("【警告】削除結果が一致しません！："+title+"／"+r.getTitle());
									}
									
									if ( recorder.getUseCalendar()) {
										// カレンダーから削除する
										for ( HDDRecorder calendar : recorders ) {
											if (calendar.getType() == RecType.CALENDAR) {
												stwin.appendMessage("カレンダーから予約情報を削除します");
												//calendar.setProgressArea(stwin);
												if ( ! calendar.UpdateRdEntry(r, null)) {
													mwin.appendError("【カレンダー】"+calendar.getErrmsg());
													ringBeep();
												}
											}
										}
									}
									
									r = null;
								}
								else {
									mwin.appendError("削除に失敗しました："+title);
								}
								
								//
								if ( ! recorder.getErrmsg().equals("")) {
									mwin.appendError("【追加情報】"+recorder.getErrmsg());
									ringBeep();
								}
								break;
							}
						}
						return null;
					}
					
					@Override
					protected void doFinally() {
						stwin.setVisible(false);
					}
				}.execute();
				
				CommonSwingUtils.setLocationCenter(Viewer.this, stwin);
				stwin.setVisible(true);
				
				// 予約状況を更新
				listed.updateReserveMark();
				paper.updateReserveBorder(chnam);
				reserved.redrawReservedList();
			}
		});
		
		return menuItem;
	}
	
	
	
	
	/*
	 * 他のクラスに分離できなかったというか、しなかったというか、そんなメソッド群
	 */
	
	/**
	 * 
	 */
	private boolean doExecOnOff(final boolean fexec, final String title, final String chnam, final String rsvId, final String recId) {
		
		CommonSwingUtils.setLocationCenter(mainWindow,rdialog);
		
		String mode = (fexec ? "ON" : "OFF");
		
		if ( rdialog.open(recId,rsvId) ) {
			rdialog.setOnlyUpdateExec(fexec);
			rdialog.doUpdate();
			
			if (rdialog.isReserved()) {
				// 予約状況を更新
				listed.updateReserveMark();
				paper.updateReserveBorder(chnam);
				reserved.redrawReservedList();
				
				{
					String msg = "予約を"+mode+"にしました【"+title+"("+chnam+")/"+recId+"】";
					//StdAppendMessage(msg);
					mwin.appendMessage(msg);
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 *  予約実行をONOFFするメニューアイテム
	 */
	private JMenuItem getExecOnOffMenuItem(final boolean fexec, final String title, final String chnam, final String rsvId, final String recId, int n) {
		
		JMenuItem menuItem = new JMenuItem();
		
		String mode;
		if ( ! fexec ) {
			mode = "ON";
			menuItem.setForeground(Color.BLUE);
		}
		else {
			mode = "OFF";
		}
		
		menuItem.setText(((n==0)?"予約を":"隣接予約を")+mode+"にする【"+title+"("+chnam+")/"+recId+")】");
		
		if ( recId.equals(toolBar.getSelectedRecorder()) ) {
			// 選択中のレコーダのものは太字に
			Font f = menuItem.getFont();
			menuItem.setFont(f.deriveFont(f.getStyle()|Font.BOLD));
		}

		final String xmode = mode;
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//VWReserveDialog rD = new VWReserveDialog(0, 0, env, tvprograms, recorders, avs, chavs, stwin);
				//rdialog.clear();
				CommonSwingUtils.setLocationCenter(mainWindow,rdialog);
				
				if ( rdialog.open(recId,rsvId) ) {
					rdialog.setOnlyUpdateExec( ! fexec);
					rdialog.doUpdate();
					
					if (rdialog.isReserved()) {
						// 予約状況を更新
						listed.updateReserveMark();
						paper.updateReserveBorder(chnam);
						reserved.redrawReservedList();
						
						{
							String msg = "予約を"+xmode+"にしました【"+title+"("+chnam+")/"+recId+"】";
							StdAppendMessage(msg);
							mwin.appendMessage(msg);
						}
					}
				}
				else {
					//rdialog.setVisible(false);
				}
			}
		});
		
		return menuItem;
	}
	
	/**
	 *  新聞形式へジャンプするメニューアイテム
	 */
	private JMenuItem getJumpMenuItem(final String title, final String chnam, final String startDT) {
		JMenuItem menuItem = new JMenuItem("番組欄へジャンプする【"+title+" ("+chnam+")】");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				paper.jumpToBangumi(chnam,startDT);
			}
		});
		return menuItem;
	}
	private JMenuItem getJumpToLastWeekMenuItem(final String title, final String chnam, final String startDT) {
		GregorianCalendar cal = CommonUtils.getCalendar(startDT);
		if ( cal != null ) {
			JMenuItem menuItem = new JMenuItem("先週の番組欄へジャンプする【"+title+" ("+chnam+")】");
			cal.add(Calendar.DATE, -7);
			final String lastweek = CommonUtils.getDateTime(cal);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					paper.jumpToBangumi(chnam,lastweek);
				}
			});
			return menuItem;
		}
		return null;
	}
	
	// カーソル位置にかかる予約枠の検索
	private void searchOverlapRsv(LikeReserveList overlapRsvList, ProgDetailList tvd, int h)
	{
		String clicked = "";
		if ( h >= 0 && tvd.start.length() != 0 ) {
			// 新聞形式ならクリック位置の日時を算出する
			GregorianCalendar cala = CommonUtils.getCalendar(tvd.startDateTime);
			if ( CommonUtils.isLateNight(cala.get(Calendar.HOUR_OF_DAY)) ) {
				cala.set(Calendar.HOUR_OF_DAY, TIMEBAR_START);
				cala.set(Calendar.MINUTE, 0);
			}
			cala.add(Calendar.MINUTE, Math.round(h/bounds.getPaperHeightMultiplier()));
			clicked = CommonUtils.getDateTime(cala);
			//StdAppendError("clicked:"+clicked);
		}
		
		HashMap<String,Boolean> misCN = new HashMap<String, Boolean>();
		for ( HDDRecorder recorder : recorders ) {
			
			// 終了した予約を整理する
			recorder.refreshReserves();
			
			for ( ReserveList r : recorder.getReserves() ) {
				
				// 放送局のマッチング
				if (r.getCh_name() == null) {
					if ( r.getChannel() == null ) {
						System.err.println(ERRID+"予約情報にCHコードが設定されていません。バグの可能性があります。 recid="+recorder.Myself()+" chname="+r.getCh_name());
						continue;
					}
					if(r.getChannel().length() > 0) {
						misCN.put(r.getChannel(),true);
					}
					continue;
				}
				if ( ! r.getCh_name().equals(tvd.center)) {
					continue;
				}
				
				// 重複時間チェック
				boolean inRange = false;
				long d = 0;
				{
					ArrayList<String> starts = new ArrayList<String>();
					ArrayList<String> ends = new ArrayList<String>();
					CommonUtils.getStartEndList(starts, ends, r);
					if ( h >= 0 ) {
						// 新聞形式はピンポイント（マウスポインタのある位置の時刻）
						for (int j=0; j<starts.size(); j++) {
							if ( clicked.compareTo(starts.get(j)) >= 0 && clicked.compareTo(ends.get(j)) <= 0 ) {
								inRange = true;
								break;
							}
						}
					}
					else {
						// リスト形式は幅がある（開始～終了までの間のいずれかの時刻）
						for (int j=0; j<starts.size(); j++) {
							if ( CommonUtils.isOverlap(tvd.startDateTime, tvd.endDateTime, starts.get(j), ends.get(j), false) ) {
								inRange = true;
								d = CommonUtils.getDiffDateTime(tvd.startDateTime, starts.get(j));
								break;
							}
						}
					}
				}
				if ( ! inRange) {
					continue;
				}
				
				// 類似予約あり！
				overlapRsvList.add(new LikeReserveItem(recorder, r, d));
			}
		}
		
		return;
	}
	
	
	/*******************************************************************************
	 * タイマー関連
	 ******************************************************************************/
	
		
	/*******************************************************************************
	 * ここからおおむね初期化処理にかかわるメソッド群
	 ******************************************************************************/
	
	// レコーダから取得したエンコーダ情報で、登録済みレコーダ一覧を更新する
	private void setEncoderInfo2RecorderList(HDDRecorder recorder) {
		for (RecorderInfo ri : recInfoList ) {
			//if (rl.getRecorderEncoderList().size() == 0)
			{
				String mySelf = ri.getRecorderIPAddr()+":"+ri.getRecorderPortNo()+":"+ri.getRecorderId();
				String myMail = "MAIL"+":"+ri.getRecorderMacAddr()+":"+ri.getRecorderId();
				if (recorder.isMyself(mySelf) || recorder.isMyself(myMail)) {
					ri.clearEncoders();
					for (TextValueSet enc : recorder.getEncoderList()) {
						ri.addEncoder(enc.getText());
					}
					break;
				}
			}
		}
	}
	
	/**
	 *  レコーダの予約情報をＤＬする
	 */
	private boolean reLoadRdReserve(final String recId) {
		//
		StWinClear();
		
		new SwingBackgroundWorker(false) {
			
			@Override
			protected Object doWorks() throws Exception {
				
				TatCount tc = new TatCount();
				
				loadRdReserve(true, recId);
				
				// エンコーダ情報が更新されるかもしれないので、一覧のエンコーダ表示にも反映する
				recsetting.redrawRecorderEncoderEntry();
				
				// 各タブに反映する
				paper.updateReserveBorder(null);
				listed.updateReserveMark();
				reserved.redrawReservedList();
				recorded.redrawRecordedList();
				
				mwin.appendMessage(String.format("【予約一覧の取得処理が完了しました】 所要時間： %.2f秒",tc.end()));
				return null;
			}
			
			@Override
			protected void doFinally() {
				StWinSetVisible(false);
			}
		}.execute();
		
		StWinSetLocationCenter(this);
		StWinSetVisible(true);
		
		return true;
	}
	private void loadRdReserve(final boolean force, final String myself) {

		//
		new SwingBackgroundWorker(true) {
			
			@Override
			protected Object doWorks() throws Exception {
				
				HDDRecorderList recs;
				if ( myself != null ) {
					recs = recorders.findInstance(myself);
				}
				else {
					recs = recorders;
				}
				for ( HDDRecorder recorder : recs ) {
					switch ( recorder.getType() ) {
					case RECORDER:
					case EPG:
					case MAIL:
					case NULL:
					case TUNER:
						loadRdReserveOnce(recorder, force);
						break;
					default:
						break;
					}
				}
				
				return null;
			}
			
			@Override
			protected void doFinally() {
			}
		}.execute();
	}
	
	private boolean loadRdReserveOnce(HDDRecorder recorder, boolean force) {
		
		mwin.appendMessage("【レコーダ情報取得】レコーダから情報を取得します: "+recorder.getRecorderId()+"("+recorder.getIPAddr()+":"+recorder.getPortNo()+")");
		if ( recorder.isThereAdditionalDetails() && ! env.getForceGetRdReserveDetails() ) {
			mwin.appendMessage("＜＜＜注意！＞＞＞このレコーダでは予約詳細の個別取得を実行しないと正確な情報を得られない場合があります。");
		}
		
		try {
			
			// 各種設定の取得
			if ( ! recorder.GetRdSettings(force) ) {
				// 取得に失敗
				mwin.appendError(recorder.getErrmsg()+" "+recorder.getIPAddr()+":"+recorder.getPortNo()+":"+recorder.getRecorderId());
				ringBeep();
				return false;
			}
			
			// 予約一覧の取得
			if ( ! recorder.GetRdReserve(force) ) {
				// 取得に失敗
				mwin.appendError(recorder.getErrmsg()+" "+recorder.getIPAddr()+":"+recorder.getPortNo()+":"+recorder.getRecorderId());
				ringBeep();
				return false;
			}
			
			// レコーダから取得したエンコーダ情報で、登録済みレコーダ一覧を更新する
			setEncoderInfo2RecorderList(recorder);
			if ( force ) {
				recInfoList.save();
			}
			
			// 予約詳細の取得
			if ( env.getNeverGetRdReserveDetails() ) {
				mwin.appendMessage("【！】予約詳細情報の取得はスキップされました");
			}
			else if ( force && recorder.isThereAdditionalDetails() ) {
				boolean getDetails = true;
				if ( ! env.getForceGetRdReserveDetails() ) {
					int ret = JOptOptionPane.showConfirmDialog(null, "詳細情報を取得しますか？（時間がかかります）", "今回の選択を既定の動作とする", "確認", JOptionPane.YES_NO_OPTION);
					getDetails = (ret == JOptOptionPane.YES_OPTION);
					if ( JOptOptionPane.isSelected() ) {
						// 今回の選択を既定の動作とする
						env.setForceGetRdReserveDetails(getDetails);
						env.setNeverGetRdReserveDetails( ! getDetails);
						env.save();
						setting.updateSelections();
					}
				}
				if ( ! getDetails ) {
					mwin.appendMessage("【！】予約詳細情報の取得はスキップされました");
				}
				else {
					if ( ! recorder.GetRdReserveDetails()) {
						// 取得に失敗
						mwin.appendError(recorder.getErrmsg()+" "+recorder.getIPAddr()+":"+recorder.getPortNo()+":"+recorder.getRecorderId());
						ringBeep();
					}
				}
			}
			
			// レコーダの放送局名をWeb番組表の放送局名に置き換え
			{	
				HashMap<String,String> misCN = new HashMap<String,String>();
				for ( ReserveList r : recorder.getReserves() ) {
					if ( r.getCh_name() == null ) {
						misCN.put(r.getChannel(),recorder.getRecorderId());
					}
				}
				if ( misCN.size() > 0 ) {
					for ( String cn : misCN.keySet() ) {
						String msg = "【警告(予約一覧)】 <"+misCN.get(cn)+"> \"レコーダの放送局名\"を\"Web番組表の放送局名\"に変換できません。CHコード設定に設定を追加してください：\"レコーダの放送局名\"="+cn;
						mwin.appendMessage(msg);
					}
					ringBeep();
				}
			}
			
			// 自動予約一覧の取得
			if ( recorder.isEditAutoReserveSupported() ) {
				if ( ! recorder.GetRdAutoReserve(force) ) {
					// 取得に失敗
					mwin.appendError(recorder.getErrmsg()+" "+recorder.getIPAddr()+":"+recorder.getPortNo()+":"+recorder.getRecorderId());
					ringBeep();
					return false;
				}
			}
			
			// 録画結果一覧の取得
			if ( env.getSkipGetRdRecorded() ) {
				mwin.appendMessage("【！】録画結果一覧の取得はスキップされました");
			}
			else {
				if ( ! recorder.GetRdRecorded(force) ) {
					// 取得に失敗
					mwin.appendError(recorder.getErrmsg()+" "+recorder.getIPAddr()+":"+recorder.getPortNo()+":"+recorder.getRecorderId());
					ringBeep();
					return false;
				}
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
			mwin.appendError("【致命的エラー】予約一覧の取得で例外が発生 "+recorder.getIPAddr()+":"+recorder.getPortNo()+":"+recorder.getRecorderId());
			ringBeep();
			return false;
		}
		return true;
	}

	/**
	 * 録画結果一覧をＤＬする
	 */
	private boolean reLoadRdRecorded(final String myself) {
		//
		StWinClear();
		
		new SwingBackgroundWorker(false) {
			
			@Override
			protected Object doWorks() throws Exception {
				
				TatCount tc = new TatCount();
			
				boolean succeeded = true;
				
				HDDRecorderList recs;
				if ( myself != null ) {
					recs = recorders.findInstance(myself);
				}
				else {
					recs = recorders;
				}
				for ( HDDRecorder recorder : recs ) {
					switch ( recorder.getType() ) {
					case RECORDER:
					case EPG:
					case MAIL:
					case NULL:
					case TUNER:
						if ( ! recorder.GetRdSettings(true) ) {
							succeeded = false;
						}
						if ( ! recorder.GetRdRecorded(true) ) {
							succeeded = false;
						}
						break;
					default:
						break;
					}
				}
				
				if ( succeeded ) {
					reserved.redrawReservedList();
					recorded.redrawRecordedList();
					
					mwin.appendMessage(String.format("【録画結果一覧の取得処理が完了しました】 所要時間： %.2f秒",tc.end()));
				}
				else {
					ringBeep();
					mwin.appendMessage(String.format("【録画結果一覧の取得処理に失敗しました】 所要時間： %.2f秒",tc.end()));
				}
				return null;
			}
			
			@Override
			protected void doFinally() {
				StWinSetVisible(false);
			}
		}.execute();
		
		StWinSetLocationCenter(this);
		StWinSetVisible(true);
		
		return true;
	}

	/**
	 * Web番組表をＤＬする
	 * <P>単体実行の場合はこちらを呼び出す
	 * <P>部品実行の場合はこちらを呼び出す：{@link #loadTVProgram(boolean, LoadFor)}
	 */
	private boolean reLoadTVProgram(final LoadFor lf) {
		//
		StWinClear();
		
		new SwingBackgroundWorker(false) {
			
			@Override
			protected Object doWorks() throws Exception {
				
				TatCount tc = new TatCount();
				
				loadTVProgram(true, lf);
				
				// 新聞描画枠のリセット
				paper.clearPanel();
				paper.buildMainViewByDate();
				
				// サイドツリーの再構築
				paper.redrawTreeByPassed();
				
				// 再描画
				paper.reselectTree();
				listed.reselectTree();
				
				mwin.appendMessage(String.format("[Web番組表取得] 【完了しました】 所要時間： %.2f秒",tc.end()));
				return null;
			}
			
			@Override
			protected void doFinally() {
				StWinSetVisible(false);
			}
		}.execute();
		
		StWinSetLocationCenter(this);
		StWinSetVisible(true);
		
		return true;
	}
	
	/**
	 * Web番組表をＤＬする
	 * <P>単体実行の場合はこちらを呼び出す：{@link #reLoadTVProgram(LoadFor)}
	 * <P>部品実行の場合はこちらを呼び出す
	 */
	private void loadTVProgram(final boolean b, final LoadFor lf) {
		
		final String FUNCID = "[Web番組表取得] ";
		final String ERRID = "[ERROR]"+FUNCID;
		//
		new SwingBackgroundWorker(true) {
			
			@Override
			protected Object doWorks() throws Exception {
				try {
					String msg;
					TVProgram tvp;
					
					tvp = tvprograms.getTvProgPlugin(null);
					if ( tvp != null )
					{
						String sType = "地上波＆ＢＳ番組表";
						if (lf == LoadFor.ALL || lf == LoadFor.TERRA) {
							loadTVProgramOnce(tvp, sType, tvp.getSelectedArea(), false, b);
						}
						else {
							stwin.appendMessage(FUNCID+sType+"へのアクセスはスキップされました: "+tvp.getTVProgramId());
						}
					}
					
					tvp = tvprograms.getCsProgPlugin(null);
					if ( tvp != null )
					{
						String sType = "ＣＳ番組表[プライマリ]";
						if (lf == LoadFor.ALL || lf == LoadFor.CS || lf == LoadFor.CSo1) {
							loadTVProgramOnce(tvp, sType, tvp.getSelectedArea(), false, b);
						}
						else {
							stwin.appendMessage(FUNCID+sType+"へのアクセスはスキップされました: "+tvp.getTVProgramId());
						}
					}
					
					tvp = tvprograms.getCs2ProgPlugin(null);
					if ( tvp != null )
					{
						String sType = "ＣＳ番組表[セカンダリ]";
						if (lf == LoadFor.ALL || lf == LoadFor.CS || lf == LoadFor.CSo2) {
							loadTVProgramOnce(tvp, sType, tvp.getSelectedArea(), false, b);
						}
						else {
							stwin.appendMessage(FUNCID+sType+"へのアクセスはスキップされました: "+tvp.getTVProgramId());
						}
					}
					
					tvp = tvprograms.getSyobo();
					if ( tvp != null ) {
						String sType = "しょぼかる";
						if ( (lf == LoadFor.ALL || lf == LoadFor.SYOBO) && enableWebAccess && env.getUseSyobocal()) {
							tvp.loadCenter(tvp.getSelectedCode(), b);	// しょぼかるには放送局リストを取得するイベントが他にないので
							loadTVProgramOnce(tvp, sType, null, true, b);
						}
						else {
							stwin.appendMessage(FUNCID+sType+"へのアクセスはスキップされました.");
						}
						
						// しょぼかるの新番組マークを引き継ぐ
						attachSyoboNew();
					}
				
					PickedProgram pickup = tvprograms.getPickup();
					if ( tvp != null ) {
						pickup.refresh();
						//pickup.save();
					}
					
					// 番組タイトルを整形する
					fixTitle();
					fixDetail();
					
					// 検索結果の再構築
					stwin.appendMessage(FUNCID+"検索結果を生成します.");
					mpList.clear(env.getDisableFazzySearch(), env.getDisableFazzySearchReverse());
					mpList.build(tvprograms, trKeys.getTraceKeys(), srKeys.getSearchKeys());
					
					// 過去ローグ
					if ( env.getUsePassedProgram() ) {
						TatCount tc = new TatCount();
						stwin.appendMessage(FUNCID+"過去ログを生成します.");
						if ( tvprograms.getPassed().save(tvprograms.getIterator(), chsort.getClst(), env.getPrepPassedProgramCount()) ) {
							msg = String.format(FUNCID+"過去ログを生成しました [%.2f秒].",tc.end());
							StdAppendMessage(msg);
						}
						//PassedProgramList.getDateList(env.getPassedLogLimit());
					}
					else {
						stwin.appendMessage(FUNCID+"過去ログは記録されません.");
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					mwin.appendError(ERRID+"番組情報の取得で例外が発生");
					ringBeep();
					return null;
				}
				return null;
			}
			
			@Override
			protected void doFinally() {
			}
		}.execute();
	}
	
	private void loadTVProgramOnce(TVProgram tvp, String sType, String aName, boolean loadonly, boolean force) {
		
		final String FUNCID = "[Web番組表取得] ";
		final String ERRID = "[ERROR]"+FUNCID;
		
		// ログ
		String msg = FUNCID+sType+"を取得します: "+tvp.getTVProgramId();
		stwin.appendMessage(msg);
		if (aName!=null) stwin.appendMessage(FUNCID+"＋選択されているエリア="+aName);
		
		// 読み込み
		//tvp.setProgressArea(stwin);
		tvp.loadProgram(tvp.getSelectedCode(), force);
		
		if (loadonly) {
			return;
		}
		
		// 延長警告
		tvp.setExtension(null, null, false, extKeys.getSearchKeys());	// 最初の３引数は盲腸。ダミー
		// NGワード
		tvp.abon(env.getNgword());
		// 抜けチェック
		String errmsg = tvp.chkComplete(); 
		if (errmsg != null) {
			stwin.appendError(FUNCID+"取得した情報が不正です："+errmsg);
			if (mainWindow!=null) mwin.appendMessage(msg);
			ringBeep();
		}
	}
	
	// しょぼかるの番組詳細を番組表に反映する
	private void attachSyoboNew() {
		TVProgram syobo = tvprograms.getSyobo();
		if (syobo == null) {
			return;
		}
		
		for ( TVProgram tvp : tvprograms ) {
			
			if ( tvp.getType() != ProgType.PROG ) {
				continue;
			}
			if ( ! (tvp.getSubtype() == ProgSubtype.TERRA || tvp.getSubtype() == ProgSubtype.CS || tvp.getSubtype() == ProgSubtype.CS2) ) {
				continue;
			}
			
			for ( ProgList tvpl : tvp.getCenters() ) {
				if ( ! tvpl.enabled) {
					continue;
				}
				for ( ProgList svpl : syobo.getCenters() ) {
					if ( ! tvpl.Center.equals(svpl.Center)) {
						continue;
					}
					for ( ProgDateList tvc : tvpl.pdate ) {
						
						ProgDateList mSvc = null;
						for ( ProgDateList svc : svpl.pdate ) {
							if (tvc.Date.equals(svc.Date) ) {
								mSvc = svc;
								break;
							}
						}
						if (mSvc == null) {
							// しょぼかる側に該当する日付自体ないので全部フラグを立てっぱなしでいい
							for ( ProgDetailList tvd : tvc.pdetail ) {
								if ( tvd.isEqualsGenre(ProgGenre.ANIME, null) ) {
									tvd.addOption(ProgOption.NOSYOBO);
								}
							}
						}
						else {
							// しょぼかる側に該当する日付があるのでマッチング。アニメと映画と音楽
							for ( ProgDetailList tvd : tvc.pdetail ) {
								
								// アニメはいったんフラグを立てる
								if ( tvd.isEqualsGenre(ProgGenre.ANIME, null) ) {
									tvd.addOption(ProgOption.NOSYOBO);
								}
								
								boolean isFind = false;
								for ( ProgDetailList svd : mSvc.pdetail ) {
									if ( tvd.start.equals(svd.start) ) {
										
										// 番組ID
										{
											//svd.progid = tvd.progid;
											svd.setContentIdStr();
										}
										
										boolean isAnime = tvd.isEqualsGenre(ProgGenre.ANIME, null);
										if ( ! isAnime && ! tvd.isEqualsGenre(ProgGenre.MOVIE, null) && ! tvd.isEqualsGenre(ProgGenre.MUSIC, null) ) {
											break;
										}
										
										// みつけた
										isFind = true;
										
										// しょぼかるとWeb番組表の両方に存在する
										svd.nosyobo = true;
			
										// 各種フラグ
										{
											boolean isAttached = false;
											
											// 新番組フラグ
											if ( svd.flag == ProgFlags.NEW && tvd.flag != ProgFlags.NEW ) {
												tvd.flag = ProgFlags.NEW;
												isAttached = true;
											}
											
											// 最終回フラグ
											if ( svd.flag == ProgFlags.LAST && tvd.flag != ProgFlags.LAST ) {
												tvd.flag = ProgFlags.LAST;
												isAttached = true;
											}
											
											// ジャンル
											if ( tvd.isEqualsGenre(ProgGenre.MOVIE, null) && ! tvd.isEqualsGenre(ProgGenre.MOVIE, ProgSubgenre.MOVIE_ANIME) ) {
												if ( tvd.genrelist == null ) {
													tvd.genrelist = new ArrayList<ProgGenre>();
													tvd.genrelist.add(tvd.genre);
													tvd.genrelist.add(ProgGenre.MOVIE);
													tvd.subgenrelist = new ArrayList<ProgSubgenre>();
													tvd.subgenrelist.add(tvd.subgenre);
													tvd.subgenrelist.add(ProgSubgenre.MOVIE_ANIME);
												}
												else {
													tvd.genrelist.add(ProgGenre.MOVIE);
													tvd.subgenrelist.add(ProgSubgenre.MOVIE_ANIME);
												}
												isAttached = true;
											}
											
											// その他のフラグ
											for ( ProgOption sopt : svd.getOption() ) {
												if ( tvd.addOption(sopt) && isAttached == false ) {
													isAttached = true;
												}
											}
											
											// ログ
											if (isAttached && env.getDebug()) {
												StdAppendMessage("しょぼかるのフラグを引き継ぎました: ("+tvpl.Center+") "+tvd.title);
											}
										}
										
										// 番組詳細
										if ( tvd.detail.length() < svd.detail.length() ) {
											tvd.detail = svd.detail;
										}
										else {
											int idx = svd.detail.indexOf("<!");
											if (idx != -1) {
												tvd.detail += svd.detail.substring(idx);
											}
										}
										
										// 「しょぼかるにのみ存在」フラグの上げ下げ（これはアニメ限定）
										if ( isAnime ) {
											if ( isFind ) {
												tvd.removeOption(ProgOption.NOSYOBO);	// NOSYOBOって…
											}
											else {
												//tvd.addOption(ProgOption.NOSYOBO);
											}
										}
										
										break;
									}
								}
							}
						}
					}
					break;
				}
			}
		}
	}
	
	// 番組タイトルを整形する
	private void fixTitle() {
		//
		if ( ! env.getFixTitle()) {
			return;
		}
		//
		for ( TVProgram tvp : tvprograms ) {
			//if ( ! (tvp.getType() == ProgType.PROG && tvp.getSubtype() == ProgSubtype.TERRA) ) {
			if ( tvp.getType() != ProgType.PROG ) {
				continue;
			}
			//
			for ( ProgList pl : tvp.getCenters() ) {
				if ( ! pl.enabled ) {
					continue;
				}

				for ( ProgDateList pcl : pl.pdate ) {
					//
					for ( ProgDetailList tvd : pcl.pdetail ) {
						if ( tvd.isEqualsGenre(ProgGenre.ANIME, null) ) {
							if ( pl.Center.startsWith("NHK") || pl.Center.startsWith("ＮＨＫ") ) {
								// NHK系で先頭が「アニメ　」ではじまるものから「アニメ　」を削除する
								tvd.title = tvd.title.replaceFirst("^アニメ[ 　・]+","");
								tvd.titlePop = TraceProgram.replacePop(tvd.title);
								tvd.SearchStrKeys = TraceProgram.splitKeys(tvd.titlePop);
							}
							if ( (tvd.title.contains("劇場版") || tvd.detail.contains("映画")) && ! tvd.isEqualsGenre(ProgGenre.MOVIE, ProgSubgenre.MOVIE_ANIME) ) {
								// ジャンル＝アニメだがタイトルに「劇場版」が含まれるならジャンル＝映画（アニメ映画）を追加する
								if ( tvd.genrelist == null ) {
									tvd.genrelist = new ArrayList<ProgGenre>();
									tvd.genrelist.add(tvd.genre);
									tvd.genrelist.add(ProgGenre.MOVIE);
									tvd.subgenrelist = new ArrayList<ProgSubgenre>();
									tvd.subgenrelist.add(tvd.subgenre);
									tvd.subgenrelist.add(ProgSubgenre.MOVIE_ANIME);
								}
								else {
									tvd.genrelist.add(ProgGenre.MOVIE);
									tvd.subgenrelist.add(ProgSubgenre.MOVIE_ANIME);
								}
							}
						}
						else if ( tvd.isEqualsGenre(ProgGenre.MOVIE, ProgSubgenre.MOVIE_ANIME) && tvd.subgenre != ProgSubgenre.MOVIE_ANIME ) {
							// ジャンル＝映画でサブジャンルが複数ありアニメが優先されてないものはアニメを優先する
							tvd.subgenre = ProgSubgenre.MOVIE_ANIME;
						}
						
						// サブタイトルを番組追跡の対象から外す
						if ( env.getTraceOnlyTitle() && tvd.title != tvd.splitted_title ) {
							tvd.SearchStrKeys = TraceProgram.splitKeys(TraceProgram.replacePop(tvd.splitted_title));	// 番組追跡の検索用インデックスは、サブタイトルを削除したもので置き換える
						}
					}
				}
			}
		}
	}
	
	/**
	 * {@link ProgDetailList} の情報を整形する
	 */
	private void fixDetail() {
		for ( TVProgram tvp : tvprograms ) {
			for ( ProgList pl : tvp.getCenters() ) {
				if ( ! pl.enabled ) {
					continue;
				}
				for ( ProgDateList pcl : pl.pdate ) {
					for ( ProgDetailList tvd : pcl.pdetail ) {
						if ( tvd.start == null || tvd.start.length() == 0 ) {
							continue;
						}
						
						fixDetailSub(tvp, pl, tvd);
					}
				}
			}
		}
	}
	
	private void fixDetailSub(TVProgram tvp, ProgList pl, ProgDetailList tvd) {
		tvd.type = tvp.getType();
		tvd.subtype = tvp.getSubtype();
		tvd.center = pl.Center;
		
		tvd.recmin = CommonUtils.getRecMinVal(tvd.startDateTime, tvd.endDateTime);
		
		tvd.extension_mark = markchar.getExtensionMark(tvd);
		tvd.prefix_mark = markchar.getOptionMark(tvd);
		tvd.newlast_mark = markchar.getNewLastMark(tvd);
		tvd.postfix_mark = markchar.getPostfixMark(tvd);
		
		tvd.dontoverlapdown = (tvd.center.startsWith("ＮＨＫ") || tvd.center.startsWith("NHK"));
	}
	
	/**
	 * <P>過去ログから検索キーワードにマッチする情報を取得する
	 * <P>全部検索がヒットした結果がかえるのだから {@link ProgDetailList} ではなく {@link MarkedProgramList} を使うべきなのだが…
	 */
	private boolean searchPassedProgram(final SearchKey sKey, final String target) {
		
		Matcher ma = Pattern.compile("^(\\d\\d\\d\\d/\\d\\d/\\d\\d)-(\\d\\d\\d\\d/\\d\\d/\\d\\d)$").matcher(target);
		if ( ! ma.find() ) {
			return false;
		}
		
		final GregorianCalendar s = CommonUtils.getCalendar(ma.group(1));
		final GregorianCalendar e = CommonUtils.getCalendar(ma.group(2));
		final long dDays = (e.getTimeInMillis() - s.getTimeInMillis())/86400000 + 1;

		final ArrayList<ProgDetailList> srchpdl = tvprograms.getSearched().getResultBuffer(sKey.getLabel()) ;

		stwin.clear();
		
		// 検索実行（時間がかかるので状況表示する）
		new SwingBackgroundWorker(false) {
			
			@Override
			protected Object doWorks() throws Exception {
				
				TatCount tc = new TatCount();
				
				// 検索中
				int resultCnt = 0;
				for (int cnt=1; cnt<=dDays; cnt++) {
					
					String passdt = CommonUtils.getDate(e);
					stwin.appendMessage(String.format("[過去ログ検索] 検索中：(%d/%d) %s", cnt, dDays, passdt));
					
					PassedProgram tvp = new PassedProgram();
					if ( tvp.loadAllCenters(passdt) ) {
						for ( ProgList pl : tvp.getCenters() ) {
							if ( ! pl.enabled ) {
								continue;
							}
							
							for ( ProgDateList pcl : pl.pdate ) {
								for ( ProgDetailList tvd : pcl.pdetail ) {
									if ( tvd.start == null || tvd.start.length() == 0 ) {
										continue;
									}
									
									if ( SearchProgram.isMatchKeyword(sKey, pl.Center, tvd) ) {
										tvd.dynKey = sKey;
										tvd.dynMatched = SearchProgram.getMatchedString();
										fixDetailSub(tvp, pl, tvd);
										srchpdl.add(tvd);
										if ( ++resultCnt >= env.getSearchResultMax() ) {
											mwin.appendMessage(String.format("[過去ログ検索] 検索件数の上限に到達しました。所要時間： %.2f秒",tc.end()));
											return null;
										}
									}
								}
							}
						}
					}
					
					e.add(Calendar.DATE,-1);
				}

				mwin.appendMessage(String.format("[過去ログ検索] 検索完了。所要時間： %.2f秒",tc.end()));
				return null;
			}
			
			@Override
			protected void doFinally() {
				StWinSetVisible(false);
			}
		}.execute();
		
		StWinSetLocationCenter(this);
		StWinSetVisible(true);

		return true;
	}

	// システムトレイ関係
	private void getTrayIcon() {
		if ( trayicon != null ) {
			return;
		}
		
		try {
			Image image = ImageIO.read(new File(ICONFILE_SYSTRAY));
			trayicon = new TrayIcon(image,"Tainavi");
			
			final Viewer thisClass = this;
			
			// メニューの追加
			PopupMenu popup = new PopupMenu();
			{
				MenuItem item = new MenuItem("開く");
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						thisClass.setVisible(true);
						thisClass.setState(Frame.NORMAL);
					}
				});
				popup.add(item);
			}
			{
				MenuItem item = new MenuItem("終了する");
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						ExitOnClose();
						System.exit(0);
					}
				});
				popup.add(item);
			}
			trayicon.setPopupMenu(popup);
			
			// 左クリックで復帰
			trayicon.addMouseListener(new MouseAdapter() {
				//
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						thisClass.setVisible(true);
						thisClass.setState(Frame.NORMAL);
					}
				}
			});
		
		} catch (IOException e) {
			StdAppendError("アイコンファイルが読み込めませんでした: "+ICONFILE_SYSTRAY);
			e.printStackTrace();
		}
	}
	private void setTrayIconVisible(boolean b) {
		
		if ( ! SystemTray.isSupported() || trayicon == null ) {
			return;
		}
		
		try {
			if ( b ) {
				// システムトレイに追加
				SystemTray.getSystemTray().remove(trayicon);
				SystemTray.getSystemTray().add(trayicon);
			}
			else {
				// システムトレイから削除
				SystemTray.getSystemTray().remove(trayicon);
			}
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	private void HideToTray() {
		if ( SystemTray.isSupported() && trayicon != null && (env.getShowSysTray() && env.getHideToTray()) ) {
			this.setVisible(false);
		}
	}
	private void setXButtonAction(boolean b) {
		if ( b ) {
			this.setDefaultCloseOperation(JFrame.ICONIFIED);
		}
		else {
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
	}
	
	// コマンドライン引数の処理
	private void procArgs(String[] args) {
		int flag = 0;
		for (String arg : args) {
			switch (flag) {
			case 0:
				if (arg.compareTo("-L") == 0) {
					// -l : ロギング
					//logging = false;
				}
				else if (arg.compareTo("-L") == 0) {
					// -L : ロギング不可
					logging = false;
				}
				else if (arg.compareTo("-w") == 0) {
					// -w : レコーダ起動
					runRecWakeup = true;
				}
				else if (arg.compareTo("-nowebaccess") == 0) {
					// -nowebaccess : 起動時のWeb番組表へのアクセス無効
					enableWebAccess = false;
				}
				else if (arg.compareTo("-proxy") == 0) {
					// -proxy : Web番組表へのアクセスにProxy経由を強制する
					flag = 1;
				}
				else if (arg.compareTo("-loadrec") == 0) {
					// -loadrec : 起動時にレコーダにアクセスする
					runRecLoad = true;
				}
				else if (arg.compareTo("-onlyLoadProgram") == 0) {
					// -onlyLoadProgram : 番組表の取得だけ行う
					onlyLoadProgram = true;
				}
				break;
			case 1:
				String[] dat = arg.split(":");
				if (dat.length == 1 ) {
					pxaddr = dat[0];
					pxport = "8080";
				} if (dat.length >= 2 ) {
					pxaddr = dat[0];
					pxport = dat[1];
				}
				flag = 0;
				break;
			}
		}
	}
	
	// メインの環境設定ファイルを読みだす
	private void loadEnvfile() {
		StdAppendMessage("【環境設定】環境設定ファイルを読み込みます.");
		env.load();
	}
	
	// 引き続きその他の環境設定ファイルも読みだす
	private void procEnvs() {
		
		StdAppendMessage("【環境設定】環境設定ファイル類を読み込みます.");

		// 各種設定
		env.makeEnvDir();
		
		// レコーダ一覧
		recInfoList.load();

		// Proxyサーバ
		if (pxaddr != null) {
			env.setUseProxy(true);
			env.setProxyAddr(pxaddr);
			env.setProxyPort(pxport);
		}
		
		// Cookieの処理を入れようとしたけど無理だった
		/*
		{
			CookieManager manager = new CookieManager();
			manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
			CookieHandler.setDefault(manager);
		}
		*/
		
		// ジャンル別背景色
		pColors.load();
		
		// 深夜の帯予約の補正（一日前にずらす）
		// 可能なら番組表を８日分取得する
		// 【WIN】ファイルオープンにrundll32を使用する
		CommonUtils.setAdjLateNight(env.getAdjLateNight());
		CommonUtils.setExpandTo8(env.getExpandTo8());
		CommonUtils.setUseRundll32(env.getUseRundll32());
		CommonUtils.setDisplayPassedReserve(env.getDisplayPassedReserve());
		CommonUtils.setDebug(env.getDebug());
		
		SwingBackgroundWorker.setDebug(env.getDebug());
		
		// クリップボードアイテム
		cbitems.load();
		
		// サイズ・位置情報取得
		bounds.setLoaded(bounds.load());
		
		// 番組追跡キーワード取得
		trKeys.load();
		
		// 検索キーワード取得
		srKeys.load();
		
		// 検索キーワードグループ取得
		srGrps.load();
		
		// 延長警告源設定取得
		extKeys.load();
		
		// デフォルトＡＶ設定取得
		avs.load();
		chavs.load();

		// スポーツ延長警告のデフォルト設定のコードはもういらないので削除（3.15.4β） 
		
		// 簡易描画はもういらないので削除
		
		// ChannelConvert
		chconv.load();
	}
	
	// 二重起動チェック
	private void chkDualBoot() {
		if ( ! env.getOnlyOneInstance() ) {
			return;
		}
		
		if ( ! CommonUtils.getLock() ) {
			// 既にロックされている
			ringBeep();
			System.exit(1);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				// 鯛ナビ終了時にロックを解除する
				CommonUtils.getUnlock();
			}
		});
	}
	
	// アップデートの有無チェック
	private void chkVerUp() {
		if ( ! enableWebAccess || onlyLoadProgram ) {
			stwin.appendError("【オンラインアップデート】オンラインアップデートは無効です");
			return;
		}
		
		VWUpdate vu = new VWUpdate(stwin);
		if ( ! vu.isExpired(env.getUpdateMethod()) ) {
			// メッセージはVWUpdate内で出力されます
			return;
		}
		if ( doVerUp(vu) ) {
			System.exit(0);
		}
	}
	
	private boolean doVerUp(VWUpdate vu) {
		UpdateResult res = vu.checkUpdate(VersionInfo.getVersion());
		switch ( res ) {
		case DONE:
			// 成功
			// 履歴は更新しない（連続アップデートがあるかも知れないので）
			LogViewer lv = new LogViewer(HISTORY_FILE);
			lv.setModal(true);
			lv.setCaretPosition(0);
			lv.setVisible(true);
			return true;
		case PASS:
			// キャンセル
			// 履歴は更新しない（次回に持ち越し）
			break;
		case NOUPDATE:
			// アップデートなし
			vu.updateHistory();
			break;
		default:
			// 失敗
			// 履歴は更新しない（次回再挑戦）
			break;
		}
		return false;
	}
	
	/**
	 *  レコーダプラグインをすべて読み込みます。
	 */
	private boolean loadRecPlugins() {
		
		stwin.appendMessage("【レコーダプラグイン】プラグインを読み込みます.");
		
		boolean isMailPluginEnabled = false;
		try {
			Class.forName("javax.mail.Session");
			isMailPluginEnabled = true;
		}
		catch ( Exception e ) {
			System.err.println("【レコーダプラグイン】メール系プラグイン用の外部ライブラリがみつかりません： "+e.toString());
		}

		boolean isCalendarPluginEnabled = false;
		try {
			Class.forName("com.google.gdata.client.calendar.CalendarService");
			isCalendarPluginEnabled = true;
		}
		catch ( Exception e ) {
			System.err.println("【レコーダプラグイン】カレンダー系プラグイン用の外部ライブラリがみつかりません： "+e.toString());
		}
		
		//
		ArrayList<String> recIda = new ArrayList<String>();
		for ( File f : new File(CommonUtils.joinPath(new String[]{"bin","tainavi"})).listFiles() ) {
			Matcher ma = Pattern.compile("^(PlugIn_Rec[^$]+)[^$]*\\.class$").matcher(f.getName());
			if ( ma.find() ) {
				if ( ! isMailPluginEnabled && f.getName().toLowerCase().contains("mail") ) {
					System.out.println("【レコーダプラグイン】メール系プラグインは無効です： "+f.getName());
					continue;
				}
				if ( ! isCalendarPluginEnabled && f.getName().toLowerCase().contains("calendar") ) {
					System.out.println("【レコーダプラグイン】カレンダー系プラグインは無効です： "+f.getName());
					continue;
				}
				
				recIda.add(ma.group(1));
			}
		}
		String[] recIdd = recIda.toArray(new String[0]);
		Arrays.sort(recIdd);
		
		// servicesに追記
		StringBuilder sb = new StringBuilder();
		for ( String recId : recIdd ) {
			sb.append("tainavi.");
			sb.append(recId);
			sb.append("\n");
		}
		if ( ! CommonUtils.write2file(CommonUtils.joinPath(new String[] {"bin","META-INF","services","tainavi.HDDRecorder"}), sb.toString()) ) {
			stwin.appendError("【レコーダプラグイン】プラグインの読み込みに失敗しました: ");
			return false;
		}

		// ここで例外が起きてもトラップできない、スレッドが落ちる
		ServiceLoader<HDDRecorder> r = ServiceLoader.load(HDDRecorder.class);
		
		recPlugins.clear();
		for ( HDDRecorder recorder : r ) {
			if (env.getDebug()) StdAppendMessage("+追加します: "+recorder.getRecorderId());
			recPlugins.add(recorder.clone());
			StdAppendMessage("+追加しました: "+recorder.getRecorderId());
		}
		
		return true;
	}
	
	/**
	 * レコーダ設定をもとにレコーダプラグインから実レコーダのインスタンスを生成します。
	 */
	private void initRecPluginAll() {
		//
		recorders.clear();
		for ( RecorderInfo ri : recInfoList ) {
			ArrayList<HDDRecorder> rl = recPlugins.findPlugin(ri.getRecorderId());
			if ( rl.size() == 0 ) {
				stwin.appendError("【レコーダプラグイン】プラグインがみつかりません: "+ri.getRecorderId()+"("+ri.getRecorderIPAddr()+":"+ri.getRecorderPortNo()+")");
			}
			else { 
				stwin.appendMessage("【レコーダプラグイン】プラグインを初期化します: "+ri.getRecorderId()+"("+ri.getRecorderIPAddr()+":"+ri.getRecorderPortNo()+")");
				for ( HDDRecorder rPlugin : rl ) {
					initRecPlugin(rPlugin, ri);
				}
			}
		}
	}
	protected HDDRecorder initRecPlugin(HDDRecorder rPlugin, RecorderInfo ri) {
		HDDRecorder rec = rPlugin.clone();
		recorders.add(rec);
		
		rec.getChCode().load(true);	// true : ログ出力あり
		setSettingRecPluginBase(rec, ri);
		setSettingRecPluginExt(rec,env);
		rec.setProgressArea(stwin);
		return rec;
	}
	protected void setSettingRecPluginBase(HDDRecorder to, RecorderInfo from) {
		to.setIPAddr(from.getRecorderIPAddr());
		to.setPortNo(from.getRecorderPortNo());
		to.setUser(from.getRecorderUser());
		to.setPasswd(from.getRecorderPasswd());
		to.setMacAddr(from.getRecorderMacAddr());
		to.setBroadcast(from.getRecorderBroadcast());
		to.setUseCalendar(from.getUseCalendar());
		to.setUseChChange(from.getUseChChange());
		to.setRecordedCheckScope(from.getRecordedCheckScope());
		to.setTunerNum(from.getTunerNum());
		to.setColor(from.getRecorderColor());
	}
	protected void setSettingRecPluginExt(HDDRecorder recorder, Env nEnv) {
		recorder.setUserAgent(nEnv.getUserAgent());
		recorder.setDebug(nEnv.getDebug());
		recorder.setAdjNotRep(nEnv.getAdjoiningNotRepetition());
		recorder.setRecordedSaveScope(nEnv.getRecordedSaveScope());
	}
	
	//
	protected void doRecWakeup() {
		for ( HDDRecorder rec : recorders ) {
			if ( ! rec.getMacAddr().equals("") && ! rec.getBroadcast().equals("") ) {
				rec.wakeup();
			}
		}
	}
	
	/**
	 * 一時間は再実行させないんだ
	 */
	private boolean isOLPExpired(int expire) {
		String fname = "env"+File.separator+"olp.history";
		if ( ! new File(fname).exists() || ! new File(fname).canWrite() ) {
			stwin.appendError("【警告】実行履歴ファイルがないから実行させないよ！");
			ringBeep();
			return false;
		}
		String dat = CommonUtils.read4file(fname, true);
		if ( dat == null ) {
			stwin.appendError("【警告】実行履歴を取得できなかったから実行させないよ！");
			ringBeep();
			return false;
		}
		GregorianCalendar ca = null;
		dat = EncryptPassword.dec(b64.dec(dat));
		if ( dat != null ) {
			ca = CommonUtils.getCalendar(dat);
		}
		if ( ca == null ) {
			stwin.appendError("【警告】実行履歴の内容が不正だったから実行させないよ！ "+dat);
			ringBeep();
			return false;
		}
		if ( CommonUtils.getCompareDateTime(ca, CommonUtils.getCalendar(-expire*3600)) >= 0 ) {
			ca.add(Calendar.HOUR,expire);
			stwin.appendError("【警告】"+expire+"時間以内の再実行は許さないよ！"+CommonUtils.getDateTime(ca)+"まで待って！");
			ringBeep();
			return false;
		}
		if ( ! CommonUtils.write2file(fname, b64.enc(EncryptPassword.enc(CommonUtils.getDateTime(0)))) ) {
			stwin.appendError("【警告】実行履歴を保存できなかったから実行させないよ！");
			ringBeep();
			return false;
		}
		
		return true;
	}
	
	/**
	 *  Web番組表プラグインをすべて読み込みます。
	 */
	private boolean loadProgPlugins() {
	
		final String FUNCID = "[Web番組表プラグイン組込] ";
		final String ERRID = "[ERROR]"+FUNCID;
		
		// Web番組表プラグインの処理
		stwin.appendMessage(FUNCID+"プラグインを読み込みます.");
		
		// Web番組表共通設定
		setSettingProgPluginCommon(env);
		
		/*
		 * 重要 - ここから
		 */
		
		// TVProgramListのインスタンスは別途初期化が必要
		progPlugins.clear();
		tvprograms.clear();

		/*
		 * 重要 - ここまで
		 */

		ArrayList<String> prgIda = new ArrayList<String>();
		for ( File f : new File(CommonUtils.joinPath("bin","tainavi")).listFiles() ) {
			Matcher ma = Pattern.compile("^(PlugIn_(TV|CS|RAD)P[^$]+)\\.class$").matcher(f.getName());
			if (ma.find()) {
				prgIda.add(ma.group(1));
			}
		}
		String[] prgIdd = prgIda.toArray(new String[0]);
		Arrays.sort(prgIdd);
		
		// servicesに追記
		StringBuilder sb = new StringBuilder();
		for ( String prgId : prgIdd ) {
			sb.append("tainavi.");
			sb.append(prgId);
			sb.append("\n");
		}
		if ( ! CommonUtils.write2file(CommonUtils.joinPath("bin","META-INF","services","tainavi.TVProgram"), sb.toString()) ) {
			stwin.appendError(ERRID+"プラグインの読み込みに失敗しました: ");
			return false;
		}
		
		ServiceLoader<TVProgram> p = ServiceLoader.load(TVProgram.class);

		// 実際必要ないのだが、プラグインのインスタンスはclone()して使う
		for ( TVProgram pg : p ) {
			TVProgram prog = pg.clone();
			
			stwin.appendMessage("+追加しました: "+prog.getTVProgramId());
			
			// CH設定タブではプラグイン側のインスタンスを使うので情報を追加してやる必要があるのであった
			setSettingProgPlugin(prog, env);
			
			progPlugins.add(prog);
		}
		
		p = null;
		
		return true;
	}
	
	/**
	 *  設定にあわせてWeb番組表プラグインを絞り込みます。
	 */
	private void setSelectedProgPlugin() {
		
		// この３つは保存しておく
		Syobocal syobo = tvprograms.getSyobo();
		PassedProgram passed = tvprograms.getPassed();
		PickedProgram pickup = tvprograms.getPickup();
		SearchResult searched = tvprograms.getSearched();
		
		tvprograms.clear();
		
		{
			TVProgram tvp = progPlugins.getTvProgPlugin(env.getTVProgramSite());
			if ( tvp == null ) {
				// デフォルトもなければ先頭にあるもの
				tvp = progPlugins.getTvProgPlugin(null);
			}
			if ( tvp == null ) {
				// てか一個もなくね？
				StdAppendError("【Web番組表選択】地上波＆ＢＳ番組表が選択されていません: "+env.getTVProgramSite());
			}
			else {
				StdAppendMessage("【Web番組表選択】地上波＆ＢＳ番組表が選択されました: "+tvp.getTVProgramId());
				tvprograms.add(tvp.clone());
			}
		}
		{
			TVProgram tvp = progPlugins.getCsProgPlugin(env.getCSProgramSite());
			if ( tvp == null ) {
				tvp = progPlugins.getCsProgPlugin(null);
			}
			if ( tvp == null ) {
				StdAppendError("【Web番組表選択】ＣＳ番組表[プライマリ]が選択されていません： "+env.getCSProgramSite());
			}
			else {
				StdAppendMessage("【Web番組表選択】ＣＳ番組表[プライマリ]が選択されました: "+tvp.getTVProgramId());
				tvprograms.add(tvp.clone());
			}
		}
		{
			TVProgram tvp = progPlugins.getCs2ProgPlugin(env.getCS2ProgramSite());
			if ( tvp == null ) {
				tvp = progPlugins.getCs2ProgPlugin(null);
			}
			if ( tvp == null ) {
				StdAppendError("【Web番組表選択】ＣＳ番組表[プライマリ]が選択されていません： "+env.getCS2ProgramSite());
			}
			else {
				StdAppendMessage("【Web番組表選択】ＣＳ番組表[プライマリ]が選択されました: "+tvp.getTVProgramId());
				tvprograms.add(tvp.clone());
			}
		}
		/*
		if ( progPlugins.getRadioProgPlugins().size() > 0 )
		{
			TVProgram tvp = progPlugins.getCsProgPlugin(env.getRadioProgramSite());
			if ( tvp == null ) {
				tvp = progPlugins.getCsProgPlugin(null);
			}
			if ( tvp == null ) {
				StdAppendError("【Web番組表選択】ラジオ番組表が選択されていません： "+env.getRadioProgramSite());
			}
			else {
				StdAppendMessage("【Web番組表選択】ラジオ番組表が選択されました: "+tvp.getTVProgramId());
				tvprograms.add(tvp.clone());
			}
		}
		*/
		
		{
			if ( syobo == null ) {
				syobo = new Syobocal();
			}
			tvprograms.add(syobo);
		}
		{
			if ( passed == null ) {
				passed = new PassedProgram();
			}
			tvprograms.add(passed);
		}
		{
			if ( pickup == null ) {
				pickup = new PickedProgram();
				pickup.loadProgram(null, false);
			}
			tvprograms.add(pickup);
		}
		{
			if ( searched == null ) {
				searched = new SearchResult();
			}
			tvprograms.add(searched);
		}
	}
	
	/**
	 * Web番組表設定をもとにレコーダプラグインのインスタンスを生成します。
	 */
	private void initProgPluginAll() {

		final String FUNCID = "[Web番組表プラグイン初期化] ";
		final LinkedHashMap<ArrayList<TVProgram>,String> map = new LinkedHashMap<ArrayList<TVProgram>, String>();
		map.put(tvprograms.getTvProgPlugins(), "地上波＆ＢＳ番組表");
		map.put(tvprograms.getCsProgPlugins(), "ＣＳ番組表[プライマリ]");
		map.put(tvprograms.getCs2ProgPlugins(), "ＣＳ番組表[セカンダリ]");
		//map.put(progPlugins.getRadioProgPlugins(), "ラジオ番組表");
		
		new SwingBackgroundWorker(true) {
			
			@Override
			protected Object doWorks() throws Exception {

				for ( ArrayList<TVProgram> tvpa : map.keySet() ) {
					stwin.appendMessage(FUNCID+map.get(tvpa)+"のベース情報（放送局リストなど）を取得します.");
					for ( TVProgram p : tvpa ) {
						stwin.appendMessage(FUNCID+"プラグインを初期化します： "+p.getTVProgramId());
						
						try {
							// 個別設定（２）　…（１）と（２）の順番が逆だったので前に移動してきました(3.17.3β）
							setSettingProgPlugin(p,env);				// 他からも呼び出される部分だけ分離
							
							// 個別設定（１）
							p.setOptString(null);						// フリーオプション初期化
							p.loadAreaCode();							// 放送エリア情報取得
							p.loadCenter(p.getSelectedCode(),false);	// 放送局情報取得
							p.setSortedCRlist();						// 有効放送局だけよりわける
						}
						catch (Exception e) {
							stwin.appendError(FUNCID+"ベース情報の取得に失敗しました.");
							e.printStackTrace();
						}
					}
				}
				
				// 共通設定部分の一斉更新
				//setSettingProgPluginAll(env);
				
				if ( env.getUseSyobocal() ) {
					TVProgram syobo = tvprograms.getSyobo();
					if ( syobo != null ) {
						stwin.appendMessage(FUNCID+"しょぼかるを初期化します.");
						setSettingProgPlugin(syobo,env);				// 他からも呼び出される部分だけ分離
						syobo.setUserAgent("tainavi");
						syobo.setOptString(null);						// フリーオプション初期化
						syobo.loadCenter(syobo.getSelectedCode(), false);
					}
				}
				
				return null;
			}
			
			@Override
			protected void doFinally() {
			}
		}.execute();
	}
	protected void setSettingProgPluginAll(Env nEnv) {
		// 通常
		setSettingProgPlugin(tvprograms.getTvProgPlugin(null),nEnv);
		setSettingProgPlugin(tvprograms.getCsProgPlugin(null),nEnv);
		setSettingProgPlugin(tvprograms.getCs2ProgPlugin(null),nEnv);
		//setSettingProgPlugin(tvprograms.getRadioProgPlugin(null),nEnv);
		setSettingProgPlugin(tvprograms.getSyobo(),nEnv);
		
		// しょぼかるは特殊
		tvprograms.getSyobo().setUserAgent("tainavi");
		// 検索結果も特殊
		tvprograms.getSearched().setResultBufferMax(nEnv.getSearchResultBufferMax());
	}
	protected void setSettingProgPlugin(TVProgram p, Env nEnv) {
		if ( p == null ) {
			return;
		}		
		p.setUserAgent(nEnv.getUserAgent());
		p.setProgDir(nEnv.getProgDir());
		p.setCacheExpired((enableWebAccess)?(nEnv.getCacheTimeLimit()):(0));
		p.setContinueTomorrow(nEnv.getContinueTomorrow());
		p.setExpandTo8(nEnv.getExpandTo8());
		//p.setUseDetailCache(nEnv.getUseDetailCache());
		p.setUseDetailCache(false);
		p.setSplitEpno(nEnv.getSplitEpno());
	}
	
	/**
	 * staticで持っている共通設定の更新
	 */
	protected void setSettingProgPluginCommon(Env nEnv) {
		
		if ( nEnv.getUseProxy() && (nEnv.getProxyAddr().length() > 0 && nEnv.getProxyPort().length() > 0) ) {
			stwin.appendMessage("＋Web番組表へのアクセスにProxyが設定されています： "+nEnv.getProxyAddr()+":"+nEnv.getProxyPort());
			TVProgramUtils.setProxy(nEnv.getProxyAddr(),nEnv.getProxyPort());
		}
		else {
			TVProgramUtils.setProxy(null,null);
		}
		
		TVProgramUtils.setProgressArea(stwin);
		TVProgramUtils.setChConv(chconv);
	}
	
	//
	private void initMpList() {
		//mpList = new MarkedProgramList();			// 検索結果リスト
		mpList.setHistoryOnlyUpdateOnce(env.getHistoryOnlyUpdateOnce());
		mpList.setShowOnlyNonrepeated(env.getShowOnlyNonrepeated());
	}
	
	// L&FとFontを設定
	private void initLookAndFeelAndFont() {

		try {
			{
				vwlaf = new VWLookAndFeel();
				
				String lafname = vwlaf.update(env.getLookAndFeel());
				if ( lafname != null && ! lafname.equals(env.getLookAndFeel())) {
					env.setLookAndFeel(lafname);
				}
				
				if ( CommonUtils.isMac() ) {
					UIManager.getDefaults().put("Table.gridColor", new Color(128,128,128));
					//UIManager.getDefaults().put("Table.selectionBackground", new Color(182,207,229));
					//UIManager.getDefaults().put("Table.selectionForeground", new Color(0,0,0));
				}
			}
			
			{
				vwfont = new VWFont();
				
				String fname = vwfont.update(env.getFontName(),env.getFontSize());
				if ( fname != null && ! fname.equals(env.getFontName())) {
					env.setFontName(fname);
				}
			}
		}
		catch ( Exception e ) {
			// 落ちられると困るからトラップしておこうぜ
			e.printStackTrace();
		}
	}
	
	// L&FやFontを変えたらコンポーネントに通知が必要
	protected void updateComponentTreeUI() {
		try {
			SwingUtilities.updateComponentTreeUI(this);
			SwingUtilities.updateComponentTreeUI(stwin);
			SwingUtilities.updateComponentTreeUI(mwin);
			SwingUtilities.updateComponentTreeUI(pcwin);
			SwingUtilities.updateComponentTreeUI(rdialog);
			SwingUtilities.updateComponentTreeUI(ccwin);
		}
		catch ( Exception e ) {
			// 落ちられると困るからトラップしておこうぜ
			e.printStackTrace();
		}
	}

	// ツールチップの表示遅延時間を設定する
	private void setTooltipDelay() {
		ToolTipManager tp = ToolTipManager.sharedInstance();
		tp.setInitialDelay(env.getTooltipInitialDelay()*100);
		tp.setDismissDelay(env.getTooltipDismissDelay()*100);
	}
	
	/**
	 * 
	 * @return true:前回終了時の設定がある場合
	 */
	private boolean buildMainWindow() {
		//
		mainWindow.addToolBar(toolBar);
		mainWindow.addStatusArea(mwin);
		
		mainWindow.addTab(listed, MWinTab.LISTED);
		mainWindow.addTab(paper, MWinTab.PAPER);
		mainWindow.addTab(reserved, MWinTab.RSVED);
		mainWindow.addTab(recorded, MWinTab.RECED);
		mainWindow.addTab(autores, MWinTab.AUTORES);
		mainWindow.addTab(setting, MWinTab.SETTING);
		mainWindow.addTab(recsetting, MWinTab.RECSET);
		mainWindow.addTab(chsetting, MWinTab.CHSET);
		mainWindow.addTab(chsortsetting, MWinTab.CHSORT);
		mainWindow.addTab(chconvsetting, MWinTab.CHCONV);
		mainWindow.addTab(chdatsetting, MWinTab.CHDAT);
		
		//新聞描画枠のリセット
		paper.clearPanel();
		paper.buildMainViewByDate();
		
		// サイドツリーのデフォルトノードの選択
		paper.selectTreeDefault();
		listed.selectTreeDefault();
		
		if ( recInfoList.size() > 0 ) {
			// 前回終了時設定が存在する場合
			
			// 開いていたタブ
			mainWindow.setShowSettingTabs(bounds.getShowSettingTabs());
			
			// ステータスエリアの高さ
			mwin.setRows(bounds.getStatusRows());
			
			return false;
		}
		
		// 前回終了時設定が存在しない場合
		return true;
	}
	
	private void ShowInitTab() {
		
		// いったん無選択状態にしてから
		mainWindow.setSelectedTab(null);
		
		if ( recInfoList.size() <= 0 ) {
			// 設定が存在しない場合
			mainWindow.setSelectedTab(MWinTab.RECSET);
		}
		else {
			// 設定が存在する場合
			mainWindow.setSelectedTab(MWinTab.getAt(bounds.getSelectedTab()));
		}
	}
	
	//
	private void setInitBounds() {
		// ウィンドウのサイズと表示位置を設定する
		Rectangle window = bounds.getWinRectangle();
		if (bounds.isLoaded()) {
			// 設定ファイルを読み込んであったらそれを設定する
			System.out.println(DBGID+"set bounds "+window);
			this.setBounds(window.x, window.y, window.width, window.height);
		}
		else {
			// 設定ファイルがなければ自動設定する
			Rectangle screen = this.getGraphicsConfiguration().getBounds();
			int x = 0;
			int w = window.width;
			if (window.width > screen.width) {
				x = 0;
				w = screen.width;
			}
			else {
				x = (screen.width - window.width)/2;
			}
			int y = 0;
			int h = window.height;
			if (window.height > screen.height) {
				y = 0;
				h = screen.height;
			}
			else {
				y = (screen.height - window.height)/2;
			}
			this.setBounds(x, y, w, h);
		}
	}

	/**
	 * <P>ステータスエリアを隠す
	 * {@link VWMainWindow#setStatusVisible(boolean)}の置き換え
	 */
	private void setStatusVisible(boolean b) {
		
		if (b) {
			listed.setDetailVisible(true);
			paper.setDetailVisible(true);
			MWinSetVisible(true);
		}
		else {
			listed.setDetailVisible(false);
			paper.setDetailVisible(false);
			MWinSetVisible(false);
		}
	}
	
	// フルスクリーンモードをトグル切り替え
	private Dimension f_dim;
	private Point f_pnt;
	private int divloc_l = 0;
	private int divloc_p = 0;
	
	private void setFullScreen(boolean b) {
		
		if ( b == true ) {
			// 枠の撤去
			this.dispose();
			this.setUndecorated(true);
			this.setVisible(true);
			
			//全画面表示へ
			Toolkit tk = getToolkit();
			Insets in = tk.getScreenInsets(getGraphicsConfiguration());
			Dimension d = tk.getScreenSize();
			f_dim = this.getSize();
			f_pnt = this.getLocation();
			this.setBounds(in.left, in.top, d.width-(in.left+in.right), d.height-(in.top+in.bottom));
			
			divloc_l = bounds.getTreeWidth();
			divloc_p = bounds.getTreeWidthPaper();
			
			// ツリーを閉じる
			paper.setCollapseTree();
			listed.setCollapseTree();
		}
		else {
			if ( f_pnt != null && f_dim != null ) {	// 起動直後などは値がないですしね
				
				// 枠の復帰
				this.dispose();
				this.setUndecorated(false);
				this.setVisible(true);
				
				//全画面表示終了
				this.setBounds(f_pnt.x, f_pnt.y, f_dim.width, f_dim.height);
				
				bounds.setTreeWidth(divloc_l);
				bounds.setTreeWidthPaper(divloc_p);
				
				// ツリーの幅を元に戻す
				paper.setExpandTree();
				listed.setExpandTree();
			}
		}
	}
	
	// タイトルバー
	private void setTitleBar() {
		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
		MemoryUsage heapUsage = mbean.getHeapMemoryUsage();
		
		this.setTitle(
				String.format(
						"%s - %s - Memory Usage Max:%dM Committed:%dM Used:%dM - FrameBuffer Status:%s",
						VersionInfo.getVersion(),
						CommonUtils.getDateTime(0),
						heapUsage.getMax()/(1024*1024),
						heapUsage.getCommitted()/(1024*1024),
						heapUsage.getUsed()/(1024*1024),
						(paper!=null)?(paper.getFrameBufferStatus()):("N/A")
				)
		);
	}
	
	@Override
	public void timerRised(VWTimerRiseEvent e) {
		if (env.getDebug()) System.out.println("Timer Rised: now="+CommonUtils.getDateTimeYMDx(e.getCalendar()));
		setTitleBar();
	}
	
	// 終了処理関連
	private void ExitOnClose() {
		// 座標・サイズ
		if ( ! this.toolBar.isFullScreen()) {
			Rectangle r = this.getBounds();
			bounds.setWinRectangle(r);
		}
		else {
			Rectangle r = new Rectangle();
			r.x = this.f_pnt.x;
			r.y = this.f_pnt.y;
			r.width = this.f_dim.width;
			r.height = this.f_dim.height;
			bounds.setWinRectangle(r);
		}
		listed.copyColumnWidth();
		reserved.copyColumnWidth();
		
		bounds.setStatusRows(mwin.getRows());

		// 動作状態
		bounds.setSelectedTab(mainWindow.getSelectedTab().getIndex());
		bounds.setShowSettingTabs(mainWindow.getShowSettingTabs());
		bounds.setSelectedRecorderId(toolBar.getSelectedRecorder());
		bounds.setShowStatus(toolBar.isStatusShown());
		
		// 保存する
		bounds.save();
		
		// ツリーの展開状態の保存
		listed.saveTreeExpansion();
		paper.saveTreeExpansion();
	}
	

	/*******************************************************************************
	 * main()
	 ******************************************************************************/
	
	// 初期化が完了したら立てる
	private static boolean initialized = false;
	private static Viewer myClass = null;
	
	/**
	 * めいーん
	 * @param args
	 * @throws NoSuchAlgorithmException
	 * @version 今まで初期化を行ってからウィンドウを作成していたが<BR>
	 * 途中で例外が起こるとダンマリの上にゾンビになってたりとヒドかったので<BR>
	 * 先にウィンドウを作成してから初期化を行うように変えました
	 * @throws InterruptedException 
	 * @throws InvocationTargetException 
	 */
	public static void main(final String[] args) throws NoSuchAlgorithmException, InvocationTargetException, InterruptedException {
		
		if ( myClass != null ) {
			// 既に起動していたらフォアグラウンドにする
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					// うーん、いいのかこのコード？
					myClass.setVisible(true);
					myClass.setState(Frame.NORMAL);
				}
			});
			return;
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				final Viewer thisClass = myClass = new Viewer(args);
				
				thisClass.addComponentListener(new ComponentAdapter() {
					@Override
					public void componentShown(ComponentEvent e) {
						
						// 一回実行したらもういらないよ
						thisClass.removeComponentListener(this);
						
						// 初期化するよ
						thisClass.initialize(args);
						
					}
				});
				
				thisClass.setVisible(true);
			}
		});
	}

	
	
	/*******************************************************************************
	 * コンストラクタ
	 ******************************************************************************/

	/**
	 * デフォルトコンストラクタ
	 */
	public Viewer(final String[] args) {
		
		super();
		
		env.loadText();
		bounds.loadText();
		
		
		// 初期化が終わるまでは閉じられないよ　→　どうせステータスウィンドウにブロックされて操作できない
		//setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//setResizable(false);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setTitleBar();
		
		try {
			Image image = ImageIO.read(new File(ICONFILE_TAINAVI));
			setIconImage(image);
		}
		catch (IOException e) {
			StdAppendError("[ERROR] アイコンが設定できない： "+e.toString());
		}
		
		JLabel jLabel_splash_img = new JLabel(new ImageIcon("splash.gif"));
		jLabel_splash_img.setPreferredSize(new Dimension(400,300));
		//getContentPane().setLayout(new BorderLayout());
		getContentPane().add(jLabel_splash_img, BorderLayout.CENTER);
		pack();
		
		setLocationRelativeTo(null);	// 画面の真ん中に
		
		// SwingLocker共有設定
		SwingLocker.setOwner(this);
		
		// とりあえずルックアンドフィールはリセットしておかないとだめっぽいよ
		initLookAndFeelAndFont();
		updateComponentTreeUI();
	}
	
	// 初期化をバックグラウンドで行う
	private void initialize(final String[] args) {
		
		StWinClear();
		
		// 初期化処理はバックグラウンドで行う
		new SwingBackgroundWorker(false) {
			
			@Override
			protected Object doWorks() throws Exception {
				
				TatCount tc = new TatCount();
				
				// 初期化処理
				_initialize(args);
				
				// 終わったら閉じられるようにするよ
				//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				//setResizable(true);

				stwin.append("");
				stwin.appendMessage(String.format("【タイニー番組ナビゲータが起動しました】 所要時間： %.2f秒",tc.end()));
				return null;
			}
			
			@Override
			protected void doFinally() {
				if ( ! initialized ) System.err.println("[ERROR][鯛ナビ] 【致命的エラー】 初期化処理を行っていたスレッドが異常終了しました。");
				stwin.setClosingEnabled(false);
				CommonUtils.milSleep(OPENING_WIAT);
				StWinSetVisible(false);
			}
		}.execute();
		
		StWinSetLocationUnder(this);
		StWinSetVisible(true);
	}
	
	// 初期化の本体
	private void _initialize(final String[] args) {
		
		// コマンドライン引数を処理する
		procArgs(args);
		
		// ログ出力を設定する（Windowsの場合は文字コードをMS932にする） →DOS窓を殺したので終了
		System.setOut(new DebugPrintStream(System.out,LOG_FILE,logging));
		System.setErr(new DebugPrintStream(System.err,LOG_FILE,logging));
		
		// 起動メッセージ
		StdAppendMessage("================================================================================");
		StdAppendMessage("以下のメッセージは無視してください（原因調査中）");
		StdAppendMessage("Exception occurred during event dispatching:");
		StdAppendMessage("	java.lang.NullPointerException");
		StdAppendMessage("		at javax.swing.plaf.basic.BasicScrollBarUI.layoutHScrollbar(Unknown Source)");
		StdAppendMessage("		（以下略）");
		StdAppendMessage("================================================================================");
		stwin.appendMessage(CommonUtils.getDateTime(0));
		stwin.appendMessage(String.format("タイニー番組ナビゲータが起動を開始しました(VersionInfo:%s on %s)",VersionInfo.getVersion(),VersionInfo.getEnvironment()));
		
		// 起動時にアップデートを確認する
		chkVerUp();
		
		try {
			// メインの環境設定ファイルを読み込む
			loadEnvfile();
			
			// 二重起動防止
			chkDualBoot();
			
			// その他の環境設定ファイルを読み込む
			procEnvs();
			
			if ( onlyLoadProgram ) {
				if ( ! isOLPExpired(4) ) {
					CommonUtils.milSleep(3000);
					System.exit(1);
				}
				// プラグインのロード
				loadProgPlugins();
				// プラグインの初期化
				setSelectedProgPlugin();
				initProgPluginAll();
				// 検索結果リストの初期化（loadTVProgram()中で使うので）
				initMpList();
				// データのロード
				loadTVProgram(true,LoadFor.ALL);
				stwin.appendMessage("番組表を取得したので終了します");
				CommonUtils.milSleep(3000);
				System.exit(1);
			}
			
			// プラグインのロード
			loadProgPlugins();
			loadRecPlugins();

			// プラグインの初期化
			setSelectedProgPlugin();
			initProgPluginAll();

			initRecPluginAll();

			// WOL指定があったなら
			if ( runRecWakeup ) {
				doRecWakeup();
			}

			// 検索結果リストの初期化（loadTVProgram()中で使うので）
			initMpList();
			
			// データのロード
			loadTVProgram(false,LoadFor.ALL);
			
			// 放送局の並び順もロード
			chsort.load();
			
			loadRdReserve(runRecLoad, null);
		}
		catch ( Exception e ) {
			System.err.println("【致命的エラー】設定の初期化に失敗しました");
			e.printStackTrace();
			System.exit(1);
		}
		
		// 背景色設定ダイアログにフォント名の一覧を設定する
		pcwin.setFontList(vwfont);
		
		// （新聞形式の）ツールチップの表示時間を変更する
		setTooltipDelay();

		boolean firstRun = true;
		try {
			// メインウィンドウの作成
			mainWindow = new VWMainWindow();
		
			// 内部クラスのインスタンス生成
			toolBar = new VWToolBar();
			listed = new VWListedView();
			paper = new VWPaperView();
			reserved = new VWReserveListView();
			recorded = new VWRecordedListView();
			autores = new VWAutoReserveListView();
			setting = new VWSettingView();
			recsetting = new VWRecorderSettingView();
			chsetting = new VWChannelSettingView();
			chdatsetting = new VWChannelDatSettingView();
			chsortsetting = new VWChannelSortView();
			chconvsetting = new VWChannelConvertView();
			
			// 設定のほにゃらら
			toolBar.setDebug(env.getDebug());
			autores.setDebug(env.getDebug());

			// ページャーの設定
			toolBar.setPagerItems();
			
			// ウィンドウを構築
			firstRun = buildMainWindow();
			
			// ステータスエリアを開く
			setStatusVisible(bounds.getShowStatus());
		}
		catch ( Exception e ) {
			System.err.println("【致命的エラー】ウィンドウの構築に失敗しました");
			e.printStackTrace();
			System.exit(1);
		}
		
		// ★★★★★★★★★★
		//int x = 2/0;	// サブスレッドの突然死のトラップを確認するためのコード
		// ★★★★★★★★★★
		
		// トレイアイコンを作る
		getTrayIcon();
		setTrayIconVisible(env.getShowSysTray());
		
		// ウィンドウを閉じたときの処理
		setXButtonAction(env.getShowSysTray() && env.getHideToTray());
		
		// ウィンドウ操作のリスナー登録
		this.addWindowListener(new WindowAdapter() {
			// ウィンドウを最小化したときの処理
			@Override
			public void windowIconified(WindowEvent e) {
				HideToTray();
			}
		
			// ウィンドウを閉じたときの処理
			@Override
			public void windowClosing(WindowEvent e) {
				ExitOnClose();
			}
		});
		
		// タブを選択
		ShowInitTab();
		
		// 初回起動時はレコーダの登録を促す
		if (firstRun) {
			Container cp = getContentPane();
			JOptionPane.showMessageDialog(cp, "レコーダが登録されていません。\n最初に登録を行ってください。\n番組表だけを使いたい場合は、\nNULLプラグインを登録してください。");
		}
		
		// メインウィンドウをスプラッシュからコンポーネントに入れ替える
		this.setVisible(false);
		this.setContentPane(mainWindow);
		setInitBounds();
		this.setVisible(true);
		
		// タイトル更新
		setTitleBar();
		
		// [ツールバー/共通] レコーダ情報変更
		toolBar.addHDDRecorderChangeListener(autores);
		
		// [ツールバー/レコーダ選択]
		toolBar.addHDDRecorderSelectionListener(autores);	// 自動予約一覧
		toolBar.addHDDRecorderSelectionListener(rdialog);	// 予約ダイアログ

		// レコーダ選択イベントキック
		toolBar.setSelectedRecorder(bounds.getSelectedRecorderId());
		
		// [タイマー] タイトルバー更新／リスト形式の現在時刻ノード／新聞形式の現在時刻ノード
		timer_now.addVWTimerRiseListener(this);
		timer_now.addVWTimerRiseListener(listed);
		timer_now.addVWTimerRiseListener(paper);
		
		// タイマー起動
		timer_now.start();
		
		// メッセージだ
		mwin.appendMessage(String.format("タイニー番組ナビゲータが起動しました (VersionInfo:%s on %s)",VersionInfo.getVersion(),VersionInfo.getEnvironment()));
		
		initialized = true;
	}
}
