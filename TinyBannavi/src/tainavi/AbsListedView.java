package tainavi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterEvent.Type;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import tainavi.TVProgram.ProgFlags;
import tainavi.TVProgram.ProgGenre;
import tainavi.TVProgram.ProgSubgenre;
import tainavi.TVProgram.ProgSubtype;
import tainavi.TVProgram.ProgType;
import tainavi.TVProgramIterator.IterationType;
import tainavi.VWMainWindow.MWinTab;


/**
 * リスト形式タブのクラス
 * @since 3.15.4β　{@link Viewer}から分離
 */
public abstract class AbsListedView extends JPanel implements VWTimerRiseListener {

	private static final long serialVersionUID = 1L;
	
	public static String getViewName() { return "リスト形式"; } 
	
	public void setDebug(boolean b) { debug = b; }
	private static boolean debug = false;
	
	
	/*******************************************************************************
	 * 抽象メソッド
	 ******************************************************************************/
	
	protected abstract Env getEnv();
	protected abstract Bounds getBoundsEnv();
	protected abstract ChannelSort getChannelSort();
	
	protected abstract MarkedProgramList getMarkedProgramList();
	protected abstract TraceProgram getTraceProgram();
	protected abstract SearchProgram getSearchProgram();
	protected abstract SearchGroupList getSearchGroupList();
	protected abstract ExtProgram getExtProgram();
	
	protected abstract TVProgramList getTVProgramList();
	protected abstract HDDRecorderList getRecorderList();
	
	protected abstract StatusWindow getStWin(); 
	protected abstract StatusTextArea getMWin();
	protected abstract AbsReserveDialog getReserveDialog();
	
	protected abstract Component getParentComponent();
	
	protected abstract void ringBeep();
	
	// クラス内のイベントから呼び出されるもの
	
	/**
	 * タブが開いた
	 */
	protected abstract void onShown();
	/**
	 * タブが閉じた
	 */
	protected abstract void onHidden();
	
	/**
	 * マウス右クリックメニューを表示する
	 */
	protected abstract void showPopupForTraceProgram(
			final JComponent comp,
			final ProgDetailList tvd, final String keyword, final int threshold,
			final int x, final int y, final int h
			);

	/**
	 * 予約マーク・予約枠を更新してほしい
	 */
	protected abstract void updateReserveDisplay(String chname);

	/**
	 * 番組枠を更新してほしい
	 */
	protected abstract void updateBangumiColumns();

	
	/**
	 * 開けてくれ！
	 */
	protected abstract void clearPaper();
	
	/**
	 * ぷれびゅりたいお
	 */
	protected abstract void previewKeywordSearch(SearchKey search);
	//protected abstract void previewExtensionSearch(SearchKey search);
	/**
	 * 新聞形式にジャンプしてほしい
	 */
	protected abstract void jumpToPaper(String Center, String StartDateTime);
	
	/**
	 * ピックアップに追加してほしい
	 */
	protected abstract boolean addToPickup(final ProgDetailList tvd);
	
	protected abstract boolean isTabSelected(MWinTab tab);
	protected abstract void setSelectedTab(MWinTab tab);

	/**
	 * @see Viewer.VWToolBar#getSelectedRecorder()
	 */
	protected abstract String getSelectedRecorderOnToolbar();
	protected abstract boolean isFullScreen();
	//protected abstract void setPagerItems(int total_page, int idx);
	protected abstract void setPagerEnabled(boolean b);
	protected abstract int getPagerCount();
	protected abstract int getSelectedPagerIndex();

	/**
	 * ツリーペーンの幅の変更を保存してほしい
	 */
	protected abstract void setDividerEnvs(int loc);

	
	/*******************************************************************************
	 * 呼び出し元から引き継いだもの
	 ******************************************************************************/
	
	// オブジェクト
	private final Env env = getEnv();
	private final Bounds bounds = getBoundsEnv();
	private final ChannelSort chsort = getChannelSort();
	
	private final MarkedProgramList mpList = getMarkedProgramList();;
	private final TraceProgram trKeys = getTraceProgram();
	private final SearchProgram srKeys = getSearchProgram();
	private final SearchGroupList srGrps = getSearchGroupList();
	private final ExtProgram extKeys = getExtProgram();
	
	private final TVProgramList tvprograms = getTVProgramList();
	private final HDDRecorderList recorders = getRecorderList();

	private final StatusWindow StWin = getStWin();			// これは起動時に作成されたまま変更されないオブジェクト
	private final StatusTextArea MWin = getMWin();			// これは起動時に作成されたまま変更されないオブジェクト
	private final AbsReserveDialog rD = getReserveDialog();	// これは起動時に作成されたまま変更されないオブジェクト
	
	private final Component parent = getParentComponent();	// これは起動時に作成されたまま変更されないオブジェクト

	// メソッド
	private void StdAppendMessage(String message) { System.out.println(message); }
	private void StdAppendError(String message) { System.err.println(message); }
	//private void StWinSetVisible(boolean b) { StWin.setVisible(b); }
	//private void StWinSetLocationCenter(Component frame) { CommonSwingUtils.setLocationCenter(frame, (VWStatusWindow)StWin); }
	
	
	/*******************************************************************************
	 * 定数
	 ******************************************************************************/
	
	private static final String MSGID = "["+getViewName()+"] ";
	private static final String ERRID = "[ERROR]"+MSGID;
	private static final String DBGID = "[DEBUG]"+MSGID;
	
	
	/**
	 * メインテーブルのカラムの定義
	 */
	private static final HashMap<String,Integer> lcmap = new HashMap<String, Integer>();
	public static HashMap<String,Integer> getColumnIniWidthMap() {
		if (lcmap.size() == 0 ) {
			for ( ListedColumn lc : ListedColumn.values() ) {
				lcmap.put(lc.toString(),lc.getIniWidth());	// toString()!
			}
		}
		return lcmap;
	}
	
	/**
	 * テーブルのカラムの設定（名前と幅の初期値）。できれば@{link {@link ListedItem}と一体化させたかったのだが無理っぽい
	 * @see ListedItem
	 */
	public static enum ListedColumn {
		RSVMARK		("予約",			35),
		DUPMARK		("重複",			35),
		CHNAME		("チャンネル名",	100),
		OPTIONS		("オプション",		100),
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
		;

		private String name;
		private int iniWidth;

		private ListedColumn(String name, int iniWidth) {
			this.name = name;
			this.iniWidth = iniWidth;
		}

		public String getName() {
			return name;
		}

		public int getIniWidth() {
			return iniWidth;
		}
		
		public int getColumn() {
			return ordinal();
		}
		
		public boolean equals(String s) {
			return name.equals(s);
		}
	};

	/**
	 * 検索範囲（番組追跡か、キーワード検索か、予約待機か）
	 */
	private static enum SearchBy { TRACE, KEYWORD, BOTH } ;
	
	public static final String RSVMARK_NOEXEC		= "×";	// 予約無効
	public static final String RSVMARK_NORMAL		= "●";	// ぴったり
	public static final String RSVMARK_OVERRUN		= "◎";	// のりしろより大きく予約時間がとられている
	public static final String RSVMARK_UNDERRUN		= "○";	// 時間延長が考慮されていない
	public static final String RSVMARK_DELAYED		= "◇";	// 開始時刻が一致しない
	public static final String RSVMARK_CLIPPED		= "▲";	// １分短縮済み
	public static final String RSVMARK_CLIPPED_E	= "△";	// １分短縮済み（延長警告あり）
	public static final String RSVMARK_SHORTAGE		= "▼";	// ２分以上短かい
	public static final String RSVMARK_SHORTAGE_E	= "▽";	// ２分以上短かい（延長警告あり）
	
	public static final String RSVMARK_PICKUP		= "★";	// ピックアップ
	public static final String RSVMARK_URABAN		= "■";	// 裏番組
	private static final String PICKUP_COLOR		= CommonUtils.color2str(Color.BLACK);
	private static final String URABAN_COLOR		= CommonUtils.color2str(Color.BLACK);

	private static final String DUPMARK_NORMAL		= "■";
	private static final String DUPMARK_REP			= "□";
	private static final String DUPMARK_COLOR		= "#FFB6C1";
	
	private static final String TreeExpRegFile_Listed = "env"+File.separator+"tree_expand_listed.xml";
	
	private final String SearchItemLabel_Passed = JTreeLabel.Nodes.PASSED.getLabel();	// 過去ログの名前
	
	// 定数ではないが
	
	private int vrowInFocus = -1;	// マウスクリックした時のフォーカス行
	
	
	/*******************************************************************************
	 * 部品
	 ******************************************************************************/
	
	// コンポーネント
	
	private JDetailPanel jTextPane_detail = null;
	private JSplitPane jSplitPane_view = null;
	private JPanel jPanel_tree = null;
	private JScrollPane jScrollPane_tree_top = null;
	private JTreeLabel jLabel_tree = null;
	private JScrollPane jScrollPane_tree = null;
	private JTree jTree_tree = null;
	private JScrollPane jScrollPane_listed = null;
	private JTableRowHeader jTable_rowheader = null;
	private ListedTable jTable_listed = null;
	private VWColorCharCellRenderer2 titleCellRenderer = null;
	
	private VWListedTreeNode listRootNode = null;		// リスト形式のツリー
	
	VWListedTreeNode searchedNode	= null;
	VWListedTreeNode startNode		= null;
	VWListedTreeNode endNode		= null;
	VWListedTreeNode nowNode		= null;
	VWListedTreeNode syobocalNode	= null;
	VWListedTreeNode standbyNode	= null;
	VWListedTreeNode traceNode		= null;
	VWListedTreeNode keywordNode	= null;
	VWListedTreeNode keywordGrpNode	= null;
	VWListedTreeNode genreNode		= null;
	VWListedTreeNode centerListNode = null;
	VWListedTreeNode extensionNode	= null;
	
	VWListedTreeNode defaultNode	= null;
	
	private ListedTableModel tableModel_listed = null;
	
	private DefaultTableModel rowheaderModel_listed = null;
	
	// コンポーネント以外
	
	// テーブルの実態
	private final RowItemList<ListedItem> rowData = new RowItemList<ListedItem>();
	
	// ツリーの展開状態の保存場所
	TreeExpansionReg ter = null;
	
	// 現在放送中のタイマー
	private boolean timer_now_enabled = false;
	
	
	/*******************************************************************************
	 * コンストラクタ
	 ******************************************************************************/
	
	public AbsListedView() {
		
		super();
		
		// コンポーネントを追加
		this.setLayout(new BorderLayout());
		this.add(getJTextPane_detail(), BorderLayout.PAGE_START);
		this.add(getJSplitPane_view(), BorderLayout.CENTER);
		
		// バグ対応
		if ( bounds.getListedColumnSize() == null ) {
			StWin.appendError(ERRID+"なんらかの不具合によりテーブルのカラム幅設定が取得できませんでした。設定はリセットされました。申し訳ありません。");
			bounds.setListedColumnSize(lcmap);
		}
		else {
			for ( Entry<String, Integer> en : lcmap.entrySet() ) {
				try {
					bounds.getListedColumnSize().get(en.getKey());
				}
				catch (NullPointerException e) {
					System.err.println(ERRID+en.getKey()+", "+e.toString());
					bounds.getListedColumnSize().put(en.getKey(),en.getValue());
				}
			}
		}
		
		// タブが開いたり閉じたりしたときの処理
		this.addComponentListener(cl_tabshownhidden);
	}
	
	
	/*******************************************************************************
	 * アクション
	 ******************************************************************************/
	
	// このクラスの肝、リスト表示について。
	
	/*
	 *  検索総当り版
	 */
	
	/**
	 * 検索条件
	 */
	private class RedrawCond {
		ProgType progtype = null;
		SearchKey searchkeyword = null;
		ProgFlags flag = null;
		ProgGenre genre = null;
		ProgSubgenre subgenre = null;
		String center = null;
		String targetdate = null;
	}
	
	/**
	 *  プレビュー表示とか（親から呼ばれるよ！）
	 * @see #redrawListByKeywordDyn(SearchKey, String)
	 * @see #redrawListByPassed(SearchKey, String)
	 * @see #redrawListByKeywordFilter(SearchKey, String)
	 */
	public void redrawListByPreview(SearchKey sKey) {
		stopTimer(true);
		jLabel_tree.setView(JTreeLabel.Nodes.SEARCHED, JTreeLabel.PREVIEW);
		redrawListByKeywordDyn(sKey);
	}
	
	/**
	 *  キーワード検索ボックスとか（親から呼ばれるよ！）
	 */
	public void redrawListByKeywordDyn(SearchKey sKey, String target) {
		stopTimer(true);
		jLabel_tree.setView(JTreeLabel.Nodes.SEARCHED, target);
		redrawListByKeywordDyn(sKey);
	}	
	
	
	// キーワード検索結果に基づきリストを作成（動的）
	// (1)延長警告管理、(2)ツールバーからの検索
	private void redrawListByKeywordDyn(SearchKey sKey) {
		rowData.clear();
		RedrawCond c = new RedrawCond();
		c.progtype = ProgType.PROG;
		c.searchkeyword = sKey;
		_redrawListBy(c);
		//_redrawListBy(ProgType.RADIO,sKey,null,null,null,null);
		setReservedMarks();
		tableModel_listed.fireTableDataChanged();
		rowheaderModel_listed.fireTableDataChanged();
		
		setOverlapMark();
	}
	private void redrawListByExtkeywordAll(ArrayList<SearchKey> sKeys) {
		rowData.clear();
		RedrawCond c = new RedrawCond();
		c.progtype = ProgType.PROG;
		for (SearchKey sKey : sKeys) { 
			c.searchkeyword = sKey;
			_redrawListBy(c);
		}
		setReservedMarks();
		tableModel_listed.fireTableDataChanged();
		rowheaderModel_listed.fireTableDataChanged();
		
		setOverlapMark();
	}
	// フラグに基づきリストを作成
	private void redrawListByFlag(ProgFlags flag) {
		rowData.clear();
		RedrawCond c = new RedrawCond();
		c.progtype = ProgType.PROG;
		c.flag = flag;
		_redrawListBy(c);
		setReservedMarks();
		tableModel_listed.fireTableDataChanged();
		rowheaderModel_listed.fireTableDataChanged();
		
		setOverlapMark();
	}
	// フラグ＋ジャンルでリストを作成
	private void redrawListByFlag(ProgFlags flag, ProgGenre genre) {
		rowData.clear();
		RedrawCond c = new RedrawCond();
		c.progtype = ProgType.PROG;
		c.genre = genre;
		c.flag = flag;
		_redrawListBy(c);
		setReservedMarks();
		tableModel_listed.fireTableDataChanged();
		rowheaderModel_listed.fireTableDataChanged();
		
		setOverlapMark();
	}
	// 現在時刻で絞り込み
	private void redrawListByNow(ProgGenre genre) {
		jTable_listed.getRowSorter().setSortKeys(null);	// ソーターをリセットする
		rowData.clear();
		RedrawCond c = new RedrawCond();
		c.progtype = ProgType.PROG;
		c.genre = genre;
		c.targetdate = "";
		_redrawListBy(c);	// target == ""は現在日時しぼりこみ
		setReservedMarks();
		tableModel_listed.fireTableDataChanged();
		rowheaderModel_listed.fireTableDataChanged();
		
		setOverlapMark();
	}
	// ジャンル検索結果に基づきリストを作成
	private void redrawListByGenre(ProgGenre genre, ProgSubgenre subgenre) {
		rowData.clear();
		RedrawCond c = new RedrawCond();
		c.progtype = ProgType.PROG;
		c.genre = genre;
		c.subgenre = subgenre;
		_redrawListBy(c);
		setReservedMarks();
		tableModel_listed.fireTableDataChanged();
		rowheaderModel_listed.fireTableDataChanged();
		
		setOverlapMark();
	}
	// 放送局ごとにリストを作成
	private void redrawListByCenterList(String center) {
		rowData.clear();
		RedrawCond c = new RedrawCond();
		c.progtype = ProgType.PROG;
		c.center = center;
		_redrawListBy(c);
		setReservedMarks();
		tableModel_listed.fireTableDataChanged();
		rowheaderModel_listed.fireTableDataChanged();
		
		//setOverlapMark();
	}
	
