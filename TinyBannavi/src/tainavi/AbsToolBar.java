package tainavi;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import tainavi.HDDRecorder.RecType;
import tainavi.SearchKey.TargetId;
import tainavi.TVProgramIterator.IterationType;
import tainavi.VWMainWindow.MWinTab;
import tainavi.VWUpdate.UpdateResult;
import tainavi.Viewer.LoadFor;
import tainavi.Viewer.LoadRsvedFor;;

/**
 * ツールバーのクラス
 * @version 3.16.3β Viewer.classから分離
 */
public abstract class AbsToolBar extends JToolBar implements HDDRecorderSelectable {

	private static final long serialVersionUID = 1L;

	public static String getViewName() { return "ツールバー"; } 

	public void setDebug(boolean b) {debug = b; }
	private boolean debug = false;
	
	/*******************************************************************************
	 * 抽象メソッド
	 ******************************************************************************/

	// 共用部品群
	
	protected abstract Env getEnv();
	protected abstract Bounds getBoundsEnv();
	protected abstract TVProgramList getTVPrograms();
	protected abstract ChannelSort getChannelSort();
	protected abstract HDDRecorderList getHDDRecorders();
	
	protected abstract StatusWindow getStWin(); 
	protected abstract StatusTextArea getMWin();

	protected abstract Component getParentComponent();
	
	protected abstract void ringBeep();
	
	// 親に依頼
	
	// リスト形式
	protected abstract boolean doKeywordSerach(SearchKey search, String kStr, String sStr, boolean doFilter);
	protected abstract boolean doBatchReserve();
	// 新聞形式
	protected abstract boolean jumpToNow();
	protected abstract boolean jumpToPassed(String passed);
	protected abstract boolean redrawByPager();
	protected abstract void toggleMatchBorder();
	protected abstract void setPaperColorDialogVisible(boolean b);
	protected abstract void setPaperZoom(int n);
	// 共通
	protected abstract boolean recorderSelectorChanged();
	protected abstract void takeSnapShot();
	// メインウィンドウ
	/**
	 * 親から呼ばないでくださいね！
	 * @see #setToggleShowStatusButton(boolean)
	 */
	protected abstract void setStatusVisible(boolean b);
	/**
	 * 親から呼ばないでくださいね！
	 * @see #setToggleFullScreenButton(boolean)
	 */
	protected abstract void setFullScreen(boolean b);
	protected abstract void toggleSettingTabVisible();
	protected abstract boolean isTabSelected(MWinTab tab);
	// 部品
	protected abstract boolean addKeywordSearch(SearchKey search);
	protected abstract boolean reLoadTVProgram(LoadFor lf);
	
	protected abstract boolean doLoadRdRecorder(LoadRsvedFor lrf);

	/*******************************************************************************
	 * 呼び出し元から引き継いだもの
	 ******************************************************************************/

	private final Env env = getEnv();
	private final Bounds bounds = getBoundsEnv();
	private final TVProgramList tvprograms = getTVPrograms();
	private final ChannelSort chsort = getChannelSort();
	private final HDDRecorderList recorders = getHDDRecorders();
	
	private final StatusWindow StWin = getStWin();			// これは起動時に作成されたまま変更されないオブジェクト
	private final StatusTextArea MWin = getMWin();			// これは起動時に作成されたまま変更されないオブジェクト
	
	private final Component parent = getParentComponent();	// これは起動時に作成されたまま変更されないオブジェクト
	
	/*******************************************************************************
	 * 定数
	 ******************************************************************************/
	
	// アイコンファイル
	
	private static final String ICONFILE_SEARCH			= "icon/system-search-2.png";
	private static final String ICONFILE_ADDKEYWORD		= "icon/bookmark-new-list-4.png";
	private static final String ICONFILE_RELOADPROG		= "icon/internet-news-reader.png";
	private static final String ICONFILE_BATCHCMD		= "icon/checkbox.png";
	private static final String ICONFILE_RELOADRSV		= "icon/video-television.png";
	private static final String ICONFILE_WAKEUP			= "icon/system-shutdown-2.png";
	private static final String ICONFILE_SHUTDOWN		= "icon/user-offline.png";
	private static final String ICONFILE_SHOWSETTING	= "icon/system.png";
	private static final String ICONFILE_STATUSHIDDEN	= "icon/view-split-top-bottom-3.png";
	private static final String ICONFILE_STATUSSHOWN	= "icon/view-close.png";
	private static final String ICONFILE_TOFULL			= "icon/view-fullscreen-5.png";
	private static final String ICONFILE_TOWIN			= "icon/view-nofullscreen-3.png";
	private static final String ICONFILE_SHOWMATCHBORDER= "icon/view-calendar-timeline.png";
	private static final String ICONFILE_JUMPTONOW		= "icon/view-calendar-time-spent.png";
	private static final String ICONFILE_PALLET			= "icon/colorize-2.png";
	private static final String ICONFILE_TIMER			= "icon/tool_timer.png";
	private static final String ICONFILE_SCREENSHOT		= "icon/camera.png";
	private static final String ICONFILE_SHOWLOG		= "icon/utilities-log_viewer.png";
	private static final String ICONFILE_UPDATE			= "icon/system-software-update-2.png";
	private static final String ICONFILE_HELP			= "icon/help-browser-2.png";

	private static final String ICONFILE_PULLDOWNMENU	= "icon/down-arrow.png";

	// ツールチップ関連
	
