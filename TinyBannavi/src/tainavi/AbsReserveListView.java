package tainavi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.RowSorterEvent.Type;
import javax.swing.event.RowSorterListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


/**
 * 本体予約一覧タブのクラス
 * @since 3.15.4β　{@link Viewer}から分離
 */
public abstract class AbsReserveListView extends JScrollPane {

	private static final long serialVersionUID = 1L;

	public static void setDebug(boolean b) {debug = b; }
	private static boolean debug = false;
	

	/*******************************************************************************
	 * 抽象メソッド
	 ******************************************************************************/
	
	protected abstract Env getEnv();
	protected abstract Bounds getBoundsEnv();
	
	protected abstract HDDRecorderList getRecorderList();
	
	//protected abstract StatusWindow getStWin(); 
	//protected abstract StatusTextArea getMWin();
	protected abstract AbsReserveDialog getReserveDialog();
	
	protected abstract Component getParentComponent();
	
	protected abstract void ringBeep();
	
	/**
	 * 予約マーク・予約枠を更新してほしい
	 */
	protected abstract void updateReserveDisplay(String chname);

	/**
	 * 予約実行を更新してほしい
	 */
	protected abstract boolean doExecOnOff(boolean fexec, String title, String chnam, String rsvId, String recId);

	/**
	 *  予約実行をONOFFするメニューアイテム
	 */
	protected abstract JMenuItem getExecOnOffMenuItem(final boolean fexec, final String title, final String chnam, final String rsvId, final String recId);
	
	/**
	 *  予約を削除するメニューアイテム
	 */
	protected abstract JMenuItem getRemoveRsvMenuItem(final String title, final String chnam, final String rsvId, final String recId);
	
	/**
	 *  新聞形式へジャンプするメニューアイテム
	 */
	protected abstract JMenuItem getJumpMenuItem(final String title, final String chnam, final String startDT);
	protected abstract JMenuItem getJumpToLastWeekMenuItem(final String title, final String chnam, final String startDT);
	
	/**
	 * @see Viewer.VWToolBar#getSelectedRecorder()
	 */
	protected abstract String getSelectedRecorderOnToolbar();
	

	/*******************************************************************************
	 * 定数
	 ******************************************************************************/
	
	private static final String MSGID = "[本体予約一覧] ";
	private static final String ERRID = "[ERROR]"+MSGID;
	private static final String DBGID = "[DEBUG]"+MSGID;
	
	private static final String DUPMARK_NORMAL = "■";
	private static final String DUPMARK_REP = "□";
	private static final String DUPMARK_COLOR = "#FFB6C1";

	private static final String VALID_CHNAME_COLOR = "#0000ff";
	private static final String INVALID_CHNAME_COLOR = "#ff0000";

	private static final String ICONFILE_EXEC			= "icon/media-record-3.png";
	
	/*******************************************************************************
	 * 部品
	 ******************************************************************************/
	
	// オブジェクト
	private final Env env = getEnv();
	private final Bounds bounds = getBoundsEnv();
	private final HDDRecorderList recorders = getRecorderList();

	//private final StatusWindow StWin = getStWin();			// これは起動時に作成されたまま変更されないオブジェクト
	//private final StatusTextArea MWin = getMWin();			// これは起動時に作成されたまま変更されないオブジェクト
	private final AbsReserveDialog rD = getReserveDialog();	// これは起動時に作成されたまま変更されないオブジェクト
	
	private final Component parent = getParentComponent();	// これは起動時に作成されたまま変更されないオブジェクト
	
	// メソッド
	//private void StdAppendMessage(String message) { System.out.println(message); }
	//private void StdAppendError(String message) { System.err.println(message); }
	//private void StWinSetVisible(boolean b) { StWin.setVisible(b); }
	//private void StWinSetLocationCenter(Component frame) { CommonSwingUtils.setLocationCenter(frame, (VWStatusWindow)StWin); }

	private final ImageIcon execicon = new ImageIcon(ICONFILE_EXEC);
			
