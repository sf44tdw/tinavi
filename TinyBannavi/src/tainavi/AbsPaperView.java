package tainavi;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SpringLayout;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultTreeModel;

import tainavi.TVProgram.ProgSubtype;
import tainavi.TVProgram.ProgType;
import tainavi.TVProgramIterator.IterationType;
import tainavi.VWMainWindow.MWinTab;


/**
 * 新聞形式タブのクラス
 * @since 3.15.4β　{@link Viewer}から分離
 */
public abstract class AbsPaperView extends JPanel implements TickTimerListener,HDDRecorderListener {

	private static final long serialVersionUID = 1L;

	public static String getViewName() { return "新聞形式"; }
	
	public void setDebug(boolean b) { debug = b; }
	private static boolean debug = false;
	
	
	/*******************************************************************************
	 * 抽象メソッド
	 ******************************************************************************/
	
	protected abstract Env getEnv();
	protected abstract Bounds getBoundsEnv();
	protected abstract PaperColorsMap getPaperColorMap();
	protected abstract ChannelSort getChannelSort();
	
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
	protected abstract void updateReserveDisplay();
	
	/**
	 * ピックアップに追加してほしい
	 */
	protected abstract void addToPickup(final ProgDetailList tvd);
	
	protected abstract boolean isTabSelected(MWinTab tab);
	protected abstract void setSelectedTab(MWinTab tab);

	protected abstract boolean isFullScreen();
	/**
	 * ページャーコンボボックスを更新してほしい
	 */
	protected abstract void setPagerEnabled(boolean b);
	protected abstract int getPagerCount();
	protected abstract int getSelectedPagerIndex();
	protected abstract void setSelectedPagerIndex(int idx);
	protected abstract void setPagerItems(TVProgramIterator pli, int curindex);

	protected abstract String getExtensionMark(ProgDetailList tvd);
	protected abstract String getOptionMark(ProgDetailList tvd);
	protected abstract String getPostfixMark(ProgDetailList tvd);

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
	private final PaperColorsMap pColors = getPaperColorMap();
	private final ChannelSort chsort = getChannelSort();
	
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

	private final String MSGID = "["+getViewName()+"] ";
	private final String ERRID = "[ERROR]"+MSGID;
	private final String DBGID = "[DEBUG]"+MSGID;

	private final int DASHBORDER_LENGTH = 6;	// ダッシュの長さ
	private final int DASHBORDER_SPACE = 4;		// ダッシュの間隔
	
	private static final String TreeExpRegFile_Paper = "env"+File.separator+"tree_expand_paper.xml";
	
	private static final int TIMEBAR_START = Viewer.TIMEBAR_START;

	//
	private static final String TUNERLABEL_PICKUP = "PICKUP";
	
	// 定数ではないが
	
	/**
	 * 現在時刻追従スクロールで日付がかわったかどうかを確認するための情報を保持する
	 */
	private String prevDT4Now = CommonUtils.getDate529(0,true);
	private String prevDT4Tree = prevDT4Now;
	
	/**
	 * 番組枠フレームバッファのサイズ
	 */
	private int framebuffersize = 512;
	
	
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
	private JScrollPane jScrollPane_space_main = null;
	private JLayeredPane jLayeredPane_space_main_view = null;
	private ArrayList<JTaggedLayeredPane> jLayeredPane_space_main_view_byDate = null;
	private JLayeredPane jLayeredPane_space_main_view_byMakeshift = null; 
	private JPanel jPanel_space_top_view = null;
	private JPanel jPanel_space_side_view = null;
	private JViewport vport = null;
	
	private final JTimeline jLabel_timeline = new JTimeline();
	
	private DefaultMutableTreeNode paperRootNode = null;	// 新聞形式のツリー
	private DefaultMutableTreeNode dateNode = null;
	private DefaultMutableTreeNode dgNode = null;
	private DefaultMutableTreeNode bsNode = null;
	private DefaultMutableTreeNode csNode = null;
	private DefaultMutableTreeNode centerNode = null;
	private DefaultMutableTreeNode passedNode = null;

	private DefaultMutableTreeNode defaultNode = null;

	// コンポーネント以外
	
	// 番組枠をしまっておくバッファ（newが遅いので一回作ったら捨てない）
	private ArrayList<JTXTButton> frameUsed = new ArrayList<JTXTButton>();			// 画面に表示されている番組枠
	private ArrayList<JTXTButton> frameUnused = new ArrayList<JTXTButton>();		// 未使用の予備
	private ArrayList<JTXTButton> frameUsedByDate = new ArrayList<JTXTButton>();	// 高速描画時の日付別ペーンに表示されている番組枠。高速描画時も、過去ログはframeUsedが使われる

	// 予約枠をしまっておくバッファ（検索用）
	private ArrayList<JRMLabel> reserveBorders = new ArrayList<JRMLabel>();
	
	// ツリーの展開状態の保存場所
	TreeExpansionReg ter = null;
	
	DefaultMutableTreeNode nowNode = null;
	
	// 現在放送中のタイマー
	private boolean timer_now_enabled = false;
	
	private IterationType cur_tuner = null;
	
	
	// 予約待機枠と番組枠
	private final DashBorder dborder = new DashBorder(Color.RED,env.getMatchedBorderThickness(),DASHBORDER_LENGTH,DASHBORDER_SPACE);
	private final LineBorder lborder = new ChippedBorder(Color.BLACK,1);
	
	private float paperHeightZoom = 1.0F;
	
	/**
	 * 現在時刻線のオブジェクト
	 */
	private class JTimeline extends JLabel {
		
		private static final long serialVersionUID = 1L;
		
		private int minpos = 0;
		
		public int setMinpos(int x, int minpos, float multiplier) {
			if ( minpos >= 0 ) {
				this.minpos = minpos;
			}
			
			int timeline = Math.round(this.minpos*multiplier);
			this.setLocation(x,timeline);
			
			return timeline;
		}
	}
	
	/*******************************************************************************
	 * コンストラクタ
	 ******************************************************************************/
	
	public AbsPaperView() {
		
		super();
		
		this.setLayout(new BorderLayout());
		this.add(getJTextPane_detail(), BorderLayout.PAGE_START);
		this.add(getJSplitPane_view(), BorderLayout.CENTER);
		
		// タブが開いたり閉じたりしたときの処理
		this.addComponentListener(cl_shownhidden);
	}
	
	
	
	/*******************************************************************************
	 * アクション
	 ******************************************************************************/
	
	// 主に他のクラスから呼び出されるメソッド
	
	public String getFrameBufferStatus() { return String.format("%d/%d",frameUsed.size(),framebuffersize); }

	/**
	 * 現在日時表示にリセット
	 */
	public void jumpToNow() {
		if ( nowNode != null ) {
			TreePath tp = new TreePath(nowNode.getPath());
			jTree_tree.setSelectionPath(null);
			jTree_tree.setSelectionPath(tp);
		}
	}
	