	private static final String TIPS_KEYWORD			= "<HTML><B>検索ボックスの書式</B><BR>検索：(オプション1) (オプション2) キーワード <BR>過去ログ検索：開始日[(YYYY/)MM/DD] 終了日[(YYYY/)MM/DD] (オプション2) キーワード<BR>過去ログ閲覧：日付[YYYY/MM/DD]<BR>※オプション1：@filter..絞込検索（過去ログは対象外）<BR>※オプション2：#title..番組名一致、#detail..番組詳細一致、なし..番組名＆番組詳細一致<BR></HTML>";
	private static final String TIPS_SEARCH				= "キーワード検索 or 過去ログ閲覧";
	private static final String TIPS_ADDKEYWORD			= "キーワードリストに登録";
	private static final String TIPS_PAGER				= "ページャー";
	private static final String TIPS_RELOADPROG			= "Webから番組情報を再取得";
	private static final String TIPS_BATCHRESERVATION	= "一括予約";
	private static final String TIPS_RECORDERSEL		= "操作するレコーダを選択する";
	private static final String TIPS_RELOADRSVED		= "レコーダから予約情報を再取得＆レコーダの各種設定情報の収集";
	private static final String TIPS_WAKEUP				= "レコーダの電源を入れる";
	private static final String TIPS_DOWN				= "レコーダの電源を落とす";
	private static final String TIPS_SHOWSETTING		= "設定タブを表示する";
	private static final String TIPS_STATUSHIDDEN		= "ステータスエリアを表示する";
	private static final String TIPS_STATUSSHOWN		= "ステータスエリアを隠す";
	private static final String TIPS_TOFULL				= "フルスクリーンモードへ";
	private static final String TIPS_TOWIN				= "ウィンドウモードへ";
	private static final String TIPS_SHOWBORDER			= "予約待機一覧を重ね合わせ表示する";
	private static final String TIPS_JUMPTO				= "新聞形式の現在日時までジャンプ";
	private static final String TIPS_PAPERCOLOR			= "新聞形式のジャンル別背景色を設定する";
	private static final String TIPS_PAPERZOOM			= "新聞形式の番組枠の高さを拡大する";
	private static final String TIPS_SNAPSHOT			= "<HTML><P>スナップショットをとる<P><B>★メモリの使用量が大きめなので、スナップショット作成後は再起動をおすすめします</B></HTML>";
	private static final String TIPS_LOGVIEW			= "ログをビューアで開く";
	private static final String TIPS_UPDATE				= "オンラインアップデートを行う";
	private static final String TIPS_OPENHELP			= "ブラウザでヘルプを開く";

	// その他
	
	private static final String HELP_URL = "http://sourceforge.jp/projects/tainavi/wiki/FrontPage";
	
	private static final int OPENING_WIAT = 500;				// まあ起動時しか使わないんですけども

	// ログ関連
	
	private static final String MSGID = "["+getViewName()+"] ";
	private static final String ERRID = "[ERROR]"+MSGID;
	private static final String DBGID = "[DEBUG]"+MSGID;

	/*******************************************************************************
	 * 部品
	 ******************************************************************************/

	// ツールバーのコンポーネント
	
	private JTextFieldWithPopup jTextField_keyword = null;
	private JButton jButton_search = null;
	private JButton jButton_addkeyword = null;
	private JWideComboBox jComboBox_select_recorder = null;
	private JWideComboBox jComboBox_pager = null;
	private JButton jButton_reloadprogs = null;
	private JButton jButton_reloadprogmenu = null;
	private JPopupMenu jPopupMenu_reloadprogmenu = null;
	private JButton jButton_reloadrsved = null;
	private JButton jButton_reloadrsvedmenu = null;
	private JPopupMenu jPopupMenu_reloadrsvedmenu = null;
	//private JButton jButton_reloadreced = null;
	private JButton jButton_batchreservation = null;
	private JToggleButton jToggleButton_showmatchborder = null;
	private JButton jButton_moveToNow = null;
	private JButton jButton_wakeup = null;
	private JButton jButton_shutdown = null;
	private JButton jButton_snapshot = null;
	private JButton jButton_paperColors = null;
	private JSlider jSlider_paperZoom = null;
	private JToggleButton jToggleButton_timer = null;
	private JButton jButton_logviewer = null;
	private JToggleButton jToggleButton_showsetting = null;
	private JToggleButton jToggleButton_showstatus = null;
	private JToggleButton jToggleButton_fullScreen = null;
	private JButton jButton_update = null;
	private JButton jButton_help = null;

	// レコーダ選択イベント発生時にキックするリスナーのリスト
	private final ArrayList<HDDRecorderListener> lsnrs_recsel = new ArrayList<HDDRecorderListener>();
	
	// 各種情報の変更イベント発生時にキックするリスナーのリスト
	private final ArrayList<HDDRecorderListener> lsnrs_infochg = new ArrayList<HDDRecorderListener>();
	
	// その他
	
	private String selectedMySelf = null;
	private HDDRecorderList selectedRecorderList = null;
	
	private boolean statusarea_shown = bounds.getShowStatus();
	
	/*******************************************************************************
	 * コンストラクタ
	 ******************************************************************************/
	
	public AbsToolBar() {
		
		super();
		
		this.add(getJTextField_keyword());
		this.add(getJButton_search("キーワード検索"));
		this.add(getJButton_addkeyword("キーワード一覧に登録"));
		this.addSeparator(new Dimension(4,0));
		this.add(getJButton_reloadprogs("番組情報再取得"));
		this.add(getJButton_reloadprogmenu("番組情報再取得メニュー"));
		this.add(getJToggleButton_showmatchborder("予約待機一覧を重ね合わせ表示する"));
		this.add(getJButton_moveToNow("現在日時"));
		//this.addSeparator(new Dimension(4,0));
		this.addSeparator(new Dimension(4,0));
		this.add(getJComboBox_pager());
		this.addSeparator(new Dimension(6,0));
		this.add(getJButton_batchreservation("一括予約"));
		this.add(getJButton_reloadrsved("レコ情報再取得"));
		this.add(getJButton_reloadrsvedmenu("レコ情報再取得メニュー"));
		this.addSeparator(new Dimension(4,0));
		this.add(getJButton_wakeup("入"));
		this.add(getJButton_shutdown("切"));
		this.add(getJComboBox_select_recorder());
		this.addSeparator(new Dimension(6,0));
		this.add(getJButton_snapshot("スナップショット"));
		this.add(getJButton_paperColors("ジャンル別背景色設定"));
		this.add(getJSlider_paperZoom("番組枠表示拡大"));
		this.add(getJButton_logviewer("ログビューア"));
		this.add(getJToggleButton_timer("タイマー"));
		this.add(getJToggleButton_showsetting("設定タブを開く"));
		this.addSeparator(new Dimension(4,0));
		this.add(getJToggleButton_showstatus("ステータス領域"));
		this.add(getJToggleButton_fullScreen("全"));
		this.addSeparator(new Dimension(4,0));
		this.add(getJButton_update("オンラインアップデート"));
		this.add(getJButton_help("ヘルプ"));
	}
	
	/*******************************************************************************
	 * アクション
	 ******************************************************************************/
	