	/**
	 * カラム定義
	 */
	
	public static HashMap<String,Integer> getColumnIniWidthMap() {
		if (rcmap.size() == 0 ) {
			for ( RsvedColumn rc : RsvedColumn.values() ) {
				rcmap.put(rc.toString(),rc.getIniWidth());	// toString()!
			}
		}
		return rcmap;
	}
	
	private static final HashMap<String,Integer> rcmap = new HashMap<String, Integer>();
	
	public static enum RsvedColumn {
		PATTERN		("パタン",			110),
		DUPMARK		("重複",			35),
		EXEC		("実行",			35),
		TRACE		("追跡",			35),
		AUTO		("自動",			35),
		NEXTSTART	("次回実行予定",	150),
		END			("終了",			50),
		LENGTH		("長さ",			50),
		ENCODER		("ｴﾝｺｰﾀﾞ",		50),
		VRATE		("画質",			100),
		ARATE		("音質",			50),
		TITLE		("番組タイトル",	300),
		CHNAME		("チャンネル名",	150),
		RECORDER	("レコーダ",		200),
		
		/*
		HID_INDEX	("INDEX",		-1),
		HID_RSVID	("RSVID",		-1),
		*/
		;

		private String name;
		private int iniWidth;

		private RsvedColumn(String name, int iniWidth) {
			this.name = name;
			this.iniWidth = iniWidth;
		}

		@Override
		public String toString() {
			return name;
		}

		public int getIniWidth() {
			return iniWidth;
		}
		
		public int getColumn() {
			return ordinal();
		}
	};
	

	/*******************************************************************************
	 * コンポーネント
	 ******************************************************************************/

	private class ReservedItem extends RowItem implements Cloneable {
	
		String pattern;
		String dupmark;
		Boolean exec;
		String trace;
		String auto;
		String nextstart;	// YYYY/MM/DD(WD) hh:mm
		String end;			// hh:mm
		String length;
		String encoder;
		String vrate;
		String arate;
		String title;
		String chname;
		String recorder;
		
		String hide_chname;
		String hide_rsvid;
		String hide_centercolor;
		String hide_encodercolor;
		String hide_itecolor;
		Boolean hide_tunershort;
		Boolean hide_recorded;
		
		@Override
		protected void myrefresh(RowItem o) {
			ReservedItem c = (ReservedItem) o;
			
			c.addData(pattern);
			c.addData(dupmark);
			c.addData(exec);
			c.addData(trace);
			c.addData(auto);
			c.addData(nextstart);
			c.addData(end);
			c.addData(length);
			c.addData(encoder);
			c.addData(vrate);
			c.addData(arate);
			c.addData(title);
			c.addData(chname);
			c.addData(recorder);
		}
		
		public ReservedItem clone() {
			return (ReservedItem) super.clone();
		}
	}

	// ソートが必要な場合はTableModelを作る。ただし、その場合Viewのrowがわからないので行の入れ替えが行えない
	private class ReservedTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		
		private RowItemList<ReservedItem> rDat;
		
		@Override
		public Object getValueAt(int row, int column) {
			ReservedItem c = rDat.get(row); 
			if ( c.getColumnCount() > column ) {
				if ( column == RsvedColumn.DUPMARK.getColumn() ) {
					return CommonSwingUtils.getColoredString(DUPMARK_COLOR,c.dupmark);
				}
				else if ( column == RsvedColumn.ENCODER.getColumn() ) {
					return CommonSwingUtils.getColoredString(c.hide_encodercolor,c.encoder);
				}
				else if ( column == RsvedColumn.TITLE.getColumn() ) {
					if ( c.hide_itecolor!=null ) {
						return CommonSwingUtils.getColoredString(c.hide_itecolor,c.title);
					}
					else {
						return c.title;
					}
				}
				else if ( column == RsvedColumn.CHNAME.getColumn() ) {
					return CommonSwingUtils.getColoredString(c.hide_centercolor,c.chname);
				}
				else if ( column == RsvedColumn.LENGTH.getColumn() ) {
					return c.length+"m";
				}
				return c.get(column);
			}
			return null;
		}
		