	/**
	 * リスト形式・本体予約一覧からの目的の番組へジャンプ
	 */
	public boolean jumpToBangumi(String center, String startdt) {
		
		// ページャーは効くよ
		if ( env.isPagerEnabled() ) {
			setPagerEnabled(true);
		}
		
		// タイマーは止める
		stopTimer();

		// 日付群
		GregorianCalendar c = CommonUtils.getCalendar(startdt);
		String adate = CommonUtils.getDate(c);
		String atime = CommonUtils.getTime(c);
		String adate529 = CommonUtils.getDate529(c,true);
		
		// 指定日付に移動して放送局の位置を確認する
		TVProgramIterator pli = redrawByDateWithCenter(center,adate529);
		if ( pli == null ) {
			// どちらにもない
			MWin.appendError(ERRID+"ジャンプ先の日付がみつかりません: "+adate529);
			ringBeep();
			return false;
		}
		
		// 新聞形式に移動
		if ( ! isTabSelected(MWinTab.PAPER) ) {
			setSelectedTab(MWinTab.PAPER);
		}
			
		// 横の列
		int crindex = pli.getIndex(center);
		if ( crindex == -1 ) {
			MWin.appendError(ERRID+"「"+center+"」は有効な放送局ではありません");
			ringBeep();
			return false;
		}
		
		int x = 0;
		if ( env.isPagerEnabled() ) {
			int idx = env.getPageIndex(1+crindex);
			x = (crindex - idx*env.getCenterPerPage()) * bounds.getBangumiColumnWidth();
		}
		else {
			x = crindex * bounds.getBangumiColumnWidth();
		}
		
		// 縦の列
		int h = 0;
		int m = 0;
		int y = 0;
		Matcher ma = Pattern.compile("^(\\d\\d):(\\d\\d)$").matcher(atime);
		if (ma.find()) {
			h = Integer.valueOf(ma.group(1));
			m = Integer.valueOf(ma.group(2));
		}
		if (adate529.equals(adate)) {
			if (h < TIMEBAR_START) {
				h = TIMEBAR_START;
				m = 0;
			}
		}
		else {
			h += 24;
		}
		y = Math.round((float)((h-TIMEBAR_START)*60+m)*bounds.getPaperHeightMultiplier()*paperHeightZoom);
		
		// 新聞面を移動する
		{
			// Viewのサイズ変更をJavaまかせにすると実際に表示されるまで変更されないので明示的に変更しておく
			Dimension dm = vport.getView().getPreferredSize();
			vport.setViewSize(dm);
			
			// 一旦位置情報をリセットする
			Point pos = new Point(0, 0);
			//vport.setViewPosition(pos);
			
			Rectangle ra = vport.getViewRect();
			pos.x = x + bounds.getBangumiColumnWidth()/2 - ra.width/2;
			pos.y = y - ra.height/4;
			
			// ViewのサイズがViewPortのサイズより小さい場合はsetViewPosition()が正しく動作しないので０にする
			if (pos.x < 0 || dm.width < ra.width) {
				pos.x=0;
			}
			else if ((dm.width - ra.width) < pos.x) {
				pos.x = dm.width - ra.width;
			}
			
			if (pos.y < 0 || dm.height < ra.height)  {
				pos.y=0;
			}
			else if ((dm.height - ra.height) < pos.y) {
				pos.y = dm.height - ra.height;
			}
			
			vport.setViewPosition(pos);
		}
		
		// マウスカーソルを移動する
		{
			Point sc = vport.getLocationOnScreen();
			Point pos = vport.getViewPosition();
			
			Point loc = new Point();
			loc.x = sc.x + (x + bounds.getBangumiColumnWidth()/2) - pos.x;
			loc.y = sc.y + (y + Math.round(5*bounds.getPaperHeightMultiplier()*paperHeightZoom)) - pos.y;
			
			try {
				Robot robo = new Robot();
				robo.mouseMove(loc.x,loc.y);
				robo = null;
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	/**
	 * ページャーによる再描画
	 */
	public boolean redrawByPager() {
		 
		//JTreeLabel.Nodes node = jLabel_tree.getNode();
		String value = jLabel_tree.getValue();

		if ( value != null ) {
			if ( JTreeLabel.Nodes.NOW.getLabel().equals(value) ) {
				redrawByNow(cur_tuner);
			}
			else {
				redrawByDate(value, cur_tuner);
			}

			return true;
		}
		
		return false;
	}
	
	/**
	 * 予約待機赤枠の描画（全部）
	 * @see #putReserveBorder(String, String, int)
	 */
	public void updateReserveBorder(String center) {
		
		// 予約の赤枠を表示する（上：日付別表示中、下：放送局別表示中）
		
		JTreeLabel.Nodes node = jLabel_tree.getNode();
		String value = jLabel_tree.getValue();
		
		switch ( node ) {
		case DATE:
		case TERRA:
		case BS:
		case CS:
			{
				// 日付別は４種類ある
				IterationType sTyp;
				switch ( node ) {
				case TERRA:
					sTyp = IterationType.TERRA;
					break;
				case BS:
					sTyp = IterationType.BS;
					break;
				case CS:
					sTyp = IterationType.CS;
					break;
				default:
					sTyp = IterationType.ALL;
					break;
				}
				
				// "現在日付"を現在日付にする
				String dt = value;
				if ( JTreeLabel.Nodes.NOW.getLabel().equals(dt) ) {
					dt = CommonUtils.getDate529(0,true);
				}
				
				TVProgramIterator pli = tvprograms.getIterator().build(chsort.getClst(), sTyp);
				
				// ページャーが有効なら表示すべきページ番号を取得する
				int colmin = 0;
				int colmax = pli.size();
				int divider = 0;
				if ( env.isPagerEnabled() ) {
					int selectedpage = getSelectedPagerIndex();	// 予約枠の描画なのだから、ページ移動の必要はないはずだ
					if ( selectedpage >= 0 ) {
						colmin = env.getCenterPerPage() * selectedpage;
						colmax = colmin + env.getCenterPerPage()-1;
						divider = env.getCenterPerPage();
					}
					else {
						StWin.appendError(ERRID+"ページャーコンボボックスが不正です： "+selectedpage);
						return;
					}
				}
	
				if ( center != null ) {
					// 特定の放送局のみ更新
					int cnt = pli.getIndex(center);
					if ( colmin <= cnt && cnt <= colmax ) {
						int col = (divider==0) ? (cnt) : (cnt % divider);
						putReserveBorder(dt, center, col);
					}
				}
				else {
					// すべての放送局を更新
					int cnt = -1;
					for ( ProgList pl : pli ) {
						++cnt;
						if ( cnt < colmin ) {
							continue;
						}
						else if ( cnt > colmax ) {
							break;
						}
						
						int col = (divider==0) ? (cnt) : (cnt % divider);
						
						putReserveBorder(dt, pl.Center, col);
					}
				}
			}
			break;
			
		case BCAST:
			{
				
				if (center != null && ! center.equals(value)) {
					// 更新の必要はない
					return;
				}
				if (center == null) {
					// 選択中の放送局で更新する
					center = value;
				}
				
				TVProgramIterator pli = tvprograms.getIterator().build(chsort.getClst(), IterationType.ALL);
				int cnt = tvprograms.getIterator().getIndex(center);
				if ( cnt == -1 ) {
					MWin.appendError(ERRID+"「"+center+"」は有効な放送局ではありません");
				}
				else {
					ProgList pl = pli.getP();
					for (int col=0; col<pl.pdate.size(); col++) {
						// 予約の赤枠を一週間分表示する
						putReserveBorder(pl.pdate.get(col).Date, center, col);
					}
				}
			}
			break;
		
		default:
			break;
		}
	}
	
	/**
	 * 
	 */
	public void updateBangumiColumns() {
		for (JTXTButton b : frameUsed ) {
			ProgDetailList tvd = b.getInfo();
			if ( tvd.type == ProgType.PROG ) {
				if (bounds.getShowMatchedBorder() && b.isStandby()) {
					if ( b.getBorder() != dborder )
						b.setBorder(dborder);
				}
				else {
					if ( b.getBorder() != lborder )
						b.setBorder(lborder);
				}
			}
		}
	}
	
	/**
	 * 予約待機赤枠の描画（ツールバーからのトグル操作）
	 */
	public boolean toggleMatchBorder() {
		
		// 状態を保存
		bounds.setShowMatchedBorder( ! bounds.getShowMatchedBorder());
		
		_updPBorders(env, bounds, frameUsed);
		
		if ( env.getDrawcacheEnable() ) {
			_updPBorders(env, bounds, frameUsedByDate);
		}
		
		return bounds.getShowMatchedBorder();
	}

	
	/**
	 * 新聞枠の拡縮（ツールバーからの操作）
	 */
	public void setZoom(int n) {
		paperHeightZoom = n * 0.01F;
		updateBounds(env, bounds);
		updateRepaint();
	}

	/**
	 * 新聞ペーンをクリアする。
	 */
	public void clearPanel() {
		
		// 番組枠の初期化
		/* 移動しました */
		
		// 予約枠の初期化
		for ( JRMLabel b : reserveBorders) {
			jLayeredPane_space_main_view.remove(b);
		}
		reserveBorders.clear();
		
		// タイムラインの削除
		if (jLabel_timeline != null && jLayeredPane_space_main_view != null) {
			jLayeredPane_space_main_view.remove(jLabel_timeline);
		}
		
		// 時間枠・日付枠・放送局枠の初期化
		jPanel_space_top_view.removeAll();
		redrawTimebar(jPanel_space_side_view);
		
		// 選択してないことにする
		//paper.jLabel_tree.setText("");
	}
	
	/**
	 * <P>新聞ペーンを選択する。
	 * <P>高速描画ＯＮの場合は、主ペーンのほかに複数の日付別ペーンが作成されるのでどれを利用するか選択する。
	 * @param pane : nullの場合、主ペーンを選択する。過去ログは常にnullで。
	 * @see #jLayeredPane_space_main_view_byMakeshift 主ペーン
	 * @see #jLayeredPane_space_main_view_byDate 日付別ペーン
	 */
	private void selectMainView(JLayeredPane pane) {
		
		// 表示開始位置を記憶する
		Point p = vport.getViewPosition();
		
		if (pane == null) {
			// 番組枠の初期化
			StdAppendMessage(MSGID+"番組枠描画バッファをリセット: "+frameUsed.size()+"/"+framebuffersize);
			for (int i=frameUsed.size()-1; i>=0; i--) {
				JTXTButton b = frameUsed.remove(i);
				b.setToolTipText(null);
				b.clean();
				frameUnused.add(b);
				//jLayeredPane_space_main_view_byMakeshift.remove(b);	// 削除しちゃダメよ？
			}
			
			if (jLayeredPane_space_main_view == jLayeredPane_space_main_view_byMakeshift) {
				return;
			}
			jScrollPane_space_main.setViewportView(jLayeredPane_space_main_view = jLayeredPane_space_main_view_byMakeshift);
		}
		else {
			if (jLayeredPane_space_main_view == pane) {
				return;
			}
			jScrollPane_space_main.setViewportView(jLayeredPane_space_main_view = pane);
		}
		
		// 表示開始位置を戻す
		vport.setViewPosition(p);
	}
	
	/**
	 * 現在時刻追従スクロールを開始する
	 */
	private void startTimer() {
		timer_now_enabled = true;
	}
	
	/**
	 * 現在時刻追従スクロールを停止する
	 */
	private boolean stopTimer() {
		prevDT4Now = null;
		jLabel_timeline.setVisible(false);
		return (timer_now_enabled = false);
	}
	
	/**
	 * サイドツリーの「現在日時」を選択する
	 */
	public void selectTreeDefault() {
		if ( defaultNode != null ) jTree_tree.setSelectionPath(new TreePath(defaultNode.getPath()));
	}
	
	/**
	 * サイドツリーの現在選択中のノードを再度選択して描画しなおす
	 */
	public void reselectTree() {
		String[] names = new String[] { jLabel_tree.getNode().getLabel(), jLabel_tree.getValue() };
		TreeNode[] nodes = ter.getSelectedPath(paperRootNode, names, 0);
		if (nodes != null) {
			TreePath tp = new TreePath(nodes);
			if ( tp != null ) {
				// 表示位置を記憶
				Point vp = vport.getViewPosition(); //= SwingUtilities.convertPoint(vport,0,0,label);
				// ツリー再選択
				jTree_tree.setSelectionPath(null);
				jTree_tree.setSelectionPath(tp);
				// 表示位置を復帰
				if (vp.x != 0 && vp.y != 0) {
					jLayeredPane_space_main_view.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
				}
			}
		}
	}
	
	/**
	 * サイドツリーを開く
	 */
	public void setExpandTree() {
		jSplitPane_view.setDividerLocation(bounds.getTreeWidthPaper());
		jScrollPane_tree.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane_tree.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
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
	 * 画面上部の番組詳細領域の表示のＯＮ／ＯＦＦ
	 */
	public void setDetailVisible(boolean aFlag) {
		jTextPane_detail.setVisible(aFlag);
	}
	
	/**
	 * スクリーンショット用
	 */
	public Component getCenterPane() {
		return jPanel_space_top_view;
	}
	
	/**
	 * スクリーンショット用
	 */
	public Component getTimebarPane() {
		return jPanel_space_side_view;
	}
	
	/**
	 * スクリーンショット用
	 */
	public Component getCurrentPane() {
		return jLayeredPane_space_main_view;
	}
	
	/**
	 * スクリーンショット用
	 */
	public String getCurrentView() {
		return jLabel_tree.getView();
	}
	
	/**
	 * 高速描画ＯＮの場合に日付別ペーンを一気に全部描画する
	 */
	public void buildMainViewByDate() {
		
		if (env.getDebug()) System.out.println(DBGID+"CALLED buildMainViewByDate()");
		
		if (jLayeredPane_space_main_view_byMakeshift == null) {
			jLayeredPane_space_main_view_byMakeshift = new JLayeredPane();
			for (int i=0; i<framebuffersize; i++) {
				JTXTButton b2 = new JTXTButton();
				b2.clean();
				jLayeredPane_space_main_view_byMakeshift.add(b2);
				jLayeredPane_space_main_view_byMakeshift.setLayer(b2, 0);
				
				// リスナーを設定する
				b2.addMouseListener(ml_risepopup);
				b2.addMouseMotionListener(ml_risepopup);
				frameUnused.add(b2);
			}
			StdAppendMessage(MSGID+"番組枠描画バッファを初期化: "+framebuffersize);
		}
		
		// ページャー機能とは排他
		if (env.isPagerEnabled() || ! env.getDrawcacheEnable()) {
			jLayeredPane_space_main_view_byDate = null;
			return;
		}
		
		jLayeredPane_space_main_view_byDate = new ArrayList<JTaggedLayeredPane>();
		frameUsedByDate = new ArrayList<JTXTButton>();
		new SwingBackgroundWorker(true) {
			
			@Override
			protected Object doWorks() throws Exception {
				//
				int dogDays = (env.getExpandTo8())?(8):(7);
				//
				for ( int y=0; y < dogDays; y++ ) {
					jLayeredPane_space_main_view_byDate.add(new JTaggedLayeredPane());
				}
				for ( int y=0; y < dogDays; y++ ) {
					String day = CommonUtils.getDate529(y*86400,true);
					
					jLayeredPane_space_main_view_byDate.get(y).setTagstr(day);
					
					StWin.appendMessage(MSGID+"番組表を構築します："+day);
					redrawByDate(day,IterationType.ALL);
				}
				return null;
			}
			
			@Override
			protected void doFinally() {
			}
		}.execute();
	}

	/**
	 * ツリーのリスナーを止める
	 */
	private void stopTreeListener() {
		jTree_tree.removeTreeSelectionListener(tsl_nodeselected);
	}
	
	/**
	 * ツリーのリスナーを動かす
	 */
	private void startTreeListener() {
		jTree_tree.addTreeSelectionListener(tsl_nodeselected);
	}
	
	/**
	 * 日付でサブノード作成
	 */
	public  void redrawTreeByDate() {
		
		stopTreeListener();
		TreePath tp = jTree_tree.getSelectionPath();
		
		_redrawTreeByDate(dateNode);
		_redrawTreeByDate(dgNode);
		_redrawTreeByDate(bsNode);
		_redrawTreeByDate(csNode);
		
		jTree_tree.setSelectionPath(tp);
		jTree_tree.updateUI();
		startTreeListener();
	}
	
	private void _redrawTreeByDate(DefaultMutableTreeNode parent) {
		
		// ★★★ でふぉるとのーど ★★★
		DefaultMutableTreeNode nNode = new DefaultMutableTreeNode(JTreeLabel.Nodes.NOW.getLabel());
		if ( parent == dateNode ) {
			nowNode = nNode;
			defaultNode = nNode;
		}
		
		parent.removeAllChildren();
		parent.add(nNode);
		int dogDays = (env.getExpandTo8())?(8):(7);
		for ( int i=0; i<dogDays; i++ ) {
			parent.add(new DefaultMutableTreeNode(CommonUtils.getDate529(i*86400, true)));
		}
	}

	/**
	 * 放送局名でサブノード作成
	 */
	public void redrawTreeByCenter() {

		stopTreeListener();
		TreePath tp = jTree_tree.getSelectionPath();
		
		centerNode.removeAllChildren();
		TVProgramIterator pli = tvprograms.getIterator();
		pli.build(chsort.getClst(), IterationType.ALL);
		for ( ProgList pl : pli ) {
			centerNode.add(new DefaultMutableTreeNode(pl.Center));
		}
		
		jTree_tree.setSelectionPath(tp);
		jTree_tree.updateUI();
		startTreeListener();
	}

	/**
	 * 過去ログ日付でサブノード作成
	 */
	public void redrawTreeByPassed() {

		stopTreeListener();
		TreePath tp = jTree_tree.getSelectionPath();
		
		passedNode.removeAllChildren();
		if ( env.getUsePassedProgram() ) {
			String[] dd = new PassedProgram().getDateList(env.getPassedLogLimit());
			for ( int i=1; i<dd.length && i<=env.getPassedLogLimit(); i++ ) {
				passedNode.add(new DefaultMutableTreeNode(dd[i]));
			}
		}
		
		jTree_tree.setSelectionPath(tp);
		jTree_tree.updateUI();
		startTreeListener();
	}

	/**
	 *  時刻の列を生成
	 */
	private void redrawTimebar(JPanel jp)
	{
		jp.removeAll();
		
		float phm60 = 60.0F * bounds.getPaperHeightMultiplier() * paperHeightZoom;
		
		for (int row=0; row<24; row++) {
			
			int hour = row+TIMEBAR_START;
			
			JTimebarLabel b0 = new JTimebarLabel(Integer.toString(hour));
			
			if ( hour >=6 && hour <= 11 ) {
				b0.setBackground(env.getTimebarColor());
			}
			else if ( hour >=12 && hour <= 17 ) {
				b0.setBackground(env.getTimebarColor2());
			}
			else if ( hour >=18 && hour <= 23 ) {
				b0.setBackground(env.getTimebarColor3());
			}
			else {
				b0.setBackground(env.getTimebarColor4());
			}
			b0.setOpaque(true);
			b0.setBorder(lborder);
			b0.setHorizontalAlignment(JLabel.CENTER);
			
			b0.setBounds(
					0,
					(int) Math.ceil((float)row*phm60),
					bounds.getTimebarColumnWidth(),
					(int) Math.ceil(phm60));
			
			jp.add(b0);
		}
		
		Dimension d = jp.getMaximumSize();
		d.width = bounds.getTimebarColumnWidth();
		d.height = (int) Math.ceil(24*phm60);
		jp.setPreferredSize(d);
		jp.updateUI();
	}
	

	/**
	 * 現在日時に移動する
	 * @see #redrawByDate(String, IterationType)
	 */
	private void redrawByNow(final IterationType tuner) {
		
		// 古いタイマーの削除
		stopTimer();
		
		// 移動汁！！
		redrawByDate(CommonUtils.getDate529(0,true),tuner);
		
		// 時間線をひく
		jLabel_timeline.setVisible(true);
		Dimension dm = jLayeredPane_space_main_view.getPreferredSize();
		//dm.height = Math.round(1*bounds.getPaperHeightMultiplier()*paperHeightZoom);
		dm.height = 3;
		jLabel_timeline.setBorder(new LineBorder(Color.RED,2));
		jLabel_timeline.setBackground(Color.RED);
		jLabel_timeline.setOpaque(true);
		jLabel_timeline.setBounds(0, 0, dm.width, dm.height);
		jLayeredPane_space_main_view.add(jLabel_timeline);
		jLayeredPane_space_main_view.setLayer(jLabel_timeline, 2);
		
		// 現在時刻線のリセット
		setTimelinePos(true);
		
		// 新しいタイマーの作成（１分ごとに線を移動する）
		startTimer();
	}
	
	/**
	 * 日付別に表を作成する
	 * @see #_redrawByDateWithCenter(String, String, IterationType)
	 */
	private TVProgramIterator redrawByDate(String date, IterationType tuner) {
		return _redrawByDateWithCenter(null,date,tuner);
	}
	
	/**
	 * 日付別に表を作成する（ページャーが有効な場合は指定の放送局のあるページを開く）
	 * @see #_redrawByDateWithCenter(String, String, IterationType)
	 */
	private TVProgramIterator redrawByDateWithCenter(String center, String date) {
		
		// 今日は？
		String ndate529 = CommonUtils.getDate529(0, true);
		
		//　過去ログかどうか
		IterationType tuner;
		JTreeLabel.Nodes node;
		if ( ndate529.compareTo(date) > 0 ) {
			tuner = IterationType.PASSED;
			node = JTreeLabel.Nodes.PASSED;
		}
		else {
			tuner = IterationType.ALL;
			node = JTreeLabel.Nodes.DATE;
		}
		
		if ( tuner == IterationType.PASSED ) {
			// 過去ログの取得
			PassedProgram passed = tvprograms.getPassed();
			if ( ! passed.loadAllCenters(date) ) {
				System.err.println(ERRID+"過去ログの取得に失敗しました： "+date);
				return null;
			}
		}
		
		// 番組枠描画
		TVProgramIterator pli = _redrawByDateWithCenter(center, date, tuner);
		
		getJTree_tree().getSelectionModel().setSelectionPath(null); // 選択は解除する
		
		jLabel_tree.setView(node, date);
		
		return pli;
	}
	
	/**
	 * 日付別に表を作成する、の本体
	 * @see #redrawByDate(String, IterationType)
	 * @see #redrawByDateWithCenter(String, String)
	 */
	private TVProgramIterator _redrawByDateWithCenter(String center, String date, IterationType tuner) {
		
		if (env.getDebug()) System.out.println(DBGID+"CALLED redrawByDate() date="+date+" IterationType="+tuner);
		
		// 古いタイマーの削除
		stopTimer();
		
		cur_tuner = tuner;
		
		if (date == null) {
			return null;
		}

		// イテレータ－
		TVProgramIterator pli = tvprograms.getIterator().build(chsort.getClst(),tuner);
		
		// パネルの初期化
		clearPanel();
		
		// 新聞ペーンの選択
		boolean drawPrograms = true;
		if ( tuner != IterationType.ALL || env.isPagerEnabled() || ! env.getDrawcacheEnable() ) {
			selectMainView(null);
		}
		else {
			// 描画速度優先の場合
			boolean nopane = true;
			for ( JTaggedLayeredPane tlp : jLayeredPane_space_main_view_byDate ) {
				if ( tlp.getTagstr().equals(date) ) {
					selectMainView(tlp);
					if ( tlp.getComponentCountInLayer(0) > 0 ) {
						// 描画済みなら再度の描画は不要
						drawPrograms = false;
					}
					nopane = false;
					break;
				}
			}
			if ( nopane ) {
				// 該当日付のPaneがない場合
				selectMainView(null);
			}
		}
		
		// ページ制御
		int colmin = 0;
		int colmax = pli.size();
		int divider = 0;
		if ( env.isPagerEnabled() ) {
			
			int selectedpage = getSelectedPagerIndex();
			
			if ( center == null ) {
				// とび先の指定がないのでもともと選択されていたページを再度選択したい
				if ( selectedpage == -1 ) {
					// なんか、選択されてないよ？
					selectedpage = 0;
				}
				else {
					int maxindex = env.getPageIndex(pli.size());
					if ( selectedpage > maxindex ) {
						// ページ数かわったら、インデックスがはみだしちゃった
						selectedpage = 0;
					}
				}
			}
			else {
				// 特定の日付の特定の放送局を表示したい
				int crindex = pli.getIndex(center);
				if ( crindex ==  -1 ) {
					// ここに入ったらバグ
					MWin.appendError(ERRID+"「"+center+"」は有効な放送局ではありません");
					ringBeep();
					crindex = 0;
				}
				selectedpage = env.getPageIndex(1+crindex);
			}

			// 開始位置・終了位置・局数
			colmin = env.getCenterPerPage() * selectedpage;
			colmax = colmin + (env.getCenterPerPage()-1);
			divider = env.getCenterPerPage();
			
			// ページャーコンボボックスの書き換え
			setPagerItems(pli,env.getPageIndex(1+colmin));
			pli.rewind();
			
			// ページャーは有効だよ
			//setPagerEnabled(true);
		}
		
		if (env.getDebug()) System.out.println(DBGID+"[描画開始] ch_start="+colmin+" ch_end="+colmax+" ch_size="+pli.size());
		
		// 番組の枠表示用
		dborder.setDashColor(env.getMatchedBorderColor());
		dborder.setThickness(env.getMatchedBorderThickness());
		
		// 番組表時の共通設定
		updateFonts(env);
		
		// 局列・番組表を作成
		jPanel_space_top_view.setLayout(null);
		int cnt = -1;
		int col = -1;
		for ( ProgList pl : pli ) {

			++cnt;
			
			if ( cnt < colmin ) {
				continue;
			}
			else if ( cnt > colmax ) {
				break;
			}
			
			col = (divider==0) ? (cnt) : (cnt % divider);
			
			//TVProgram tvp = tvprograms.get(pli.getSiteId());
			
			//if (env.getDebug()) System.out.println(DBGID+"[描画中] "+pl.Center+" min="+colmin+" max="+colmax+" cnt="+cnt+" col="+col+" siteid="+siteid);
			
			// 局列
			JLabel b1 = new JLabel(pl.Center);
			b1.setOpaque(true);
			b1.setBackground(pl.BgColor);
			b1.setBorder(lborder);
			b1.setHorizontalAlignment(JLabel.CENTER);
			b1.setBounds(bounds.getBangumiColumnWidth()*col, 0, bounds.getBangumiColumnWidth(), bounds.getBangumiColumnHeight());
			b1.addMouseListener(cnMouseAdapter);
			jPanel_space_top_view.add(b1);
			
			// 予約の赤枠を表示する
			if (tuner != IterationType.PASSED) {
				putReserveBorder(date, pl.Center, col);
			}

			// 番組表
			if (drawPrograms == true) {
				putBangumiColumns(pl, col, date);
			}
		}
		
		++col; // 描画後にパネルサイズの変更にも使う
		
		if ( ! env.getDrawcacheEnable()) {
			// 番組枠描画バッファサイズの上限を確認する
			if (framebuffersize < frameUsed.size()) {
				framebuffersize = frameUsed.size();
				StdAppendMessage(MSGID+"番組枠描画バッファの上限を変更: "+frameUsed.size()+"/"+framebuffersize);
			}
		}
		
		// ページサイズを変更する
		//jPanel_space_top_view.setPreferredSize(new Dimension(bounds.getTimebarColumnWidth()+cnt*bounds.getBangumiColumnWidth(),bounds.getBangumiColumnHeight()));
		jPanel_space_top_view.setPreferredSize(new Dimension(col*bounds.getBangumiColumnWidth(),bounds.getBangumiColumnHeight()));
		jPanel_space_top_view.updateUI();
		
		jLayeredPane_space_main_view.setPreferredSize(new Dimension(bounds.getBangumiColumnWidth()*col,Math.round(24*60*bounds.getPaperHeightMultiplier()*paperHeightZoom)));
		
		if (env.getDebug()) System.out.println(DBGID+"END redrawByDate() date="+date+" IterationType="+tuner);
		
		pli.rewind();
		return pli;
	}

	/**
	 * 放送局別に表を作成する
	 */
	private void redrawByCenter(String center)
	{
		// 古いタイマーの削除
		stopTimer();
		
		// ページャーは効かないよ
		if ( env.isPagerEnabled() ) {
			setPagerEnabled(false);
		}
		
		// パネルの初期化
		clearPanel();
		selectMainView(null);
		
		jPanel_space_top_view.setLayout(null);
		
		for (int a=0; a<tvprograms.size(); a++) {
			//
			TVProgram tvp = tvprograms.get(a);
			//
			if (tvp.getType() != ProgType.PROG) {
				continue;
			}
			//
			for (ProgList pl : tvp.getCenters()) {
				if (pl.enabled == true && pl.Center.equals(center)) {
					// 日付ヘッダを描画する
					for (int centerid=0; centerid<pl.pdate.size(); centerid++)
					{
						ProgDateList pcl = pl.pdate.get(centerid);
						
						JTXTLabel b1 = new JTXTLabel();
						GregorianCalendar c = CommonUtils.getCalendar(pcl.Date);
						if ( c != null ) {
							String date = CommonUtils.getDate(c);
							b1.setValue(date);
							b1.setText(date.substring(5));
							b1.setOpaque(true);
							if ( c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ) {
								b1.setBackground(new Color(90,90,255));
							}
							else if ( c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ) {
								b1.setBackground(new Color(255,90,90));
							}
							else {
								b1.setBackground(new Color(180,180,180));
							}
						}
						b1.setBorder(lborder);
						b1.setHorizontalAlignment(JLabel.CENTER);
						b1.setBounds(bounds.getBangumiColumnWidth()*centerid, 0, bounds.getBangumiColumnWidth(), bounds.getBangumiColumnHeight());
						b1.addMouseListener(tbMouseAdapter);
						jPanel_space_top_view.add(b1);
					}
					//jPanel_space_top_view.setPreferredSize(new Dimension(bounds.getTimebarColumnWidth()+bounds.getBangumiColumnWidth()*tvprogram.getPlist().get(x).pcenter.size(),bounds.getBangumiColumnHeight()));
					jPanel_space_top_view.setPreferredSize(new Dimension(bounds.getBangumiColumnWidth()*pl.pdate.size(),bounds.getBangumiColumnHeight()));
					jPanel_space_top_view.updateUI();
					

					// 番組枠の表示
					{
						putBangumiColumns(pl, -1, null);
					}
					
					// 予約枠の表示
					for (int progid=0; progid<pl.pdate.size(); progid++) {
						putReserveBorder(pl.pdate.get(progid).Date, pl.Center, progid);
					}
					
					//
					jLayeredPane_space_main_view.setPreferredSize(new Dimension(tvp.getCenters().get(0).pdate.size()*bounds.getBangumiColumnWidth(),Math.round(24*60*bounds.getPaperHeightMultiplier()*paperHeightZoom)));
					//jScrollPane_space_main.updateUI();

					break;
				}
			}
		}
		
		// 番組枠描画バッファサイズの上限を確認する
		if (framebuffersize < frameUsed.size()) {
			framebuffersize = frameUsed.size();
			StdAppendMessage(MSGID+"番組枠描画バッファの上限を変更: "+frameUsed.size()+"/"+framebuffersize);
		}
	}
	
	
	/**
	 * 予約待機赤枠の描画（個々の枠）
	 * @see #updateReserveBorder(String)
	 */
	private void putReserveBorder(String date, String Center, int q) {
		
		// 古いマークの削除（一見取りこぼしがあるように見えるが無問題）
		for (int i=reserveBorders.size()-1; i>=0; i--) {
			JRMLabel rb = reserveBorders.get(i);
			if ( rb.getDate().equals(date) && rb.getCenter().equals(Center) ) {
				rb.setVisible(false);
				jLayeredPane_space_main_view.remove(rb);
				reserveBorders.remove(i);
			}
		}
		
		// 座標系
		JRMLabel.setColumnWidth(bounds.getBangumiColumnWidth());
		JRMLabel.setHeightMultiplier(bounds.getPaperHeightMultiplier() * paperHeightZoom);
		
		// 表示範囲
		GregorianCalendar cal = CommonUtils.getCritCalendar(date);
		String topDateTime = CommonUtils.getDateTime(cal);
		cal.add(Calendar.DATE, 1);
		String bottomDateTime = CommonUtils.getDateTime(cal);
		
		// 
		String passedCritDateTime = CommonUtils.getCritDateTime(env.getDisplayPassedReserve());
		
		// ツールバーで選択されている実レコーダ
		String myself = ( env.getEffectComboToPaper() ) ? (getSelectedRecorderId()) : (null);
		
		// 予約枠の描画
		drawReserveBorders(date, Center, q, topDateTime, bottomDateTime, passedCritDateTime, myself);
		
		// ピックアップ枠の描画
		drawPickupBorders(date, Center, q, topDateTime, bottomDateTime, passedCritDateTime, TUNERLABEL_PICKUP);
	}
	private void drawReserveBorders(String date, String Center, int q, String topDateTime, String bottomDateTime, String passedCritDateTime, String myself) {
		if ( myself == HDDRecorder.SELECTED_PICKUP ) {
			return;
		}
		for ( HDDRecorder recorder : getSelectedRecorderList() ) {
			for ( ReserveList r : recorder.getReserves()) {
				
				// 「実行のみ表示」で無効な予約は表示しない
				if ( env.getDisplayOnlyExecOnEntry() && ! r.getExec() ) {
					continue;
				}
				
				// 放送局名の確認
				if ( r.getCh_name() == null ) {
					if ( r.getChannel() == null ) {
						// CHコードすらないのはバグだろう
						System.err.println(ERRID+"予約情報にCHコードが設定されていません。バグの可能性があります。 recid="+recorder.Myself()+" chname="+r.getCh_name());
					}
					continue;
				}
				
				// 描画本体
				if (r.getCh_name().equals(Center)) {
					
					// 開始終了日時リストを生成する
					ArrayList<String> starts = new ArrayList<String>();
					ArrayList<String> ends = new ArrayList<String>();
					CommonUtils.getStartEndList(starts, ends, r);
					
					// 予約枠を描画する
					for ( int j=0; j<starts.size(); j++ ) {
						if ( passedCritDateTime.compareTo(ends.get(j)) > 0 ) {
							// 過去情報の表示が制限されている場合
							continue;
						}
						
						drawBorder(date,Center,topDateTime,bottomDateTime,starts.get(j),ends.get(j),r.getRec_min(),r.getTuner(),recorder.getColor(r.getTuner()),r.getExec(),q);
					}
				}
			}
		}
	}
	private void drawPickupBorders(String date, String Center, int q, String topDateTime, String bottomDateTime, String passedCritDateTime, String tuner) {
		for ( ProgList pl : tvprograms.getPickup().getCenters() ) {
			if ( ! pl.Center.equals(Center) ) {
				continue;
			}
			for ( ProgDateList pcl : pl.pdate ) {
				for ( ProgDetailList tvd : pcl.pdetail ) {
					if ( passedCritDateTime.compareTo(tvd.endDateTime) > 0 ) {
						// 過去情報の表示が制限されている場合
						continue;
					}
					
					drawBorder(date,Center,topDateTime,bottomDateTime,tvd.startDateTime,tvd.endDateTime,tvd.length,tuner,env.getPickedColor(),false,q);
				}
			}
		}
	}
	private void drawBorder(String date, String Center, String topDateTime, String bottomDateTime, String startDateTime, String endDateTime, String recmin, String tuner, String bordercol, boolean exec, int col) {
		drawBorder(date, Center, topDateTime, bottomDateTime, startDateTime, endDateTime, Integer.valueOf(recmin), tuner, CommonUtils.str2color(bordercol), exec, col);
	}
	private void drawBorder(String date, String Center, String topDateTime, String bottomDateTime, String startDateTime, String endDateTime, int recmin, String tuner, Color bordercol, boolean exec, int col) {
		
		GregorianCalendar ca = CommonUtils.getCalendar(startDateTime);
		int ahh = ca.get(Calendar.HOUR_OF_DAY);
		int amm = ca.get(Calendar.MINUTE);
		
		int row = 0;
		int length = 0;
		if (topDateTime.compareTo(startDateTime) <= 0 && startDateTime.compareTo(bottomDateTime) < 0) {
			// 開始時刻が表示範囲内にある
			row = ahh - TIMEBAR_START;
			if (row < 0) {
				row += 24;
			}
			row = row*60 + amm;
			length = recmin;
		}
		else if (startDateTime.compareTo(topDateTime) < 0 && topDateTime.compareTo(endDateTime) < 0) {
			//　表示開始位置が番組の途中にある
			row = 0;
			length = recmin - (TIMEBAR_START*60 - ahh*60 - amm);
		}
		else {
			return;
		}
		
		{
			// 重複予約の場合のエンコーダマーク表示位置の調整
			int rc = 0;
			//int rw = 0;
			for (int k=0; k<reserveBorders.size(); k++) {
				JRMLabel rb = reserveBorders.get(k);
				if ( rb.getDate().equals(date) && rb.getCenter().equals(Center) ) {
					int drow = rb.getVRow() - row;
					int dlen = rb.getVHeight() - length;
					if ( rb.getVColumn() == col && ((drow == 0 && dlen == 0) || ((drow == 1 || drow == -1) && (dlen == 0 || dlen == -1 || dlen == 1))) ) {
						rc++;
					}
				}
			}
			
			// 予約マーク追加
			JRMLabel rb = new JRMLabel();
			
			if (rc == 0) {
				rb.setVerticalAlignment(JLabel.BOTTOM);
				rb.setHorizontalAlignment(JLabel.RIGHT);
			}
			else if (rc == 1) {
				rb.setVerticalAlignment(JLabel.BOTTOM);
				rb.setHorizontalAlignment(JLabel.LEFT);
			}
			else {
				rb.setVerticalAlignment(JLabel.TOP);
				rb.setHorizontalAlignment(JLabel.RIGHT);
			}
			
			// エンコーダの区別がないものは"■"を表示する
			rb.setEncBackground(bordercol);
			rb.setBorder(new LineBorder(bordercol,4));
			if ( tuner != null && tuner.equals(TUNERLABEL_PICKUP) ) {
				rb.setEncForeground(env.getPickedFontColor());
			}
			else if ( exec ) {
				rb.setEncForeground(env.getExecOnFontColor());
			}
			else {
				rb.setEncForeground(env.getExecOffFontColor());
			}
			if (tuner == null || tuner.equals("")) {
				rb.setEncoder("■");
			}
			else {
				rb.setEncoder(tuner);
			}

			// 検索用情報
			rb.setDate(date);
			rb.setCenter(Center);
			rb.setExec(exec);
			
			jLayeredPane_space_main_view.add(rb);
			jLayeredPane_space_main_view.setLayer(rb,1);
			rb.setVBounds(col, row, 1, length);
			rb.setVisible(true);
			
			reserveBorders.add(rb);
		}
	}
	
	/**
	 * 番組枠の表示
	 * @param cnt -1:放送局別表示、>=0:日付表示
	 */
	private void putBangumiColumns(ProgList pl, int cnt, String date) {
		int ymax = pl.pdate.size();
		int col = -1;
		for ( int dateid=0; dateid < ymax; dateid++ ) {
			ProgDateList pcl = pl.pdate.get(dateid);
			
			if ( cnt >= 0 ) {
				if ( ! pcl.Date.equals(date) ) {
					// 日付表示の場合は１列のみ描画
					continue;
				}
				
				col = cnt;
			}
			else if ( cnt == -1 ) {
				col++;
			}
			
			int row = 0;
			int pEnd = 0;
			int zmax = pcl.pdetail.size();
			for ( int progid=0; progid<zmax; progid++ ) {
				ProgDetailList tvd = pcl.pdetail.get(progid);
				if ( progid != 0 ) {
					// ２つめ以降は開始時刻から計算
					String[] st = tvd.start.split(":",2);
					if ( st.length == 2 ) {
						int ahh = Integer.valueOf(st[0]);
						int amm = Integer.valueOf(st[1]);
						if ( CommonUtils.isLateNight(ahh) ) {
							ahh += 24;
						}
						row = (ahh-TIMEBAR_START)*60+amm;
					}
					else {
						// 「番組情報がありません」は前の番組枠のお尻に
						row = pEnd;
					}
				}
				else {
					// その日の最初のエントリは5:00以前の場合もあるので強制０スタート
					row = 0;
				}
				
				// 番組枠描画
				putBangumiColumnSub(tvd, row, col);
				
				// 「番組情報がありません」用に保存
				pEnd = row + tvd.length;
			}
		}
	}
	private void putBangumiColumnSub(ProgDetailList tvd, int row, int col) {
		
		// 新規生成か既存流用かを決める
		JTXTButton b2 = null;
		if (jLayeredPane_space_main_view == jLayeredPane_space_main_view_byMakeshift && ! frameUnused.isEmpty()) {
			b2 = frameUnused.remove(frameUnused.size()-1);
			//b2.setVisible(true);	// JTXTButton.clear()内でsetVisible(false)しているので
		}
		else {
			// 生成する
			b2 = new JTXTButton();
			jLayeredPane_space_main_view.add(b2);
			jLayeredPane_space_main_view.setLayer(b2, 0);
			
			// リスナーを設定する
			b2.addMouseListener(ml_risepopup);
			b2.addMouseMotionListener(ml_risepopup);
		}
		if (jLayeredPane_space_main_view == jLayeredPane_space_main_view_byMakeshift) {
			frameUsed.add(b2);
		}
		else {
			// 裏描画は十分遅いのでb2をUnusedキャッシュには入れず都度生成で構わない
			frameUsedByDate.add(b2);
		}
		
		// 情報設定
		b2.setInfo(tvd);
		
		JTXTButton.setColumnWidth(bounds.getBangumiColumnWidth());
		JTXTButton.setHeightMultiplier(bounds.getPaperHeightMultiplier() * paperHeightZoom);
		
		b2.setBackground(pColors.get(tvd.genre));
		if (bounds.getShowMatchedBorder() && b2.isStandby() ) {
			b2.setBorder(dborder);
		}
		else {
			b2.setBorder(lborder);
		}
		
		// 配置を決定する
		b2.setVBounds(col,row,1,tvd.length);
		
		// ツールチップを付加する
		if ( env.getTooltipEnable() == true && ! tvd.title.equals("") && ! tvd.start.equals("") ) {
			String t = "";
			int tlen = bounds.getTooltipWidth();
			for (int i=0; i<tvd.title.length(); i+=tlen) {
				t += tvd.title.substring(i, (i+tlen<tvd.title.length())?(i+tlen):(tvd.title.length()))+"<BR>";
			}
			String d = "";
			int dlen = tlen+2;
			for (int i=0; i<tvd.detail.length(); i+=dlen) {
				d += "&nbsp;&nbsp;&nbsp;&nbsp;"+tvd.detail.substring(i, (i+dlen<tvd.detail.length())?(i+dlen):(tvd.detail.length()))+"<BR>";
			}
			String e = getExtensionMark(tvd);
			b2.setToolTipText(("<html>"+tvd.start+"&nbsp;<FONT COLOR=RED><EM>"+e+"</EM></FONT><BR>&nbsp;<FONT COLOR=BLUE><STRONG><U>"+t+"</U></STRONG></FONT>"+d+"</html>"));
		}
	}
	
	/**
	 * 現在時刻線の位置を変える
	 * @param minpos : MINPOS.RESET=初回、MINPOS.UPDATE=自動更新時
	 */
	private int setTimelinePos(boolean reset) {
		if ( vport != null && jLabel_timeline != null && jLabel_timeline.isVisible() ) {
			
			int correct = 0; // 24:00-28:59迄は前日の日付になる
			GregorianCalendar c = CommonUtils.getCalendar(0);
			if ( CommonUtils.isLateNight(c) ) {
				c.add(Calendar.DATE, -1);
				correct += 24;
			}
			
			Point vp = vport.getViewPosition();
			Point tp = jLabel_timeline.getLocation();
			
			// ビュー上の位置
			int minpos_new = (c.get(Calendar.HOUR_OF_DAY)-TIMEBAR_START+correct)*60+c.get(Calendar.MINUTE);
			int timeline_vpos = jLabel_timeline.setMinpos(0, minpos_new, bounds.getPaperHeightMultiplier()*paperHeightZoom);
			
			// ビューポートの位置（05:30まではスクロールしないよ）
			if ( env.getTimerbarScrollEnable() && minpos_new >= 30 ) {
				if ( reset ) {
					// 初回描画
					Rectangle ra = vport.getViewRect();
					ra.y =  Math.round(timeline_vpos - (float)bounds.getTimelinePosition() * bounds.getPaperHeightMultiplier() * paperHeightZoom);
					vport.setViewPosition(new Point(ra.x, ra.y));
				}
				else {
					// 自動更新
					vp.y += (timeline_vpos - tp.y);
					vport.setViewPosition(vp);
				}
			}
			
			jLabel_timeline.updateUI();
			
			return minpos_new;
		}
		
		return -1;
	}
	
	
	/*******************************************************************************
	 * ハンドラ―メソッド
	 ******************************************************************************/
	
	/**
	 * ツールバーでレコーダの選択イベントが発生
	 */
	@Override
	public void valueChanged(HDDRecorderSelectionEvent e) {
		if (debug) System.out.println(DBGID+"recorder selection rised");
		
		// 選択中のレコーダ情報を保存する
		src_recsel = (HDDRecorderSelectable) e.getSource();
		
		// 予約枠を書き換える
		updateReserveBorder(null);
	}
	
	private String getSelectedRecorderId() {
		return ( src_recsel!=null ? src_recsel.getSelectedId() : null );
	}
	
	private HDDRecorderList getSelectedRecorderList() {
		return ( src_recsel!=null ? src_recsel.getSelectedList() : null );
	}
	
	private HDDRecorderSelectable src_recsel;
	
	
	/**
	 * レコーダ情報の変更イベントが発生
	 */
	@Override
	public void stateChanged(HDDRecorderChangeEvent e) {
		// テーブルをリフレッシュする処理
		
	}
	
	

	/*******************************************************************************
	 * リスナー
	 ******************************************************************************/

	/**
	 * 現在時刻追従スクロール
	 */
	@Override
	public void timerRised(TickTimerRiseEvent e) {
		
		String curDT = CommonUtils.getDate529(0,true);
		
		if ( prevDT4Tree != null && ! prevDT4Tree.equals(curDT) ) {
			// 日付が変わったらツリーを書き換える
			redrawTreeByDate();
			redrawTreeByPassed();
			prevDT4Tree = curDT;
		}
		
		if ( timer_now_enabled ) {

			if (prevDT4Now != null && ! prevDT4Now.equals(curDT)) {
				// 日付切り替え
				StdAppendError(MSGID+"日付が変わったので番組表を切り替えます("+CommonUtils.getDateTime(0)+")");
				redrawByNow(cur_tuner);
			}
			else {
				// 現在時刻線の移動
				setTimelinePos(false);
			}
			
			// 前回実行日
			prevDT4Now = curDT;
		}
	}
	
	/**
	 * タブを開いたり閉じたりしたときに動くリスナー
	 */
	private ComponentListener cl_shownhidden = new ComponentAdapter() {
		@Override
		public void componentShown(ComponentEvent e) {
			
			// 前日以前の番組情報を削除する
			for ( TVProgram tvp : tvprograms ) {
				tvp.refresh();
			}
			
			// 終了した予約を整理する
			for ( HDDRecorder recorder : recorders ) {
				recorder.refreshReserves();
			}
			
			// 他のコンポーネントと連動
			onShown();
			
			setPagerEnabled(true);
		}
		
		@Override
		public void componentHidden(ComponentEvent e) {
			
			onHidden();
			
			setPagerEnabled(false);
		}
	};
	
	/**
	 * 番組枠につけるマウス操作のリスナー
	 */
	private final MouseInputListener ml_risepopup = new MouseInputListener() {
		//
		private final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		private final Point pp = new Point();
		
		private Color bgcolor = null;

		@Override
		public void mouseClicked(MouseEvent e) {
			
			// ポインタの位置
			Point p = e.getPoint();
			
			// ポインタの乗っている番組
			JTXTButton b = (JTXTButton) e.getSource();
			ProgDetailList tvd = b.getInfo();
			
			if (e.getButton() == MouseEvent.BUTTON3) {
				if (e.getClickCount() == 1) {
					// 右シングルクリックでメニューの表示
					showPopupForTraceProgram(b, tvd, tvd.title, TraceKey.noFazzyThreshold, p.x, p.y, e.getY());
				}
			}
			else if (e.getButton() == MouseEvent.BUTTON1) {
				// 過去ログは閲覧のみ
				if (tvd.type == ProgType.PASSED) {
					if (e.getClickCount() == 2) {
						MWin.appendMessage(MSGID+"過去ログでダブルクリックは利用できません");
						ringBeep();
					}
					
					return;
				}
				
				if (e.getClickCount() == 2) {
					// 左ダブルクリックで予約ウィンドウを開く
					openReserveDialog(tvd);					
				}
			}
			else if (e.getButton() == MouseEvent.BUTTON2) {
				// ピックアップに追加
				addToPickup(tvd);
			}
		}
		
		private void openReserveDialog(ProgDetailList tvd) {
			
			// レコーダが登録されていない場合はなにもしない
			if (recorders.size() == 0) {
				return;
			}

			// ダイアログの位置指定
			CommonSwingUtils.setLocationCenter(parent,rD);
			
			// サブタイトルを番組追跡の対象から外す
			boolean succeeded = false;
			if ( ! env.getSplitEpno() && env.getTraceOnlyTitle() ) {
				succeeded = rD.open(tvd,tvd.title,TraceKey.defaultFazzyThreshold);
			}
			else {
				succeeded = rD.open(tvd);
			}
			
			if (succeeded) {
				rD.setVisible(true);
			}
			else {
				rD.dispose();
			}
			
			if (rD.isReserved()) {
				updateReserveDisplay();
				updateReserveBorder(tvd.center);
			}
		}

		/**
		 * 詳細情報の自動表示
		 */
		@Override
		public void mouseEntered(MouseEvent e) {
			
			JTXTButton b = (JTXTButton) e.getSource();
			ProgDetailList tvd = b.getInfo();
			
			if ( env.getEnableHighlight() ) {
				bgcolor = ((JTXTButton)e.getSource()).getBackground();
				((JTXTButton)e.getSource()).setBackground(env.getHighlightColor());
			}
			jTextPane_detail.setLabel(tvd.start,tvd.end,tvd.title);
			jTextPane_detail.setText(tvd.detail+"\n"+tvd.getAddedDetail());
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if ( env.getEnableHighlight() ) {
				((JTXTButton)e.getSource()).setBackground(bgcolor);
			}
		}

		@Override
		public void mouseDragged(final MouseEvent e) {
			Point cp = e.getLocationOnScreen();
			Point vp = vport.getViewPosition(); //= SwingUtilities.convertPoint(vport,0,0,label);
			vp.translate(pp.x-cp.x, pp.y-cp.y);
			jLayeredPane_space_main_view.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
			pp.setLocation(cp);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			pp.setLocation(e.getLocationOnScreen());
			jLayeredPane_space_main_view.setCursor(hndCursor);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			jLayeredPane_space_main_view.setCursor(defCursor);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
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
		public void valueChanged(TreeSelectionEvent e){
			
			TreePath path = jTree_tree.getSelectionPath();

			if ( path != null && path.getPathCount() == 2 ) {
				// 親ノードとか触られても…
				return;
			}
			
			if ( path != null && path.getPathCount() == 3 ) {
				
				if (env.getDebug()) System.out.println(DBGID+"SELECTED treeSelListner "+path);

				stopTimer();

				JTreeLabel.Nodes node = JTreeLabel.Nodes.getNode(path.getPathComponent(1).toString());
				String value = path.getLastPathComponent().toString();
			
				switch ( node ) {
				case DATE:
					if ( JTreeLabel.Nodes.NOW.getLabel().equals(value) ) {
						// 現在日時に移動する
						redrawByNow(IterationType.ALL);
					}
					else {
						redrawByDate(value,IterationType.ALL);
					}
					if ( env.isPagerEnabled() ) {
						setPagerEnabled(true);
					}
					break;
				case TERRA:
					if ( JTreeLabel.Nodes.NOW.getLabel().equals(value) ) {
						redrawByNow(IterationType.TERRA);
					}
					else {
						redrawByDate(value,IterationType.TERRA);
					}
					if ( env.isPagerEnabled() ) {
						setPagerEnabled(true);
					}
					break;
				case BS:
					if ( JTreeLabel.Nodes.NOW.getLabel().equals(value) ) {
						redrawByNow(IterationType.BS);
					}
					else {
						redrawByDate(path.getLastPathComponent().toString(),IterationType.BS);
					}
					if ( env.isPagerEnabled() ) {
						setPagerEnabled(true);
					}
					break;
				case CS:
					if ( JTreeLabel.Nodes.NOW.getLabel().equals(value) ) {
						redrawByNow(IterationType.CS);
					}
					else {
						redrawByDate(path.getLastPathComponent().toString(),IterationType.CS);
					}
					if ( env.isPagerEnabled() ) {
						setPagerEnabled(true);
					}
					break;
				case BCAST:
					redrawByCenter(value);
					if ( env.isPagerEnabled() ) {
						setPagerEnabled(false);
					}
					break;
				case PASSED:
					PassedProgram passed = tvprograms.getPassed();
					if ( passed.loadAllCenters(value) ) {
						redrawByDate(value, IterationType.PASSED);
					}
					else {
						MWin.appendError(ERRID+"過去ログが存在しません: "+value);
						ringBeep();
					}
					if ( env.isPagerEnabled() ) {
						setPagerEnabled(true);
					}
					break;
				default:
					break;
				}
				
				jLabel_tree.setView(node, value);
				return;
			}
			
			// なんかおかしいのでデフォルト選択にまわす
			CommonUtils.printStackTrace();
			MWin.appendError(ERRID+"バグの可能性あり");
			//redrawByNow(IterationType.ALL);
			//jLabel_tree.setView(JTreeLabel.Nodes.DATE, JTreeLabel.Nodes.NOW.getLabel());
		}
	};
	
	/**
	 * フルスクリーン時にツリーを隠したりするの
	 */
	private final MouseListener ml_treehide = new MouseAdapter() {
		public void mouseEntered(MouseEvent e) {
			if (isFullScreen()) {
				setExpandTree();
				//StdAppendMessage("Show tree (N)");
			}
		}
		public void mouseExited(MouseEvent e) {
			if (isFullScreen()) {
				setCollapseTree();
				//StdAppendMessage("Hide tree (N)");
			}
		}
	};
	
	/**
	 * 放送局名につけるリスナー（ダブルクリックで一週間表示にジャンプ）
	 */
	private final MouseAdapter cnMouseAdapter = new MouseAdapter() {
		
		private Color bgcolor = null;
		
		public void mouseExited(MouseEvent e) {
			((JLabel)e.getSource()).setBackground(bgcolor);
		}
		public void mouseEntered(MouseEvent e) {
			bgcolor = ((JLabel)e.getSource()).getBackground();
			((JLabel)e.getSource()).setBackground(new Color(180,180,255));
		}
		
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (e.getClickCount() == 2) {
					if ( cur_tuner == IterationType.PASSED ) {
						MWin.appendMessage(MSGID+"過去ログでは一局表示に切り替えられません");
						return;
					}
					
					// 右ダブルクリックで局表示に切り替え
					String center = ((JLabel)e.getSource()).getText();
					StdAppendMessage(MSGID+"一局表示に切り替え："+center);
					//redrawByCenter(center);
					jLabel_tree.setView(JTreeLabel.Nodes.BCAST, center);
					reselectTree();
				}
			}
		}
	};
	
	/**
	 * 日付枠につけるリスナー（ダブルクリックで放送局別表示にジャンプ）
	 */
	private final MouseAdapter tbMouseAdapter = new MouseAdapter() {
		private Color bgcolor = null;
		//
		public void mouseExited(MouseEvent e) {
			((JTXTLabel)e.getSource()).setBackground(bgcolor);
		}
		public void mouseEntered(MouseEvent e) {
			bgcolor = ((JTXTLabel)e.getSource()).getBackground();
			((JTXTLabel)e.getSource()).setBackground(new Color(180,180,255));
		}
		
		//
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (e.getClickCount() == 2) {
					// 右ダブルクリックで日付表示に切り替え
					String date = ((JTXTLabel)e.getSource()).getValue();
					StdAppendMessage(MSGID+"日付表示に切り替え："+date);
					//redrawByDate(date, -1);
					jLabel_tree.setView(JTreeLabel.Nodes.DATE, date);
					reselectTree();
				}
			}
		}
	};
	
	
	
	/*******************************************************************************
	 * コンポーネント
	 ******************************************************************************/

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
			jSplitPane_view.setRightComponent(getJScrollPane_space_main());
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
	
	//
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
	
	/**
	 * ツリーの作成
	 */
	private JTree getJTree_tree() {
		if (jTree_tree == null) {

			// ツリーの作成
			jTree_tree = new JTree();
			jTree_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			jTree_tree.setRootVisible(env.getRootNodeVisible());
			
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
		
		paperRootNode = new DefaultMutableTreeNode(JTreeLabel.Nodes.ROOT.getLabel());
		
		dateNode	= new DefaultMutableTreeNode(JTreeLabel.Nodes.DATE.getLabel());
		dgNode		= new DefaultMutableTreeNode(JTreeLabel.Nodes.TERRA.getLabel());
		bsNode		= new DefaultMutableTreeNode(JTreeLabel.Nodes.BS.getLabel());
		csNode		= new DefaultMutableTreeNode(JTreeLabel.Nodes.CS.getLabel());
		centerNode	= new DefaultMutableTreeNode(JTreeLabel.Nodes.BCAST.getLabel());
		passedNode	= new DefaultMutableTreeNode(JTreeLabel.Nodes.PASSED.getLabel());
		
		paperRootNode.add(dateNode);
		paperRootNode.add(dgNode);
		paperRootNode.add(bsNode);
		paperRootNode.add(csNode);
		paperRootNode.add(centerNode);
		paperRootNode.add(passedNode);

		// 子の描画
		redrawTreeByDate();
		redrawTreeByCenter();
		redrawTreeByPassed();
		
		return paperRootNode;
	}
	
	private void undoTreeExpansion() {
			
		// 展開状態の復帰
		stopTreeListener();
		
		// 展開状態の記憶域の初期化
		ter = new TreeExpansionReg(jTree_tree, TreeExpRegFile_Paper);
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

	private JScrollPane getJScrollPane_space_main() {
		if (jScrollPane_space_main == null) {
			jScrollPane_space_main = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			jScrollPane_space_main.getVerticalScrollBar().setUnitIncrement(bounds.getBangumiColumnHeight());
			jScrollPane_space_main.getHorizontalScrollBar().setUnitIncrement(bounds.getBangumiColumnWidth());
			
			//jScrollPane_space_main.setViewportView(getJLayeredPane_space_main_view());
			jScrollPane_space_main.setColumnHeaderView(getJPanel_space_top_view());
			jScrollPane_space_main.setRowHeaderView(getJPanel_space_side_view());
			
			vport = jScrollPane_space_main.getViewport();
		}
		return jScrollPane_space_main;
	}
	
	private JPanel getJPanel_space_top_view() {
		if (jPanel_space_top_view == null) {
			jPanel_space_top_view = new JPanel();
			jPanel_space_top_view.setLayout(new SpringLayout());
		}
		return jPanel_space_top_view;
	}
	
	private JPanel getJPanel_space_side_view() {
		if (jPanel_space_side_view == null) {
			jPanel_space_side_view = new JPanel();
			jPanel_space_side_view.setLayout(null);
		}
		return jPanel_space_side_view;
	}
	
	
	/*
	 * 以下は、pcwinから呼び出されるメソッドをまとめたもの 
	 */
	
	// 時間枠のコンポーネント
	public Component[] getTimebarComponents() {
		return jPanel_space_side_view.getComponents();
	}

	// 背景色ほかの変更
	public void updateColors(Env ec,PaperColorsMap pc) {
		_updPColors(ec, pc, frameUsed);
		
		if ( env.getDrawcacheEnable() ) {
			_updPColors(ec, pc, frameUsedByDate);
		}
		
		// マウスオーバー時のハイライト
		/* no proc. */
		
		// タイムバーの色
		for ( Component c : getTimebarComponents() ) {
			if ( c instanceof JTimebarLabel ) {
				int j = Integer.valueOf(((JTimebarLabel) c).getTs());
				if ( j >=6 && j <= 11 ) {
					c.setBackground(ec.getTimebarColor());
				}
				else if ( j >=12 && j <= 17 ) {
					c.setBackground(ec.getTimebarColor2());
				}
				else if ( j >=18 && j <= 23 ) {
					c.setBackground(ec.getTimebarColor3());
				}
				else {
					c.setBackground(ec.getTimebarColor4());
				}
			}
		}
	}
	
	// サイズの変更
	public void updateBounds(Env ec, Bounds bc) {
		
		int maxCol = jPanel_space_top_view.getComponentCount();
		float maxRow = 24*60;
		
		float phm = bc.getPaperHeightMultiplier() * paperHeightZoom ;

		int vieww = maxCol * bc.getBangumiColumnWidth();
		int viewh = (int) Math.ceil(maxRow * phm);

		// 変更前のビューの位置
		Point vp = vport.getViewPosition();
		float vh = vport.getView().getPreferredSize().height;
		
		// タイムバーのサイズ変更
		{
			int h = (int) Math.ceil(60.0F*phm);
			int row = 0;
			for ( Component b0 : jPanel_space_side_view.getComponents() ) {
				b0.setBounds(0,(int) Math.ceil((float)row*phm),bc.getTimebarColumnWidth(),h);
				row += 60;
			}
			
			Dimension d = jPanel_space_side_view.getPreferredSize();
			d.height = viewh;
			jPanel_space_side_view.setPreferredSize(d);
		}
		
		// 放送局名(or日付)のサイズ変更
		{
			for ( int col=0; col<jPanel_space_top_view.getComponentCount(); col++ ) {
				Component b1 = jPanel_space_top_view.getComponent(col);
				b1.setBounds(
						bc.getBangumiColumnWidth() * col,
						0,
						bc.getBangumiColumnWidth(),
						bc.getBangumiColumnHeight());
			}
			Dimension d = jPanel_space_top_view.getPreferredSize();
			d.width = vieww;
			jPanel_space_top_view.setPreferredSize(d);
		}
		
		// 各番組枠のサイズ変更・検索マッチ枠の表示変更
		{
			{
				_updPBounds(bc, frameUsed);
				_updPBorders(ec, bc, frameUsed);
				
				Dimension d = jLayeredPane_space_main_view.getPreferredSize();
				d.width = vieww;
				d.height = viewh;
				jLayeredPane_space_main_view.setPreferredSize(d);
			}
			
			if ( ec.getDrawcacheEnable() ) {
				_updPBounds(bc, frameUsedByDate);
				_updPBorders(ec, bc, frameUsedByDate);
				
				for ( JLayeredPane pane : jLayeredPane_space_main_view_byDate ) {
					Dimension d = pane.getPreferredSize();
					d.width = vieww;
					d.height = viewh;
					pane.setPreferredSize(d);
				}
			}
		}
		
		// 予約枠・ピックアップ枠のサイズ変更＆色変更
		{
			JRMLabel.setColumnWidth(bc.getBangumiColumnWidth());
			JRMLabel.setHeightMultiplier(phm);
			
			for ( JRMLabel rb : reserveBorders ) {
				
				rb.reVBounds();
				
				if ( rb.getEncoder().equals(TUNERLABEL_PICKUP) ) {
					rb.setEncBackground(ec.getPickedColor());
					rb.setEncForeground(ec.getPickedFontColor());
					rb.setBorder(new LineBorder(ec.getPickedColor(),4));
				}
				else if ( rb.getExec() ) {
					rb.setEncForeground(ec.getExecOnFontColor());
				}
				else {
					rb.setEncForeground(ec.getExecOffFontColor());
				}
				rb.repaint();
			}
		}
		
		// 現在時刻線の位置変更
		setTimelinePos(false);
		
		// 枠のサイズを更新したのでupdateUI()
		jScrollPane_space_main.updateUI();
		
		// ビューの位置調整
		vp.y = (int)Math.ceil(maxRow * (float)vp.y * phm / vh);
		vport.setViewPosition(vp);
		
	}
	
	// フォントの変更
	public void updateFonts(Env ec) {
		JTXTButton.setShowStart(ec.getShowStart());
		JTXTButton.setSplitEpno(ec.getSplitEpno());
		JTXTButton.setShowDetail(ec.getShowDetail());
		JTXTButton.setDetailTab(ec.getDetailTab());
		JTXTButton.setDetailRows(ec.getDetailRows());
		
		JTXTButton.setTitleFont(ec.getTitleFont());
		JTXTButton.setTitleFontStyle(ec.getTitleFontStyle());
		JTXTButton.setDetailFont(ec.getDetailFont());
		JTXTButton.setDetailFontStyle(ec.getDetailFontStyle());
		JTXTButton.setTitleFontSize(ec.getTitleFontSize());
		JTXTButton.setTitleFontColor(ec.getTitleFontColor());
		JTXTButton.setDetailFontSize(ec.getDetailFontSize());
		JTXTButton.setDetailFontColor(ec.getDetailFontColor());
		JTXTButton.setAAHint(ec.getPaperAAMode().getHint());
	}
	
	// 再描画？
	public void updateRepaint() {
		_updPRepaint(frameUsed);
		
		if ( env.getDrawcacheEnable() ) {
			_updPRepaint(frameUsedByDate);
		}
	}
	
	// 以下共通部品
	
	private void _updPColors(Env ec, PaperColorsMap pc, ArrayList<JTXTButton> fa) {
		for ( JTXTButton b2 : fa ) {
			b2.setBackground(pc.get(b2.getInfo().genre));
		}
	}

	private void _updPBounds(Bounds bc, ArrayList<JTXTButton> fa) {
		
		JTXTButton.setColumnWidth(bc.getBangumiColumnWidth());
		JTXTButton.setHeightMultiplier(bc.getPaperHeightMultiplier() * paperHeightZoom);
		
		for ( JTXTButton b2 :  fa ) {
			b2.reVBounds();
		}
	}
	
	private void _updPBorders(Env ec, Bounds bc, ArrayList<JTXTButton> fa) {
		dborder.setDashColor(ec.getMatchedBorderColor());
		dborder.setThickness(ec.getMatchedBorderThickness());
		for ( JTXTButton b2 :  fa ) {
			if ( bc.getShowMatchedBorder() && b2.isStandby() ) {
				if ( b2.getBorder() != dborder )
					b2.setBorder(dborder);
			}
			else {
				if ( b2.getBorder() != lborder )
					b2.setBorder(lborder);
			}
		}
	}
	
	private void _updPRepaint(ArrayList<JTXTButton> fa) {
		for ( JTXTButton b2 :  fa ) {
			b2.forceRepaint();
		}
	}

}