	/**
	 * キーワード検索ボックスからの検索の実行
	 */
	private void toolbarSearch() {
		// 入力形式による分岐
		boolean doFilter = false;
		String sStr = null;
		String kStr = null;
		Matcher ma = Pattern.compile("^(@(.+?)[ 　]+)").matcher(jTextField_keyword.getText());
		if ( ma.find() ) {
			if ( ma.group(2).matches("^f(ilter)?$") ) {
				// 絞込検索
				kStr = jTextField_keyword.getText();
				kStr = kStr.substring(ma.group(1).length()-1,kStr.length()).trim();
				doFilter = true;
			}
		}
		else {
			ma = Pattern.compile("^(\\d\\d\\d\\d/)?(\\d\\d/\\d\\d)([ 　]+((\\d\\d\\d\\d/)?\\d\\d/\\d\\d))?[  　]+").matcher(jTextField_keyword.getText());
			if (ma.find()) {
				// 過去ログ検索(範囲指定あり）
				String sD;
				if (ma.group(1) == null || ma.group(1).length() == 0) {
					GregorianCalendar c = CommonUtils.getCalendar(0);
					sD = String.format("%04d/%s", c.get(Calendar.YEAR), ma.group(2));
				}
				else {
					sD = ma.group(1)+ma.group(2);
				}
				String cD = CommonUtils.getDate529(0,false);
				String pD = CommonUtils.getDate529(-86400,false);
				//String sD = ma.group(2);
				String eD;
				if (ma.group(4) == null) {
					eD = pD;
				}
				else {
					if (ma.group(5) == null) {
						GregorianCalendar c = CommonUtils.getCalendar(0);
						eD = String.format("%04d/%s", c.get(Calendar.YEAR), ma.group(4));
					}
					else {
						eD = ma.group(4);
					}
				}
				if (sD.compareTo(eD) > 0) {
					// 開始日と終了日が逆転していたら入れ替える
					String tD = sD;
					sD = eD;
					eD = tD;
				}
				if (eD.compareTo(cD) >= 0) {
					MWin.appendError(ERRID+"[過去ログ検索] 終了日付には前日以前の日付("+pD+")を指定してください");
					ringBeep();
					return;
				}
				else {
					sStr = String.format("%s-%s", sD, eD);
				}
				kStr = ma.replaceFirst("");
			}
			else {
				// 通常ログ検索
				kStr = jTextField_keyword.getText().trim();
			}
		}
		if ( kStr == null || kStr.matches("^[ 　]*$") ) {
			// 検索キーワードがない
			doKeywordSerach(null,null,null,false);
			return;
		}
		
		// 検索キーワードの解析
		SearchKey search = decSearchKeyText(kStr);
		if ( search == null ) {
			return;
		}
		
		// 検索実行
		if (search.alTarget.size() > 0) {
			doKeywordSerach(search,kStr,sStr,doFilter);
		}
	}
	
	/**
	 * キーワード検索ボックスに入力された字句の解析
	 * @param kStr キーワード検索ボックスに入力された字句
	 * @return
	 */
	private SearchKey decSearchKeyText(String kStr) {
		
		// オプション
		TargetId tId = TargetId.TITLEANDDETAIL;
		while ( true ) {
			Matcher ma = Pattern.compile("^#(.+?)\\s+").matcher(kStr);
			if ( ! ma.find() ) {
				break;
			}
			
			if ( ma.group(1).matches("^t(itle)?$") ) {
				tId = TargetId.TITLE;
			}
			else if ( ma.group(1).matches("^d(etail)?$") ) {
				tId = TargetId.DETAIL;
			}
			kStr = ma.replaceFirst("");
		}
		
		SearchKey search = new SearchKey();
		
		search.setLabel(kStr);
		
		// 完全一致
		if ( kStr.matches("^\".+\"$") ) {
			search.setCondition("0");
			search.alTarget.add(tId);
			search.alContain.add("0");
			search.alKeyword_plane.add(kStr.substring(1,kStr.length()-1));
			search.alKeyword.add(kStr.substring(1,kStr.length()-1));
			search.setCaseSensitive(true);
			return search;
		}
		
		// 正規表現
		{
			search.setCondition("0");
			for (String s : kStr.split("[ 　]")) {
				if ( ! s.equals("")) {
					search.alTarget.add(tId);
					search.alContain.add("0");
					try {
						search.alKeyword_regex.add(Pattern.compile("("+TraceProgram.replacePop(s)+")"));
						search.alKeyword.add(s);
					}
					catch (PatternSyntaxException e) {
						MWin.appendError(ERRID+"正規表現の文法に則っていません： "+s);
						ringBeep();
						return null;
					}
				}
			}
			search.setCaseSensitive(false);
			return search;
		}
	}
	
	/**
	 * 
	 */
	public void setAddkeywordEnabled(boolean b) {
		jButton_addkeyword.setEnabled(b);
	}
	
	/**
	 * ばちー
	 */
	public void setBatchReservationEnabled(boolean b) {
		jButton_batchreservation.setEnabled(b);
	}
	
	/**
	 * すなー
	 */
	public void setSnapShotEnabled(boolean b) {
		jButton_snapshot.setEnabled(b);
	}
	
	/**
	 * ぺぱー
	 */
	public void setPaperColorDialogEnabled(boolean b) {
		jButton_paperColors.setEnabled(b);
		jSlider_paperZoom.setEnabled(b);
	}
	
	/**
	 * ぼだー
	 */
	public void setBorderToggleEnabled(boolean b) {
		jToggleButton_showmatchborder.setEnabled(b);
	}
	
	/**
	 * ページャーコンボボックスの有効無効。新聞形式を開いている時以外は有効にならないよ
	 * @param b
	 */
	public void setPagerEnabled(boolean b) {// 新聞形式を開いてないとだめだよ
		jComboBox_pager.removeItemListener(il_pagerSelected);	// そんな…
		
		jComboBox_pager.setEnabled(b && isTabSelected(MWinTab.PAPER) && env.isPagerEnabled());
		
		jComboBox_pager.addItemListener(il_pagerSelected);
	}
	
	/**
	 * ページャーコンボボックスの書き換え（汎用版）
	 */
	public void setPagerItems() {
		
		if ( env.isPagerEnabled() ) {
			TVProgramIterator pli = tvprograms.getIterator().build(chsort.getClst(), IterationType.ALL);
			setPagerItems(pli,null);
		}
		else {
			jComboBox_pager.removeItemListener(il_pagerSelected);
			jComboBox_pager.removeAllItems();
			jComboBox_pager.addItemListener(il_pagerSelected);
		}
		
	}
	