	/**
	 *  しょぼーん
	 */
	private void redrawSyobocalAll() {
		rowData.clear();
		RedrawCond c = new RedrawCond();
		c.progtype = ProgType.SYOBO;
		_redrawListBy(c);
		tableModel_listed.fireTableDataChanged();
		rowheaderModel_listed.fireTableDataChanged();
		setReservedMarks();
	}

	/**
	 * 検索総当り版の本体（全件に対して検索処理をかける）
	 */
	private void _redrawListBy(RedrawCond cond) {
		
		String curDateTime = CommonUtils.getDateTime(0);
		String critDateTime = CommonUtils.getCritDateTime();
		
		for ( TVProgram tvp : tvprograms ) {
			if (tvp.getType() != cond.progtype) {
				continue;
			}
			
			for ( ProgList tvpl : tvp.getCenters() ) {
				if ( ! tvpl.enabled ) {
					// 有効局のみだよ
					continue;
				}
				if (cond.center != null && ! cond.center.equals(tvpl.Center)) {
					// 放送局指定があれば
					continue;
				}
				
				// キーワード検索用
				String centerPop = TraceProgram.replacePop(tvpl.Center);
				
				for ( ProgDateList tvc : tvpl.pdate ) {

					for ( ProgDetailList tvd : tvc.pdetail ) {
						
						// 過去日のものは表示しない
						if (tvp.getType() != ProgType.PASSED) {
							if (tvd.endDateTime.compareTo(critDateTime) < 0) {
								continue;
							}
						}
						// 当日過去分は表示しない（オプション）
						if ( ! env.getDisplayPassedEntry()) {
							if (tvp.getType() != ProgType.PASSED) {
								if (tvd.endDateTime.compareTo(curDateTime) <= 0) {
									continue;
								}
							}
						}
						
						// 番組情報がありませんは表示しない
						if (tvd.start.equals("")) {
							continue;
						}
						
						// 放送休止は表示しない
						if (tvd.title.equals("放送休止") || tvd.title.equals("休止") || tvd.title.contains("放送を休止")) {
							continue;
						}
						
						//マッチング
						String label = "";
						String okini = "";
						String matched = null;
						long cur_remain = -1;
						long cur_wait = 0;
						boolean isFind = false;
						if (cond.center != null) {
							isFind = true;
						}
						else if (cond.targetdate != null) {
							// 現在放送中！
							if ( cond.targetdate.length() == 0 ) {
								String cridt = CommonUtils.getDateTime(-env.getCurrentAfter());
								String curdt = CommonUtils.getDateTime(0);
								String nextdt = CommonUtils.getDateTime(env.getCurrentBefore());
								if ( tvd.endDateTime.compareTo(cridt) <= 0 ) {
									continue;	// オワタ
								}
								if ( tvd.startDateTime.compareTo(cridt) <= 0 && cridt.compareTo(tvd.endDateTime) <= 0 ||
										tvd.startDateTime.compareTo(curdt) <= 0 && curdt.compareTo(tvd.endDateTime) <= 0) {
									cur_remain = CommonUtils.getCompareDateTime(tvd.endDateTime, curdt);
									isFind = true;
								}
								else if ( tvd.startDateTime.compareTo(cridt) > 0 && tvd.startDateTime.compareTo(nextdt) <= 0 ) {
									cur_wait = CommonUtils.getCompareDateTime(tvd.startDateTime, curdt);
									isFind = true;	// 今後一時間以内に開始予定のものも表示
								}
								else if ( tvd.startDateTime.compareTo(nextdt) < 0 ) {
									continue;	// これ以上みても無駄
								}
								
								if ( isFind && cond.genre != null && ! tvd.isEqualsGenre(cond.genre, null) ) {
									continue;
								}
							}
						}
						else {
							if (cond.searchkeyword != null) {
								isFind = SearchProgram.isMatchKeyword(cond.searchkeyword, ((cond.searchkeyword.getCaseSensitive()==false)?(centerPop):(tvpl.Center)), tvd);
								label = ((cond.progtype == ProgType.PASSED)?(SearchItemLabel_Passed):(cond.searchkeyword.getLabel()));
								okini = cond.searchkeyword.getOkiniiri();
								matched = SearchProgram.getMatchedString();
							}
							else if (cond.flag != null) {
								if (tvd.flag == cond.flag) {
									isFind = true;
									label = cond.flag.toString();
								}
								
								if ( isFind && cond.genre != null && ! tvd.isEqualsGenre(cond.genre, null) ) {
									continue;
								}
							}
							else if (cond.genre != null) {
								isFind = tvd.isEqualsGenre(cond.genre, cond.subgenre);
							}
							else if ( cond.progtype == ProgType.SYOBO && cond.searchkeyword == null ) {
								// 全しょぼかる
								isFind = true;
							}
						}
						
						if (isFind) {
							String[] tStr = new String[3];
							if (matched != null) {
								int a = tvd.title.indexOf(matched);
								tStr[0] = tvd.title.substring(0,a);
								tStr[1] = matched;
								tStr[2] = tvd.title.substring(a+matched.length());
							}
							else {
								tStr[0] = tvd.title;
								tStr[1] = "";
								tStr[2] = "";
							}
							
							String prefixMark = "";
							if ( cond.targetdate == "" ) {
								if ( cur_remain > 0 ) {
									prefixMark = String.format("\0終了まで%3d分",cur_remain/60000);
								}
								else if ( cur_wait > 0 ){
									prefixMark = String.format("\0開始まで%3d分",cur_wait/60000);
								}
							}
							else {
								prefixMark = tvd.extension_mark+tvd.prefix_mark;
							}
							
							ListedItem sa = new ListedItem();
							
							sa.tvd = tvd;
							
							sa.rsvmark		= "";
							sa.dupmark		= "";
							sa.prefix		= prefixMark;
							sa.title		= tvd.newlast_mark+"\0"+tStr[0]+"\0"+tStr[1]+"\0"+tStr[2]+tvd.postfix_mark;
							sa.searchlabel	= label;
							sa.okiniiri		= okini;
							sa.score		= "";
							sa.threshold	= "";
							
							sa.hide_rsvmarkcolor	= "";
							
							sa.fireChanged();
							
							addRow(sa);
						}
					}
				}
			}
		}
		
		if ( cond.targetdate == "" ) {
			// 現在放送中の終了済み番組をリストの先頭に移動する
			RowItemList<ListedItem> passed = new RowItemList<ListedItem>();
			RowItemList<ListedItem> cur = new RowItemList<ListedItem>();
			RowItemList<ListedItem> future = new RowItemList<ListedItem>();
			for ( ListedItem c : rowData ) {
				if ( c.prefix == "" ) {
					passed.add(c);
				}
				else if ( c.prefix.startsWith("\0終了まで") ){
					cur.add(c);
				}
				else {
					future.add(c);
				}
			}
			
			rowData.clear();
			for ( ListedItem c : passed ) {
				rowData.add(c);
			}
			
			int toprow = rowData.size();
			for ( ListedItem c : cur ) {
				int row = toprow;
				for ( ; row<rowData.size(); row++ ) {
					ListedItem d = rowData.get(row);
					if ( c.tvd.endDateTime.compareTo(d.tvd.endDateTime) < 0 ) {
						break;
					}
				}
				rowData.add(row,c);
			}
			
			for ( ListedItem c : future ) {
				rowData.add(c);
			}
		}
	}
	
	/**
	 * 検索総当り版の本体（全件に対して検索処理をかける）
	 * <P><B>★将来的には、動的検索結果の表示はすべてこちらに移行する 
	 */
	public boolean redrawListBySearched(ProgType typ, int index) {
		
		stopTimer(true);
		
		// 検索結果が入っているところ
		SearchResult searched = tvprograms.getSearched();
		
		// 検索結果の履歴数より大きい番号を指定された場合はエラー
		if ( searched.getResultBufferSize() < index ) {
			return false;
		}
		
		JTreeLabel.Nodes desc = ((typ == ProgType.PASSED) ? (JTreeLabel.Nodes.SEARCHHIST):(JTreeLabel.Nodes.SEARCHED));
		String label = searched.getLabel(index);
		
		jLabel_tree.setView(desc, label);

		rowData.clear();
		
		for ( ProgDetailList tvd : searched.getResult(index) ) {
			
			String[] tStr = new String[3];
			
			if (tvd.dynMatched != null) {
				int a = tvd.title.indexOf(tvd.dynMatched);
				tStr[0] = tvd.title.substring(0,a);
				tStr[1] = tvd.dynMatched;
				tStr[2] = tvd.title.substring(a+tvd.dynMatched.length());
			}
			else {
				tStr[0] = tvd.title;
				tStr[1] = "";
				tStr[2] = "";
			}
			
			String prefixMark = tvd.extension_mark+tvd.prefix_mark;
			
			ListedItem sa = new ListedItem();
			
			sa.tvd = tvd;
			
			sa.rsvmark		= "";
			sa.dupmark		= "";
			sa.prefix		= prefixMark;
			sa.title		= tvd.newlast_mark+"\0"+tStr[0]+"\0"+tStr[1]+"\0"+tStr[2]+tvd.postfix_mark;
			sa.searchlabel	= tvd.dynKey.getLabel();
			sa.okiniiri		= tvd.dynKey.getOkiniiri();
			sa.score		= "";
			sa.threshold	= "";
			
			sa.hide_rsvmarkcolor	= "";
			
			sa.fireChanged();
			
			addRow(sa);
		}
		
		tableModel_listed.fireTableDataChanged();
		rowheaderModel_listed.fireTableDataChanged();
		
		return true;
	}
	
	/*
	 *  検索高速化版
	 */
	
	// しょぼかるリストを作成
	private void redrawSyobocalListByTrace() {
		_redrawListByTraceAndKeyword(ProgType.SYOBO,SearchBy.TRACE,null,null,null);
	}
	private void redrawSyobocalListByKeyword() {
		_redrawListByTraceAndKeyword(ProgType.SYOBO,SearchBy.KEYWORD,null,null,null);
	}

	// しょぼかるのみに存在
	private void redrawSyobocalListByOnly() {
		_redrawListByTraceAndKeyword(ProgType.SYOBO,SearchBy.BOTH,null,null,null,true);
	}
	
	private void redrawSyobocalListByTraceAndKeyword() {
		_redrawListByTraceAndKeyword(ProgType.SYOBO,SearchBy.BOTH,null,null,null);
	}

	// 番組追跡に基づきリストを作成
	private void redrawListByTrace(TraceKey tKey) {
		_redrawListByTraceAndKeyword(ProgType.PROG,SearchBy.TRACE,tKey,null,null);
	}
	
	// キーワード検索結果に基づきリストを作成（静的）
	private void redrawListByKeyword(SearchKey sKey) {
		_redrawListByTraceAndKeyword(ProgType.PROG,SearchBy.KEYWORD,null,sKey,null);
	}
	// キーワードグループに基づきリストを作成（静的）
	private void redrawListByKeywordGrp(SearchGroup gr) {
		_redrawListByTraceAndKeyword(ProgType.PROG,SearchBy.KEYWORD,null,null,null,false,gr,false,false);
	}
	// ピックアップに基づきリストを作成（静的）
	private void redrawListByPickup() {
		_redrawListByTraceAndKeyword(ProgType.PICKED,SearchBy.BOTH,null,null,null);
	}
	
	// 番組追跡＆キーワード検索結果に基づきリストを作成
	private void redrawListByTraceAndKeywordOkini(String oKey) {
		_redrawListByTraceAndKeyword(ProgType.PROG,SearchBy.BOTH,null,null,oKey);
		selectBatchTarget();
	}
	private void redrawListByTraceAndKeyword() {
		_redrawListByTraceAndKeyword(ProgType.PROG,SearchBy.BOTH,null,null,null);
		selectBatchTarget();
	}

	private void redrawListByTraceAndKeywordNewArrival() {
		_redrawListByTraceAndKeyword(ProgType.PROG,SearchBy.BOTH,null,null,null,false,null,true,false);
		selectBatchTarget();
	}
	private void redrawListByTraceAndKeywordModified() {
		_redrawListByTraceAndKeyword(ProgType.PROG,SearchBy.BOTH,null,null,null,false,null,false,true);
		selectBatchTarget();
	}

	private void _redrawListByTraceAndKeyword(ProgType typ, SearchBy opt, TraceKey tKey, SearchKey sKey, String oKey) {
		_redrawListByTraceAndKeyword(typ, opt, tKey, sKey, oKey, false, null, false, false); 
	}
	private void _redrawListByTraceAndKeyword(ProgType typ, SearchBy opt, TraceKey tKey, SearchKey sKey, String oKey, boolean only) {
		_redrawListByTraceAndKeyword(typ, opt, tKey, sKey, oKey, only, null, false, false); 
	}
	