		@Override
		public void setValueAt(Object aValue, int row, int column) {
			/*
			ReservedItem c = rowView.get(row);
			if ( column == RsvedColumn.EXEC.getColumn() ) {
				//c.exec = (Boolean) aValue;
				//c.fireChanged();
			}
			*/
		}
		
		@Override
		public int getRowCount() {
			return (rDat!=null) ? rDat.size() : 0;	// ↓ のsuper()で呼ばれるのでnullチェックが必要
		}

		public ReservedItem getRowItem(int row) { return rDat.get(row); }
			
		public ReservedTableModel(String[] colname, int i, RowItemList<ReservedItem> rowdata) {
			super(colname,i);
			this.rDat = rowdata;
		}
		
	}
	
	//private final ReservedItem sa = new ReservedItem();
	
	private JNETableReserved jTable_rsved = null;
	private JTable jTable_rowheader = null;

	private DefaultTableModel tableModel_rsved = null;
	
	private DefaultTableModel rowheaderModel_rsved = null;
	
	// 表示用のテーブル
	private final RowItemList<ReservedItem> rowViewTemp = new RowItemList<ReservedItem>();
	
	// テーブルの実体
	private final RowItemList<ReservedItem> rowData = new RowItemList<ReservedItem>();
	
	/*******************************************************************************
	 * コンストラクタ
	 ******************************************************************************/
	
	public AbsReserveListView() {
		
		super();
		
		this.setRowHeaderView(jTable_rowheader = new JTableRowHeader(rowViewTemp));
		this.setViewportView(getNETable_rsved());
		
		Dimension d = new Dimension(jTable_rowheader.getPreferredSize().width,0);
		this.getRowHeader().setPreferredSize(d);
		
		this.setRowHeaderVisible(env.getRowHeaderVisible());
		
		// バグ対応
		if ( bounds.getRsvedColumnSize() == null ) {
			System.err.println(ERRID+"なんらかの不具合によりテーブルのカラム幅設定が取得できませんでした。設定はリセットされました。申し訳ありません。");
			bounds.setRsvedColumnSize(rcmap);
		}
		else {
			for ( Entry<String, Integer> en : rcmap.entrySet() ) {
				try {
					bounds.getListedColumnSize().get(en.getKey());
				}
				catch (NullPointerException e) {
					System.err.println(ERRID+en.getKey()+", "+e.toString());
					bounds.getListedColumnSize().put(en.getKey(),en.getValue());
				}
			}
		}
		
		//
		this.addComponentListener(cl_tabshown);
	}
	

	/*******************************************************************************
	 * アクション
	 ******************************************************************************/
	
	// 対外的な
	
	/**
	 * 予約一覧を描画してほしいかなって
	 * ★synchronized(rowData)★
	 * @see #cl_tabshown
	 */
	public void redrawReservedList() {
		// ★★★　イベントにトリガーされた処理がかちあわないように synchronized()　★★★
		synchronized ( rowViewTemp ) {
			_redrawReservedList();
		}
	}
		
	private void _redrawReservedList() {
		
		//
		rowData.clear();
		
		// 選択されたレコーダ
		String myself = getSelectedRecorderOnToolbar();
		HDDRecorderList recs = recorders.findInstance(myself);

		// 現在日時
		String curDateTime = CommonUtils.getDateTime(0);
		
		// 繰り返し予約関連
		String itecolor = CommonUtils.color2str(env.getIterationItemForeground());
		int maxn = ( env.getShowAllIterationItem() ) ? (env.getDogDays()) : (1);

		// 背景色
		jTable_rsved.setTunerShortColor(env.getTunerShortColor());
		jTable_rsved.setRecordedColor(env.getRecordedColor());

		for ( HDDRecorder recorder : recs )
		{
			if ( recorder.isBackgroundOnly() ) {
				continue;
			}
			
			// 終了した番組があれば整理
			recorder.refreshReserves();
			
			// 並べ替えるために新しいリストを作成する
			for ( ReserveList ro : recorder.getReserves() ) {
				ArrayList<String> starts = new ArrayList<String>();
				ArrayList<String> ends = new ArrayList<String>();
				CommonUtils.getStartEndList(starts, ends, ro);
				for ( int n=0; n<starts.size() && n<maxn; n++ ) {
					if ( ! env.getDisplayPassedReserve() ) {
						if ( ends.get(n).compareTo(curDateTime) < 0 ) {
							continue;
						}
					}
					
					ReservedItem sa = new ReservedItem();
					
					sa.pattern = ro.getRec_pattern();
					sa.dupmark = "";
					sa.exec = ro.getExec();
					sa.trace = ((ro.getPursues())?("追"):(""));
					sa.auto = ((ro.getAutoreserved())?("○"):(""));
					sa.nextstart = CommonUtils.getDate(CommonUtils.getCalendar(starts.get(n)))+" "+ro.getAhh()+":"+ro.getAmm();	// YYYY/MM/DD(WD) hh:mm
					sa.end = ro.getZhh()+":"+ro.getZmm();			// hh:mm
					sa.length = ro.getRec_min();
					sa.encoder = (ro.getTuner() != null)?(ro.getTuner()):("★エンコーダ不正");
					sa.vrate = getRec_mode(ro);
					sa.arate = ro.getRec_audio();
					sa.title = ro.getTitle();
					sa.chname = (ro.getCh_name()!=null && ro.getCh_name().length()>0)?(ro.getCh_name()):("★放送局名不正("+ro.getChannel()+")");
					sa.recorder = recorder.Myself();
					
					sa.hide_chname = (ro.getCh_name()!=null)?(ro.getCh_name()):("");
					sa.hide_rsvid = ro.getId();
					sa.hide_centercolor = (ro.getCh_name()!=null && ro.getCh_name().length()>0)?(VALID_CHNAME_COLOR):(INVALID_CHNAME_COLOR);
					sa.hide_encodercolor = recorder.getColor(ro.getTuner());
					sa.hide_itecolor = (n>0)?(itecolor):(null);
					sa.hide_tunershort = ro.getTunershort();
					sa.hide_recorded = ro.getRecorded();

					sa.fireChanged();
					
					addRow(sa);
				}
			}
		}
		
		// 表示用
		rowViewTemp.clear();
		for ( ReservedItem a : rowData ) {
			rowViewTemp.add(a);
		}
		
		tableModel_rsved.fireTableDataChanged();
		((DefaultTableModel)jTable_rowheader.getModel()).fireTableDataChanged();
		
		setOverlapMark();
	}
	
	private String getRec_mode(ReserveList reserve) { String s = ((reserve.getAppsRsv())?(reserve.getRec_mvchapter()):(reserve.getRec_mode())); return (s==null)?(""):(s); }
	
	/**
	 * 絞り込み検索の本体（現在リストアップされているものから絞り込みを行う）（親から呼ばれるよ！）
	 */
	public void redrawListByKeywordFilter(SearchKey keyword, String target) {
		
		rowViewTemp.clear();
		
		// 情報を一行ずつチェックする
		if ( keyword != null ) {
			for ( ReservedItem a : rowData ) {
				
				ProgDetailList tvd = new ProgDetailList();
				tvd.title = a.title;
				tvd.titlePop = TraceProgram.replacePop(tvd.title);
				
				// タイトルを整形しなおす
				boolean isFind = SearchProgram.isMatchKeyword(keyword, "", tvd);
				
				if ( isFind ) {
					rowViewTemp.add(a);
				}
			}
		}
		else {
			for ( ReservedItem a : rowData ) {
				rowViewTemp.add(a);
			}
		}
		
		// fire!
		tableModel_rsved.fireTableDataChanged();
		rowheaderModel_rsved.fireTableDataChanged();
	}
	
	/**
	 * カラム幅を保存する（鯛ナビ終了時に呼び出されるメソッド）
	 */
	public void copyColumnWidth() {
		DefaultTableColumnModel columnModel = (DefaultTableColumnModel)jTable_rsved.getColumnModel();
		TableColumn column = null;
		for ( RsvedColumn rc : RsvedColumn.values() ) {
			if ( rc.getIniWidth() < 0 ) {
				continue;
			}
			column = columnModel.getColumn(rc.ordinal());
			bounds.getRsvedColumnSize().put(rc.toString(), column.getPreferredWidth());
		}
	}
	
	/**
	 * テーブルの行番号の表示のＯＮ／ＯＦＦ
	 */
	public void setRowHeaderVisible(boolean b) {
		this.getRowHeader().setVisible(b);
	}
	
	// 内部的な
	
	/**
	 * テーブル（の中の人）に追加
	 */
	private void addRow(ReservedItem data) {
		// 開始日時＋放送局でソート
		int i=0;
		for (; i<rowData.size(); i++) {
			ReservedItem ra = rowData.get(i);
			int x = ra.nextstart.compareTo(data.nextstart);
			int y = ra.hide_chname.compareTo(data.hide_chname);
			int z = ra.encoder.compareTo(data.encoder);
			if (x == 0 && y == 0 && z > 0) {
				break;	// 挿入位置確定
			}
			if (x == 0 && y > 0) {
				break;	// 挿入位置確定
			}
			else if (x > 0) {
				break;	// 挿入位置確定
			}
		}
		
		// 有効データ
		rowData.add(i, data);
	}

	/**
	 * 重複マークつけてください
	 */
	private void setOverlapMark() {
		
		if ( rowViewTemp.size() < 2 ) {
			return;
		}
		
		// 最初の一行はリセットしておかないとなんの処理も行われない場合がある
		ReservedItem fr = rowViewTemp.get(jTable_rsved.convertRowIndexToModel(0));
		fr.dupmark = "";
		fr.fireChanged();
		
		// 時間重複のマーキング
		String sDT = "";
		String eDT = "";
		String sDT2 = "";
		String eDT2 = "";
		for (int i=0; i<jTable_rsved.getRowCount()-1; i++) {
			int vrow = jTable_rsved.convertRowIndexToModel(i);
			int vrow2 = jTable_rsved.convertRowIndexToModel(i+1);

			ReservedItem ra = rowViewTemp.get(vrow);
			ReservedItem rb = rowViewTemp.get(vrow2);

			if ( ! sDT2.equals("")) {
				sDT = sDT2;
				eDT = eDT2;
			}
			else {
				GregorianCalendar ca = CommonUtils.getCalendar(ra.nextstart);
				if ( ca != null ) {
					sDT = CommonUtils.getDateTime(ca);
					
					int len = Integer.valueOf(ra.length);
					ca.add(Calendar.MINUTE, len);
					eDT = CommonUtils.getDateTime(ca);
				}
			}
			
			{
				GregorianCalendar ca = CommonUtils.getCalendar(rb.nextstart);
				if ( ca != null ) {
					sDT2 = CommonUtils.getDateTime(ca);
					
					int len = Integer.valueOf(rb.length);
					ca.add(Calendar.MINUTE, len);
					eDT2 = CommonUtils.getDateTime(ca);
				}
			}
			
			if ( eDT.equals(sDT2) ) {
				ra.dupmark = rb.dupmark = DUPMARK_REP;
				ra.fireChanged();
				rb.fireChanged();
			}
			else if ( CommonUtils.isOverlap(sDT, eDT, sDT2, eDT2, false) ) {
				ra.dupmark = rb.dupmark = DUPMARK_NORMAL;
				ra.fireChanged();
				rb.fireChanged();
			}
			else {
				//raは操作しない
				rb.dupmark = "";
				rb.fireChanged();
			}
		}
	}
	
	/**
	 * 予約を編集したい
	 */
	private void editReserve(String recId,String rsvId,String chnam,int vrow) {

		//VWReserveDialog rD = new VWReserveDialog(0, 0, env, tvprograms, recorders, avs, chavs, stwin);
		//rD.clear();
		CommonSwingUtils.setLocationCenter(parent,rD);
		
		if (rD.open(recId,rsvId)) {
			rD.setVisible(true);
		}
		else {
			rD.setVisible(false);
		}
		
		if (rD.isReserved()) {
			// よそさま
			updateReserveDisplay(chnam);
			// じぶん
			_redrawReservedList();
			// フォーカスを戻す
			jTable_rsved.getSelectionModel().setSelectionInterval(vrow,vrow);
		}
	}
	

	/*******************************************************************************
	 * リスナー
	 ******************************************************************************/
	
	/**
	 * タブが開かれたら表を書き換える
	 * ★synchronized(rowData)★
	 * @see #redrawReservedList()
	 */
	private final ComponentAdapter cl_tabshown = new ComponentAdapter() {
		@Override
		public void componentShown(ComponentEvent e) {
			// ★★★　イベントにトリガーされた処理がかちあわないように synchronized()　★★★
			synchronized ( rowViewTemp ) {
				// 終了した予約を整理する
				for (HDDRecorder recorder : recorders) {
					recorder.refreshReserves();
				}
				
				// 予約一覧を再構築する
				_redrawReservedList();
			}
		}
	};
	
	private final MouseAdapter ma_showpopup = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			//
			Point p = e.getPoint();
			final int vrow = jTable_rsved.rowAtPoint(p);
			jTable_rsved.getSelectionModel().setSelectionInterval(vrow,vrow);
			//
			final int row = jTable_rsved.convertRowIndexToModel(vrow);
			ReservedItem ra = rowViewTemp.get(row);
			final boolean fexec = ra.exec;
			final String start = ra.nextstart;
			final String title = ra.title;
			final String chnam = ra.hide_chname;
			final String recId = ra.recorder;
			final String rsvId = ra.hide_rsvid;
			//
			if (e.getButton() == MouseEvent.BUTTON3) {
				if (e.getClickCount() == 1) {
					// 右クリックで予約削除メニュー表示
					JPopupMenu pop = new JPopupMenu();
					//
					{
						JMenuItem menuItem = new JMenuItem("予約を編集する");
						menuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								editReserve(recId,rsvId,chnam,vrow);
							}
						});
						pop.add(menuItem);
					}

					pop.addSeparator();

					// 予約実行ON・OFF
					{
						pop.add(getExecOnOffMenuItem(fexec,title,chnam,rsvId,recId));
					}
					
					pop.addSeparator();
					
					{
						pop.add(getRemoveRsvMenuItem(title,chnam,rsvId,recId));
					}
					
					pop.addSeparator();
					
					{
						pop.add(getJumpMenuItem(title,chnam,start));
						pop.add(getJumpToLastWeekMenuItem(title,chnam,start));
					}
					
					pop.show(jTable_rsved, e.getX(), e.getY());
				}
			}
			else if (e.getButton() == MouseEvent.BUTTON1) {
				if (e.getClickCount() == 1) {
					
				}
				else if (e.getClickCount() == 2) {
					// 左ダブルクリックで予約ウィンドウを開く
					editReserve(recId,rsvId,chnam,vrow);
				}
			}
		}
	};
	
	private final RowSorterListener rsl_sorterchanged = new RowSorterListener() {
		@Override
		public void sorterChanged(RowSorterEvent e) {
			if ( e.getType() == Type.SORTED ) {
				if (rowViewTemp.size()>=2) setOverlapMark();
			}
		}
	}; 
	
	/*******************************************************************************
	 * コンポーネント
	 ******************************************************************************/
	
	private JNETableReserved getNETable_rsved() {
		if (jTable_rsved == null) {
			
			ArrayList<String> cola = new ArrayList<String>();
			for ( RsvedColumn rc : RsvedColumn.values() ) {
				if ( rc.getIniWidth() >= 0 ) {
					cola.add(rc.toString());
				}
			}
			String[] colname = cola.toArray(new String[0]);
			
			tableModel_rsved = new ReservedTableModel(colname, 0, rowViewTemp);
			jTable_rsved = new JNETableReserved(tableModel_rsved, false);
			jTable_rsved.setAutoResizeMode(JNETable.AUTO_RESIZE_OFF);
			
			// ヘッダのモデル
			rowheaderModel_rsved = (DefaultTableModel) jTable_rowheader.getModel();
			
			// ソータを付ける
			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel_rsved);
			jTable_rsved.setRowSorter(sorter);
			
			sorter.addRowSorterListener(rsl_sorterchanged);
			
			// 数値でソートする項目用の計算式（番組長とか）
			final Comparator<String> titlecomp = new Comparator<String>() {
				
				@Override
				public int compare(String o1, String o2) {
					String t1 = TraceProgram.replacePop(o1.replaceAll(TVProgram.titlePrefixRemoveExpr, "")).replaceFirst(TVProgram.epnoNormalizeExpr, "$1\\0$2");
					String t2 = TraceProgram.replacePop(o2.replaceAll(TVProgram.titlePrefixRemoveExpr, "")).replaceFirst(TVProgram.epnoNormalizeExpr, "$1\\0$2");
					return t1.compareTo(t2);
				}
			};
			
			// ソーターの効かない項目用の計算式（重複マーク）
			final Comparator<String> noncomp = new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return 0;
				}
			};
			
			sorter.setComparator(jTable_rsved.getColumn(RsvedColumn.TITLE.toString()).getModelIndex(),titlecomp);
			sorter.setComparator(jTable_rsved.getColumn(RsvedColumn.DUPMARK.toString()).getModelIndex(),noncomp);
			
			// 各カラムの幅
			DefaultTableColumnModel columnModel = (DefaultTableColumnModel)jTable_rsved.getColumnModel();
			TableColumn column = null;
			for ( RsvedColumn rc : RsvedColumn.values() ) {
				if ( rc.getIniWidth() < 0 ) {
					continue;
				}
				column = columnModel.getColumn(rc.ordinal());
				column.setPreferredWidth(bounds.getRsvedColumnSize().get(rc.toString()));
			}
			
			// 重複マーク・実行マークはちょっとだけ表示の仕方が違う
			VWColorCharCellRenderer renderer = new VWColorCharCellRenderer(JLabel.CENTER); 
			if ( CommonUtils.isMac() ) renderer.setMacMarkFont();
			jTable_rsved.getColumn(RsvedColumn.DUPMARK.toString()).setCellRenderer(renderer);
			ButtonColumn buttonColumn = new ButtonColumn(execicon);
			jTable_rsved.getColumn(RsvedColumn.EXEC.toString()).setCellRenderer(buttonColumn);
			jTable_rsved.getColumn(RsvedColumn.EXEC.toString()).setCellEditor(buttonColumn);
			jTable_rsved.getColumn(RsvedColumn.EXEC.toString()).setResizable(false);
			jTable_rsved.getColumn(RsvedColumn.TRACE.toString()).setCellRenderer(renderer);
			jTable_rsved.getColumn(RsvedColumn.AUTO.toString()).setCellRenderer(renderer);

			VWColorCharCellRenderer renderer2 = new VWColorCharCellRenderer(JLabel.LEFT); 
			jTable_rsved.getColumn(RsvedColumn.TITLE.toString()).setCellRenderer(renderer2);
			jTable_rsved.getColumn(RsvedColumn.CHNAME.toString()).setCellRenderer(renderer2);
			jTable_rsved.getColumn(RsvedColumn.VRATE.toString()).setCellRenderer(renderer2);
			
			VWColorCharCellRenderer renderer3 = new VWColorCharCellRenderer(JLabel.LEFT); 
			jTable_rsved.getColumn(RsvedColumn.ENCODER.toString()).setCellRenderer(renderer3);

			// 一覧表クリックで削除メニュー出現
			jTable_rsved.addMouseListener(ma_showpopup);
		}
		return jTable_rsved;
	}

	
	
	
	/*******************************************************************************
	 * 表表示
	 ******************************************************************************/
	
	private class JNETableReserved extends JNETable {

		private static final long serialVersionUID = 1L;

		// futuer use.
		public void setDisabledColor(Color c) { disabledColor = c; }
		private Color disabledColor = new Color(180,180,180);

		public void setTunerShortColor(Color c) { tunershortColor = c; }
		private Color tunershortColor = new Color(255,255,0);
		
		public void setRecordedColor(Color c) { recordedColor = c; }
		private Color recordedColor = new Color(204,153,255);
		
		private int prechkrow = -1;
		private boolean prechkdisabled = false;
		private boolean prechktunershort = false;
		private boolean prechkrecorded = false;
		
		@Override
		public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
			Component c = super.prepareRenderer(tcr, row, column);
			Color fgColor = null;
			Color bgColor = null;
			if(isRowSelected(row)) {
				fgColor = this.getSelectionForeground();
				bgColor = this.getSelectionBackground();
			}
			else {
				fgColor = this.getForeground();
				isRowPassed(row);
				if ( prechkdisabled ) {
					bgColor = disabledColor;
				}
				else if ( prechktunershort ) {
					bgColor = tunershortColor;
				}
				else if ( prechkrecorded ) {
					bgColor = recordedColor;
				}
				else {
					bgColor = (isSepRowColor && row%2 == 1)?(evenColor):(super.getBackground());
				}
			}
			if ( ! (tcr instanceof VWColorCharCellRenderer) && ! (tcr instanceof VWColorCharCellRenderer2) && ! (tcr instanceof VWColorCellRenderer)) {
				c.setForeground(fgColor);
			}		
			if ( ! (tcr instanceof VWColorCellRenderer)) {
				c.setBackground(bgColor);
			}
			return c;
		}
		
		// 連続して同じ行へのアクセスがあったら計算を行わず前回のままにする
		private boolean isRowPassed(int prow) {
			
			if(prechkrow == prow) {
				return prechkdisabled;
			}

			int row = this.convertRowIndexToModel(prow);
			ReservedItem c = rowViewTemp.get(row);

			{
				// 実行可能かどうか
				prechkrow = prow;
				prechkdisabled = ! c.exec;
				prechktunershort = c.hide_tunershort;
				prechkrecorded = c.hide_recorded;
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
			prechkdisabled = false;
			prechktunershort = false;
			prechkrecorded = false;
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			if ( column == RsvedColumn.EXEC.getColumn() ) {
				return true;
			}
			return false;
		}
		
		// コンストラクタ
		public JNETableReserved(boolean b) {
			super(b);
		}
		public JNETableReserved(TableModel d, boolean b) {
			super(d,b);
		}
	}
	
	
	/**
	 * EXECボタン
	 */
	private class ButtonColumn extends AbstractExecButtonColumn {

		private static final long serialVersionUID = 1L;

		// コンストラクタ
		public ButtonColumn(ImageIcon icon) {
			super(icon);
		}
		
		@Override
		protected void toggleAction(ActionEvent e) {
			
			fireEditingStopped();
			
			int vrow = jTable_rsved.getSelectedRow();
			int row = jTable_rsved.convertRowIndexToModel(vrow);
			
			ReservedItem c = ((ReservedTableModel) jTable_rsved.getModel()).getRowItem(row);
			
			if ( doExecOnOff( ! c.exec, c.title, c.chname, c.hide_rsvid, c.recorder) ) {
				c.exec = ! c.exec;
				c.fireChanged();
			}
			
			jTable_rsved.clearSelection();
			jTable_rsved.setRowSelectionInterval(vrow, vrow);
		}
	}
}