	/**
	 * ページャーコンボボックスの書き換え（こちらはPaperViewからしか呼ばれないはずである）
	 */
	public void setPagerItems(TVProgramIterator pli, Integer curindex) {
		
		if ( ! env.isPagerEnabled() ) {
			return;
		}
		
		int total_page = 1+env.getPageIndex(pli.size());
		
		// イベント停止
		jComboBox_pager.removeItemListener(il_pagerSelected);
		
		int index = jComboBox_pager.getSelectedIndex();
		
		// ページャー書き換え
		jComboBox_pager.removeAllItems();
		
		// これは…
		if ( total_page == 0 ) {
			// イベント再開…はしなくていいか
			return;
		}

		pli.rewind();	// 巻き戻してください
		
		for (int i=1; i<=total_page; i++) {
			String centers = "";
			for ( int j=0; j<env.getCenterPerPage() && pli.hasNext(); j++ ) {
				centers += pli.next().Center+"、";
			}
			centers = centers.replaceFirst(".$", "");
			jComboBox_pager.addItem(i+"/"+total_page+((centers.length()>0)?(" - "+centers):("")));
		}

		// 選択するページ番号を決定する
		int newindex = 0;
		if ( curindex == null ) {
			// 指定されていないなら基本は以前選択されていたもの
			newindex = (index>=0) ? (index) : (0);
		}
		else {
			// 指定されていればそれ
			newindex = curindex;
		}
		if ( newindex >= jComboBox_pager.getItemCount() ) {
			// 書き換えの結果ページの数が減ってしまう（通常ここにはこないはずなので、これは例外防止用）
			if (debug) System.out.println(DBGID+"ページ数が変更された： "+newindex+" -> "+jComboBox_pager.getItemCount());
			newindex = 0;
		}
		jComboBox_pager.setSelectedIndex(newindex);
		
		// イベント再開
		jComboBox_pager.addItemListener(il_pagerSelected);
	}
	
	/**
	 * ページャーコンボボックスのアイテム数
	 */
	public int getPagerCount() {
		return jComboBox_pager.getItemCount();
	}
	
	/**
	 * ページャーコンボボックスの選択位置
	 */
	public int getSelectedPagerIndex() {
		return jComboBox_pager.getSelectedIndex();
	}
	
	/**
	 * ページャーコンボボックスの選択
	 */
	public void setSelectedPagerIndex(int idx) {
		if (jComboBox_pager.isEnabled() ) { 
			jComboBox_pager.setSelectedItem(null);
			jComboBox_pager.setSelectedIndex(idx);
		}
	}

	/**
	 * 指定のレコーダは選択されているか
	 */
	public boolean isRecorderSelected(String myself) {
		String sid = getSelectedRecorder();
		if ( sid == null || sid.equals(myself)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 選択されているレコーダー（のMySelf()）を返す
	 * @return 「すべて」が選択されている場合はNULL、「ピックアップ」が選択されている場合は""を返す
	 */
	public String getSelectedRecorder() {
		
		if ( jComboBox_select_recorder == null ) {
			return HDDRecorder.SELECTED_ALL;
		}
		
		String recId = (String)jComboBox_select_recorder.getSelectedItem();
		
		/*
		if ( recId.equals(HDDRecorder.SELECTED_ALL) ) {
			return HDDRecorderListener.SELECTED_ALL;
		}
		else if ( recId.equals(HDDRecorder.SELECTED_PICKUP) ) {
			return HDDRecorderListener.SELECTED_PICKUP;
		}
		*/
		
		return recId;
	}
	
	/**
	 * 選択しているレコーダを変える
	 * @param myself 「すべて」を選択する場合はNULLを渡す
	 */
	public void setSelectedRecorder(String myself) {
		if ( jComboBox_select_recorder != null ) {
			jComboBox_select_recorder.setSelectedItem(null);
			jComboBox_select_recorder.setSelectedItem((myself == null)?(HDDRecorder.SELECTED_ALL):(myself));
		}
	}
	
	/**
	 * レコーダコンボボックスを初期化する
	 */
	public void updateRecorderComboBox() {
		
		jComboBox_select_recorder.removeItemListener(il_recorderSelected);

		// レコーダの選択情報をリセット
		setSelectedRecorderInfo(null);
		
		jComboBox_select_recorder.removeAllItems();
		jComboBox_select_recorder.addItem(HDDRecorder.SELECTED_ALL);
		for (HDDRecorder r : recorders) {
			switch ( r.getType() ) {
			case RECORDER:
			case EPG:
			case MAIL:
			case NULL:
				jComboBox_select_recorder.addItem(r.Myself());
				break;
			default:
				break;
			}
		}
		jComboBox_select_recorder.addItem(HDDRecorder.SELECTED_PICKUP);

		jComboBox_select_recorder.addItemListener(il_recorderSelected);
	}

	/**
	 * 
	 */
	public boolean updateReloadProgramExtention() {
		
		// 消して
		jPopupMenu_reloadprogmenu.removeAll();
		
		// 追加する
		for (LoadFor lf : reloadProgMenu ) {
			if ( lf == null ) {
				jPopupMenu_reloadprogmenu.addSeparator();
				continue;
			}
			
			if ( (lf == LoadFor.CSwSD && ! env.isShutdownEnabled()) ||
					(lf == LoadFor.SYOBO && ! env.getUseSyobocal()) ) {
				// 無効なメニューがある
				continue;
			}
		
			JMenuItem menuItem = new JMenuItem(lf.getName());
			jPopupMenu_reloadprogmenu.add(menuItem);
			
			menuItem.addActionListener(al_reloadProgramIndividual);
		}
		
		return true;
	}
	
	/**
	 * ステータスエリアは表示中？
	 */
	public boolean isStatusShown() {
		return jToggleButton_showstatus.isSelected();
	}
	
	/**
	 * フルスクリーン化しているかな？
	 */
	public boolean isFullScreen() {
		return jToggleButton_fullScreen.isSelected();
	}
	
	
	/*******************************************************************************
	 * リスナー追加／削除
	 ******************************************************************************/
	
	/**
	 * レコーダ選択イベントリスナー
	 */
	@Override
	public void addHDDRecorderSelectionListener(HDDRecorderListener l) {
		if ( ! lsnrs_recsel.contains(l) ) {
			lsnrs_recsel.add(l);
		}
	}

	@Override
	public void removeHDDRecorderSelectionListener(HDDRecorderListener l) {
		lsnrs_recsel.remove(l);
	}
	
	@Override
	public String getSelectedMySelf() {
		return selectedMySelf;
	}
	
	@Override
	public HDDRecorderList getSelectedList() {
		return selectedRecorderList;
	}
	
	/**
	 * 情報変更イベントリスナー（番組表リロード、レコーダ情報リロード、etc）
	 */
	@Override
	public void addHDDRecorderChangeListener(HDDRecorderListener l) {
		if ( ! lsnrs_infochg.contains(l) ) {
			lsnrs_infochg.add(l);
		}
	}

	@Override
	public void removeHDDRecorderChangeListener(HDDRecorderListener l) {
		lsnrs_infochg.remove(l);
	}

	
	/*******************************************************************************
	 * イベントトリガー
	 ******************************************************************************/

	/**
	 * レコーダ選択イベントトリガー
	 */
	private void fireHDDRecorderSelected() {
		
		HDDRecorderSelectionEvent e = new HDDRecorderSelectionEvent(this);

		if (debug) System.out.println(DBGID+"recorder select rise.");

		for ( HDDRecorderListener l : lsnrs_recsel ) {
			l.valueChanged(e);
		}
	}
	
	/**
	 * レコーダ状態変更イベントトリガー
	 */
	private void fireHDDRecorderChanged() {
		
		HDDRecorderChangeEvent e = new HDDRecorderChangeEvent(this);

		if (debug) System.out.println(DBGID+"recorder change rise.");

		for ( HDDRecorderListener l : lsnrs_infochg ) {
			l.stateChanged(e);
		}
	}
	
	/*******************************************************************************
	 * リスナー
	 ******************************************************************************/

	// キーワード検索ボックスが確定された or キーワード検索ボタンが押された
	private final ActionListener al_keywordEntered = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (jTextField_keyword.getText().matches("^\\d\\d\\d\\d/\\d\\d/\\d\\d$")) {
				if ( ! jumpToPassed(jTextField_keyword.getText())) {
					JOptionPane.showConfirmDialog(null, jTextField_keyword.getText()+"はみつからなかったでゲソ！", "警告", JOptionPane.CLOSED_OPTION);
				}
			}
			else {
				toolbarSearch();
			}
		}
	};
	