	/**
	 * 検索高速化版の本体（作成済み検索結果から必要なものを選ぶだけ、検索処理は行わない）
	 */
	private void _redrawListByTraceAndKeyword(ProgType typ, SearchBy opt, TraceKey tKey, SearchKey sKey, String oKey, boolean only, SearchGroup gr, boolean doChkNewArr, boolean doChkModify) {
		
		rowData.clear();
		
		String curDateTime = CommonUtils.getDateTime(0);
		String critDateTime = CommonUtils.getCritDateTime();
		
		for (int n=0; n<mpList.size(); n++) {
			
			// Web番組表・しょぼかる分岐点
			if (mpList.getProg(n).type != typ) {
				continue;
			}
			
			// 番組追跡・キーワード検索
			if (opt == SearchBy.TRACE && mpList.getTKey(n) == null) {
				continue;
			}
			if (opt == SearchBy.KEYWORD && mpList.getSKey(n) == null) {
				continue;
			}
			
			ProgDetailList tvd = mpList.getProg(n);

			// 過去日のものは表示しない
			if (tvd.endDateTime.compareTo(critDateTime) < 0) {
				continue;
			}
			// 当日過去分は表示しない
			if ( ! env.getDisplayPassedEntry()) {
				if (tvd.endDateTime.compareTo(curDateTime) <= 0) {
					continue;
				}
			}
			
			// 検索キーワード関連の値算出
			ArrayList<TraceKey>  td = mpList.getTKey(n);
			ArrayList<SearchKey> sd = mpList.getSKey(n);
			
			String label = "";
			String okini = "";
			String matched = null;
			String fazScore = "";
			String threshold = "";
			
			if ((opt == SearchBy.BOTH  || opt == SearchBy.TRACE) && td.size() > 0) {
				int i=0;
				if (tKey != null) {
					for (i=0; i<td.size(); i++) {
						if (td.get(i) == tKey) {
							break;
						}
					}
					if (i >= td.size()) {
						continue;	// キー指定で見つからなかったもの
					}
				}
				
				label = td.get(i).getLabel();
				okini = td.get(i).getOkiniiri();
				threshold = String.valueOf(td.get(i).getFazzyThreshold());
				fazScore = String.valueOf(mpList.getTScore(n).get(i));
			}
			else if ((opt == SearchBy.BOTH  || opt == SearchBy.KEYWORD) && sd.size() > 0) {
				int i=0;
				if (sKey != null) {
					for (i=0; i<sd.size(); i++) {
						if (sd.get(i) == sKey) {
							break;
						}
					}
					if (i >= sd.size()) {
						continue;	// キー指定で見つからなかったもの
					}
				}
				else if (gr != null) {
					for (i=0; i<sd.size(); i++) {
						boolean f = false;
						for ( String gmember : gr ) {
							if (sd.get(i).getLabel().equals(gmember)) {
								f = true;
								break;
							}
						}
						if (f) {
							break;
						}
					}
					if (i >= sd.size()) {
						continue;	// キー指定で見つからなかったもの
					}
				}
				
				if ( opt == SearchBy.BOTH && ! sd.get(i).getShowInStandby() ) {
					continue;	// 予約待機への表示はYA・DA・YO
				}
				
				label = sd.get(i).getLabel();
				okini = sd.get(i).getOkiniiri();
				matched = mpList.getSStr(n).get(i);
			}
			else {
				continue;
			}
			
			if ( doChkNewArr ) {
				if ( ! tvd.newarrival ) {
					// 新着フィルタリング
					continue;
				}
			}
			else if ( doChkModify ) {
				if ( ! tvd.modified ) {
					// 更新フィルタリング
					continue;
				}
			}
			else if (oKey != null && oKey.compareTo(okini) > 0) {
				// お気に入り度フィルタリング
				continue;
			}
			
			// しょぼかるのみに存在
			if ( typ == ProgType.SYOBO && (env.getSyoboFilterByCenters() || only) ) {
				if ( only && tvd.nosyobo ) {
					// nosyoboって名前と内容が一致していないよね…
					continue;
				}
				// 有効局のみに限定する
				boolean encr = false;
				for ( int x=0; x<tvprograms.size() && encr == false; x++ ) {
					TVProgram tvp = tvprograms.get(x);
					if ( tvp.getType() != ProgType.PROG ) {
						continue;
					}
					for ( Center cr : tvp.getSortedCRlist() ) {
						if ( mpList.getProg(n).center.equals(cr.getCenter()) ) {
							encr = true;
							break;
						}
					}
				}
				if ( ! encr ) {
					continue;
				}
			}
			
			// リストに追加
			String[] tStr = new String[3];
			if (matched != null) {
				int a = tvd.title.indexOf(matched);
				tStr[0] = tvd.title.substring(0,a);
				tStr[1] = matched;
				tStr[2] = tvd.title.substring(a+matched.length());
			}
			else {
				tStr[0] = tvd.title;
				tStr[1] = "";
				tStr[2] = "";
			}
			
			ListedItem sa = new ListedItem();

			sa.tvd = tvd;
			
			sa.rsvmark		= "";
			sa.dupmark		= "";
			sa.prefix		= tvd.extension_mark+tvd.prefix_mark;
			sa.title		= tvd.newlast_mark+"\0"+tStr[0]+"\0"+tStr[1]+"\0"+tStr[2]+tvd.postfix_mark;
			sa.searchlabel	= label;
			sa.okiniiri		= okini;
			sa.score		= fazScore;
			sa.threshold	= threshold;
			
			sa.hide_rsvmarkcolor	= "";
			
			sa.fireChanged();
			
			addRow(sa);
		}
		
		// ピックアップ（予約待機（親）のみで表示）
		if ((typ == ProgType.PROG || typ == ProgType.PICKED) && opt == SearchBy.BOTH && oKey == null) {
			if ( ! doChkNewArr && ! doChkModify ) {
				addPickedPrograms(curDateTime);
			}
		}
		
		// 予約マーク
		setReservedMarks();
		// テーブルに反映
		tableModel_listed.fireTableDataChanged();
		rowheaderModel_listed.fireTableDataChanged();
		
		// 時間重複マーク
		setOverlapMark();
	}
	
	private void addPickedPrograms(String curDateTime) {
		TVProgram tvp = tvprograms.getPickup();
		if ( tvp != null ) {
			for ( ProgList tPl : tvp.getCenters() ) {
				for ( ProgDateList tPcl : tPl.pdate ) {
					for ( ProgDetailList tvd : tPcl.pdetail ) {
						
						// すでに過去になっているものは表示しない
						if ( ! env.getDisplayPassedEntry()) {
							if (tvd.endDateTime.compareTo(curDateTime) <= 0) {
								continue;
							}
						}
						
						// リストに追加
						ListedItem sa = new ListedItem();
						
						sa.tvd = tvd;
						
						sa.rsvmark		= "";
						sa.dupmark		= "";
						sa.prefix		= tvd.extension_mark+tvd.prefix_mark;
						sa.title		= tvd.newlast_mark+"\0"+tvd.title+tvd.postfix_mark;
						sa.searchlabel	= "ピックアップ";
						sa.okiniiri		= "";
						sa.score		= "";
						sa.threshold	= "";
						
						sa.hide_rsvmarkcolor	= "";
						
						sa.fireChanged();
						
						addRow(sa);
					}
				}
			}
		}
	}
	
	/*
	 * 絞り込み検索
	 */
	
	/**
	 * 絞り込み検索の本体（現在リストアップされているものから絞り込みを行う）（親から呼ばれるよ！）
	 */
	public void redrawListByKeywordFilter(SearchKey keyword, String target) {
		
		stopTimer(true);

		jLabel_tree.setView(JTreeLabel.Nodes.FILTERED, target);
		
		ArrayList<ListedItem> tmpRowData = new ArrayList<ListedItem>();
		
		for ( ListedItem c : rowData ) {
			
			// 表示中の情報を一行ずつチェックする
			ProgDetailList tvd = c.tvd;
			
			// タイトルを整形しなおす
			boolean isFind = SearchProgram.isMatchKeyword(keyword, "", tvd);
			if ( isFind ) {
				String matched = SearchProgram.getMatchedString();
				String[] tStr = new String[3];
				if (matched != null) {
					int a = tvd.title.indexOf(matched);
					tStr[0] = tvd.title.substring(0,a);
					tStr[1] = matched;
					tStr[2] = tvd.title.substring(a+matched.length());
				}
				else {
					tStr[0] = tvd.title;
					tStr[1] = "";
					tStr[2] = "";
				}
				
				// 修正して仮置き場に入れる
				c.prefix = tvd.extension_mark+tvd.prefix_mark;
				c.title = tvd.newlast_mark+"\0"+tStr[0]+"\0"+tStr[1]+"\0"+tStr[2]+tvd.postfix_mark;
				c.fireChanged();
				tmpRowData.add(c);
			}
		}
		
		// 表示データを置き換える
		rowData.clear();
		for ( ListedItem a : tmpRowData ) {
			addRow(a);
		}
		
		// fire!
		tableModel_listed.fireTableDataChanged();
		rowheaderModel_listed.fireTableDataChanged();
	}
	
	public boolean addRow(ListedItem data) {
		// 開始日時でソート
		int i=0;
		for (; i<rowData.size(); i++) {
			ListedItem c = rowData.get(i);
			ProgDetailList tvd = c.tvd;
			int x = tvd.startDateTime.compareTo(data.tvd.startDateTime);
			int y = tvd.endDateTime.compareTo(data.tvd.endDateTime);
			boolean isChMatched = c.tvd.center.equals(data.tvd.center);
			boolean isTitleMatched = c.tvd.title.equals(data.tvd.title);
			if (x == 0 && y == 0 && isChMatched ) {
				// 日またがりで発生した重複エントリを整理　→　ピックアップとかも重複するよ
				ProgType typ = tvd.type;
				if ( debug ) {
					if ( isTitleMatched ) {
						StdAppendMessage("[重複エントリ] 省略しました: "+typ+" "+data.tvd.center+" "+data.tvd.title+" "+data.tvd.startDateTime+" "+data.tvd.endDateTime);
					}
					else {
						StdAppendMessage("[重複エントリ] 放送局と開始終了日時が同がじでタイトルの異なる情報がありました: "+typ+" "+data.tvd.center+" "+data.tvd.startDateTime+" "+data.tvd.title+" -> "+c.title);
					}
				}
				return false;
			}
			else if (x > 0) {
				break;	// 挿入位置確定
			}
		}
		
		// 有効データ
		rowData.add(i, data);
		return true;
	}
	
	public void setReservedMarks() {
		
		for ( ListedItem data : rowData ) {
			
			// 予約済みマーク
			Marker rm = getReservedMarkChar(data);
			
			if (rm != null) {
				data.rsvmark = rm.mark;
				data.hide_rsvmarkcolor = rm.color;
				data.fireChanged();
			}
			else {
				data.rsvmark = "";
				data.hide_rsvmarkcolor = "";
				data.fireChanged();
			}
		}
	}
	
	/**
	 * 重複マークつけてください
	 */
	private void setOverlapMark() {
		
		if ( rowData.size() <= 1 ) {
			// １個以下ならソートの意味ないよね
			return;
		}
		
		// リセット
		for (int vrow=0; vrow<rowData.size(); vrow++) {
			ListedItem rf = rowData.get(vrow); 
			rf.dupmark = "";
			rf.fireChanged();
		}
		
		// 時間重複のマーキング
		String sDT = "";
		String eDT = "";
		String sDT2 = "";
		String eDT2 = "";
		for (int vrow=0; vrow<rowData.size()-1; vrow++) {
			ListedItem ra = rowData.get(vrow);
			
			for ( int vrow2=vrow+1; vrow2<rowData.size(); vrow2++ ) {
				ListedItem rb = rowData.get(vrow2);
				
				if ( CommonUtils.getCompareDateTime(ra.tvd.endDateTime, rb.tvd.startDateTime) < 0) {
					// もう見なくていい
					break;
				}
				
				/*
				if ( ! sDT2.equals("")) {
					sDT = sDT2;
					eDT = eDT2;
				}
				else
				*/
				{
					sDT = ra.tvd.startDateTime;
					eDT = ra.tvd.endDateTime;
				}
				
				{
					sDT2 = rb.tvd.startDateTime;
					eDT2 = rb.tvd.endDateTime;
				}
				
				if ( eDT.equals(sDT2) ) {
					if ( ra.dupmark.length() == 0 ) {
						ra.dupmark = DUPMARK_REP;
						ra.fireChanged();
					}
					if ( rb.dupmark.length() == 0 ) {
						rb.dupmark = DUPMARK_REP;
						rb.fireChanged();
					}
				}
				else if ( CommonUtils.isOverlap(sDT, eDT, sDT2, eDT2, false) ) {
					ra.dupmark = rb.dupmark = DUPMARK_NORMAL;
					ra.fireChanged();
					rb.fireChanged();
				}
			}
		}
	}
	
	/**
	 * 現在時刻追従スクロールを開始する
	 * @see #stopTimer
	 * @see #pauseTimer
	 */
	private void startTimer() {
		timer_now_enabled = true;
	}
	
	/**
	 * 現在時刻追従スクロールを停止する
	 */
	private boolean stopTimer(boolean showmsg) {
		return (timer_now_enabled = false);
	}
	
	// 主に他のクラスから呼び出されるメソッド
	
	/**
	 * サイドツリーの「予約待機」を選択する
	 */
	public void selectTreeDefault() {
		if ( defaultNode != null ) jTree_tree.setSelectionPath(new TreePath(defaultNode.getPath()));
	}
	
	/**
	 * サイドツリーの現在選択中のノードを再度選択して描画しなおす
	 */
	public void reselectTree() {
		JTreeLabel.Nodes node = jLabel_tree.getNode();
		String value = jLabel_tree.getValue();
		String[] names = new String[] { node.getLabel(), value };
		TreeNode[] nodes = ter.getSelectedPath(listRootNode, names, 0);
		if (nodes != null) {
			TreePath tp = new TreePath(nodes);
			if ( tp != null ) {
				jTree_tree.setSelectionPath(null);
				jTree_tree.setSelectionPath(tp);
			}
		}
	}
	
	/**
	 * 他から検索を実行される時にツリーの選択をはずす
	 */
	public void clearSelection() {
		jTree_tree.clearSelection();
	}
	
	/**
	 * 特定のノードを選択しているか？
	 */
	public boolean isNodeSelected(JTreeLabel.Nodes node) {
		return(node == jLabel_tree.getNode());
	}

	/**
	 * サイドツリーを開く
	 */
	public void setExpandTree() {
		jSplitPane_view.setDividerLocation(bounds.getTreeWidth());
		jScrollPane_tree.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		jScrollPane_tree.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}
	
	/**
	 * サイドツリーを閉じる
	 */
	public void setCollapseTree() {
		jSplitPane_view.setDividerLocation(bounds.getMinDivLoc());
		jScrollPane_tree.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		jScrollPane_tree.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	}
	
	/**
	 * サイドツリーの展開状態を設定ファイルに保存（鯛ナビ終了時に呼び出される）
	 */
	public void saveTreeExpansion() {
		ter.save();
	}
	
	/**
	 * いまはどんな条件で表示しているのかな？
	 */
	public String getCurrentView() {
		return jLabel_tree.getView();
	}
	
	/**
	 * 画面上部の番組詳細領域の表示のＯＮ／ＯＦＦ
	 */
	public void setDetailVisible(boolean aFlag) {
		jTextPane_detail.setVisible(aFlag);
	}

	/**
	 * テーブルの行番号の表示のＯＮ／ＯＦＦ
	 */
	public void setRowHeaderVisible(boolean b) {
		jScrollPane_listed.getRowHeader().setVisible(b);
	}
	
	/**
	 * スクリーンショット用
	 */
	public Component getTableHeader() {
		return jTable_listed.getTableHeader();
	}
	
	/**
	 * スクリーンショット用
	 */
	public Component getTableBody() {
		return jTable_listed;
	}
	
	/*******************************************************************************
	 * リスナー
	 ******************************************************************************/
	
	/**
	 * 現在時刻追従スクロール
	 */
	@Override
	public void timerRised(VWTimerRiseEvent e) {
		
		if ( ! timer_now_enabled ) {
			return;
		}
			
		stopTimer(false);
		
		ProgDetailList tvd = null;
		
		// 更新前に選択していた行を確認する
		{
			int row = jTable_listed.getSelectedRow();
			if ( row >= 0 ) {
				int vrow = jTable_listed.convertRowIndexToModel(row);
				tvd = rowData.get(vrow).tvd;
			}
		}
		
		reselectTree();	// タイマーはこの中で再開される

		// 更新前に選択していた行を再度選択する
		if ( tvd != null ) {
			int vrow = -1;
			for ( ListedItem c : rowData ) {
				vrow++;
				if ( c.tvd == tvd ) {
					int row = jTable_listed.convertRowIndexToView(vrow);
					jTable_listed.setRowSelectionInterval(row,row);
					break; 
				}
			}
		}
	}

	/**
	 * タブを開いたり閉じたりしたときに動くリスナー
	 * ★synchronized(rowData)★
	 * @see #updateReserveMark()
	 */
	private ComponentListener cl_tabshownhidden = new ComponentAdapter() {
		@Override
		public void componentShown(ComponentEvent e) {
			
			// ★★★　イベントにトリガーされた処理がかちあわないように synchronized()　★★★
			synchronized ( rowData ) {
				// 終了した予約を整理する
				for (HDDRecorder recorder : recorders) {
					recorder.refreshReserves();
				}
				
				// 他のコンポーネントと連動
				onShown();
			}
		}
		@Override
		public void componentHidden(ComponentEvent e) {
			// フォーカスは無効にする
			vrowInFocus = -1;
			
			// 他のコンポーネントと連動
			onHidden();
		}
	};
	