	// キーワード検索への登録ボタンが押された
	private final ActionListener al_keywordAdded = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			// 「キーワード検索の設定」ウィンドウを開く
			String kStr = jTextField_keyword.getText().trim();
			
			SearchKey search = decSearchKeyText(kStr);
			if ( search == null ) {
				return;
			}
			
			jTextField_keyword.setText("");

			addKeywordSearch(search);
		}
	};
	
	// ページャーコンボボックスが選択された
	private final ItemListener il_pagerSelected = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				if (debug) System.out.println(DBGID+"PAGER SELECTED");
				redrawByPager();
			}
		}
	};
	
	// 番組表の再取得の実行
	private final MouseListener ml_reloadProgram = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			reLoadTVProgram(LoadFor.ALL);
		}
	};
	
	// 番組表の再取得の拡張メニュー
	private final MouseAdapter ma_reloadProgramExtention = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			jPopupMenu_reloadprogmenu.show(jButton_reloadprogs,0,jButton_reloadprogs.getHeight());
		}
	};
	
	// 番組表の再取得の拡張メニューの個々のアイテムにつけるリスナー
	private final ActionListener al_reloadProgramIndividual = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			String selected = ((JMenuItem)e.getSource()).getText();
			LoadFor lf = LoadFor.get(selected);
			switch (lf) {
			case CSwSD: 
				if ( env.isShutdownEnabled() ) {
					reLoadTVProgram(LoadFor.CS);
					CommonUtils.executeCommand(env.getShutdownCmd());
				}
				else {
					JOptionPane.showMessageDialog(parent, "シャットダウンコマンドが利用できません");
				}
				break;
			default:
				reLoadTVProgram(lf);
				break;
			}
		}
	};
	
	// 一括登録の実行
	private final ActionListener al_batchreservation = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			doBatchReserve();
		}
	};

	/**
	 * レコーダコンボボックスが選択された
	 */
	private final ItemListener il_recorderSelected = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				
				// 選択中のレコーダ情報を保存
				setSelectedRecorderInfo(getSelectedRecorder());
				
				// 各タブへの反映
				
				// 旧ロジック
				recorderSelectorChanged();
				
				// 新ロジック
				fireHDDRecorderSelected();
			}
			
		}
	};
	
	/**
	 * 選択中のレコーダ情報を保存
	 */
	private void setSelectedRecorderInfo(String myself) {
		selectedMySelf = myself;
		selectedRecorderList = recorders.findInstance(myself);
	}
	
	// 予約一覧の再取得
	private final ActionListener al_reloadReserved = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			
			doLoadRdRecorder(null);
			
			fireHDDRecorderChanged();		// 各タブへの反映
		}
	};
	
	// 番組表の再取得の拡張メニューの個々のアイテムにつけるリスナー
	private final ActionListener al_reloadReservedIndividual = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			doLoadRdRecorder(LoadRsvedFor.get(((JMenuItem)e.getSource()).getText()));
			
			fireHDDRecorderChanged();		// 各タブへの反映
		}
	};

	// 番組表の再取得の拡張メニュー
	private final MouseAdapter ma_reloadReservedExtention = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			jPopupMenu_reloadrsvedmenu.show(jButton_reloadrsved,0,jButton_reloadrsved.getHeight());
		}
	};
	
	// レコーダにWOL
	private final ActionListener al_wakeup = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			for (HDDRecorder r : recorders) {
				if ( ! isRecorderSelected(r.Myself())) {
					continue;
				}
				if ( r.getType() != RecType.RECORDER) {
					continue;
				}
				if ( r.getMacAddr().equals("") || r.getBroadcast().equals("")) {
					MWin.appendError(ERRID+"MACアドレスとブロードキャストアドレスを設定してください： "+r.Myself());
					ringBeep();
					continue;
				}
				r.wakeup();
				MWin.appendMessage(MSGID+"wakeupリクエストを送信しました： "+r.Myself());
			}
		}
	};
	
	// レコーダの電源を落とす
	private final ActionListener al_down = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			for (HDDRecorder r : recorders) {
				if ( ! isRecorderSelected(r.Myself())) {
					continue;
				}
				if ( ! r.getMacAddr().equals("") && ! r.getBroadcast().equals("")) {
					r.shutdown();
					MWin.appendMessage(MSGID+"shutdownリクエストを送信しました： "+r.Myself());
				}
			}
		}
	};
	
	// 設定タブを出したりしまったり
	private final ActionListener al_showsetting = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			toggleSettingTabVisible();
		}
	};

	// ステータスエリアを出したりしまったり
	private final ActionListener al_toggleShowStatus = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {

			JToggleButton btn = (JToggleButton) e.getSource();
			boolean b = btn.isSelected();

			if (debug) System.out.println(DBGID+"act_toggleShowStatus "+b);
			
			setStatusVisible(b);
			
			bounds.setShowStatus(b);
		}
	};
	
	// フルスクリーンになったりウィンドウになったり
	private final ActionListener al_toggleFullscreen = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			JToggleButton btn = (JToggleButton) e.getSource();
			boolean b = btn.isSelected();
			
			if (debug) System.out.println(DBGID+"al_toggleFullscreen "+b);
			
			// ウィンドウ化
			setFullScreen(b);
			
			// ステータスエリアの表示／非表示
			if ( b ) {
				// フルスクリーン化の際、ステータスエリアは隠す
				statusarea_shown = jToggleButton_showstatus.isSelected();
				if ( jToggleButton_showstatus.isSelected() == true ) jToggleButton_showstatus.doClick();
			}
			else {
				// ウィンドウに戻す際、もともとステータスエリアを開いていた場合だけ戻す
				if ( jToggleButton_showstatus.isSelected() != statusarea_shown ) jToggleButton_showstatus.doClick();
			}
		}
	};
	
	// 新聞形式に予約待機枠を表示させたりしなかったり
	private final ActionListener al_showborder = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			toggleMatchBorder();
		}
	};
	
	// 新聞形式の現在日付までジャンプ
	private final ActionListener al_jumpto = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			jumpToNow();
		}
	};
	
	// 新聞形式の背景色選択ダイアログを表示する
	private final ActionListener al_showpapercolordialog = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			setPaperColorDialogVisible(true);
		}
	};
	
	private final MouseAdapter ml_paperzoom = new MouseAdapter() {
		
		@Override
		public void mouseReleased(MouseEvent e) {
			JSlider sl = (JSlider) e.getSource();
			setPaperZoom(sl.getValue());
		}
	};
	
	// スナップショットをとる
	private final ActionListener al_snapshot = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			takeSnapShot();
		}
	};
	
	// ログをビューアで開く
	private final ActionListener al_logview = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			LogViewer lv = new LogViewer(Viewer.LOG_FILE);
			lv.setVisible(true);
		}
	};
	
	//
	private final ActionListener al_update = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			
			StWin.clear();
			new SwingBackgroundWorker(false) {
				@Override
				protected Object doWorks() throws Exception {
					UpdateResult res = new VWUpdate(StWin).checkUpdate(VersionInfo.getVersion()); 
					if (res == UpdateResult.DONE) {
						LogViewer lv = new LogViewer(Viewer.HISTORY_FILE);
						lv.setCaretPosition(0);
						lv.setVisible(true);
					}
					return null;
				}
				@Override
				protected void doFinally() {
					CommonUtils.milSleep(OPENING_WIAT);
					StWin.setVisible(false);
				}
			}.execute();
			
			CommonSwingUtils.setLocationCenter(parent, (Component) StWin);
			StWin.setVisible(true);
		}
	};

	// ヘルプを開く
	private final ActionListener al_openhelp = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			try {
				Desktop desktop = Desktop.getDesktop();
				desktop.browse(new URI(HELP_URL));
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
		}
	};
	
	/*******************************************************************************
	 * コンポーネント
	 ******************************************************************************/

	// キーワード検索ボックス
	private JTextField getJTextField_keyword() {
		if (jTextField_keyword == null) {
			jTextField_keyword = new JTextFieldWithPopup(16);
			Dimension d = jTextField_keyword.getPreferredSize();

			jTextField_keyword.setMaximumSize(d);	// 固定しないと環境によってサイズがかわっちゃう
			jTextField_keyword.setMinimumSize(d);
			
			jTextField_keyword.setToolTipText(TIPS_KEYWORD);
			
			jTextField_keyword.addActionListener(al_keywordEntered);
		}
		return jTextField_keyword;
	}

	// 「検索ボタン」
	private JButton getJButton_search(String s) {
		if (jButton_search == null) {
			ImageIcon icon = new ImageIcon(ICONFILE_SEARCH);
			jButton_search = new JButton(icon);
			jButton_search.setToolTipText(TIPS_SEARCH);
			
			//jButton_search.addActionListener(al_searchRequested);
			jButton_search.addActionListener(al_keywordEntered);
		}
		return jButton_search;
	}

	// 「キーワード検索に登録」
	private JButton getJButton_addkeyword(String s) {
		if (jButton_addkeyword == null) {
			ImageIcon icon = new ImageIcon(ICONFILE_ADDKEYWORD);
			jButton_addkeyword = new JButton(icon);
			jButton_addkeyword.setToolTipText(TIPS_ADDKEYWORD);

			jButton_addkeyword.addActionListener(al_keywordAdded);
		}
		return jButton_addkeyword;
	}
	
	// 「ページャー」
	private JComboBox getJComboBox_pager() {
		if (jComboBox_pager == null) {
			jComboBox_pager = new JWideComboBox();
			jComboBox_pager.addPopupWidth(600);
			jComboBox_pager.setToolTipText(TIPS_PAGER);
			
			int w = 16*6;
			Dimension d = jComboBox_pager.getPreferredSize();
			d.width = w;
			jComboBox_pager.setPreferredSize(d);
			
			d = jComboBox_pager.getMaximumSize();
			d.width = w;
			jComboBox_pager.setMaximumSize(d);
			jComboBox_pager.setMinimumSize(d);
			jComboBox_pager.setEnabled(false);
			
			// 選択されたっぽい
			jComboBox_pager.addItemListener(il_pagerSelected);
		}
		return(jComboBox_pager);
	}

	// 「番組情報を再取得」
	private JButton getJButton_reloadprogs(String s) {
		if (jButton_reloadprogs == null) {
			ImageIcon icon = new ImageIcon(ICONFILE_RELOADPROG);
			jButton_reloadprogs = new JButton(icon);
			jButton_reloadprogs.setToolTipText(TIPS_RELOADPROG);
			
			jButton_reloadprogs.addMouseListener(ml_reloadProgram);
		}
		return jButton_reloadprogs;
	}

	// 「番組情報を再取得」の拡張メニューの並び順
	private final LoadFor[] reloadProgMenu =
		{
			LoadFor.TERRA,
			LoadFor.CS,
			LoadFor.CSo1,
			LoadFor.CSo2,
			null,
			LoadFor.CSwSD,
			null,
			LoadFor.SYOBO
		};

	// 「番組情報を再取得」の拡張メニュー
	private JButton getJButton_reloadprogmenu(String s) {
		if (jButton_reloadprogmenu == null) {
			// メニューの作成
			jPopupMenu_reloadprogmenu = new JPopupMenu();

			// アイテムの登録
			updateReloadProgramExtention();
			
			ImageIcon arrow = new ImageIcon(ICONFILE_PULLDOWNMENU);
			jButton_reloadprogmenu = new JButton(arrow);
			jButton_reloadprogmenu.addMouseListener(ma_reloadProgramExtention);
		}
		return jButton_reloadprogmenu;
	}
	
	// 「一括予約」
	private JButton getJButton_batchreservation(String s) {
		if (jButton_batchreservation == null) {
			ImageIcon icon = new ImageIcon(ICONFILE_BATCHCMD);
			jButton_batchreservation = new JButton(icon);
			jButton_batchreservation.setToolTipText(TIPS_BATCHRESERVATION);
			
			jButton_batchreservation.addActionListener(al_batchreservation);
		}
		return jButton_batchreservation;
	}
	
	//
	private JComboBox getJComboBox_select_recorder() {
		if (jComboBox_select_recorder == null) {
			jComboBox_select_recorder = new JWideComboBox();
			jComboBox_select_recorder.addPopupWidth(200);
			jComboBox_select_recorder.setToolTipText(TIPS_RECORDERSEL);
			
			Dimension d = jComboBox_select_recorder.getPreferredSize();
			d.width = 16*10;
			jComboBox_select_recorder.setPreferredSize(d);
			
			d = jComboBox_select_recorder.getMaximumSize();
			d.width = 16*10;
			jComboBox_select_recorder.setMaximumSize(d);
			jComboBox_select_recorder.setMinimumSize(d);
			//jComboBox_select_recorder.setEnabled(false);
			
			// 初期値（ItemListenerは↓の中で追加される）
			updateRecorderComboBox();
		}
		return(jComboBox_select_recorder);
	}

	// 「予約一覧の再取得」
	private JButton getJButton_reloadrsved(String s) {
		if (jButton_reloadrsved == null) {
			ImageIcon icon = new ImageIcon(ICONFILE_RELOADRSV);
			jButton_reloadrsved = new JButton(icon);
			jButton_reloadrsved.setToolTipText(TIPS_RELOADRSVED);
			
			jButton_reloadrsved.addActionListener(al_reloadReserved);
		}
		return jButton_reloadrsved;
	}

	// 「番組情報を再取得」の拡張メニュー
	private JButton getJButton_reloadrsvedmenu(String s) {
		if (jButton_reloadrsvedmenu == null) {
			// メニューの作成
			jPopupMenu_reloadrsvedmenu = new JPopupMenu();

			// アイテムの登録
			{
				for ( LoadRsvedFor lrf : LoadRsvedFor.values() ) {
					JMenuItem menuItem = new JMenuItem(lrf.getName());
					jPopupMenu_reloadrsvedmenu.add(menuItem);
					
					menuItem.addActionListener(al_reloadReservedIndividual);
				}
			}
			
			ImageIcon arrow = new ImageIcon(ICONFILE_PULLDOWNMENU);
			jButton_reloadrsvedmenu = new JButton(arrow);
			jButton_reloadrsvedmenu.addMouseListener(ma_reloadReservedExtention);
		}
		return jButton_reloadrsvedmenu;
	}

	// 「入」
	private JButton getJButton_wakeup(String s) {
		if (jButton_wakeup == null) {
			ImageIcon icon = new ImageIcon(ICONFILE_WAKEUP);
			jButton_wakeup = new JButton(icon);
			jButton_wakeup.setToolTipText(TIPS_WAKEUP);

			jButton_wakeup.addActionListener(al_wakeup);
		}
		return jButton_wakeup;
	}
	// 「切」
	private JButton getJButton_shutdown(String s) {
		if (jButton_shutdown == null) {
			ImageIcon icon = new ImageIcon(ICONFILE_SHUTDOWN);
			jButton_shutdown = new JButton(icon);
			jButton_shutdown.setToolTipText(TIPS_DOWN);
			
			jButton_shutdown.addActionListener(al_down);
		}
		return jButton_shutdown;
	}
	
	// 「設定タブを表示」
	private JToggleButton getJToggleButton_showsetting(String s) {
		if (jToggleButton_showsetting == null) {
			final ImageIcon icon = new ImageIcon(ICONFILE_SHOWSETTING);
			jToggleButton_showsetting = new JToggleButton(icon);
			jToggleButton_showsetting.setToolTipText(TIPS_SHOWSETTING);
			
			jToggleButton_showsetting.setSelected(bounds.getShowSettingTabs());
			
			jToggleButton_showsetting.addActionListener(al_showsetting);
		}
		return jToggleButton_showsetting;
	}
	
	// 「ステータス領域」
	private JToggleButton getJToggleButton_showstatus(String s) {
		if (jToggleButton_showstatus == null) {
			
			final ImageIcon IconHidden = new ImageIcon(ICONFILE_STATUSHIDDEN);
			final ImageIcon IconShown = new ImageIcon(ICONFILE_STATUSSHOWN);
			
			jToggleButton_showstatus = new JToggleButton(IconHidden) {
				
				private static final long serialVersionUID = 1L;
				
				@Override
				public void setSelected(boolean b) {
					super.setSelected(b);
					if ( b ) {
						this.setToolTipText(TIPS_STATUSSHOWN);
					}
					else {
						this.setToolTipText(TIPS_STATUSHIDDEN);
					}
				}
			};
			jToggleButton_showstatus.setSelectedIcon(IconShown);
			jToggleButton_showstatus.setSelected(bounds.getShowStatus());
			jToggleButton_showstatus.addActionListener(al_toggleShowStatus);
		}
		return jToggleButton_showstatus;
	}
	
	// 「全画面」
	private JToggleButton getJToggleButton_fullScreen(String s) {
		if (jToggleButton_fullScreen == null) {
			
			final ImageIcon IconToWin = new ImageIcon(ICONFILE_TOWIN);
			final ImageIcon IconToFull = new ImageIcon(ICONFILE_TOFULL);
			
			jToggleButton_fullScreen = new JToggleButton(IconToFull) {
				
				private static final long serialVersionUID = 1L;
				
				@Override
				public void setSelected(boolean b) {
					super.setSelected(b);
					if (b) {
						this.setToolTipText(TIPS_TOWIN);
					}
					else {
						this.setToolTipText(TIPS_TOFULL);
					}
				}
			};
			jToggleButton_fullScreen.setSelectedIcon(IconToWin);
			jToggleButton_fullScreen.setSelected(false);
			jToggleButton_fullScreen.addActionListener(al_toggleFullscreen);
		}
		return jToggleButton_fullScreen;
	}
	
	// 「設定タブを表示」
	private JToggleButton getJToggleButton_showmatchborder(String s) {
		if (jToggleButton_showmatchborder == null) {
			final ImageIcon icon = new ImageIcon(ICONFILE_SHOWMATCHBORDER);
			jToggleButton_showmatchborder = new JToggleButton(icon);
			jToggleButton_showmatchborder.setToolTipText(TIPS_SHOWBORDER);
			jToggleButton_showmatchborder.setSelected(bounds.getShowMatchedBorder());
			
			jToggleButton_showmatchborder.addActionListener(al_showborder);
		}
		return jToggleButton_showmatchborder;
	}
	
	// 「現在日時」
	private JButton getJButton_moveToNow(String s) {
		if (jButton_moveToNow == null) {
			ImageIcon icon = new ImageIcon(ICONFILE_JUMPTONOW);
			jButton_moveToNow = new JButton(icon);
			jButton_moveToNow.setToolTipText(TIPS_JUMPTO);

			jButton_moveToNow.addActionListener(al_jumpto);
		}
		return jButton_moveToNow;
	}
	
	// 「現在日時」
	private JButton getJButton_paperColors(String s) {
		if (jButton_paperColors == null) {
			ImageIcon icon = new ImageIcon(ICONFILE_PALLET);
			jButton_paperColors = new JButton(icon);
			jButton_paperColors.setToolTipText(TIPS_PAPERCOLOR);

			jButton_paperColors.addActionListener(al_showpapercolordialog);
		}
		return jButton_paperColors;
	}
	
	private JSlider getJSlider_paperZoom(String s) {
		if ( jSlider_paperZoom == null ) {
			jSlider_paperZoom = new JSlider(100,300,100);
			jSlider_paperZoom.setToolTipText(TIPS_PAPERZOOM);

			Dimension d = jSlider_paperZoom.getPreferredSize();
			d.width = 32;
			jSlider_paperZoom.setPreferredSize(d);
			jSlider_paperZoom.setMaximumSize(d);
			jSlider_paperZoom.setMinimumSize(d);
			
			jSlider_paperZoom.addMouseListener(ml_paperzoom);
		}
		return jSlider_paperZoom;
	}
	/*
	// 「タイマー」
	private String nextEventDateTime = "29991231 2359";
	private void setNextEventDateTime() {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		c.add(Calendar.HOUR_OF_DAY, env.getCacheTimeLimit());
		nextEventDateTime = String.format("%04d%02d%02d %02d%02d", c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH),c.get(Calendar.HOUR_OF_DAY),00);
		StdAppendMessage("Next Event Time: "+nextEventDateTime);
	}
	private ActionListener alTimer = new ActionListener() {
		public void actionPerformed(ActionEvent e){
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(new Date());
			String curDateTime = String.format("%04d%02d%02d %02d%02d", c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH),c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE));
			if (nextEventDateTime.compareTo(curDateTime) <= 0) {
				funcReloadProgs(LoadFor.ALL);
				setNextEventDateTime();
			}
		}
	};
	private Timer tbTimer = new Timer(60*1000, alTimer);
	private void tbTimerOn() {
		String tip = "タイマーOFF";
		jToggleButton_timer.setToolTipText(tip);
		setNextEventDateTime();
		tbTimer.start();
		bounds.setEnableTimer(true);
	}
	private void tbTimerOff() {
		String tip = "タイマーON";
		jToggleButton_timer.setToolTipText(tip);
		tbTimer.stop();
		bounds.setEnableTimer(false);
	}
	*/
	private JToggleButton getJToggleButton_timer(String s) {
		if (jToggleButton_timer == null) {
			ImageIcon icon = new ImageIcon(ICONFILE_TIMER);
			String tip = "タイマーON";
			jToggleButton_timer = new JToggleButton(icon);
			jToggleButton_timer.setToolTipText(tip);
			
			jToggleButton_timer.setEnabled(false);
			jToggleButton_timer.setToolTipText("future use");
			
			/*
			if (bounds.getEnableTimer()) {
				jToggleButton_timer.setSelected(true);
				tbTimerOn();
			}
			
			jToggleButton_timer.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					ButtonModel bm = jToggleButton_timer.getModel();
					if (bm.isPressed() && bm.isSelected()) {
						tbTimerOn();
					}
					else if (bm.isPressed() && ! bm.isSelected()) {
						tbTimerOff();
					}
				}
			});
			*/
		}
		return jToggleButton_timer;
	}
	
	// 「スナップショット」
	private JButton getJButton_snapshot(String s) {
		if (jButton_snapshot == null) {
			ImageIcon icon = new ImageIcon(ICONFILE_SCREENSHOT);
			jButton_snapshot = new JButton(icon);
			jButton_snapshot.setToolTipText(TIPS_SNAPSHOT);

			jButton_snapshot.addActionListener(al_snapshot);
		}
		return jButton_snapshot;
	}
	
	// 「ログビューア」
	private JButton getJButton_logviewer(String s) {
		if (jButton_logviewer == null) {
			ImageIcon icon = new ImageIcon(ICONFILE_SHOWLOG);
			jButton_logviewer = new JButton(icon);
			jButton_logviewer.setToolTipText(TIPS_LOGVIEW);

			jButton_logviewer.addActionListener(al_logview);
		}
		return jButton_logviewer;
	}
	
	// 「オンラインアップデート」
	private JButton getJButton_update(String s) {
		if (jButton_update == null) {
			ImageIcon icon = new ImageIcon(ICONFILE_UPDATE);
			jButton_update = new JButton(icon);
			jButton_update.setToolTipText(TIPS_UPDATE);

			jButton_update.addActionListener(al_update);
		}
		return jButton_update;
	}
	
	// 「ヘルプ」
	private JButton getJButton_help(String s) {
		if (jButton_help == null) {
			ImageIcon icon = new ImageIcon(ICONFILE_HELP);
			jButton_help = new JButton(icon);
			jButton_help.setToolTipText(TIPS_OPENHELP);

			jButton_help.addActionListener(al_openhelp);
		}
		return jButton_help;
	}
	
}