	/**
	 *  行を選択すると詳細が表示されるようにする
	 */
	private ListSelectionListener lsSelectListner = new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			if(e.getValueIsAdjusting()) return;
			if (jTable_listed.getSelectedRow() >= 0) {
				int row = jTable_listed.convertRowIndexToModel(jTable_listed.getSelectedRow());
				ListedItem c = rowData.get(row);
				jTextPane_detail.setLabel(
						c.tvd.start,
						c.tvd.end,
						c.tvd.title);
				jTextPane_detail.setText(c.tvd.detail);
			}
			else {
				jTextPane_detail.setLabel("","","");
				jTextPane_detail.setText("");
			}
		}
	};
	
	/**
	 * マウスクリックでメニュー表示
	 */
	private MouseAdapter lsClickAdapter = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
			
			JTable t = (JTable) e.getSource();
			Point p = e.getPoint();
			int vrow = t.rowAtPoint(p);
			
			// カーソル位置を記憶する
			vrowInFocus = vrow;
			
			//t.getSelectionModel().setSelectionInterval(vrow,vrow);
			int row = t.convertRowIndexToModel(vrow);
			ListedItem c = rowData.get(row);
			
			ProgDetailList tvd = c.tvd;
			
			// 番組表リストの挿入位置を決める
			GregorianCalendar cal = CommonUtils.getCalendar(c.tvd.startDateTime);
			if (CommonUtils.isLateNight(cal)) {
				cal.add(Calendar.DATE,-1);
			}
			
			switch ( tvd.type ) {
			case PROG:
			case SYOBO:
			case PASSED:
			case PICKED:
			case OTHERS:
				break;
			default:
				MWin.appendError(ERRID+"未定義の番組表種別です： "+tvd.type);
				return;
			};
			
			if (e.getButton() == MouseEvent.BUTTON3) {
				if (e.getClickCount() == 1) {
					// 右シングルクリックでメニューの表示
					t.getSelectionModel().setSelectionInterval(vrow,vrow);
					
					int threshold = getThrValByRow(row);
					String keyword = (threshold > 0) ? (getKeyValByRow(row)) : (tvd.title);
					showPopupForTraceProgram(t, tvd, keyword, threshold, p.x, p.y, -1);
				}
			}
			else if (e.getButton() == MouseEvent.BUTTON1) {
				if (e.getClickCount() == 2) {
					// RADIOは閲覧のみ 
					if (tvd.type == ProgType.PROG && tvd.subtype == ProgSubtype.RADIO) {
						return;
					}
					// レコーダが選択されていない場合はなにもしない
					if (recorders.size() == 0) {
						return;
					}

					// 左ダブルクリックで
					switch ( env.getDblClkCmd() ) {
					
					// 予約ウィンドウを開く
					case SHOWRSVDIALOG:
						if (tvd.type == ProgType.PASSED) {
							// 過去ログは閲覧のみ 
							return;
						}
						
						// 類似予約抽出条件の設定
						String keyword = "";
						int threshold = getThrValByRow(row);
						if (threshold > 0) {
							keyword = getKeyValByRow(row);
						}
						
						// ダイアログを開く
						//rD.clear();
						CommonSwingUtils.setLocationCenter(parent,rD);
						if (rD.open(tvd, keyword, threshold)) {
							rD.setVisible(true);
						}
						else {
							rD.setVisible(false);
						}
						
						// 予約マークを更新する
						if (rD.isReserved()) {
							// 自分
							setReservedMarks();
							tableModel_listed.fireTableDataChanged();
							rowheaderModel_listed.fireTableDataChanged();
							refocus();
							// 他人
							updateReserveDisplay(tvd.center);
						}
						break;
					 
					// 番組欄にジャンプ
					case JUMPTOPAPER:
						jumpToPaper(tvd.center,tvd.startDateTime);
						break;
						
					// ブラウザで番組詳細を開く
					case JUMPTOWEB:
						if ( tvd.link.startsWith("http") ) {
							try {
								Desktop desktop = Desktop.getDesktop();
								desktop.browse(new URI(tvd.link));
							} catch (IOException e1) {
								e1.printStackTrace();
							} catch (URISyntaxException e1) {
								e1.printStackTrace();
							}
						}
						break;
					}
				}
			}
			else if (e.getButton() == MouseEvent.BUTTON2) {
				// ピックアップに追加or削除
				addToPickup(tvd);
				setReservedMarks();
				tableModel_listed.fireTableDataChanged();
				rowheaderModel_listed.fireTableDataChanged();
				refocus();
			}
			
			// カーソル位置をリセットする
			vrowInFocus = -1;
		}
	};
	
	/**
	 * サイドツリーのノードをさわると実行されるリスナー
	 */
	private final MouseListener ml_nodeselected = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				int selRow = jTree_tree.getRowForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					jTree_tree.setSelectionRow(selRow);
				}
				
				TreePath path = jTree_tree.getPathForLocation(e.getX(), e.getY());
				
				if ( path != null ) {
					
					JTreeLabel.Nodes node = (path.getPathCount() < 2) ? null : JTreeLabel.Nodes.getNode(path.getPathComponent(1).toString());
					String value = path.getLastPathComponent().toString();
					
					switch ( path.getPathCount() ) {
					case 2:
						switch ( node ) {
						case TRACE:
							showPopupForSortTraceKey(e.getX(), e.getY());
							break;
						case KEYWORD:
							showPopupForSortSearchKey(e.getX(), e.getY());
							break;
						case KEYWORDGROUP:
							showPopupForRemoveKeywordGrp(e.getX(), e.getY());
							break;
						case EXTENTION:
							showPopupForSortExtension(e.getX(), e.getY());
							break;
						default:
							break;
						}
						break;
						
					case 3:
						switch ( node ) {
						case TRACE:
							showPopupForRemoveTraceKey(e.getX(), e.getY(), value);
							break;
						case KEYWORD:
							showPopupForRemoveKeyword(e.getX(), e.getY(), value);
							break;
						case KEYWORDGROUP:
							showPopupForRemoveKeywordGrpName(e.getX(), e.getY(), value);
							break;
						case EXTENTION:
							showPopupForRemoveExtension(e.getX(), e.getY(), value);
							break;
						default:
							break;
						}
						break;
						
					case 4:
						switch ( node ) {
						case KEYWORDGROUP:
							showPopupForRemoveKeywordGrpEntry(e.getX(), e.getY(), path.getPathComponent(2).toString(), value);
							break;
						default:
							break;
						}
						break;
					}
				}
			}
		}
	};
	
	/**
	 * サイドツリーにつけるリスナー（ツリーの展開状態を記憶する）
	 */
	private final TreeExpansionListener tel_nodeexpansion = new TreeExpansionListener() {
		
		@Override
		public void treeExpanded(TreeExpansionEvent event) {
			ter.reg();
		}
		
		@Override
		public void treeCollapsed(TreeExpansionEvent event) {
			ter.reg();
		}
	};
	
	/**
	 * サイドツリーにつけるリスナー（クリックで描画実行）
	 */
	private final TreeSelectionListener tsl_nodeselected = new TreeSelectionListener() {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			
			TreePath path = jTree_tree.getSelectionPath();
			
			if (path != null) {
				
				// 本当は排他するべきかな
				stopTimer(false);
				boolean stop_timer = true;
				
				JTreeLabel.Nodes node = (path.getPathCount() < 2) ? null : JTreeLabel.Nodes.getNode(path.getPathComponent(1).toString());

				switch ( path.getPathCount() ) {
				
				// 親ノード
				case 2:
					switch ( node ) {
					case START:
						redrawListByFlag(ProgFlags.NEW);
						break;
					case END:
						redrawListByFlag(ProgFlags.LAST);
						break;
					case NOW:
						redrawListByNow(null);
						stop_timer = false;
						break;
					case SYOBOCAL:
						redrawSyobocalListByTraceAndKeyword();
						break;
					case STANDBY:
						redrawListByTraceAndKeyword();
						break;
					case TRACE:
						redrawListByTrace(null);
						break;
					case KEYWORD:
						redrawListByKeyword(null);
						break;
					case EXTENTION:
						redrawListByExtkeywordAll(extKeys.getSearchKeys());
						break;
					default:
						break;
					}
					jLabel_tree.setView(node, null);
					break;
					
				// 子ノード
				case 3:
					switch ( node ) {
					case SEARCHHIST:
						VWListedTreeNode inode = (VWListedTreeNode) path.getLastPathComponent();
						VWListedTreeNode parent = (VWListedTreeNode) inode.getParent();
						redrawListBySearched(ProgType.PASSED,parent.getIndex(inode));
						break;
					case START:
						{
							ProgGenre genre = ProgGenre.get(path.getLastPathComponent().toString());
							if ( genre != null ) {
								redrawListByFlag(ProgFlags.NEW,genre);
							}
						}
						break;
					case END:
						{
							ProgGenre genre = ProgGenre.get(path.getLastPathComponent().toString());
							if ( genre != null ) {
								redrawListByFlag(ProgFlags.LAST,genre);
							}
						}
						break;
					case SYOBOCAL:
						{
							JTreeLabel.Nodes subnode = JTreeLabel.Nodes.getNode(path.getLastPathComponent().toString());
							switch ( subnode ) {
							case TRACE:
								redrawSyobocalListByTrace();
								break;
							case KEYWORD:
								redrawSyobocalListByKeyword();
								break;
							case SYOBOALL:
								redrawSyobocalAll();
								break;
							default:
								break;
							}
						}
						break;
					case NOW:
						{
							ProgGenre genre = ProgGenre.get(path.getLastPathComponent().toString());
							if ( genre != null ) {
								redrawListByNow(genre);
								stop_timer = false;
							}
						}
						break;
					case STANDBY:
						{
							JTreeLabel.Nodes subnode = JTreeLabel.Nodes.getNode(path.getLastPathComponent().toString());
							switch ( subnode ) {
							case NEWARRIVAL:
								redrawListByTraceAndKeywordNewArrival();
								break;
							case MODIFIED:
								redrawListByTraceAndKeywordModified();
								break;
							case SYOBOONLY:
								redrawSyobocalListByOnly();
								break;
							case PICKUP:
								redrawListByPickup();
								break;
							default:
								redrawListByTraceAndKeywordOkini(path.getLastPathComponent().toString());
								break;
							}
						}
						break;
					case TRACE:
						for (TraceKey trace : trKeys.getTraceKeys()) {
							if (path.getLastPathComponent().toString().equals(trace.getLabel())) {
								redrawListByTrace(trace);
								break;
							}
						}
						break;
					case KEYWORD:
						for (SearchKey search : srKeys.getSearchKeys()) {
							if (path.getLastPathComponent().toString().equals(search.getLabel())) {
								redrawListByKeyword(search);
								break;
							}
						}
						break;
					case KEYWORDGROUP:
						for (SearchGroup gr : srGrps) {
							if (gr.getName().equals(path.getLastPathComponent().toString())) {
								redrawListByKeywordGrp(gr);
								break;
							}
						}
						break;
					case GENRE:
						for (ProgGenre genre : ProgGenre.values()) {
							if (path.getLastPathComponent().toString().equals(genre.toString())) {
								redrawListByGenre(genre, null);
								break;
							}
						}
						break;
					case BCASTLIST:
						redrawListByCenterList(path.getLastPathComponent().toString());
						break;
					case EXTENTION:
						for (SearchKey search : extKeys.getSearchKeys()) {
							if (path.getLastPathComponent().toString().equals(search.getLabel())) {
								redrawListByKeywordDyn(search);
								break;
							}
						}
						break;
					default:
						break;
					}
					jLabel_tree.setView(node, path.getLastPathComponent().toString());
					System.out.println(jLabel_tree.getView());
					break;
					
				// 孫ノード
				case 4:
					switch ( node ) {
					case KEYWORDGROUP:
						for (SearchKey search : srKeys.getSearchKeys()) {
							if (path.getLastPathComponent().toString().equals(search.getLabel())) {
								redrawListByKeyword(search);
								break;
							}
						}
						break;
					case GENRE:
						ProgGenre genre = ProgGenre.get(path.getPathComponent(2).toString());
						if ( genre != null ) {
							ProgSubgenre subgenre = ProgSubgenre.get(path.getLastPathComponent().toString());
							if ( subgenre != null ) {
								redrawListByGenre(genre, subgenre);
							}
						}
						break;
					default:
						break;
					}
					jLabel_tree.setView(node, path.getLastPathComponent().toString());
					break;
				}
				
				if (stop_timer) {
					stopTimer( ! (path.getPathCount() >= 2 && path.getPathComponent(1).toString().equals(JTreeLabel.Nodes.NOW.toString())));
				}
				else {
					startTimer();
				}
			}
		}
	};
	
	private final MouseListener ml_treehide = new MouseAdapter() {
		public void mouseEntered(MouseEvent e) {
			if (isFullScreen()) {
				setExpandTree();
				//StdAppendMessage("Show tree (L)");
			}
		}
		public void mouseExited(MouseEvent e) {
			if (isFullScreen()) {
				setCollapseTree();
				//StdAppendMessage("Hide tree (L)");
			}
		}
	};
	
	
	/**
	 * ツリーのリスナーを止める
	 */
	private void stopTreeListener() {
		jTree_tree.removeMouseListener(ml_nodeselected);
		jTree_tree.removeTreeSelectionListener(tsl_nodeselected);
	}
	
	/**
	 * ツリーのリスナーを動かす
	 */
	private void startTreeListener() {
		jTree_tree.addMouseListener(ml_nodeselected);
		jTree_tree.addTreeSelectionListener(tsl_nodeselected);
	}

	/**
	 * 検索履歴でサブノード作成
	 */
	public void redrawTreeByHistory() {
		
		stopTreeListener();
		TreePath tp = jTree_tree.getSelectionPath();
		
		SearchResult searched = tvprograms.getSearched();
		
		searchedNode.removeAllChildren();
		for ( int i=0; i<searched.getResultBufferSize(); i++) {
			searchedNode.add(new VWListedTreeNode(searched.getLabel(i)));
		}
		
		jTree_tree.setSelectionPath(tp);
		jTree_tree.updateUI();
		startTreeListener();
	}

	/**
	 * 新番組／最終回でサブノード作成
	 */
	private void redrawTreeByGenre() {
		
		stopTreeListener();
		TreePath tp = jTree_tree.getSelectionPath();
		
		_redrawTreeByGenre(nowNode);
		_redrawTreeByGenre(startNode);
		_redrawTreeByGenre(endNode);
		
		jTree_tree.setSelectionPath(tp);
		jTree_tree.updateUI();
		startTreeListener();
	}
	
	private void _redrawTreeByGenre(DefaultMutableTreeNode parent) {
		parent.removeAllChildren();
		for ( ProgGenre genre : ProgGenre.values() ) {
			parent.add(new VWListedTreeNode(genre.toString()));
		}
	}

	/**
	 * しょぼかるでサブノード作成
	 */
	private void redrawTreeBySyobo() {
		
		stopTreeListener();
		TreePath tp = jTree_tree.getSelectionPath();
		
		syobocalNode.removeAllChildren();
		syobocalNode.add(new VWListedTreeNode(JTreeLabel.Nodes.TRACE.getLabel()));
		syobocalNode.add(new VWListedTreeNode(JTreeLabel.Nodes.KEYWORD.getLabel()));
		syobocalNode.add(new VWListedTreeNode(JTreeLabel.Nodes.SYOBOALL.getLabel()));
		
		jTree_tree.setSelectionPath(tp);
		jTree_tree.updateUI();
		startTreeListener();
	}
	
	/**
	 * 予約待機でサブノード作成
	 */
	private void redrawTreeByStandby() {
		
		stopTreeListener();
		TreePath tp = jTree_tree.getSelectionPath();
		
		standbyNode.removeAllChildren();
		for ( String okini : TVProgram.OKINIIRI ) {
			if ( ! "".equals(okini) ) {
				standbyNode.add(new VWListedTreeNode(okini));
			}
		}
		standbyNode.add(new VWListedTreeNode(JTreeLabel.Nodes.PICKUP.getLabel()));
		standbyNode.add(new VWListedTreeNode(JTreeLabel.Nodes.NEWARRIVAL.getLabel()));
		standbyNode.add(new VWListedTreeNode(JTreeLabel.Nodes.MODIFIED.getLabel()));
		if ( env.getUseSyobocal() ) {
			standbyNode.add(new VWListedTreeNode(JTreeLabel.Nodes.SYOBOONLY.getLabel()));
		}
		
		jTree_tree.setSelectionPath(tp);
		jTree_tree.updateUI();
		startTreeListener();
	}
	
	/**
	 * 番組追跡でサブノード作成
	 */
	public void redrawTreeByTrace() {
		
		stopTreeListener();
		TreePath tp = jTree_tree.getSelectionPath();
		
		traceNode.removeAllChildren();
		for ( TraceKey key : trKeys.getTraceKeys() ) {
			traceNode.add(new VWListedTreeNode(key.getLabel(), mpList.isTrKeyUsed(key.getLabel())));
		}
		
		jTree_tree.setSelectionPath(tp);
		jTree_tree.updateUI();
		startTreeListener();
	}
	
	/**
	 * キーワード検索でサブノード作成
	 */
	public void redrawTreeByKeyword() {
		
		stopTreeListener();
		TreePath tp = jTree_tree.getSelectionPath();
		
		keywordNode.removeAllChildren();
		for ( SearchKey key : srKeys.getSearchKeys() ) {
			keywordNode.add(new VWListedTreeNode(key.getLabel(), mpList.isSrKeyUsed(key.getLabel())));
		}
		
		jTree_tree.setSelectionPath(tp);
		jTree_tree.updateUI();
		startTreeListener();
	}

	/**
	 * サブジャンルでサブノード作成
	 */
	public void redrawTreeBySubGenre() {
		
		stopTreeListener();
		TreePath tp = jTree_tree.getSelectionPath();
		
		genreNode.removeAllChildren();
		for ( ProgGenre genre : ProgGenre.values() ) {
			VWListedTreeNode g = new VWListedTreeNode(genre.toString());
			genreNode.add(g);
			for ( ProgSubgenre subgenre : ProgSubgenre.values(genre) ) {
				VWListedTreeNode sg = new VWListedTreeNode(subgenre.toString()); 
				g.add(sg);
			}
		}
		
		jTree_tree.setSelectionPath(tp);
		jTree_tree.updateUI();
		startTreeListener();
	}

	/**
	 * 放送局でサブノード作成
	 */
	public void redrawTreeByCenter() {
		
		stopTreeListener();
		TreePath tp = jTree_tree.getSelectionPath();
		
		centerListNode.removeAllChildren();
		TVProgramIterator pli = tvprograms.getIterator().build(chsort.getClst(), IterationType.ALL);
		for ( ProgList pl : pli ) {
			centerListNode.add(new VWListedTreeNode(pl.Center));
		}
		
		jTree_tree.setSelectionPath(tp);
		jTree_tree.updateUI();
		startTreeListener();
	}

	/**
	 * キーワードグループでサブノード作成
	 */
	public void redrawTreeByKeywordGroup() {
		
		stopTreeListener();
		TreePath tp = jTree_tree.getSelectionPath();
		
		keywordGrpNode.removeAllChildren();
		for ( SearchGroup gr : srGrps ) {
			VWListedTreeNode gn = new VWListedTreeNode(gr.getName());
			keywordGrpNode.add(gn);
			for ( String kw : gr ) {
				gn.add(new VWListedTreeNode(kw));
			}
		}
		
		jTree_tree.setSelectionPath(tp);
		jTree_tree.updateUI();
		startTreeListener();
	}

	/**
	 * 放送局でサブノード作成
	 */
	public void redrawTreeByExtension() {
		
		stopTreeListener();
		TreePath tp = jTree_tree.getSelectionPath();
		
		extensionNode.removeAllChildren();
		for ( SearchKey key : extKeys.getSearchKeys() ) {
			extensionNode.add(new VWListedTreeNode(key.getLabel()));
		}
		
		jTree_tree.setSelectionPath(tp);
		jTree_tree.updateUI();
		startTreeListener();
	}
	
	
	
	/**
	 * 現時点でまだ開始していない番組を上から順に選択する。
	 */
	public void selectBatchTarget() {
		
		String dt = CommonUtils.getDateTime(0);
		
		int cnt=1;
		for (int row=0; row<rowData.size(); row++) {
			ListedItem c = rowData.get(row);
			if (dt.compareTo(c.tvd.startDateTime) > 0) {
				// 開始日時が過去のものは対象外
				continue;
			}
			if ( c.rsvmark.length() == 0 || c.rsvmark.equals(RSVMARK_URABAN) ) {
				int vrow = jTable_listed.convertRowIndexToView(row);
				jTable_listed.getSelectionModel().addSelectionInterval(vrow, vrow);
				if (cnt++ >= env.getRsvTargets()) {
					return;
				}
			}
		}
	}
	
	/**
	 * ツールバーの一括予約ボタンを押して実行される一括予約処理
	 */
	public void doBatchReserve() {
		//
		boolean mod = false;
		for (int vrow : jTable_listed.getSelectedRows()) {
			
			int row = jTable_listed.convertRowIndexToModel(vrow);
			ProgDetailList tvd = rowData.get(row).tvd;
			
			//VWReserveDialog rD = new VWReserveDialog(0, 0, env, tvprograms, recorders, avs, chavs, stwin);
			//rD.clear();

			if (rD.open(tvd)) {
				rD.doRecord();
			}
			
			// 予約ダイアログは見せないまま更新を実行する
			
			if ( ! rD.isReserved()) {
				StdAppendError("【警告】予約の登録に失敗しました: "+rowData.get(row).tvd.title);
				break;
			}
			else {
				mod = true;
			}
		}
		if (mod) {
			// 自分
			setReservedMarks();
			tableModel_listed.fireTableDataChanged();
			rowheaderModel_listed.fireTableDataChanged();
			refocus();
			// 他人
			updateReserveDisplay(null);
		}
	}
	
	/**
	 * 他のクラスで発生したイベント中に呼び出されてリスト形式の予約マーク表示を更新するためのメソッド。
	 * ★synchronized(rowData)★
	 * @see #cl_tabshownhidden
	 */
	public void updateReserveMark() {
		// ★★★　イベントにトリガーされた処理がかちあわないように synchronized()　★★★
		synchronized ( rowData ) {
			setReservedMarks();
			tableModel_listed.fireTableDataChanged();
			rowheaderModel_listed.fireTableDataChanged();
		}
	}
	
	/**
	 * テーブルを更新した後、セレクション状態が解除されるので再度セレクションする。
	 */
	public void refocus() {
		if (vrowInFocus >= 0) {
			jTable_listed.getSelectionModel().addSelectionInterval(vrowInFocus, vrowInFocus);
			vrowInFocus = -1;
		}
	}

	/**
	 * カラム幅を保存する（鯛ナビ終了時に呼び出されるメソッド）
	 */
	public void copyColumnWidth() {
		//DefaultTableColumnModel columnModel = (DefaultTableColumnModel)jTable_listed.getColumnModel();
		TableColumn column = null;
		for ( ListedColumn lc : ListedColumn.values() ) {
			if ( lc.getIniWidth() < 0 ) {
				continue;
			}
			try {
				column = jTable_listed.getColumn(lc.getName());
				bounds.getListedColumnSize().put(lc.toString(), column.getPreferredWidth());	// toString()!
			}
			catch (IllegalArgumentException e) {
				// 非表示のカラムは操作できない
			}
		}
	}
	
	// キーワードにマッチした箇所の強調色
	public void setMatchedKeywordColor(Color c) {
		titleCellRenderer.setMatchedKeywordColor(c);
	}
	
	// 予約行の強調表示
	public void setRsvdLineColor(Color c) {
		jTable_listed.setReservedColor(c);
	}
	
	// ピックアップ行の強調表示
	public void setPickedLineColor(Color c) {
		jTable_listed.setPickedColor(c);
	}
	
	// 予約行の強調表示
	public void setCurrentLineColor(Color c) {
		jTable_listed.setCurrentColor(c);
	}
	
	// 番組詳細の表示・非表示
	public void setMarkColumnVisible(boolean b) {
		jTable_listed.setColumnVisible(ListedColumn.OPTIONS.getName(), b);
	}
	
	// 番組詳細の表示・非表示
	public void setDetailColumnVisible(boolean b) {
		jTable_listed.setColumnVisible(ListedColumn.DETAIL.getName(), b);
	}

	/*
	 * 特定の項目を取得しやすくした感じ？
	 */
	
	//　検索結果一覧上のIdNumをintに戻す
	private int getThrValByRow(int row) { 
		try {
			return Integer.valueOf(rowData.get(row).threshold);
		} catch (NumberFormatException e2) {
			// No proc.
		}
		return 0;
	}
	private String getKeyValByRow(int row) {
		Matcher ma = Pattern.compile("^(.+)\\s*\\([^\\)]+?\\)$").matcher(rowData.get(row).searchlabel);
		if (ma.find()) {
			return ma.group(1);
		}
		return "";
	}
	
	
	
	
	
	/*
	 * 予約マークの取得だお！
	 */
	
	private class Marker {
		String mark; 
		String myself;
		String color;
		
		public Marker(String mark, String myself, String color) {
			this.mark = mark;
			this.myself = myself;
			this.color = color;
		}
	}
	
	/**
	 * 引数で指定した番組を予約している、または予約に一部時間が重なっている場合に表示する予約マークを取得する。
	 * @param Center チェックする番組の放送局名
	 * @param startDateTime チェックする番組の開始日時
	 * @param endDateTime チェックする番組の終了日時
	 * @param title チェックする番組のタイトル
	 * @param extention 延長警告がなされている番組の場合はtrueを指定。
	 * @return String [0]マーク [1]予約しているレコーダのユニークID({@link HDDRecorder#Myself()}) [2]色({@link CommonUtils#str2color(String)})
	 */
	private Marker getReservedMarkChar(ListedItem data) {
		
		//
		HDDRecorder recorder = null;						// その番組の予約がみつかった最初のレコーダ
		ReserveList reserve = null;							// 見つかった予約情報
		String start = null;								// 実行予定が複数あったら一番近いのを選ぶ
		String end = "";									// 同上
		long diff = 86400L*30L;
		
		String myself = getSelectedRecorderOnToolbar();
		HDDRecorderList recs = recorders.getMyself(myself);

		// コンボボックスの指定はピックアップである
		boolean isPickupOnly = ( myself != null && myself.length() == 0 ) ;
		
		if ( ! isPickupOnly ) {
			
			// 「ピックアップ」が選択されていればここは通らない
			
			// 基準日時
			String critDateTime = CommonUtils.getCritDateTime(env.getDisplayPassedReserve());
			
			// 全予約をなめて、一番近い予約を探さなければならない
			for ( HDDRecorder rec : recs )
			{
				if (diff == 0) break;
				
				for ( ReserveList res : rec.getReserves() )
				{
					if (diff == 0) break;
					
					// Exec == ON ?
					if ( env.getDisplayOnlyExecOnEntry() && ! res.getExec()) {
						continue;
					}
					
					if (res.getCh_name() == null) {
						// 警告したい！
						continue;
					}
					
					// 局が一致して
					if ( ! data.tvd.center.equals(res.getCh_name())) {
						continue;
					}
						
					// 開始終了日時リストを生成する
					ArrayList<String> starts = new ArrayList<String>();
					ArrayList<String> ends = new ArrayList<String>();
					CommonUtils.getStartEndList(starts, ends, res);
					
					for (int j=0; j<starts.size(); j++) {
						if (critDateTime.compareTo(ends.get(j)) > 0) {
							// 終了済みは対象外
							continue;
						}
						if ( CommonUtils.isOverlap(data.tvd.startDateTime, data.tvd.endDateTime, starts.get(j), ends.get(j), true) ) {
							// より開始時刻が近いものを選ぶ
							if ( start == null ) {
								start = starts.get(j);
							}
							long df = CommonUtils.getDiffDateTime(starts.get(j), data.tvd.startDateTime);
							if ( diff > df ) {
								recorder = rec;
								reserve = res;
								start = starts.get(j);
								end = ends.get(j);
								diff = df;
							}
						}
					}
				}
			}
		}
		
		// 予約されている
		
		Marker mark = null;
		Marker pickmark = null;
		Marker uramark = null;
		
		// 予約マーク
		if (recorder != null) {
			mark = _getReservedMarkCharNormal(data, recorder, reserve, start, end);
		}
		// ピックアップマーク
		if (env.getShowRsvPickup()) {
			pickmark = _getReservedMarkCharPickup(data);
		}
		// 裏番組予約マーク
		if (mark == null && pickmark == null && env.getShowRsvUra()) {
			uramark = _getReservedMarkCharUra(data);
		}
		
		if ( mark != null ) {
			return mark;
		}
		if ( pickmark != null || isPickupOnly ) {
			return pickmark;
		}
		if ( uramark != null ) {
			return uramark;
		}
		
		// 予約されていない
		return(null);
	}
	/**
	 * @see #getReservedMarkChar(String, String, String, String, boolean) 
	 */
	private Marker _getReservedMarkCharNormal(ListedItem data, HDDRecorder recorder, ReserveList reserve, String start, String end) {
		
		// ここに入ってくる場合は時間の重なりが確認できているものだけである
		
		RSVMARK_COND cond = getReservedMarkCond(data, start, end);
		
		if (debug) System.err.println(DBGID+data.tvd.title+" "+data.tvd.startDateTime+" "+data.tvd.endDateTime+" "+start+" "+end+" "+cond);
		
		String mark = null;
		
		switch (cond) {
		case PREV:
			return null;
		case DELAY:
			mark = RSVMARK_DELAYED;
			break;
		case UNDER:
			mark = RSVMARK_UNDERRUN;
			break;
		case OVER:
			mark = RSVMARK_OVERRUN;
			break;
		case CLIP:
			mark = (data.tvd.extension) ? (RSVMARK_CLIPPED_E) : (RSVMARK_CLIPPED);
			break;
		case SHORT:
			mark = (data.tvd.extension) ? (RSVMARK_SHORTAGE_E) : (RSVMARK_SHORTAGE);
			break;
		default:
			mark = RSVMARK_NORMAL;
			break;
		}

		return(new Marker((reserve.getExec())?(mark):(RSVMARK_NOEXEC), recorder.Myself(), recorder.getColor(reserve.getTuner())));
	}
	private RSVMARK_COND getReservedMarkCond(ListedItem data, String start, String end) {
		{
			// 番組の終了日時と予約の開始日時（１分は想定内）
			int overlap = (int) (CommonUtils.getCompareDateTime(data.tvd.endDateTime,start)/60000L);
			if ( env.getOverlapUp() && overlap == 1 )
				return RSVMARK_COND.PREV;
		}
		{
			// 番組の開始日時と予約の開始日時（１分でも遅れちゃだめ）
			int overlap = (int) (CommonUtils.getCompareDateTime(data.tvd.startDateTime,start)/60000L);
			if ( overlap <= -1 )
				return RSVMARK_COND.DELAY;
		}
		
		// 延長警告がある場合はこんだけ延びる
		int spoex_length = (data.tvd.extension)?(Integer.valueOf(env.getSpoexLength())):(0);
		
		{
			// 番組の終了日時と予約の終了日時
			int overlap = (int) (CommonUtils.getCompareDateTime(data.tvd.endDateTime,end)/60000L);
			
			if (data.tvd.extension) {
				// ここは、延長警告で時間が延びるはずが微妙に延びてない感じの予約を探すためのもの
				
				// 通常１分短縮から～延長２分短縮まで
				// 通常１分延長から～延長０分延長まで
				// どちらでもなければ通常０分短縮から～延長１分短縮まで
				if (
						(env.getOverlapDown2() && ! data.tvd.dontoverlapdown && (overlap <= 1  && (overlap+spoex_length) >= 2)) ||
						(env.getOverlapDown()  && (overlap <= -1 && (overlap+spoex_length) >= 0)) ||
						( ( ! env.getOverlapDown2() || env.getOverlapDown2() && data.tvd.dontoverlapdown) && ! env.getOverlapDown() && (overlap <= 0 && (overlap+spoex_length) >= 1))
						)
					return RSVMARK_COND.UNDER;
			}
			
			// ケツ短縮で０分以上進んでたらだめ
			// ケツ延長で２分以上進んでたらだめ
			// どちらでもなければ１分以上進んでたらだめ
			if (
					(env.getOverlapDown2() && ! data.tvd.dontoverlapdown && (overlap+spoex_length) <= 0) ||
					(env.getOverlapDown() && (overlap+spoex_length) <= -2) ||
					( ( ! env.getOverlapDown2() || env.getOverlapDown2() && data.tvd.dontoverlapdown) && ! env.getOverlapDown() && (overlap+spoex_length) <= -1)
					)
				return RSVMARK_COND.OVER;
			
			if ( env.getOverlapDown2() ) {
				// １分短縮時に１分なら予定通り
				if ( (overlap+spoex_length) == 1 )
					return RSVMARK_COND.CLIP;
				// ２分はどうかな
				if ( (overlap+spoex_length) >= 2 )
					return RSVMARK_COND.SHORT;
			}
			else {
				// １分でもだめ
				if ( (overlap+spoex_length) >= 2 )
					return RSVMARK_COND.SHORT;
			}
			
			if ( env.getOverlapDown() ) {
				// １分延長時に１分なら予定通り
				if ( (overlap+spoex_length) == -1 )
					return RSVMARK_COND.CLIP;
				// ぴったりはだめでしょ
				if ( (overlap+spoex_length) <= 0 )
					return RSVMARK_COND.SHORT;
			}
			else {
				// １分でもだめ
				if ( (overlap+spoex_length) <= -1 )
					return RSVMARK_COND.SHORT;
			}
			
		}
		
		// それら以外の場合（正常予約）
		return RSVMARK_COND.NORMAL;
	}
	private static enum RSVMARK_COND { PREV, DELAY, UNDER, OVER, CLIP, SHORT, NORMAL };
	
	private Marker _getReservedMarkCharPickup(ListedItem data) {
		//return (data.hide_ispickup)?(new Marker(RSVMARK_PICKUP,"",PICKUP_COLOR)):(null);
		//
		PickedProgram picktvp = tvprograms.getPickup();
		
		// みつかるかな？
		ProgDetailList picktvd = picktvp.find(data.tvd);
		if ( picktvd == null ) {
			// みつかんねーよ
			return null;
		}
		
		return new Marker(RSVMARK_PICKUP,"",PICKUP_COLOR);
	}
	
	private Marker _getReservedMarkCharUra(ListedItem data) {
		//
		String myself = getSelectedRecorderOnToolbar();
		HDDRecorderList recs = recorders.getMyself(myself);
		
		for ( HDDRecorder rec : recs )
		{
			for ( ReserveList res : rec.getReserves() ) {
				// Exec == ON ?
				if ( env.getDisplayOnlyExecOnEntry() && ! res.getExec() ) {
					// 無効状態はしらねーよ
					continue;
				}
				if ( data.tvd.center.equals(res.getCh_name()) ) {
					// 局が違うならいらねーよ　→　裏番組だろ、逆だろＪＫ
					continue;
				}
					
				// 開始終了日時リストを生成する
				ArrayList<String> starts = new ArrayList<String>();
				ArrayList<String> ends = new ArrayList<String>();
				CommonUtils.getStartEndList(starts, ends, res);
				for (int j=0; j<starts.size(); j++) {
					if ( CommonUtils.isOverlap(data.tvd.startDateTime, data.tvd.endDateTime, starts.get(j), ends.get(j), env.getAdjoiningNotRepetition()) ) {
						return new Marker(RSVMARK_URABAN,"",URABAN_COLOR);
					}
				}
			}
		}
		return null;
	}
	
	
	
	
	
	/*
	 *  ここからノード編集系がいっぱいならんでるお！
	 */
	
	/**
	 * 番組追跡を編集したい
	 */
	private void editTraceKey(String keyword) {
		
		VWTraceKeyDialog tD = new VWTraceKeyDialog(0,0);
		CommonSwingUtils.setLocationCenter(parent,tD);
		
		tD.reopen(keyword, trKeys);
		tD.setVisible(true);
		
		if (tD.isRegistered()) { 
			// 検索結果の再構築
			mpList.clear(env.getDisableFazzySearch(), env.getDisableFazzySearchReverse());
			mpList.build(tvprograms, trKeys.getTraceKeys(), srKeys.getSearchKeys());
			
			//trKeys.save();	// 保存はtDの中でやってるよ
			
			// 変更したノードを選択するようにしたい
			jLabel_tree.setView(JTreeLabel.Nodes.TRACE, tD.getNewLabel());
			
			// 新聞形式を修正してほしい
			updateBangumiColumns();

			// ツリーを更新
			redrawTreeByTrace();
			
			// ツリーを再選択
			reselectTree();
		}
	}
	
	/**
	 * 番組追跡を削除したい
	 */
	private void removeTraceKey(String keyword) {
		
		if (env.getShowWarnDialog()) {
			//Container cp = frame.getContentPane();
			int ret = JOptionPane.showConfirmDialog(parent, "削除しますか？【"+keyword+"】", "確認", JOptionPane.YES_NO_OPTION);
			if (ret != JOptionPane.YES_OPTION) {
				return;
			}
		}
		
		MWin.appendMessage("番組追跡が削除されました【"+keyword+"】");

		// 保存
		trKeys.remove(keyword);
		trKeys.save();
		
		// 検索結果の再構築
		mpList.clear(env.getDisableFazzySearch(), env.getDisableFazzySearchReverse());
		mpList.build(tvprograms, trKeys.getTraceKeys(), srKeys.getSearchKeys());
		//mpList.debug();
		
		// 新聞形式を修正してほしい
		updateBangumiColumns();
		
		// ツリーを更新
		redrawTreeByTrace();
	}
	
	/**
	 * 番組追跡のお気に入りを変更したい
	 */
	private void setTraceKeyOkiniiri(TraceKey tk, String okini) {
		
		// 保存
		tk.setOkiniiri(okini);
		trKeys.save();
		
		// 検索結果の再構築
		mpList.clear(env.getDisableFazzySearch(), env.getDisableFazzySearchReverse());
		mpList.build(tvprograms, trKeys.getTraceKeys(), srKeys.getSearchKeys());
		
		// ツリーに反映する
		reselectTree();
	}
	
	/**
	 * 番組追跡を並べ替えたい
	 */
	private void sortTraceKey() {
		//
		ArrayList<String> oList = new ArrayList<String>();
		for ( TraceKey key : trKeys.getTraceKeys() ) {
			oList.add(key.getLabel());
		}
		
		// 編集前のリストサイズ
		int oCnt = oList.size();
		
		//
		JListSortDialog lsD = new JListSortDialog("番組追跡の並べ替え", oList);
		CommonSwingUtils.setLocationCenter(parent,lsD);
		
		lsD.setVisible(true);

		if (lsD.isRegistered()) {
			TraceProgram newTrKeys = new TraceProgram();
			for ( String label : oList ) {
				for ( TraceKey key : trKeys.getTraceKeys() ) {
					if ( key.getLabel().equals(label) ) {
						newTrKeys.add(key);
						break;
					}
				}
			}
			//trKeys = newTrKeys;
			CommonUtils.FieldCopy(trKeys, newTrKeys);
			trKeys.save();
			
			if ( oList.size() < oCnt ) {
				// 削除があった場合のみ検索結果の再構築
				mpList.clear(env.getDisableFazzySearch(), env.getDisableFazzySearchReverse());
				mpList.build(tvprograms, trKeys.getTraceKeys(), srKeys.getSearchKeys());
			}
			
			// ツリーを更新
			redrawTreeByTrace();
		}
	}

	/**
	 * キーワード検索を編集したい
	 */
	private void editSearchKey(String keyword) {
		//
		AbsKeywordDialog kD = new VWKeywordDialog();
		CommonSwingUtils.setLocationCenter(parent,kD);
		
		kD.reopen(keyword, srKeys);
		kD.setVisible(true);

		if (kD.isRegistered()) {
			// 検索結果の再構築
			mpList.clear(env.getDisableFazzySearch(), env.getDisableFazzySearchReverse());
			mpList.build(tvprograms, trKeys.getTraceKeys(), srKeys.getSearchKeys());
			//mpList.debug();
			
			// キーワードグループにも反映
			if ( ! kD.getNewLabel().equals(keyword) ) {
				if ( srGrps.rename(null, keyword, kD.getNewLabel()) ) {
					srGrps.save();
				}
			}
			
			// srKeys.save();	// 保存はkDの中でやってるよ

			// 変更したノードを選択するようにしたい
			jLabel_tree.setView(JTreeLabel.Nodes.KEYWORD, kD.getNewLabel());
			
			// 新聞形式を修正してほしい
			updateBangumiColumns();
			
			// ツリーを更新
			redrawTreeByKeyword();
			redrawTreeByKeywordGroup();
			
			// ツリーを再選択
			reselectTree();
		}
	}

	/**
	 * キーワード検索を削除したい
	 */
	private void removeSearchKey(String keyword) {
		
		if (env.getShowWarnDialog()) {
			//Container cp = getContentPane();
			int ret = JOptionPane.showConfirmDialog(parent, "削除しますか？【"+keyword+"】", "確認", JOptionPane.YES_NO_OPTION);
			if (ret != JOptionPane.YES_OPTION) {
				return;
			}
		}
		
		// 保存
		srKeys.remove(keyword);
		srKeys.save();

		// 検索結果の再構築
		mpList.clear(env.getDisableFazzySearch(), env.getDisableFazzySearchReverse());
		mpList.build(tvprograms, trKeys.getTraceKeys(), srKeys.getSearchKeys());
		//mpList.debug();
		
		// キーワードグループにも反映
		if ( srGrps.remove(null,keyword) ) {
			srGrps.save();
		}
		
		// 新聞形式を修正してほしい
		updateBangumiColumns();
		
		// ツリーを更新
		redrawTreeByKeyword();
		redrawTreeByKeywordGroup();
	}
	
	/**
	 * キーワード検索のお気に入りを変更したい
	 */
	private void setSearchKeyOkiniiri(SearchKey sr, String okini) {
		// 保存
		sr.setOkiniiri(okini);
		srKeys.save();
		
		// 検索結果の再構築
		mpList.clear(env.getDisableFazzySearch(), env.getDisableFazzySearchReverse());
		mpList.build(tvprograms, trKeys.getTraceKeys(), srKeys.getSearchKeys());
		
		// ツリーに反映する
		reselectTree();
	}
	
	/**
	 * キーワード検索を並べ替えたい
	 */
	private void sortSearchKey() {
		//
		ArrayList<String> oList = new ArrayList<String>();
		for ( SearchKey key : srKeys.getSearchKeys() ) {
			oList.add(key.getLabel());
		}
		//
		JListSortDialog lsD = new JListSortDialog("キーワード検索の並べ替え", oList);
		CommonSwingUtils.setLocationCenter(parent,lsD);
		
		lsD.setVisible(true);

		if (lsD.isRegistered()) {
			SearchProgram newSrKeys = new SearchProgram();
			for ( String label : oList ) {
				for ( SearchKey key : srKeys.getSearchKeys() ) {
					if ( key.getLabel().equals(label) ) {
						newSrKeys.add(key);
						break;
					}
				}
			}
			//srKeys = newSrKeys;
			CommonUtils.FieldCopy(srKeys, newSrKeys);
			srKeys.save();
			
			// ツリーを更新
			redrawTreeByKeyword();
		}
	}
	
	/**
	 * キーワードグループを追加したい
	 */
	private void addSearchKeyGroup() {
		//
		VWKeywordGroupDialog kD = new VWKeywordGroupDialog();
		CommonSwingUtils.setLocationCenter(parent,kD);
		
		kD.reopen("");
		kD.setVisible(true);

		if (kD.isRegistered()) {
			// グループ名を変更する
			srGrps.add(kD.getNewName());
			srGrps.save();
			
			// ツリーを更新
			redrawTreeByKeywordGroup();
			
			// 変更したノードを選択するようにしたい
			jLabel_tree.setView(JTreeLabel.Nodes.KEYWORDGROUP, kD.getNewName());
			
			// ツリーを再選択
			reselectTree();
		}
	}
	
	/**
	 * キーワードグループを編集したい
	 */
	private void editSeachkeyGroup(String name) {
		//
		VWKeywordGroupDialog kD = new VWKeywordGroupDialog();
		CommonSwingUtils.setLocationCenter(parent,kD);
		
		kD.reopen(name);
		kD.setVisible(true);

		if (kD.isRegistered()) {
			// グループ名を変更する
			srGrps.rename(name, kD.getNewName());
			srGrps.save();
			
			// ツリーを更新
			redrawTreeByKeywordGroup();
			
			// 変更したノードを選択するようにしたい
			jLabel_tree.setView(JTreeLabel.Nodes.KEYWORDGROUP, kD.getNewName());
			
			// ツリーを再選択
			reselectTree();
		}
	}

	/**
	 * キーワードグループを削除したい
	 */
	private void removeSearchKeyGroup(String name) {
		if (env.getShowWarnDialog()) {
			//Container cp = getContentPane();
			int ret = JOptionPane.showConfirmDialog(parent, "キーワードグループを削除しますか？【"+name+"】", "確認", JOptionPane.YES_NO_OPTION);
			if (ret != JOptionPane.YES_OPTION) {
				return;
			}
		}
		for ( SearchGroup gr : srGrps ) {
			if ( gr.getName().equals(name) ) {
				srGrps.remove();
				srGrps.save();
				redrawTreeByKeywordGroup();
				return;
			}
		}
	}
	
	/**
	 * キーワードグループのアイテムを削除したい
	 * @param groupName : nullならグループ登録だけでなくキーワード検索アイテム自体を削除する
	 */
	private void removeSearchKeyGroupItem(String groupName, String keyword) {
		if (env.getShowWarnDialog()) {
			//Container cp = getContentPane();
			String warn; 
			if ( groupName == null ) {
				warn = "削除しますか？【"+keyword+"】　※グループ登録の解除だけでなくアイテム自体が削除されます。";
			}
			else {
				warn = "削除しますか？【"+keyword+"】　※グループ登録の解除のみ行います。";
			}
			int ret = JOptionPane.showConfirmDialog(parent, warn, "確認", JOptionPane.YES_NO_OPTION);
			if (ret != JOptionPane.YES_OPTION) {
				return;
			}
		}
		
		if ( groupName == null ) {
			// nullなら全消し
			srKeys.remove(keyword);
			srKeys.save();
		}

		// 検索結果の再構築
		mpList.clear(env.getDisableFazzySearch(), env.getDisableFazzySearchReverse());
		mpList.build(tvprograms, trKeys.getTraceKeys(), srKeys.getSearchKeys());
		//mpList.debug();
		
		// キーワードグループにも反映
		if ( srGrps.remove(groupName,keyword) ) {
			srGrps.save();
		}
		
		// ツリーを更新
		if ( groupName == null ) {
			redrawTreeByKeyword();
		}
		// ツリーを更新
		redrawTreeByKeywordGroup();
	}
	
	/**
	 * キーワードグループのアイテムを追加したい
	 */
	private void addSearchKeyGroupItem(String groupName, String keyword) {
		if ( srGrps.add(groupName,keyword) ) {
			srGrps.save();
			redrawTreeByKeywordGroup();
		}
	}
	
	/**
	 * キーワードグループのアイテムを編集したい
	 */
	private void editSearchKeyGroupItem(String name, String member) {
		//
		AbsKeywordDialog kD = new VWKeywordDialog();
		CommonSwingUtils.setLocationCenter(parent,kD);
		
		kD.reopen(member, srKeys);
		kD.setVisible(true);

		if (kD.isRegistered()) {
			// 検索結果の再構築
			mpList.clear(env.getDisableFazzySearch(), env.getDisableFazzySearchReverse());
			mpList.build(tvprograms, trKeys.getTraceKeys(), srKeys.getSearchKeys());
			//mpList.debug();
			
			// キーワードグループにも反映
			if ( ! kD.getNewLabel().equals(member) ) {
				if ( srGrps.rename(null, member, kD.getNewLabel()) ) {
					srGrps.save();
				}
			}
			
			// ツリーを更新
			redrawTreeByKeywordGroup();
			
			// 変更したノードを選択するようにしたい
			jLabel_tree.setView(JTreeLabel.Nodes.KEYWORDGROUP, name);
			
			// ツリーを再選択
			reselectTree();
		}
	}
	
	/**
	 * 延長警告管理を削除したい
	 */
	private void removeExtension(String keyword) {
		if (env.getShowWarnDialog()) {
			//Container cp = getContentPane();
			int ret = JOptionPane.showConfirmDialog(parent, "削除しますか？【"+keyword+"】", "確認", JOptionPane.YES_NO_OPTION);
			if (ret != JOptionPane.YES_OPTION) {
				return;
			}
		}
			
		extKeys.remove(keyword);
		extKeys.save();
		
		// ツリーを更新
		redrawTreeByExtension();
			
		// 番組表の状態を更新する
		for (TVProgram tvp : tvprograms) {
			if (tvp.getType() == ProgType.PROG) {
				tvp.setExtension(null, null, false, extKeys.getSearchKeys());
			}
		}
	}
	
	/**
	 * 延長警告管理を追加したい
	 */
	private void editExtension(String keyword) {
		//
		AbsExtensionDialog eD = new VWExtensionDialog();
		CommonSwingUtils.setLocationCenter(parent,eD);
		
		eD.reopen(keyword, extKeys);
		eD.setVisible(true);
		
		if (eD.isRegistered()) {
			// 番組表の状態を更新する
			for (TVProgram tvp : tvprograms) {
				if (tvp.getType() == ProgType.PROG) {
					tvp.setExtension(null, null, false, extKeys.getSearchKeys());
				}
			}
			
			// ツリーを更新
			redrawTreeByExtension();
		}
	}
	
	/**
	 * 延長警告管理を並べ替えたい
	 */
	private void sortExtension() {
		//
		ArrayList<String> oList = new ArrayList<String>();
		for ( SearchKey key : extKeys.getSearchKeys() ) {
			oList.add(key.getLabel());
		}
		//
		JListSortDialog lsD = new JListSortDialog("延長警告の並べ替え", oList);
		CommonSwingUtils.setLocationCenter(parent,lsD);
		
		lsD.setVisible(true);

		if (lsD.isRegistered()) {
			ExtProgram newExtKeys = new ExtProgram();
			for ( String label : oList ) {
				for ( SearchKey key : extKeys.getSearchKeys() ) {
					if ( key.getLabel().equals(label) ) {
						newExtKeys.add(key);
						break;
					}
				}
			}
			//extKeys = newExtKeys;
			CommonUtils.FieldCopy(extKeys, newExtKeys);
			extKeys.save();
			
			// ツリーを更新
			redrawTreeByExtension();
		}
	}
	
	//　キーワード削除のポップアップ
	private void showPopupForRemoveTraceKey(int x, int y, final String keyword)
	{
		JPopupMenu pop = new JPopupMenu();
		
		{
			JMenuItem menuItem = new JMenuItem("番組追跡の編集【"+keyword+"】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					editTraceKey(keyword);
				}
			});
			pop.add(menuItem);
		}
		
		{
			JMenuItem menuItem = new JMenuItem("番組追跡の削除【"+keyword+"】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeTraceKey(keyword);
				}
			});
			pop.add(menuItem);
		}
		
		pop.addSeparator();
		
		{
			ButtonGroup bg = new ButtonGroup();
			
			for ( TraceKey t : trKeys.getTraceKeys()) {
				if (t.getLabel().equals(keyword)) {
					final TraceKey tk = t;
					for (String o : TVProgram.OKINIIRI) {
						final String okini = o; 
						JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(okini, okini.equals(tk.getOkiniiri()));
						bg.add(menuItem);
						menuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								setTraceKeyOkiniiri(tk,okini);
							}
						});
						pop.add(menuItem);
					}
					break;
				}
			}
		}
		
		pop.show(jTree_tree, x, y);
	}

	// キーワード検索アイテムの処理
	private void showPopupForRemoveKeyword(int x, int y, final String keyword)
	{
		JPopupMenu pop = new JPopupMenu();
		
		{
			JMenuItem menuItem = new JMenuItem("キーワードの編集【"+keyword+"】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					editSearchKey(keyword);
				}
			});
			pop.add(menuItem);
		}
		{
			JMenuItem menuItem = new JMenuItem("キーワードの削除【"+keyword+"】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeSearchKey(keyword);
				}
			});
			pop.add(menuItem);
		}
		
		pop.addSeparator();
		
		{
			for ( SearchGroup gr : srGrps ) {
				final String groupName = gr.getName();
				if (srGrps.isFind(groupName,keyword) ) {
					JMenuItem menuItem = new JMenuItem("キーワードグループから登録解除【"+groupName+"】");
					menuItem.setForeground(Color.RED);
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							removeSearchKeyGroupItem(groupName,keyword);
						}
					});
					pop.add(menuItem);
				}
				else {
					JMenuItem menuItem = new JMenuItem("キーワードグループに追加【"+groupName+"】");
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							addSearchKeyGroupItem(groupName,keyword);
						}
					});
					pop.add(menuItem);
				}
			}
		}
		
		pop.addSeparator();
		
		{
			ButtonGroup bg = new ButtonGroup();
			
			for ( SearchKey s : srKeys.getSearchKeys()) {
				if (s.getLabel().equals(keyword)) {
					final SearchKey sr = s;
					for (String o : TVProgram.OKINIIRI) {
						final String okini = o; 
						JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(okini, okini.equals(sr.getOkiniiri()));
						bg.add(menuItem);
						menuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								setSearchKeyOkiniiri(sr,okini);
							}
						});
						pop.add(menuItem);
					}
					break;
				}
			}
		}
		pop.show(jTree_tree, x, y);
	}

	// 番組追跡の並べ替え
	private void showPopupForSortTraceKey(int x, int y) {
		JPopupMenu pop = new JPopupMenu();
		{
			JMenuItem menuItem = new JMenuItem("番組追跡の並べ替え");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sortTraceKey();
				}
			});
			pop.add(menuItem);
		}
		pop.show(jTree_tree, x, y);
	}

	// キーワード検索の並べ替え
	private void showPopupForSortSearchKey(int x, int y) {
		JPopupMenu pop = new JPopupMenu();
		{
			JMenuItem menuItem = new JMenuItem("キーワード検索の並べ替え");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sortSearchKey();
				}
			});
			pop.add(menuItem);
		}
		pop.show(jTree_tree, x, y);
	}

	// 延長警告の並べ替え
	private void showPopupForSortExtension(int x, int y) {
		JPopupMenu pop = new JPopupMenu();
		{
			JMenuItem menuItem = new JMenuItem("延長警告の並べ替え");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sortExtension();
				}
			});
			pop.add(menuItem);
		}
		pop.show(jTree_tree, x, y);
	}
	
	// キーワードグループの処理
	private void showPopupForRemoveKeywordGrp(int x, int y)
	{
		JPopupMenu pop = new JPopupMenu();
		
		{
			JMenuItem menuItem = new JMenuItem("キーワードグループの追加");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addSearchKeyGroup();
				}
			});
			pop.add(menuItem);
		}
		pop.show(jTree_tree, x, y);
	}
	
	private void showPopupForRemoveKeywordGrpName(int x, int y, final String name)
	{
		JPopupMenu pop = new JPopupMenu();
		
		{
			JMenuItem menuItem = new JMenuItem("キーワードグループの編集【"+name+"】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					editSeachkeyGroup(name);
				}
			});
			pop.add(menuItem);
		}
		
		pop.addSeparator();
		
		{
			JMenuItem menuItem = new JMenuItem("キーワードグループの削除【"+name+"】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeSearchKeyGroup(name);
				}
			});
			pop.add(menuItem);
		}
		pop.show(jTree_tree, x, y);
	}
	
	private void showPopupForRemoveKeywordGrpEntry(int x, int y, final String name, final String member)
	{
		JPopupMenu pop = new JPopupMenu();
		
		{
			JMenuItem menuItem = new JMenuItem("キーワードの編集【"+member+"】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					editSearchKeyGroupItem(name,member);
				}
			});
			pop.add(menuItem);
		}
		{
			JMenuItem menuItem = new JMenuItem("キーワードの削除【"+member+"】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeSearchKeyGroupItem(null,member);
				}
			});
			pop.add(menuItem);
		}
		
		pop.addSeparator();
		
		{
			for ( SearchGroup gr : srGrps ) {
				final String groupName = gr.getName();
				if (srGrps.isFind(groupName,member) ) {
					JMenuItem menuItem = new JMenuItem("キーワードグループから登録解除【"+groupName+"】");
					menuItem.setForeground(Color.RED);
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							removeSearchKeyGroupItem(groupName, member);
						}
					});
					pop.add(menuItem);
				}
				else {
					JMenuItem menuItem = new JMenuItem("キーワードグループに追加【"+groupName+"】");
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							addSearchKeyGroupItem(groupName,member);
						}
					});
					pop.add(menuItem);
				}
			}
		}
		
		pop.show(jTree_tree, x, y);
	}

	// 延長警告アイテムの処理
	private void showPopupForRemoveExtension(int x, int y, final String keyword)
	{
		JPopupMenu pop = new JPopupMenu();
		
		{
			JMenuItem menuItem = new JMenuItem("延長警告の編集【"+keyword+"】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					editExtension(keyword);
				}
			});
			pop.add(menuItem);
		}
		{
			JMenuItem menuItem = new JMenuItem("延長警告の削除【"+keyword+"】");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeExtension(keyword);
				}
			});
			pop.add(menuItem);
		}
		
		pop.show(jTree_tree, x, y);
	}
	
	
	
	/*******************************************************************************
	 * コンポーネント
	 ******************************************************************************/
	
	/*
	 * 特殊部品
	 */
	
	/**
	 * キーワード検索ウィンドウの内部クラス
	 */
	private class VWKeywordDialog extends AbsKeywordDialog {
		
		private static final long serialVersionUID = 1L;

		@Override
		void preview(SearchKey search) {
			previewKeywordSearch(search);
		}
	}

	/**
	 * 延長警告管理ウィンドウの内部クラス
	 */
	private class VWExtensionDialog extends AbsExtensionDialog {

		private static final long serialVersionUID = 1L;

		@Override
		void preview(SearchKey search) {
			previewKeywordSearch(search);
		}
	}
	
	/*
	 * 部品
	 */
	
	private JSplitPane getJSplitPane_view() {
		if ( jSplitPane_view == null ) {
			jSplitPane_view = new JSplitPane() {

				private static final long serialVersionUID = 1L;

				@Override
				public void setDividerLocation(int loc) {
					setDividerEnvs(loc);
					super.setDividerLocation(loc);
				}
			};
			
			jSplitPane_view.setLeftComponent(getJPanel_tree());
			jSplitPane_view.setRightComponent(getJScrollPane_listed());
			setExpandTree();
		}
		return jSplitPane_view;
	}
	
	private JPanel getJPanel_tree() {
		if (jPanel_tree == null) {
			jPanel_tree = new JPanel();
			
			jPanel_tree.setLayout(new BorderLayout());
			jPanel_tree.add(getJScrollPane_tree_top(), BorderLayout.PAGE_START);
			jPanel_tree.add(getJScrollPane_tree(), BorderLayout.CENTER);
		}
		return jPanel_tree;
	}
	
	private JScrollPane getJScrollPane_tree_top() {
		if (jScrollPane_tree_top == null) {
			jScrollPane_tree_top = new JScrollPane();
			jScrollPane_tree_top.setViewportView(getJLabel_tree());
			jScrollPane_tree_top.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			jScrollPane_tree_top.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane_tree_top;
	}
	
	private JTreeLabel getJLabel_tree() {
		if (jLabel_tree == null) {
			jLabel_tree = new JTreeLabel();
			
			Dimension d = jLabel_tree.getMaximumSize();
			d.height = bounds.getBangumiColumnHeight();
			jLabel_tree.setPreferredSize(d);
			//jLabel_tree.setBorder(new LineBorder(Color.BLACK));
			jLabel_tree.setOpaque(true);
			jLabel_tree.setBackground(Color.WHITE);
		}
		return jLabel_tree;
	}
	
	private JScrollPane getJScrollPane_tree() {
		if (jScrollPane_tree == null) {
			jScrollPane_tree = new JScrollPane();

			jScrollPane_tree.setViewportView(getJTree_tree());
		}
		return jScrollPane_tree;
	}
	
	private JDetailPanel getJTextPane_detail() {
		if (jTextPane_detail == null) {
			jTextPane_detail = new JDetailPanel();
			jTextPane_detail.setRows(bounds.getDetailRows());
			//Dimension d = jTextPane_detail.getMaximumSize();
			//d.height = bounds.getDetailAreaHeight();
			//jTextPane_detail.setPreferredSize(d);
			//jTextPane_detail.setVerticalAlignment(JLabel.TOP);
			//jTextPane_detail.setHorizontalAlignment(JLabel.LEFT);
		}
		return jTextPane_detail;
	}
	
	/**
	 * ツリーの作成
	 */
	private JTree getJTree_tree() {
		if (jTree_tree == null) {
			
			// ツリーの作成
			jTree_tree = new JTree();
			jTree_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			jTree_tree.setRootVisible(env.getRootNodeVisible());
			jTree_tree.setCellRenderer(new VWTreeCellRenderer());	// 検索結果が存在するノードの色を変える
			
			// ノードの作成
			jTree_tree.setModel(new DefaultTreeModel(getTreeNodes()));
			
			// ツリーの展開状態の復帰
			undoTreeExpansion();
			
			// ツリーの開閉時に状態を保存する
			jTree_tree.addTreeExpansionListener(tel_nodeexpansion);
			
			// フルスクリーンの時に使う（新聞形式のツリーを自動的に隠す）
			jTree_tree.addMouseListener(ml_treehide);
		}
		return jTree_tree;
	}
	
	/**
	 * ツリーのノード作成
	 */
	private DefaultMutableTreeNode getTreeNodes() {
		
		listRootNode = new VWListedTreeNode(JTreeLabel.Nodes.ROOT.getLabel());

		searchedNode	= new VWListedTreeNode(JTreeLabel.Nodes.SEARCHHIST.getLabel());
		startNode		= new VWListedTreeNode(JTreeLabel.Nodes.START.getLabel());
		endNode			= new VWListedTreeNode(JTreeLabel.Nodes.END.getLabel());
		nowNode			= new VWListedTreeNode(JTreeLabel.Nodes.NOW.getLabel());
		syobocalNode	= new VWListedTreeNode(JTreeLabel.Nodes.SYOBOCAL.getLabel());
		standbyNode		= new VWListedTreeNode(JTreeLabel.Nodes.STANDBY.getLabel());
		traceNode		= new VWListedTreeNode(JTreeLabel.Nodes.TRACE.getLabel());
		keywordNode		= new VWListedTreeNode(JTreeLabel.Nodes.KEYWORD.getLabel());
		keywordGrpNode	= new VWListedTreeNode(JTreeLabel.Nodes.KEYWORDGROUP.getLabel());
		genreNode		= new VWListedTreeNode(JTreeLabel.Nodes.GENRE.getLabel());
		centerListNode	= new VWListedTreeNode(JTreeLabel.Nodes.BCASTLIST.getLabel());
		extensionNode	= new VWListedTreeNode(JTreeLabel.Nodes.EXTENTION.getLabel());

		// ★★★ でふぉるとのーど ★★★
		defaultNode = nowNode;

		listRootNode.add(searchedNode);
		listRootNode.add(startNode);
		listRootNode.add(endNode);
		listRootNode.add(nowNode);
		if ( env.getUseSyobocal() ) {
			listRootNode.add(syobocalNode);
		}
		listRootNode.add(standbyNode);
		listRootNode.add(traceNode);
		listRootNode.add(keywordNode);
		listRootNode.add(keywordGrpNode);
		listRootNode.add(genreNode);
		listRootNode.add(centerListNode);
		listRootNode.add(extensionNode);
		
		// 子の描画
		redrawTreeByGenre();
		redrawTreeBySyobo();
		redrawTreeByStandby();
		redrawTreeByTrace();
		redrawTreeByKeyword();
		redrawTreeByKeywordGroup();
		redrawTreeBySubGenre();
		redrawTreeByCenter();
		redrawTreeByExtension();
		
		return listRootNode;
	}

	private void undoTreeExpansion() {
		
		// 展開状態の復帰
		stopTreeListener();
		
		// ツリーの展開状態の保存場所
		ter = new TreeExpansionReg(jTree_tree, TreeExpRegFile_Listed);
		try {
			ter.load();
		}
		catch (Exception e) {
			MWin.appendMessage(ERRID+"ツリー展開情報の解析で問題が発生しました");
			e.printStackTrace();
		}
		
		// 状態を復元する
		ArrayList<TreePath> tpa = ter.get();
		for ( TreePath path : tpa ) {
			jTree_tree.expandPath(path);
		}
		
		startTreeListener();
	}
	
	private JScrollPane getJScrollPane_listed() {
		if (jScrollPane_listed == null) {
			jScrollPane_listed = new JScrollPane();
			jScrollPane_listed.setRowHeaderView(jTable_rowheader = new JTableRowHeader(rowData));
			jScrollPane_listed.setViewportView(getNETable_listed());
			
			Dimension d = new Dimension(jTable_rowheader.getPreferredSize().width,0);
			jScrollPane_listed.getRowHeader().setPreferredSize(d);
			
			setRowHeaderVisible(env.getRowHeaderVisible());
		}
		return jScrollPane_listed;
	}
	
	private JNETable getNETable_listed() {
		if (jTable_listed == null) {
			
			// カラム名の初期化
			ArrayList<String> cola = new ArrayList<String>();
			for ( ListedColumn lc : ListedColumn.values() ) {
				if ( lc.getIniWidth() >= 0 ) {
					cola.add(lc.getName());
				}
			}
			final String[] colname = cola.toArray(new String[0]);

			//　テーブルの基本的な設定
			tableModel_listed = new ListedTableModel(colname, 0);
			
			jTable_listed = new ListedTable(tableModel_listed, true);
			jTable_listed.setAutoResizeMode(JNETable.AUTO_RESIZE_OFF);
			//jTable_listed.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			// ヘッダのモデル
			rowheaderModel_listed = (DefaultTableModel) jTable_rowheader.getModel();
		
			// ソーターをつける
			final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel_listed);
			jTable_listed.setRowSorter(sorter);
			//sorter.toggleSortOrder(listedTableColumn_Sorter);
			
			sorter.addRowSorterListener(new RowSorterListener() {
				@Override
				public void sorterChanged(RowSorterEvent e) {
					if ( e.getType() == Type.SORTED ) {
						if (rowData.size()>2) setOverlapMark();
					}
				}
			});
			
			// 数値でソートする項目用の計算式（番組長とか）
			final Comparator<String> numcomp = new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					int n1 = -1;
					int n2 = -1;
					if ( o1 != null ) {
						Matcher ma = Pattern.compile("^(\\d+)").matcher(o1);
						if ( ma.find() ) {
							n1 = Integer.valueOf(ma.group(1));
						}
					}
					if ( o2 != null ) {
						Matcher ma = Pattern.compile("^(\\d+)").matcher(o2);
						if ( ma.find() ) {
							n2 = Integer.valueOf(ma.group(1));
						}
					}
					return n1-n2;
				}
			};
			
			// ソーターの効かない項目用の計算式（重複マーク）
			final Comparator<String> noncomp = new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return 0;
				}
			};
			
			// 数値比較を行う列
			sorter.setComparator(jTable_listed.getColumn(ListedColumn.LENGTH.getName()).getModelIndex(),numcomp);
			sorter.setComparator(jTable_listed.getColumn(ListedColumn.SCORE.getName()).getModelIndex(),numcomp);
			sorter.setComparator(jTable_listed.getColumn(ListedColumn.THRESHOLD.getName()).getModelIndex(),numcomp);

			sorter.setComparator(jTable_listed.getColumn(ListedColumn.DUPMARK.getName()).getModelIndex(),noncomp);

			// 予約済みマーク／重複マークはちょっとだけ表示の仕方が違う
			VWColorCharCellRenderer renderer = new VWColorCharCellRenderer();
			if ( CommonUtils.isMac() ) renderer.setMacMarkFont();
			jTable_listed.getColumn(ListedColumn.RSVMARK.getName()).setCellRenderer(renderer);
			jTable_listed.getColumn(ListedColumn.DUPMARK.getName()).setCellRenderer(renderer);
			
			// 強調色関連
			titleCellRenderer = new VWColorCharCellRenderer2();
			jTable_listed.getColumn(ListedColumn.OPTIONS.getName()).setCellRenderer(titleCellRenderer);
			jTable_listed.getColumn(ListedColumn.TITLE.getName()).setCellRenderer(titleCellRenderer);
			
			this.setMatchedKeywordColor(env.getMatchedKeywordColor());
			this.setRsvdLineColor((env.getRsvdLineEnhance())?(env.getRsvdLineColor()):(null));
			this.setPickedLineColor((env.getRsvdLineEnhance())?(env.getPickedLineColor()):(null));
			this.setCurrentLineColor((env.getCurrentLineEnhance())?(env.getCurrentLineColor()):(null));

			// スコア・閾値はちょっとだけ表示の仕方が違う
			DefaultTableCellRenderer renderer3 = new DefaultTableCellRenderer();
			renderer3.setHorizontalAlignment(SwingConstants.RIGHT);
			jTable_listed.getColumn(ListedColumn.SCORE.getName()).setCellRenderer(renderer3);
			jTable_listed.getColumn(ListedColumn.THRESHOLD.getName()).setCellRenderer(renderer3);
			
			// 詳細表示はうんぬんかんぬん
			VWDetailCellRenderer renderer4 = new VWDetailCellRenderer();
			jTable_listed.getColumn(ListedColumn.DETAIL.getName()).setCellRenderer(renderer4);

			// 各カラムの幅を設定する
			DefaultTableColumnModel columnModel = (DefaultTableColumnModel)jTable_listed.getColumnModel();
			TableColumn column = null;
			for ( ListedColumn lc : ListedColumn.values() ) {
				if ( lc.getIniWidth() < 0 ) {
					continue;
				}
				column = columnModel.getColumn(lc.ordinal());
				column.setPreferredWidth(bounds.getListedColumnSize().get(lc.toString()));	// toString()!
			}
			
			// マーク表示分離の有無
			setMarkColumnVisible(env.getSplitMarkAndTitle());
			
			// 番組詳細の表示・非表示
			setDetailColumnVisible(env.getShowDetailOnList());
			
			//　行を選択すると詳細が表示されるようにする
			jTable_listed.getSelectionModel().addListSelectionListener(lsSelectListner);
			
			// マウスクリックでメニュー表示
			jTable_listed.addMouseListener(lsClickAdapter);
		}
		return jTable_listed;
	}
	
	
	
	
	/*******************************************************************************
	 * 独自部品
	 ******************************************************************************/
	
	/**
	 *  テーブルの行データの構造
	 * @see ListedColumn
	 */
	private class ListedItem extends RowItem implements Cloneable {
		String rsvmark;
		String dupmark;
		String prefix;
		String title;
		String searchlabel;
		String okiniiri;
		String score;
		String threshold;
		
		String hide_rsvmarkcolor;

		ProgDetailList tvd;

		@Override
		protected void myrefresh(RowItem o) {
			ListedItem c = (ListedItem) o;
			c.addData(rsvmark);
			c.addData(dupmark);
			c.addData(tvd.center);
			c.addData(prefix);
			c.addData(title);		// "\0"+title or "\0"+titlebefore+"\0"+matchedkeyword+"\0"+titleafter みたいな感じで
			c.addData(tvd.detail);
			c.addData(tvd.start);
			c.addData(tvd.end);
			c.addData(tvd.recmin);
			c.addData(tvd.genre.toString());
			c.addData(searchlabel);
			c.addData(okiniiri);
			c.addData(score);
			c.addData(threshold);
		}
		
		public ListedItem clone() {
			return (ListedItem) super.clone();
		}
	}
	
	/**
	 * {@link ListedItem}を使ったJTable拡張
	 */
	private class ListedTable extends JNETable {

		private static final long serialVersionUID = 1L;

		private Color passedColor = new Color(180,180,180);
		
		// futuer use.
		public void setPassedColor(Color c) { passedColor = c; }

		private Color currentColorEven = new Color(240,120,120);
		private Color currentColorOdd = new Color(248,128,128);

		public void setCurrentColor(Color c) {
			if ( c == null ) {
				currentColorEven = null;
				currentColorOdd = null;
			}
			else {
				currentColorOdd = c;
				currentColorEven = new Color(
						((c.getRed()>=247)?(255):(c.getRed()+8)),
						((c.getGreen()>=247)?(255):(c.getGreen()+8)),
						((c.getBlue()>=247)?(255):(c.getBlue()+8))
						);
			}
		}

		private Color nextweekFgColor = new Color(120,120,120);
		
		// futuer use.
		public void setNextweekFgColor(Color c) { nextweekFgColor = c; }

		private Color reservedColorEven = new Color(255,247,204);
		private Color reservedColorOdd = new Color(255,255,212);

		public void setReservedColor(Color c) {
			if ( c == null ) {
				reservedColorEven = null;
				reservedColorOdd = null;
			}
			else {
				reservedColorOdd = c;
				reservedColorEven = new Color(
						((c.getRed()>=247)?(255):(c.getRed()+8)),
						((c.getGreen()>=247)?(255):(c.getGreen()+8)),
						((c.getBlue()>=247)?(255):(c.getBlue()+8))
						);
			}
		}
		
		private Color pickedColorEven = new Color(51,255,0);
		private Color pickedColorOdd = new Color(59,255,8);

		public void setPickedColor(Color c) {
			if ( c == null ) {
				pickedColorEven = null;
				pickedColorOdd = null;
			}
			else {
				pickedColorEven = c;
				pickedColorOdd = new Color(
						((c.getRed()>=247)?(255):(c.getRed()+8)),
						((c.getGreen()>=247)?(255):(c.getGreen()+8)),
						((c.getBlue()>=247)?(255):(c.getBlue()+8))
						);
			}
		}
		
		private int prechkrow = -1;
		private boolean prechkreserved = false;
		private boolean prechkpicked = false;
		private boolean prechkpassed = false;
		private boolean prechkcurrent = false;
		private boolean prechknextweek = false;
		
		@Override
		public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
			Component comp = super.prepareRenderer(tcr, row, column);
			Color fgColor = null;
			Color bgColor = null;
			if(isRowSelected(row)) {
				fgColor = this.getSelectionForeground();
				bgColor = this.getSelectionBackground();
			}
			else {
				isRowPassed(row);
				
				fgColor = (prechknextweek)?(nextweekFgColor):(this.getForeground());
				
				if( prechkpassed && passedColor != null ) {
					bgColor = passedColor;
				}
				else if( prechkreserved && reservedColorEven != null ) {
					bgColor = (isSepRowColor && row%2 == 1)?(reservedColorEven):(reservedColorOdd);
				}
				else if( prechkpicked && pickedColorEven != null ) {
					bgColor = (isSepRowColor && row%2 == 1)?(pickedColorEven):(pickedColorOdd);
				}
				else if( prechkcurrent && currentColorEven != null ) {
					bgColor = (isSepRowColor && row%2 == 1)?(currentColorEven):(currentColorOdd);
				}
				else {
					bgColor = (isSepRowColor && row%2 == 1)?(evenColor):(super.getBackground());
				}
			}
			if ( tcr instanceof VWColorCharCellRenderer2 ) {
				((VWColorCharCellRenderer2) tcr).setForeground(fgColor);
			}
			else if ( ! (tcr instanceof VWColorCharCellRenderer) && ! (tcr instanceof VWColorCellRenderer)) {
				// マーク類は除外
				comp.setForeground(fgColor);
			}		
			if ( ! (tcr instanceof VWColorCellRenderer)) {
				comp.setBackground(bgColor);
			}
			return comp;
		}
		
		// 直接rowDataを見に行くようになったから、このisRowPassed()はもういらないんじゃ…
		
		// 連続して同じ行へのアクセスがあったら計算を行わず前回のままにする
		private boolean isRowPassed(int prow) {
			
			if(prechkrow == prow) {
				return true;
			}

			int row = this.convertRowIndexToModel(prow);
			ListedItem c = rowData.get(row);

			prechkrow = prow;
			
			prechkreserved  = false;
			prechkpicked = false;
			prechkpassed = false;
			prechkcurrent = false;
			prechknextweek = false;
			
			{
				// 予約が入っているか否か
				if ( c.rsvmark == null || c.rsvmark.length() == 0 ) {
					//
				}
				else if ( c.rsvmark.equals(RSVMARK_NOEXEC)  ) {
					//
				}
				else if ( c.rsvmark.equals(RSVMARK_PICKUP) ) {
					prechkpicked = true;
				}
				else {
					prechkreserved = true;
				}
			}
			{
				// 終了済みの番組か否か
				String cDT = CommonUtils.getDateTime(0);
				prechkpassed = (cDT.compareTo(c.tvd.endDateTime) >= 0);
				if ( ! prechkpassed ) {
					// 現在放送中
					prechkcurrent = (cDT.compareTo(c.tvd.startDateTime) >= 0);
				}
				if ( ! prechkcurrent ) {
					// 来週かな
					String critDT = CommonUtils.getCritDateTime(7);
					prechknextweek = (critDT.compareTo(c.tvd.startDateTime) <= 0);
				}
			}
			
			return true;
		}
		
		//
		@Override
		public void tableChanged(TableModelEvent e) {
			reset();
			super.tableChanged(e);
		}
		
		private void reset() {
			prechkrow = -1;
			prechkreserved = false;
			prechkpicked = false;
			prechkpassed = false;
		}
		
		/*
		 * コンストラクタ
		 */
		public ListedTable(boolean b) {
			super(b);
			reset();
		}
		public ListedTable(TableModel d, boolean b) {
			super(d,b);
			reset();
		}
	}
	
	/**
	 *  ソートが必要な場合はTableModelを作る。ただし、その場合Viewのrowがわからないので行の入れ替えが行えない
	 * @see ListedTable
	 */
	private class ListedTableModel extends DefaultTableModel {
		
		private static final long serialVersionUID = 1L;

		@Override
		public Object getValueAt(int row, int column) {
			// 多少負荷があがるがこっちの方が見通しがいいだろう
			ListedItem c = rowData.get(row);
			if ( c.size()>column ) {
				// 特殊なカラム
				if ( column == ListedColumn.RSVMARK.getColumn() ) {
					if ( c.rsvmark.length() > 0 ) {
						return c.rsvmark+"\0"+c.hide_rsvmarkcolor;
					}
					else {
						return "";
					}
				}
				else if ( column == ListedColumn.DUPMARK.getColumn() ) {
					return c.dupmark+"\0"+DUPMARK_COLOR;
				}
				else if ( column == ListedColumn.START.getColumn() ) {
					return c.tvd.accurateDate+" "+c.tvd.start;
				}
				else if ( column == ListedColumn.LENGTH.getColumn() ) {
					return c.tvd.recmin+"m";
				}
				else if ( column == ListedColumn.GENRE.getColumn() ) {
					if ( c.tvd.subgenre != null ) {
						return c.tvd.genre.toString()+" - "+c.tvd.subgenre.toString();
					}
					else {
						// サブジャンルに非対応な番組表の場合
						return c.tvd.genre.toString();
					}
				}
				else if ( column == ListedColumn.OPTIONS.getColumn() && ! env.getSplitMarkAndTitle() ) {
					// オプション分離がＯＦＦです
					return "";
				}
				else if ( column == ListedColumn.TITLE.getColumn() && ! env.getSplitMarkAndTitle() ) {
					// オプション分離がＯＦＦです
					return c.prefix+c.title;
				}
				return c.get(column);
			}
			return null;
		}
		
		@Override
		public int getRowCount() {
			return rowData.size();
		}

		public ListedTableModel(String[] colname, int i) {
			super(colname,i);
		}
		
	}

}
