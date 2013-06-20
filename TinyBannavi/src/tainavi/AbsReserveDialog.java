package tainavi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import tainavi.HDDRecorder.RecType;
import tainavi.TVProgram.ProgGenre;
import tainavi.TVProgram.ProgOption;
import tainavi.TVProgram.ProgSubgenre;


/**
 * 予約ダイアログのクラス
 * @since 3.15.4β　ReserveDialogからクラス名変更
 */
abstract class AbsReserveDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public static String getViewName() { return "予約ダイアログ"; }
	
	public void setDebug(boolean b) { debug = b; }
	private static boolean debug = false;
	
	/*******************************************************************************
	 * 抽象メソッド
	 ******************************************************************************/

	protected abstract Env getEnv();
	protected abstract TVProgramList getTVProgramList();
	protected abstract HDDRecorderList getRecorderList();

	protected abstract AVSetting getAVSetting();
	protected abstract CHAVSetting getCHAVSetting();

	protected abstract StatusWindow getStWin(); 
	protected abstract StatusTextArea getMWin();

	protected abstract Component getParentComponent();
	
	protected abstract void ringBeep();

	// クラス内のイベントから呼び出されるもの
	protected abstract void searchLikeRsv(LikeReserveList likeRsvList, ProgDetailList tvd, String keyword, int threshold);
	protected abstract String getSelectedRecorderOnToolbar();
	
	/*******************************************************************************
	 * 呼び出し元から引き継いだもの
	 ******************************************************************************/
	
	private final Env env = getEnv();
	private final HDDRecorderList recorders = getRecorderList();
	
	private final AVSetting avs = getAVSetting();
	private final CHAVSetting chavs = getCHAVSetting();
	
	private final StatusWindow StWin = getStWin();			// これは起動時に作成されたまま変更されないオブジェクト
	private final StatusTextArea MWin = getMWin();			// これは起動時に作成されたまま変更されないオブジェクト
	
	private final Component parent = getParentComponent();	// これは起動時に作成されたまま変更されないオブジェクト
	
	private final GetEventId geteventid = new GetEventId();	// 番組IDの取得
	
	/*******************************************************************************
	 * 定数
	 ******************************************************************************/
	
	private enum ChangedSelector { ALL, RECORDER, GENRE, LIKELIST };
	
	private static final String ITEM_YES = "する";
	private static final String ITEM_NO = "しない";
	
	private static final String ITEM_EVIDNEEDED = "番組ID取得";
	
	private static final int LIKERSVTABLE_NOTSELECTED = -1;
	
	private static final String LIKERSVID_NONE			= "（類似予約なし）";
	private static final String LIKERSVID_NOTSELECTED	= "類似予約を選択しない";
	
	// レイアウト関連
	
	private static final int TITLE_WIDTH = 370;
	private static final int CHNAME_WIDTH = 240;
	private static final int RECORDER_WIDTH = 300;
	private static final int ENCODER_WIDTH = 150;
	private static final int BOX_WIDTH = 730;
	private static final int LIKELIST_WIDTH = BOX_WIDTH;
	private static final int LIKELIST_ROWS = 4;
	
	private static final int PARTS_HEIGHT = 25;
	private static final int SEP_WIDTH = 10;
	private static final int SEP_WIDTH_NARROW = 5;
	private static final int SEP_HEIGHT = 10;
	private static final int SEP_HEIGHT_NALLOW = 5;

	private static final int LABEL_WIDTH = 150;
	private static final int COMBO_WIDTH = 115;
	private static final int COMBO_WIDTH_WIDE = 155;
	private static final int COMBO_HEIGHT = 50;

	private static final int PANEL_WIDTH = 760;
	
	private static final int LRT_HEADER_WIDTH = 20;
	private static final int LRT_TITLE_WIDTH = 325;
	private static final int LRT_START_WIDTH = 115;
	private static final int LRT_RECORDER_WIDTH = 185;
	private static final int LRT_ENCODER_WIDTH = 60;
	
	public static enum LikeRsvColumn {
		TITLE		("予約名",	LRT_TITLE_WIDTH),
		START		("開始日時",	LRT_START_WIDTH),
		RECORDER	("レコーダ",	LRT_RECORDER_WIDTH),
		TUNER		("ｴﾝｺｰﾀﾞ",	LRT_ENCODER_WIDTH),
		;

		private String name;
		private int iniWidth;

		private LikeRsvColumn(String name, int iniWidth) {
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
	
	//
	
	private static final String TEXT_SAVEDEFAULT = "<HTML>録画設定を開いた時の枠内のデフォルト値として<BR>現在の値を使用するようにします。<BR><FONT COLOR=#FF0000>※ジャンル別ＡＶ設定があればそちらが優先されます。</FONT></HTML>";
	
	// ログ関連
	
	private static final String MSGID = "["+getViewName()+"] ";
	private static final String ERRID = "[ERROR]"+MSGID;
	private static final String DBGID = "[DEBUG]"+MSGID;
	
	/*******************************************************************************
	 * 部品
	 ******************************************************************************/
	
	// コンポーネント
	
	private JPanel jContentPane_rsv = null;

	private JLabel jLabel_title = null;
	private JComboBox jComboBox_ch = null;
	private JLabel jLabel_ch= null;
	private JLabel jLabel_encoder = null;
	private JComboBoxWithPopup jComboBox_title = null;
	private JWideComboBox jComboBox_encoder = null;
	private JButton jButton_getEventId = null;
	private JLabel jLabel_encoderemptywarn = null;
	private JLabel jLabel_date = null;
	private JLabel jLabel_ahh = null;
	private JLabel jLabel_zhh = null;
	private JLabel jLabel_recorders = null;
	private JComboBox jComboBox_recorder = null;
	private JComboBox jComboBox_date = null;
	private JTextField jTextField_ahh = null;
	private JTextField jTextField_amm = null;
	private JLabel jLabel_asep = null;
	private JButton jButton_amm_up = null;
	private JButton jButton_amm_down = null;
	private JTextField jTextField_Xahh = null;
	private JTextField jTextField_Xamm = null;
	private JLabel jLabel_Xasep = null;
	private JTextField jTextField_zhh = null;
	private JTextField jTextField_zmm = null;
	private JLabel jLabel_zsep = null;
	private JButton jButton_zmm_up = null;
	private JButton jButton_zmm_down = null;
	private JTextField jTextField_Xzhh = null;
	private JTextField jTextField_Xzmm = null;
	private JLabel jLabel_Xzsep = null;
	private JButton jButton_Xreset = null;
	private JLabel jLabel_detail = null;
	private JScrollPane jScrollPane_detail = null;
	private JTextAreaWithPopup jTextArea_detail = null;
	private JCheckBoxPanel jCheckBox_OverlapDown2 = null;
	private JCheckBoxPanel jCheckBox_spoex_extend = null;
	private JLabel jLabel_rectype = null;
	private JButton jButton_load = null;
	private JButton jButton_save = null;
	private JButton jButton_savedefault = null;
	private JComboBoxPanel jCBXPanel_genre = null;
	private JComboBoxPanel jCBXPanel_subgenre = null;
	private JComboBoxPanel jCBXPanel_videorate = null;
	private JComboBoxPanel jCBXPanel_audiorate = null;
	private JComboBoxPanel jCBXPanel_folder = null;
	private JComboBoxPanel jCBXPanel_dvdcompat = null;
	private JComboBoxPanel jCBXPanel_device = null;
	private JComboBoxPanel jCBXPanel_aspect = null;
	private JComboBoxPanel jCBXPanel_bvperf = null;
	private JComboBoxPanel jCBXPanel_lvoice = null;
	private JComboBoxPanel jCBXPanel_autodel = null;
	private JComboBoxPanel jCBXPanel_pursues = null;
	private JButton jButton_update = null;
	private JButton jButton_record = null;
	private JButton jButton_cancel = null;
	private JCheckBoxPanel jCheckBox_Exec = null;
	private JCheckBoxPanel jCheckBox_Autocomplete = null;
	private JButton jButton_addDate = null;
	
	private LikeRsvRowHeader likersvrowheader = null;
	private JScrollPane likersvpane = null;
	private LikeRsvTable likersvtable = null;
	
	private JComboBoxPanel jCBXPanel_xChapter = null;
	private JComboBoxPanel jCBXPanel_msChapter = null;
	private JComboBoxPanel jCBXPanel_mvChapter = null;
	
	/*
	 * その他
	 */
	
	/**
	 * 初期化漏れが怖いのでまとめて内部クラスとした。
	 */
	private class Vals {
		
		// 検索した類似予約を保持する
		final LikeReserveList likeRsvList = new LikeReserveList();
		LikeReserveItem selectedLikeRsv = null; 
		
		// 類似予約抽出条件（タイトル）
		String keyword = "";
		// 類似予約抽出条件（あいまい度）
		int threshold = 0;
		
		// オープン時の単日指定の値（状態リセット用）
		String byDateIni = "";
		// オープン時の週次予約の値（状態リセット用）
		String byWeeklyIni = "";
		
		// 延長警告分のばすかどうか
		boolean isExtended = false;
		// おしりを１分削るかどうか
		boolean isClipped = false;
		// 実行のON/OFFのみの更新かどうか
		boolean isUpdateOnlyExec = false;
	
		// 予約する番組情報
		ProgDetailList hide_tvd = null;
		// 番組IDはUI上には表示されない隠し項目
		String hide_content_id = null;
		// 未編集の番組開始日時
		String hide_startdatetime = null;
		
		// 開いたときの選択レコーダ
		HDDRecorder hide_default_recorder = null;
		
		// 本体予約一覧から開かれたかどうか
		boolean hide_atreservedlist = false;
		
	}
	
	private Vals vals = null;
	
	/**
	 * 予約操作が成功したかどうかを返す。
	 */
	public boolean isReserved() { return doneReserve; }

	private boolean doneReserve = false;

	/*******************************************************************************
	 * コンストラクタ
	 ******************************************************************************/
	
	public AbsReserveDialog(int x, int y) {
		
		super();
		
		this.setModal(true);
		this.setContentPane(getJContentPane_rsv());
		
		// タイトルバーの高さも考慮する必要がある
		Dimension d = getJContentPane_rsv().getPreferredSize();
		this.pack();
		this.setBounds(x, y, d.width, d.height+this.getInsets().top);
		this.setResizable(false);
		//
		this.setTitle("録画設定");
		
		this.addWindowListener(wl_opened);
	}
	
	/*******************************************************************************
	 * アクション
	 ******************************************************************************/

	/**
	 * 新規登録
	 * @see #doUpdate()
	 */
	public void doRecord() {
		
		if (debug) System.out.println(DBGID+"doRecord "+vals.toString());
		
		// 新規処理
		final ReserveList r = new ReserveList();
		//r.setNo(-1);				// PostRdEntry()中で取得するのでここはダミー（旧）
		r.setId(null);				// PostRdEntry()中で取得するのでここはダミー（新）
		r.setRec_pattern((String)jComboBox_date.getSelectedItem());
		r.setRec_pattern_id(-1);	// PostRdEntry()中で取得するのでここはダミー
		r.setRec_nextdate((String)jComboBox_date.getItemAt(0));	// PostRdEntry()中で取得するのでここはダミー(日付を入れるのはDIGA用)
		r.setAhh(String.format("%02d", Integer.valueOf(getJTextField_ahh().getText())));
		r.setAmm(String.format("%02d", Integer.valueOf(getJTextField_amm().getText())));
		r.setZhh(String.format("%02d", Integer.valueOf(getJTextField_zhh().getText())));
		r.setZmm(String.format("%02d", Integer.valueOf(getJTextField_zmm().getText())));
		r.setRec_min("");			// PostRdEntry()中で取得するのでここはダミー
		r.setTuner((String)jComboBox_encoder.getSelectedItem());
		r.setRec_mode((String)jCBXPanel_videorate.getSelectedItem());
		r.setRec_audio((String)jCBXPanel_audiorate.getSelectedItem());
		r.setRec_folder((String)jCBXPanel_folder.getSelectedItem());
		r.setRec_genre((String)jCBXPanel_genre.getSelectedItem());
		r.setRec_subgenre((String)jCBXPanel_subgenre.getSelectedItem());
		r.setRec_dvdcompat((String)jCBXPanel_dvdcompat.getSelectedItem());
		r.setRec_device((String)jCBXPanel_device.getSelectedItem());
		// 自動チャプタ関連
		r.setRec_xchapter((String)jCBXPanel_xChapter.getSelectedItem());
		r.setRec_mschapter((String)jCBXPanel_msChapter.getSelectedItem());
		r.setRec_mvchapter((String)jCBXPanel_mvChapter.getSelectedItem());
		// その他
		r.setRec_aspect((String)jCBXPanel_aspect.getSelectedItem());
		r.setRec_bvperf((String)jCBXPanel_bvperf.getSelectedItem());
		r.setRec_lvoice((String)jCBXPanel_lvoice.getSelectedItem());
		r.setRec_autodel((String)jCBXPanel_autodel.getSelectedItem());
		
		//r.setPursues(jCheckBox_Pursues.isSelected());
		r.setPursues(ITEM_YES.equals((String) jCBXPanel_pursues.getSelectedItem()));
		
		r.setTitle((String)jComboBox_title.getSelectedItem());
		r.setTitlePop(TraceProgram.replacePop(r.getTitle()));
		r.setDetail(jTextArea_detail.getText());
		r.setChannel("");			// PostRdEntry()中で取得するのでここはダミー
		r.setCh_name((String)jComboBox_ch.getSelectedItem());
		r.setStartDateTime("");		// PostRdEntry()中で取得するのでここはダミー
		r.setEndDateTime("");		// PostRdEntry()中で取得するのでここはダミー
		r.setExec(jCheckBox_Exec.isSelected());
		r.setAutocomplete(jCheckBox_Autocomplete.isSelected());
		
		r.setContentId(vals.hide_content_id);
		
		// 予約実行
		StWin.clear();
		new SwingBackgroundWorker(false) {
			
			@Override
			protected Object doWorks() throws Exception {
				for ( HDDRecorder recorder : recorders ) {
					if (recorder.isMyself((String)jComboBox_recorder.getSelectedItem()) == true) {
						StWin.appendMessage(MSGID+"予約を登録します："+r.getTitle());
						//recorder.setProgressArea(StWin);
						if (recorder.PostRdEntry(r)) {
							
							// 成功したよ
							MWin.appendMessage(MSGID+"正常に登録できました："+r.getTitle()+"("+r.getCh_name()+")");
							doneReserve = true;
							
							// カレンダーに登録する
							if ( recorder.getUseCalendar()) {
								if ( jCheckBox_Exec.isSelected() ) {
									for ( HDDRecorder calendar : recorders ) {
										if (calendar.getType() == RecType.CALENDAR) {
											StWin.appendMessage(MSGID+"カレンダーに予約情報を登録します");
											//calendar.setProgressArea(StWin);
											if ( ! calendar.PostRdEntry(r)) {
												MWin.appendError(ERRID+"[カレンダー] "+calendar.getErrmsg());
												ringBeep();
											}
										}
									}
								}
							}
						}
						else {
							MWin.appendError(ERRID+"登録に失敗しました："+r.getTitle()+"("+r.getCh_name()+")");
						}
						//
						if ( ! recorder.getErrmsg().equals("")) {
							MWin.appendMessage(MSGID+"[追加情報] "+recorder.getErrmsg());
							ringBeep();
						}
						break;
					}
				}
				return null;
			}
			
			@Override
			protected void doFinally() {
				//CommonUtils.milSleep(0);
				StWin.setVisible(false);
			}
		}.execute();
		
		CommonSwingUtils.setLocationCenter(parent, (Component)StWin);
		StWin.setVisible(true);
		
		//setVisible(false);
		resetWhenWindowClosed();
		dispose();
	}
	
	/**
	 * 更新処理
	 * @see #doRecord()
	 */
	public void doUpdate() {
		if (vals.likeRsvList.size() <= 0) {
			return;
		}
		
		// 更新処理
		final ReserveList newRsv = vals.selectedLikeRsv.getRsv().clone();
		//newRsv.setNo(-1);
		//newRsv.setId(null);
		newRsv.setRec_pattern((String)jComboBox_date.getSelectedItem());
		newRsv.setRec_pattern_id(-1);		// UpdateRdEntry()中で取得するのでここはダミー
		newRsv.setRec_nextdate("");		// UpdateRdEntry()中で取得するのでここはダミー
		newRsv.setAhh(String.format("%02d", Integer.valueOf(getJTextField_ahh().getText())));
		newRsv.setAmm(String.format("%02d", Integer.valueOf(getJTextField_amm().getText())));
		newRsv.setZhh(String.format("%02d", Integer.valueOf(getJTextField_zhh().getText())));
		newRsv.setZmm(String.format("%02d", Integer.valueOf(getJTextField_zmm().getText())));
		newRsv.setRec_min("");			// UpdateRdEntry()中で取得するのでここはダミー
		newRsv.setTuner((String)jComboBox_encoder.getSelectedItem());
		newRsv.setRec_mode((String)jCBXPanel_videorate.getSelectedItem());;
		newRsv.setRec_audio((String)jCBXPanel_audiorate.getSelectedItem());
		newRsv.setRec_folder((String)jCBXPanel_folder.getSelectedItem());
		newRsv.setRec_genre((String)jCBXPanel_genre.getSelectedItem());
		newRsv.setRec_subgenre((String)jCBXPanel_subgenre.getSelectedItem());
		newRsv.setRec_dvdcompat((String)jCBXPanel_dvdcompat.getSelectedItem());
		newRsv.setRec_device((String)jCBXPanel_device.getSelectedItem());
		// 自動チャプタ関連
		newRsv.setRec_xchapter((String)jCBXPanel_xChapter.getSelectedItem());
		newRsv.setRec_mschapter((String)jCBXPanel_msChapter.getSelectedItem());
		newRsv.setRec_mvchapter((String)jCBXPanel_mvChapter.getSelectedItem());
		// その他
		newRsv.setRec_aspect((String)jCBXPanel_aspect.getSelectedItem());
		newRsv.setRec_bvperf((String)jCBXPanel_bvperf.getSelectedItem());
		newRsv.setRec_lvoice((String)jCBXPanel_lvoice.getSelectedItem());
		newRsv.setRec_autodel((String)jCBXPanel_autodel.getSelectedItem());
		
		//newRsv.setPursues(jCheckBox_Pursues.isSelected());
		newRsv.setPursues(ITEM_YES.equals((String) jCBXPanel_pursues.getSelectedItem()));
		
		newRsv.setTitle((String)jComboBox_title.getSelectedItem());
		newRsv.setTitlePop(TraceProgram.replacePop(newRsv.getTitle()));
		newRsv.setDetail(jTextArea_detail.getText());
		newRsv.setChannel("");			// UpdateRdEntry()中で取得するのでここはダミー
		newRsv.setCh_name((String)jComboBox_ch.getSelectedItem());
		newRsv.setStartDateTime("");		// UpdateRdEntry()中で取得するのでここはダミー
		newRsv.setEndDateTime("");		// UpdateRdEntry()中で取得するのでここはダミー
		newRsv.setExec(jCheckBox_Exec.isSelected());
		newRsv.setAutocomplete(jCheckBox_Autocomplete.isSelected());
		newRsv.setUpdateOnlyExec(vals.isUpdateOnlyExec);
		
		newRsv.setContentId(vals.hide_content_id);

		// 更新実行
		StWin.clear();
		new SwingBackgroundWorker(false) {
			
			@Override
			protected Object doWorks() throws Exception {
				StWin.appendMessage(MSGID+"予約を更新します："+newRsv.getTitle());
				//likeRsvRecorder.setProgressArea(StWin);
				if (vals.selectedLikeRsv.getRec().UpdateRdEntry(vals.selectedLikeRsv.getRsv(), newRsv)) {
					
					// 成功したよ
					MWin.appendMessage(MSGID+"正常に更新できました："+vals.selectedLikeRsv.getRsv().getTitle()+"("+vals.selectedLikeRsv.getRsv().getCh_name()+")");
					doneReserve = true;
					
					// カレンダーを更新する
					if ( vals.selectedLikeRsv.getRec().getUseCalendar() ) {
						for ( HDDRecorder calendar : recorders ) {
							if (calendar.getType() == RecType.CALENDAR) {
								StWin.appendMessage(MSGID+"カレンダーの予約情報を更新します");
								//calendar.setProgressArea(StWin);
								if ( ! calendar.UpdateRdEntry(vals.selectedLikeRsv.getRsv(), (jCheckBox_Exec.isSelected())?(newRsv):(null))) {
									MWin.appendError(ERRID+"[カレンダー] "+calendar.getErrmsg());
									ringBeep();
								}
							}
						}
					}
				}
				else {
					MWin.appendError(ERRID+"更新に失敗しました："+vals.selectedLikeRsv.getRsv().getTitle()+"("+vals.selectedLikeRsv.getRsv().getCh_name()+")");
				}
				//
				if ( ! vals.selectedLikeRsv.getRec().getErrmsg().equals("")) {
					MWin.appendMessage(MSGID+"[追加情報] "+vals.selectedLikeRsv.getRec().getErrmsg());
					ringBeep();
				}
				return null;
			}
			
			@Override
			protected void doFinally() {
				StWin.setVisible(false);
			}
		}.execute();

		StWin.setVisible(true);

		vals.selectedLikeRsv = null;
		
		//setVisible(false);
		resetWhenWindowClosed();
		dispose();
	}
	
	/*******************************************************************************
	 * ダイアログオープン
	 ******************************************************************************/

	/**
	 * 実行のON/OFFだけしか操作しない場合に呼び出す（画面にウィンドウは表示しない）
	 * ※これがあるので、各openでは vals != null チェックの必要がある
	 */
	public void setOnlyUpdateExec(boolean b) {
		if (vals == null) vals = new Vals();
		vals.isUpdateOnlyExec = true;
		jCheckBox_Exec.setSelected(b);
	}
	
	/**
	 *  類似予約抽出条件ありオープン
	 */
	public boolean open(ProgDetailList tvd, String keywordVal, int thresholdVal) {
		if (thresholdVal > 0) {
			if (vals == null) vals = new Vals();
			vals.keyword = keywordVal;
			vals.threshold = thresholdVal;
		}
		return open(tvd);
	}
	
	/**
	 *  類似予約抽出条件なしオープン
	 */
	public boolean open(ProgDetailList tvd) {
		
		if (recorders.size() == 0) {
			return false;	// レコーダがひとつもないのはやばい
		}
		if (tvd.start.equals("")) {
			return false;	// これは「番組情報がありません」だろう
		}
		
		// 隠しパラメータ
		if (vals == null) vals = new Vals();
		vals.hide_tvd = tvd;
		vals.hide_content_id = tvd.progid;
		vals.hide_startdatetime = tvd.startDateTime;
		vals.hide_atreservedlist = false;
		
		// 番組ID取得ボタン
		getEventIdOnOpen(tvd);
		setGetEventIdButton(vals.hide_content_id,true);

		return _open(null, null);
	}
	
	/**
	 *  本体予約一覧からのオープン、または予約ＯＮ／ＯＦＦメニュー
	 */
	public boolean open(String myself, String rsvId) {
		
		HDDRecorderList recs = recorders.findInstance(myself);
		if ( recs.size() == 0 ) {
			return false;	// ここに来たらバグ
		}
		HDDRecorder myrec = recs.get(0);

		ReserveList myrsv = myrec.getReserveList(rsvId);
		if ( myrsv == null ) {
			return false;	// ここに来たらバグ
		}

		// 番組ID取得ボタンを無効にする
		setGetEventIdButton(null,false);

		ProgDetailList tvd = new ProgDetailList();
		CommonUtils.getNextDate(myrsv);
		tvd.center = myrsv.getCh_name();
		tvd.title = myrsv.getTitle();
		tvd.detail = myrsv.getDetail();
		tvd.start = myrsv.getAhh()+":"+myrsv.getAmm();
		tvd.end = myrsv.getZhh()+":"+myrsv.getZmm();
		//tvd.accurateDate = myrsv.getRec_pattern();		// これは特殊
		tvd.progid = myrsv.getContentId();
		tvd.genre = ProgGenre.get(myrsv.getRec_genre());
		tvd.subgenre = ProgSubgenre.get(tvd.genre,myrsv.getRec_subgenre());
		
		// 隠しパラメータ
		if (vals == null) vals = new Vals();
		vals.hide_tvd = tvd;
		vals.hide_content_id = null;
		vals.hide_startdatetime = null;	// 予約一覧からは番組IDの取得はできないので開始日時は保存しない
		vals.hide_atreservedlist = true;

		return _open(myrec, myrsv);
	}
	
	/**
	 * ダイアログオープン（共通処理）
	 */
	private boolean _open(HDDRecorder rsvdrec, ReserveList rsvdrsv) {
		
		// 予約は行われてないよー
		doneReserve = false;
		
		String myself = null;
		HDDRecorder myrec = null;
		ReserveList myrsv = null;
		AVs myavs = null;
		if ( rsvdrec == null ) {
			// レコーダの初期値の確認
			myself = getSelectedRecorderOnToolbar();		// ツールバーで選択されているのはどれかな？
			if ( myself != null && myself.length() > 0 ) {
				myrec = recorders.findInstance(myself).get(0);		// "すべて"と"ピックアップ"以外
			}
			else {
				myrec = recorders.get(0);						// "すべて"と"ピックアップ"なら先頭を選んでおけばいい
			}
			
			// リセット用データ収集(1)
			{
				vals.hide_default_recorder = myrec;
			}
			
			// 類似予約の確認
			searchLikeRsv(vals.likeRsvList, vals.hide_tvd, vals.keyword, vals.threshold);
			
			if ( env.getGivePriorityToReserved() && vals.likeRsvList.size() > 0 ) {
				// 類似予約が有効かつ存在しているならば
				for ( int i=0; i<vals.likeRsvList.size(); i++ ) {
					HDDRecorder rec = vals.likeRsvList.getRec(i);
					if ( rec.isMyself(myself) ) {
						myrec = rec;	// 類似予約の中にコンボボックスと一致するものがあったわ
						myself = myrec.Myself();
						myrsv = vals.likeRsvList.getRsv(i);
						vals.selectedLikeRsv = new LikeReserveItem(myrec, myrsv);
						break;
					}
				}
				if ( myrsv == null && myself != null && myself.length() != 0 ) {
					// 類似予約があってもコンボボックスで選択したレコーダのものがない場合は無視
					if (debug) System.out.println(DBGID+"類似予約に選択中のレコーダのものはなかった： "+myself);
				}
				else {
					if ( myrsv == null ) {
						// "すべて"と"ピックアップのみ"なら選択できるものはないね
						myrec = vals.likeRsvList.getRec(0);
						myself = myrec.Myself();
						myrsv = vals.likeRsvList.getRsv(0);
						vals.selectedLikeRsv = new LikeReserveItem(myrec, myrsv);
						if (debug) System.out.println(DBGID+"選択中のレコーダがないので先頭の類似予約を使う： "+myself);
					}
				}
			}
			
			// ジャンル別ＡＶ設定の確認
			if ( myrsv == null || ! env.getGivePriorityToReserved() ) {
				myavs = getSelectedAVs(vals.hide_tvd.genre, vals.hide_tvd.center, myrec.getRecorderId());
			}
			else {
				MWin.appendMessage(MSGID+"画質・音質は類似予約の設定が継承されます");
			}
		}
		else {
			myrec = rsvdrec;
			myrsv = rsvdrsv;
			myself = myrec.Myself();
			vals.selectedLikeRsv = new LikeReserveItem(myrec,myrsv);
			vals.likeRsvList.add(vals.selectedLikeRsv);
			
			// リセット用データ収集(1)
			{
				vals.hide_default_recorder = myrec;
			}
		}
		
		if ( vals.hide_content_id == null || ! ContentIdEDCB.isValid(vals.hide_content_id) ) {
			if ( myrsv != null && ContentIdEDCB.isValid(myrsv.getContentId()) ) {
				vals.hide_content_id = myrsv.getContentId();
				setGetEventIdButton(vals.hide_content_id,false);
			}
		}

		// 一旦選択系リスナーは全部止めてしまう
		setEnabledSelectionListeners(false);
		
		// 初期値を設定（固定部分）
		setInitFixies(myrec, myrsv);

		// 初期値を設定（可変部分）
		setInitVariables(myrec);

		// 初期値を選択
		setSelectedVariables(myrec, myrsv, myavs, null, null);
		
		// 項目ラベルのオーバーライド
		setLabels(myrec);

		// リスナーを全部戻す
		setEnabledSelectionListeners(true);
		
		// リセット用データ収集
		{
			vals.isExtended = jCheckBox_spoex_extend.isSelected();
			vals.isClipped = jCheckBox_OverlapDown2.isSelected();
			vals.byDateIni = (String) jComboBox_date.getItemAt(0);
			vals.byWeeklyIni = (String) jComboBox_date.getItemAt(1);
			jTextField_Xahh.setText(jTextField_ahh.getText());
			jTextField_Xamm.setText(jTextField_amm.getText());
			jTextField_Xzhh.setText(jTextField_zhh.getText());
			jTextField_Xzmm.setText(jTextField_zmm.getText());
		}
		
		return(true);
	}
	
	/**
	 * open()時に設定したら内容の変更のないコンボボックスの選択肢の設定
	 * @see #setInitVariables
	 */
	private void setInitFixies(HDDRecorder myrec, ReserveList myrsv) {
		// レコーダコンボボックスの設定
		{
			jComboBox_recorder.removeAllItems();
			for ( HDDRecorder rec : recorders ) {
				if ( rec.isBackgroundOnly() ) {
					continue;	// Googleカレンダープラグインとかははずす
				}
				jComboBox_recorder.addItem(rec.Myself());
			}
			jComboBox_recorder.setEnabled( jComboBox_recorder.getItemCount() > 0 );
			
			if ( vals.hide_atreservedlist ) {
				jComboBox_recorder.setEnabled(false);
			}
			else {
				jComboBox_recorder.setEnabled(true);
			}
		}
		
		// 予約名
		{
			jComboBox_title.removeAllItems();
			jComboBox_title.addItem(vals.hide_tvd.title);
			for ( LikeReserveItem ll : vals.likeRsvList ) {
				for ( int i=0; i<jComboBox_title.getItemCount(); i++ ) {
					String t = (String) jComboBox_title.getItemAt(i);
					if ( t != null && ! t.equals(ll.getRsv().getTitle()) ) {
						jComboBox_title.addItem(ll.getRsv().getTitle());
						break;
					}
				}
			}
			
			// 予約名の初期選択
			if ( env.getGivePriorityToReserved() && env.getGivePriorityToReservedTitle() && myrsv != null ) {
				jComboBox_title.setSelectedItem(myrsv.getTitle());
			}
			((JTextField)jComboBox_title.getEditor().getEditorComponent()).setCaretPosition(0);
		}
		
		// 詳細
		{
			jTextArea_detail.setText(vals.hide_tvd.detail+"\n"+vals.hide_tvd.getAddedDetail());
			jTextArea_detail.setCaretPosition(0);
		}

		// ジャンル
		{
			jCBXPanel_genre.removeAllItems();
			jCBXPanel_subgenre.removeAllItems();
			
			for ( ProgGenre g : ProgGenre.values() ) {
				jCBXPanel_genre.addItem(g.toString());
			}
			jCBXPanel_genre.setEnabled( jCBXPanel_genre.getItemCount() > 0 );
			
			if ( vals.hide_tvd.subgenre == null ) {
				jCBXPanel_subgenre.setEnabled(false);
			}
			else {
				for ( ProgSubgenre sg : ProgSubgenre.values(vals.hide_tvd.genre)) {
					jCBXPanel_subgenre.addItem(sg.toString());
				}
				jCBXPanel_subgenre.setEnabled( jCBXPanel_subgenre.getItemCount() > 0 );
			}
			
			// ジャンルの初期選択
			if ( vals.hide_tvd.genre != null ) {
				jCBXPanel_genre.setSelectedItem(vals.hide_tvd.genre.toString());
				if ( vals.hide_tvd.subgenre != null ) {
					jCBXPanel_subgenre.setSelectedItem(vals.hide_tvd.subgenre.toString());
				}
			}
		}
		
		boolean OverlapException = false;	// 日付コンボボックスでも使うよ
		
		// 開始・終了時刻は長いよ
		{
			int ahh = Integer.valueOf(vals.hide_tvd.start.substring(0,vals.hide_tvd.start.indexOf(":")));
			int amm = Integer.valueOf(vals.hide_tvd.start.substring(vals.hide_tvd.start.indexOf(":")+1,vals.hide_tvd.start.length()));
			int zhh = Integer.valueOf(vals.hide_tvd.end.substring(0,vals.hide_tvd.end.indexOf(":")));
			int zmm = Integer.valueOf(vals.hide_tvd.end.substring(vals.hide_tvd.end.indexOf(":")+1,vals.hide_tvd.end.length()));
			
			if ( ! vals.hide_atreservedlist ) {
				// のりしろ処理
				if (env.getOverlapUp() == true) {
					int a = ahh*60+amm-1;
					if (a<0) {
						a+=24*60;
						OverlapException = true;
					}
					amm = a % 60;
					ahh = (a-amm)/60;
				}
				if (env.getOverlapDown() == true) {
					int z = zhh*60+zmm+1;
					zmm = z % 60;
					zhh = (z-zmm)/60%24;
					jCheckBox_OverlapDown2.setSelected(false);
					jCheckBox_OverlapDown2.setForeground(Color.BLACK);
				}
				else if (
						env.getOverlapDown2() == true &&
						! vals.hide_tvd.dontoverlapdown &&			// NHKは縮めない
						! (env.getNoOverlapDown2Sp() && vals.hide_tvd.option.contains(ProgOption.SPECIAL))	// OVAとかは縮めない 
						) {
					jCheckBox_OverlapDown2.setSelected(true);
					jCheckBox_OverlapDown2.setForeground(Color.RED);
					int z = zhh*60+zmm-1;
					if (z<0) {
						z+=24*60;
					}
					zmm = z % 60;
					zhh = (z-zmm)/60%24;
				}
				else {
					jCheckBox_OverlapDown2.setSelected(false);
					jCheckBox_OverlapDown2.setForeground(Color.BLACK);
				}
				
				// 延長警告処理
				int spoexLength = Integer.valueOf(env.getSpoexLength());
				if (vals.hide_tvd.extension == true && spoexLength>0) {
					jCheckBox_spoex_extend.setSelected(true);
					jCheckBox_spoex_extend.setForeground(Color.RED);
					int z = zhh*60+zmm+spoexLength;
					zmm = z % 60;
					zhh = (z-zmm)/60%24;
				}
				else {
					jCheckBox_spoex_extend.setSelected(false);
					jCheckBox_spoex_extend.setForeground(Color.BLACK);
				}
			}
			else {
				jCheckBox_spoex_extend.setEnabled(false);
				jCheckBox_spoex_extend.setSelected(false);
				jCheckBox_spoex_extend.setForeground(Color.BLACK);
				jCheckBox_OverlapDown2.setEnabled(false);
				jCheckBox_OverlapDown2.setSelected(false);
				jCheckBox_OverlapDown2.setForeground(Color.BLACK);
			}
			
			jTextField_ahh.setText(String.format("%02d", ahh));
			jTextField_amm.setText(String.format("%02d", amm));
			jTextField_zhh.setText(String.format("%02d", zhh));
			jTextField_zmm.setText(String.format("%02d", zmm));
		}

		// 録画日付（のりしろ処理が必要な場合あり）
		{
			jComboBox_date.removeAllItems();
			if ( ! vals.hide_atreservedlist ) {
				// リスト／新聞形式からの呼び出しなら番組情報をもとに
				GregorianCalendar c = CommonUtils.getCalendar(vals.hide_tvd.accurateDate);
				if ( c != null ) {
					if (OverlapException == true) {
						c.add(Calendar.DATE, -1);
					}
					jComboBox_date.addItem(CommonUtils.getDate(c));
					jComboBox_date.addItem(HDDRecorder.RPTPTN[c.get(Calendar.DAY_OF_WEEK)-Calendar.SUNDAY]);
				}
			}
			else {
				// 本体予約一覧からの呼び出しなら既存の予約情報からの引継ぎ
				GregorianCalendar c = CommonUtils.getCalendar(myrsv.getRec_pattern());
				if ( c != null ) {
					jComboBox_date.addItem(myrsv.getRec_pattern());
					jComboBox_date.addItem(HDDRecorder.RPTPTN[c.get(Calendar.DAY_OF_WEEK)-Calendar.SUNDAY]);
				}
				else {
					jComboBox_date.addItem(myrsv.getRec_nextdate());
					jComboBox_date.addItem(myrsv.getRec_pattern());
				}
			}
			jComboBox_date.addItem(HDDRecorder.RPTPTN[7]);
			jComboBox_date.addItem(HDDRecorder.RPTPTN[8]);
			jComboBox_date.addItem(HDDRecorder.RPTPTN[9]);
			jComboBox_date.addItem(HDDRecorder.RPTPTN[10]);
		}
		
		// 類似予約コンボボックスと抽出条件、その関連
		{
			//vals.likeRsvList.clear();
			int selectedrow = LIKERSVTABLE_NOTSELECTED;
			if ( ! vals.hide_atreservedlist ) {
				jButton_record.setForeground(Color.RED);
				jButton_record.setEnabled(true);
				if ( vals.likeRsvList.size() > 0 ) {
					//jButton_update.setForeground(Color.RED);
					//jButton_update.setEnabled(true);
					likersvtable.setEnabled(true);
					
					// 類似予約中の一番近い予約を探す
					long score = 86400;
					if ( env.getGivePriorityToReserved() ) {
						for ( int i=0; i<vals.likeRsvList.size(); i++) {
							if ( vals.likeRsvList.getRec(i).isMyself(myrec.Myself()) ) {
								long sc = CommonUtils.getDiffDateTime(vals.likeRsvList.getRsv(i).getStartDateTime(), vals.hide_tvd.startDateTime);
								if ( selectedrow == -1 || score > sc ) {
									selectedrow = i;
									score = sc;
								}
							}
						}
					}

				}
				else {
					likersvtable.setEnabled(false);
				}
			}
			else {
				// 本体予約一覧の場合は１個だけ追加すればよい
				jButton_record.setForeground(Color.GRAY);
				jButton_record.setEnabled(false);
				likersvtable.setEnabled(false);
				selectedrow = 0;
				//vals.likeRsvList.add(new LikeReserveItem(myrec,myrsv));
			}

			((DefaultTableModel)likersvtable.getModel()).fireTableDataChanged();
			((DefaultTableModel)likersvrowheader.getModel()).fireTableDataChanged();
			
			likersvtable.setRowSelectionInterval(selectedrow,selectedrow);

			setEnabledUpdateButton(likersvtable.getSelectedRow());
		}
	}
	
	/**
	 * 更新ボタンの有効無効
	 */
	private void setEnabledUpdateButton(int selectedrow) {
		if ( selectedrow == LIKERSVTABLE_NOTSELECTED ) {
			jButton_update.setForeground(Color.BLACK);
			jButton_update.setEnabled(false);
		}
		else {
			jButton_update.setForeground(Color.RED);
			jButton_update.setEnabled(true);
		}
	}
	
	/**
	 * 他のコンボボックスの操作によって内容が変わるコンボボックスの選択肢の入れ替え
	 * @see #setInitFixies
	 */
	private void setInitVariables(HDDRecorder myrec) {
		
		// 番組追従
		{
			if ( myrec.isPursuesEditable() ) {
				jCBXPanel_pursues.setEnabled( ! ITEM_EVIDNEEDED.equals(jButton_getEventId.getText()));
			}
			else {
				jCBXPanel_pursues.setEnabled(false);
			}
		}
		
		// CH
		{
			jComboBox_ch.removeAllItems();
			jComboBox_ch.addItem(vals.hide_tvd.center);
			for ( TextValueSet t : myrec.getChValue() ) {
				if ( t.getText().startsWith("外部") ) {
					jComboBox_ch.addItem(t.getText());
				}
			}
			jComboBox_ch.setEnabled( jComboBox_ch.getItemCount() > 0 );
		}
		
		// エンコーダ
		{
			jComboBox_encoder.removeAllItems();
			for ( String enc : getFilteredEncoders(myrec, vals.hide_tvd.center) ) {
				jComboBox_encoder.addItem(enc);
			}
			jComboBox_encoder.setEnabled( jComboBox_encoder.getItemCount() > 0 );
		}
		
		// 日付
		{
			if ( myrec.isRepeatReserveSupported() ) {
				jComboBox_date.setEnabled(true);
			}
			else {
				jComboBox_date.setEnabled(false);
				jComboBox_date.setSelectedIndex(0);
			}
		}
		
		// ＡＶ設定
		refCBX(jCBXPanel_videorate, myrec.getVideoRateList());
		refCBX(jCBXPanel_folder, myrec.getFolderList());
		refCBX(jCBXPanel_audiorate, myrec.getAudioRateList());
		
		refCBX(jCBXPanel_device, myrec.getDeviceList());
		refCBX(jCBXPanel_bvperf, myrec.getBVperf());
		refCBX(jCBXPanel_dvdcompat, myrec.getDVDCompatList());
		
		refCBX(jCBXPanel_autodel, myrec.getAutodel());
		refCBX(jCBXPanel_lvoice, myrec.getLVoice());
		refCBX(jCBXPanel_aspect, myrec.getAspect());
		
		refCBX(jCBXPanel_msChapter, myrec.getMsChapter());
		refCBX(jCBXPanel_mvChapter, myrec.getMvChapter());
		refCBX(jCBXPanel_xChapter, myrec.getXChapter());

		{
			jCBXPanel_pursues.removeAllItems();
			jCBXPanel_pursues.addItem(ITEM_YES);
			jCBXPanel_pursues.addItem(ITEM_NO);
		}
	}
	
	/**
	 * コンボボックス操作によって連動して選択しなおし
	 */
	private void setSelectedVariables(HDDRecorder myrec, ReserveList myrsv, AVs myavs, String mychname, String myenc) {

		// 予約実行
		if ( myrsv == null ) {
			jCheckBox_Exec.setSelected(true);
			jCheckBox_Exec.setForeground(Color.BLACK);
		}
		else {
			jCheckBox_Exec.setSelected(myrsv.getExec());
			jCheckBox_Exec.setForeground((myrsv.getExec())?(Color.BLACK):(Color.RED));
		}

		// タイトル自動補完
		jCheckBox_Autocomplete.setEnabled(myrec.isAutocompleteSupported());
		if ( myrec.isAutocompleteSupported() ) {
			if ( myrsv == null ) {
				jCheckBox_Autocomplete.setSelected(env.getUseAutocomplete());
				jCheckBox_Autocomplete.setForeground(Color.BLACK);
			}
			else {
				jCheckBox_Autocomplete.setSelected(myrsv.getAutocomplete());
				jCheckBox_Autocomplete.setForeground((myrsv.getAutocomplete())?(Color.BLACK):(Color.RED));
			}
		}
		else {
			jCheckBox_Autocomplete.setSelected(false);
		}
		
		// 番組追従
		jCBXPanel_pursues.setEnabled(myrec.isPursuesEditable());
		if ( ! vals.hide_atreservedlist ) {
			if ( myrec.isPursuesEditable() ) {
				jCBXPanel_pursues.setEnabled(true);
				if ( myrsv != null ) {
					// 類似予約からの継承
					jCBXPanel_pursues.setSelectedItem((myrsv.getPursues())?(ITEM_YES):(ITEM_NO));
				}
				else {
					// デフォルトはＯＮ
					if ( ITEM_EVIDNEEDED.equals(jButton_getEventId.getText()) ) {
						jCBXPanel_pursues.setEnabled(false);
						jCBXPanel_pursues.setSelectedItem(ITEM_NO);
					}
					else {
						jCBXPanel_pursues.setSelectedItem(ITEM_YES);
					}
				}
			}
			else {
				jCBXPanel_pursues.setEnabled(false);
				jCBXPanel_pursues.setSelectedItem(ITEM_NO);
			}
		}
		else {
			if ( myrec.isPursuesEditable() ) { 
				// 既存予約の継承（番組IDが取得できるまではdisable）
				jCBXPanel_pursues.setEnabled( myrsv.getContentId() != null && myrsv.getContentId().length() > 0 );
				jCBXPanel_pursues.setSelectedItem((myrsv.getPursues())?(ITEM_YES):(ITEM_NO));
			}
			else {
				jCBXPanel_pursues.setEnabled(false);
				jCBXPanel_pursues.setSelectedItem((myrsv.getPursues())?(ITEM_YES):(ITEM_NO));
			}
		}

		// レコーダ
		jComboBox_recorder.setSelectedItem(myrec.Myself());
		
		// エンコーダ（移動しました）
		
		// サブジャンル
		{
			ProgGenre mygenre = ProgGenre.get((String) jCBXPanel_genre.getSelectedItem());
			jCBXPanel_subgenre.removeAllItems();
			for ( ProgSubgenre sg : ProgSubgenre.values(mygenre) ) {
				jCBXPanel_subgenre.addItem(sg.toString());
			}
			if ( myrsv != null && mygenre == ProgGenre.get(myrsv.getRec_genre()) ) {
				jCBXPanel_subgenre.setSelectedItem(myrsv.getRec_subgenre());
			}
			else if ( mygenre == vals.hide_tvd.genre ) {
				jCBXPanel_subgenre.setSelectedItem(vals.hide_tvd.subgenre.toString());
			}
		}
		
		// 連動するＡＶ設定
		setSelectedAVItems(myrec.getRecorderId(), myrsv, myavs);
		
		// エンコーダ（旧RDデジは画質によって利用できるエンコーダが制限される）
		{
			
			if ( vals.hide_atreservedlist ) {
				jComboBox_encoder.setSelectedItem(myrsv.getTuner());
			}
			else if ( jComboBox_encoder.getItemCount() > 0 ) {
				// 裏番組チェックとかやるよ
				String vrate = ( isVARDIA(myrec.getRecorderId()) ) ? ((String) jCBXPanel_videorate.getSelectedItem()) : (null);
				String starttm = vals.hide_tvd.start;
				String endtm = vals.hide_tvd.end;
				try {
					GregorianCalendar cal = CommonUtils.getCalendar((String) jComboBox_date.getItemAt(0));
					cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(jTextField_ahh.getText()));
					cal.set(Calendar.MINUTE, Integer.valueOf(jTextField_amm.getText()));
					starttm = CommonUtils.getTime(cal);
					
					cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(jTextField_zhh.getText()));
					cal.set(Calendar.MINUTE, Integer.valueOf(jTextField_zmm.getText()));
					endtm = CommonUtils.getTime(cal);
				}
				catch ( NumberFormatException e) {
					System.err.println(ERRID+"時刻の指定が数値ではありません");
				}
				String enc = getEmptyEncorder(myrec, vals.hide_tvd.center, vals.hide_tvd.accurateDate, starttm, endtm, vrate);
				
				if ( myrsv != null ) {
					// 類似予約最優先
					jComboBox_encoder.setSelectedItem(myrsv.getTuner());
				}
				else if ( myrec.isAutoEncSelectEnabled() && ! vals.hide_atreservedlist ) {
					// 番組情報に近い予約を探してエンコーダを絞り込む
					jComboBox_encoder.setSelectedItem(enc);
					showUraList(myrec.Myself());
				}
				else if ( jComboBox_encoder.getItemCount() > 0 ) {
					// 類似予約や自動選択がない場合は極力もとのエンコーダを選択したい
					if ( myenc != null ) {
						jComboBox_encoder.setSelectedItem(myenc);
						myenc = (String) jComboBox_encoder.getSelectedItem();
					}
					if ( myenc == null ) {
						jComboBox_encoder.setSelectedIndex(0);
					}
					showUraList(myrec.Myself());
				}
			}
		}
		
		// パターン
		if ( myrsv == null ) {
			jComboBox_date.setSelectedIndex(0);
		}
		else {
			int dateid = 0;
			if ( myrsv.getRec_pattern_id() <= HDDRecorder.RPTPTN_ID_SAT ) {
				dateid = 1;
			}
			else if ( myrsv.getRec_pattern_id() <= HDDRecorder.RPTPTN_ID_EVERYDAY ) {
				dateid = 1+myrsv.getRec_pattern_id()-HDDRecorder.RPTPTN_ID_SAT;
			}
			jComboBox_date.setSelectedIndex(dateid);
		}
	}
	
	private void setSelectedAVItems(String myrecid, ReserveList myrsv, AVs myavs) {
		if ( myrsv != null ) {
			selCBX(jCBXPanel_videorate, myrsv.getRec_mode());
			selCBX(jCBXPanel_folder, myrsv.getRec_folder());
			selCBX(jCBXPanel_audiorate, myrsv.getRec_audio());
			
			selCBX(jCBXPanel_device, myrsv.getRec_device());
			selCBX(jCBXPanel_bvperf, myrsv.getRec_bvperf());
			selCBX(jCBXPanel_dvdcompat, myrsv.getRec_dvdcompat());
			
			selCBX(jCBXPanel_autodel, myrsv.getRec_autodel());
			selCBX(jCBXPanel_lvoice, myrsv.getRec_lvoice());
			selCBX(jCBXPanel_aspect, myrsv.getRec_aspect());
			
			selCBX(jCBXPanel_msChapter, myrsv.getRec_mschapter());
			selCBX(jCBXPanel_mvChapter, myrsv.getRec_mvchapter());
			selCBX(jCBXPanel_xChapter, myrsv.getRec_xchapter());
		}
		else if ( myavs != null ) {
			selCBX(jCBXPanel_videorate, myavs.getVideorate());
			selCBX(jCBXPanel_folder, myavs.getFolder());
			selCBX(jCBXPanel_audiorate, myavs.getAudiorate());
			
			selCBX(jCBXPanel_device, myavs.getDevice());
			selCBX(jCBXPanel_bvperf, myavs.getBvperf());
			selCBX(jCBXPanel_dvdcompat, myavs.getDVDCompat());
			
			selCBX(jCBXPanel_autodel, myavs.getAutodel());
			selCBX(jCBXPanel_lvoice, myavs.getLvoice());
			selCBX(jCBXPanel_aspect, myavs.getAspect());
			
			selCBX(jCBXPanel_msChapter, myavs.getMsChapter());
			selCBX(jCBXPanel_mvChapter, myavs.getMvChapter());
			selCBX(jCBXPanel_xChapter, myavs.getXChapter());
			
			{
				jCBXPanel_pursues.setSelectedItem(myavs.getPursues()?ITEM_YES:ITEM_NO);
			}
		}
		else {
			// 特殊アイテム
			if ( isRD(myrecid) ) {
				setSelectedFolder();
			}
		}
	}
	
	/**
	 *  <P>指定したレコーダによってフォルダを変える
	 *  <P>うーん、folderを他の用途に転用してるけど問題おきないかな？
	 */
	private void setSelectedFolder() {

		// タイトルに連動
		if ( env.getAutoFolderSelect() ) {
			String titlePop = TraceProgram.replacePop((String) jComboBox_title.getSelectedItem());
			for (int i=0; i<jCBXPanel_folder.getItemCount(); i++) {
				String folderPop = TraceProgram.replacePop(((String) jCBXPanel_folder.getItemAt(i)).replaceFirst("^\\[(HDD|USB)\\] ",""));
				if (folderPop.equals(titlePop)) {
					jCBXPanel_folder.setSelectedIndex(i);
					return;
				}
			}
		}
		
		// デバイス名に連動
		int defaultFolderIdx = -1;
		int defaultHDDFolderIdx = -1;
		int defaultDVDFolderIdx = -1;
		for (int i=0; i<jCBXPanel_folder.getItemCount(); i++ ) {
			String folderName = (String) jCBXPanel_folder.getItemAt(i);
			if (folderName.indexOf("指定なし") != -1) {
				if (defaultFolderIdx == -1) {
					defaultFolderIdx = i;
				}
				if (folderName.startsWith("[HDD] ")) {
					defaultHDDFolderIdx = i;
				}
				else if (folderName.startsWith("[USB] ")) {
					defaultDVDFolderIdx = i;
				}
			}
		}
		if (jCBXPanel_device.getItemCount() > 0) {
			if (((String) jCBXPanel_device.getSelectedItem()).equals("HDD")) {
				if (defaultHDDFolderIdx != -1) {
					jCBXPanel_folder.setSelectedIndex(defaultHDDFolderIdx);
					return;
				}
			}
			else {
				if (defaultDVDFolderIdx != -1) {
					jCBXPanel_folder.setSelectedIndex(defaultDVDFolderIdx);
					return;
				}
			}
		}
		if (defaultFolderIdx != -1) {
			jCBXPanel_folder.setSelectedIndex(defaultFolderIdx);
		}
	}
	
	private void setLabels(HDDRecorder recorder) {
		String val;
		
		val = recorder.getLabel_Videorate();
		jCBXPanel_videorate.setText((val!=null)?(val):("画質"));
		val = recorder.getLabel_Audiorate();
		jCBXPanel_audiorate.setText((val!=null)?(val):("音質"));
		val = recorder.getLabel_Folder();
		jCBXPanel_folder.setText((val!=null)?(val):("記録先フォルダ"));
		val = recorder.getLabel_Device();
		jCBXPanel_device.setText((val!=null)?(val):("記録先デバイス"));
		val = recorder.getLabel_DVDCompat();
		jCBXPanel_dvdcompat.setText((val!=null)?(val):("BD/DVD互換モード"));
		val = recorder.getLabel_XChapter();
		jCBXPanel_xChapter.setText((val!=null)?(val):("無音部分ﾁｬﾌﾟﾀ分割"));
		val = recorder.getLabel_MsChapter();
		jCBXPanel_msChapter.setText((val!=null)?(val):("ﾏｼﾞｯｸﾁｬﾌﾟﾀ(ｼｰﾝ)"));
		val = recorder.getLabel_MvChapter();
		jCBXPanel_mvChapter.setText((val!=null)?(val):("ﾏｼﾞｯｸﾁｬﾌﾟﾀ(本編)"));
		val = recorder.getLabel_Aspect();
		jCBXPanel_aspect.setText((val!=null)?(val):("録画のりしろ"));
		val = recorder.getLabel_BVperf();
		jCBXPanel_bvperf.setText((val!=null)?(val):("録画優先度"));
		val = recorder.getLabel_LVoice();
		jCBXPanel_lvoice.setText((val!=null)?(val):("ﾗｲﾝ音声選択"));
		val = recorder.getLabel_Autodel();
		jCBXPanel_autodel.setText((val!=null)?(val):("自動削除"));
	}

	/*******************************************************************************
	 * 共通部品的な
	 ******************************************************************************/
	
	/**
	 * コンボボックスのアイテムのリフレッシュ
	 */
	private void refCBX(JComboBoxPanel combo, ArrayList<TextValueSet> tvs) {
		combo.removeAllItems();
		for ( TextValueSet t : tvs ) {
			combo.addItem(t.getText());
			if (t.getDefval()) combo.setSelectedIndex(combo.getItemCount()-1);	// デフォルト値があるならば
		}
		
		combo.setEnabled( combo.getItemCount() > 0 );
	}
	
	/**
	 * コンボボックスのアイテム選択
	 */
	private void selCBX(JComboBoxPanel combo, String selected) {
		if ( selected != null ) combo.setSelectedItem(selected);
	}

	/**
	 * コンボボックス系のリスナーの設定／解除
	 */
	private void setEnabledSelectionListeners(boolean b) {
		// 重複呼び出しがこわいので一回全部削除してしまう
		jComboBox_encoder.removeItemListener(il_encoderChanged);
		jCBXPanel_videorate.removeItemListener(il_videorateChanged);
		jComboBox_recorder.removeItemListener(il_recorderChanged);
		jCBXPanel_genre.removeItemListener(il_genreChanged);
		likersvtable.removeMouseListener(ml_likelistSelected);
		if ( b ) {
			// 必要なら追加する
			jComboBox_encoder.addItemListener(il_encoderChanged);
			jCBXPanel_videorate.addItemListener(il_videorateChanged);
			jComboBox_recorder.addItemListener(il_recorderChanged);
			jCBXPanel_genre.addItemListener(il_genreChanged);
			likersvtable.addMouseListener(ml_likelistSelected);
		}
	}
	
	private void removeAllSelectionItems() {
		jComboBox_encoder.removeAllItems();
		jCBXPanel_videorate.removeAllItems();
		jComboBox_recorder.removeAllItems();
		jCBXPanel_genre.removeAllItems();
		//likersvtable.removeAllItems();
	}
	
	/*******************************************************************************
	 * ネットから番組IDを取得する
	 ******************************************************************************/
	
	/**
	 * ジャンル別ＡＶ設定の取得
	 */
	private AVs getSelectedAVs(ProgGenre key_genre, String key_webChName, String recId) {
		
		AVs myavs = new AVs();
		
		String selected_key = null;
		AVSetting xavs = null;
		if ( env.getEnableCHAVsetting() ) {
			selected_key = key_webChName;
			xavs = chavs;
		}
		else {
			selected_key = key_genre.toString();
			xavs = avs;
		}
		
		jButton_load.setEnabled(true);
		myavs = xavs.get(recId, selected_key);
		if (myavs == null) {			
			jButton_load.setEnabled(false);
			
			// デフォルトの設定があれば再チャレンジ！
			myavs = xavs.get(recId, null);
			if ( myavs != null ) {
				selected_key = "デフォルト";
			}
		}
	
		if ( myavs != null ) {
			MWin.appendMessage(MSGID+"画質・音質を自動設定します： "+recId+" & "+selected_key);
		}
		else {
			MWin.appendMessage(MSGID+"画質・音質の自動設定候補がありません： "+recId+" & "+selected_key);
		}
		
		return myavs;
	}
	
	/*******************************************************************************
	 * ネットから番組IDを取得する
	 ******************************************************************************/
	
	/**
	 * ダイアログオープン時の番組ID取得処理
	 */
	private void getEventIdOnOpen(final ProgDetailList tvd) {
		
		if (debug) System.err.println(DBGID+vals.hide_content_id+" "+ContentIdEDCB.isValid(vals.hide_content_id)+" "+vals.hide_content_id.length());
		
		if ( ContentIdEDCB.isValid(vals.hide_content_id) || ! env.getAutoEventIdComplete() ) {
			return;	// すでに番組ID取得ずみか、自動取得がOFFか
		}
			
		Integer evid = null;
		if ( ContentIdDIMORA.isValid(vals.hide_content_id) ) {
			ContentIdDIMORA.decodeContentId(vals.hide_content_id);
			String chid = ContentIdDIMORA.getChId();
			evid = doGetEventIdById(chid, false);
		}
		else {
			evid = doGetEventIdByName(tvd.center, false);
		}
		if ( evid != null ) {
			// on cache
			tvd.progid = vals.hide_content_id;
			tvd.setContentIdStr();
		}
		else {
			// online
			StWin.clear();
			
			new SwingBackgroundWorker(false) {
				@Override
				protected Object doWorks() throws Exception {
					StWin.appendMessage(MSGID+"番組IDを取得します");
					
					// 番組IDを探したい
					Integer evid = null;
					if ( ContentIdDIMORA.isValid(vals.hide_content_id) ) {
						ContentIdDIMORA.decodeContentId(vals.hide_content_id);
						String chid = ContentIdDIMORA.getChId();
						evid = doGetEventIdById(chid, true);
					}
					else {
						evid = doGetEventIdByName(tvd.center, true);
					}
					if ( evid != null ) {
						tvd.progid = vals.hide_content_id;
						tvd.setContentIdStr();
					}
					return null;
				}
				@Override
				protected void doFinally() {
					StWin.setVisible(false);
				}
			}.execute();
			
			CommonSwingUtils.setLocationCenter(parent, (Component) StWin);
			StWin.setVisible(true);
		}
	}

	/**
	 * 番組表に存在する放送局IDで
	 */
	private Integer doGetEventIdById(String chid, boolean force) {
		
		Integer evid = geteventid.getEvId(chid,vals.hide_startdatetime, force);
		
		if ( force && evid == null ) {
			MWin.appendError(ERRID+"番組ID取得でエラーが発生しました： "+chid+", "+vals.hide_startdatetime);
			ringBeep();
			return null;	// 一発死に
		}
		else if ( ! force && evid == null ) {
			System.out.println(MSGID+"キャッシュにヒットしませんでした： "+chid+", "+vals.hide_startdatetime);
			return null;
		}
		else if ( evid == -1 ) {
			MWin.appendError(ERRID+"番組IDが取得できませんでした： "+chid+", "+vals.hide_startdatetime);
			return null;
		}
		
		ContentIdDIMORA.decodeContentId(vals.hide_content_id);
		vals.hide_content_id = ContentIdDIMORA.getContentId(evid);

		MWin.appendMessage(MSGID+"番組IDを取得しました(byId)： "+vals.hide_content_id);
		setGetEventIdButton(vals.hide_content_id, true);
		
		return evid;
	}
	
	/**
	 * レコーダに登録された放送局IDで
	 */
	private Integer doGetEventIdByName(String centername, boolean force) {
		
		String chid = null;
		String chidEDCB = null;
		String chidREGZA = null;
		
		Integer evid = null;
		
		// 登録済みのレコーダプラグインを全部チェックしてみる
		for ( HDDRecorder rec : recorders ) {
			if ( rec.isBackgroundOnly() ) {
				continue;
			}

			chidEDCB = chidREGZA = chid = null;
			Integer tmpEvid = null;
			
			String chcode = rec.getChCode().getCH_WEB2CODE(centername);
			if ( chcode == null ) {
				System.err.println(ERRID+"「Web番組表の放送局名」を「放送局コード」に変換できません： "+rec.getRecorderId()+" "+centername);
				continue;
			}
			
			chidEDCB = chid = ContentIdEDCB.getChId(chcode);
			if ( chid == null ) {
				chidREGZA = chid = ContentIdREGZA.getChId(chcode);
				if ( chid == null ) {
					System.err.println(ERRID+"番組IDの取得に未対応のレコーダです： "+rec.getRecorderId());
					continue;
				}
			}
			
			if (debug) System.out.println(MSGID+"番組IDを取得します： "+rec.getRecorderId());
			
			tmpEvid = geteventid.getEvId(chid,vals.hide_startdatetime, force);
			
			if (evid == null) evid = tmpEvid;
			
			if ( force && tmpEvid == null ) {
				MWin.appendError(ERRID+"番組ID取得でエラーが発生しました： "+chid+", "+vals.hide_startdatetime);
				ringBeep();
				return null;	// 一発死に
			}
			else if ( ! force && tmpEvid == null ) {
				System.out.println(MSGID+"キャッシュにヒットしませんでした： "+chid+", "+vals.hide_startdatetime);
				return null;
			}
			else if ( tmpEvid == -1 ) {
				System.err.println(ERRID+"番組IDが取得できませんでした： "+chid+", "+vals.hide_startdatetime);
				continue;
			}
			
			break;
		}
		
		if ( evid == null ) {
			MWin.appendError(ERRID+"【致命的エラー】放送局IDを持つレコーダプラグインが存在しません");
			ringBeep();
			return null;
		}
		else if ( evid == -1 ) {
			MWin.appendError(ERRID+"【警告】番組IDの取得に失敗しました。開始時刻の移動や、まだ番組表サイトに情報が用意されていない場合などが考えられます。");
			ringBeep();
			return null;
		}
		
		if ( chidREGZA != null ) {
			vals.hide_content_id = ContentIdREGZA.getContentId(chidREGZA, evid);
		}
		else {
			vals.hide_content_id = ContentIdEDCB.getContentId(chidEDCB, evid);
		}		
		
		MWin.appendMessage(MSGID+"番組IDを取得しました(byName)： "+vals.hide_content_id);
		setGetEventIdButton(vals.hide_content_id, true);
		
		return evid;
	}

	/**
	 * @param enabled 番組IDの取得ができないシチュエーション（予約一覧から開くとか）では取得ボタンをfalseに。
	 */
	private boolean setGetEventIdButton(String cId, boolean enabled) {
		Integer evid = null;
		if ( ContentIdEDCB.decodeContentId(cId) ) {
			evid = ContentIdEDCB.getEvId();
		}
		else if ( ContentIdREGZA.decodeContentId(cId) && ContentIdREGZA.getEvId() != 0x0000 ) {
			evid = ContentIdREGZA.getEvId();
		}
		else if ( ContentIdDIMORA.decodeContentId(cId) && ContentIdDIMORA.getEvId() != 0x0000 ) {
			evid = ContentIdDIMORA.getEvId();
		}
		if ( evid == null ) {
			jButton_getEventId.setText(ITEM_EVIDNEEDED);
			if (enabled) {
				jButton_getEventId.setForeground(Color.BLUE);
				jButton_getEventId.setEnabled(true);
			}
			else {
				jButton_getEventId.setForeground(Color.GRAY);
				jButton_getEventId.setEnabled(false);
			}
			// 番組IDが取得できるまではdisable
			return false;
		}
		else {
			jButton_getEventId.setText(String.format("番組ID:%04X",evid));
			jButton_getEventId.setForeground(Color.GRAY);
			jButton_getEventId.setEnabled(false);
			// 番組IDが取得できるまではdisable
			return true;
		}
	}

	/*******************************************************************************
	 * 自動エンコーダ選択と裏番組抽出
	 ******************************************************************************/

	/**
	 * <P>使用されていないエンコーダをリストアップする
	 * <P>裏番組リストも一緒に作成する
	 */
	private String getEmptyEncorder(HDDRecorder recorder, String ch, String date, String stime, String etime, String vrate) {
		// 予約の開始・終了日時を算出する
		GregorianCalendar c = CommonUtils.getCalendar(date);
		String start = String.format("%s %s", CommonUtils.getDate(c,false),stime);
		if (stime.compareTo(etime) > 0) {
			c.add(Calendar.DATE, 1);
		}
		String end = String.format("%s %s", CommonUtils.getDate(c,false),etime);
		
		// エンコーダの一覧を作成する
		ArrayList<String> encs = getFilteredEncoders(recorder, ch);
		if ( encs.size() == 0 ) {
			if ( recorder.getEncoderList().size() > 0 ) {
				MWin.appendError(ERRID+"エンコーダ候補が見つからない： "+recorder.Myself());
			}
			return null;
		}
		
		// 空きエンコーダがみつからなかった場合の選択肢
		String firstenc = encs.get(0);
		
		// 予約リストをなめて予約済みエンコーダーをさがす
		
		String rsvedTuner = null;
		uraban.clear();
		for ( ReserveList r : recorder.getReserves() ) {
			// 実行予定のもののみ
			if (r.getExec()) {
				// 予約時間が重なるものを抽出する
				ArrayList<String> starts = new ArrayList<String>();
				ArrayList<String> ends = new ArrayList<String>();
				CommonUtils.getStartEndList(starts, ends, r);
				for (int i=0;i<starts.size(); i++) {
					// 既に予約済みの場合
					if (starts.get(i).equals(start) && ends.get(i).equals(end) && ch.equals(r.getCh_name())) {
						rsvedTuner = r.getTuner();
						continue;
					}
					
					// 時間の重なる番組
					if ( CommonUtils.isOverlap(start, end, starts.get(i), ends.get(i), env.getAdjoiningNotRepetition()) ) {
						
						String msg = starts.get(i)+", "+r.getTuner()+", "+r.getTitle();
						for ( String ura : uraban ) {
							if ( ura.equals(msg)) {
								msg = "";
								break;
							}
						}
						if ( ! msg.equals("")) {
							uraban.add(msg);
						}
						
						// 予約時間が重なるものはエンコーダーの一覧から削除する
						HashMap<String,Boolean> rems = new HashMap<String,Boolean>();
						for ( String enc : encs ) {
							if (enc.equals(r.getTuner())) {
								rems.put(enc, true);
								
								// ---- ＲＤデジタルＷ録向け暫定コード ----
								if (enc.equals("TS1") || enc.equals("DR1")) {
									// TS1が埋まっていればREは使えない
									rems.put("RE", true);
								}
								else if (enc.equals("RE")) {
									// REが埋まっていればTS1は使えない
									rems.put("TS1", true);
									rems.put("DR1", true);
								}
								// ---- ＲＤデジタルＷ録向け暫定コード ----
								
								break;
							}
						}
						for ( String key : rems.keySet() ) {
							encs.remove(key);
						}
					}
				}
			}
		}
		if ( recorder.isAutoEncSelectEnabled() ) {
			
			// 旧RDデジ系 - ここから
			if ( vrate != null ) {
				if ( ! vrate.equals("[TS]") && ! vrate.equals("[DR]")) {
					if ( ! encs.contains("RE") ) {
						// 空きエンコーダはなかった
						jLabel_encoderemptywarn.setText("空きｴﾝｺｰﾀﾞ不足");
						jLabel_encoderemptywarn.setForeground(Color.RED);
					}
					return "RE";
				}

				encs.remove("RE");
			}
			// 旧RDデジ系 - ここまで
			
			if ( encs.size() == 0  ) {
				// 空きエンコーダはなかった
				jLabel_encoderemptywarn.setText("空きｴﾝｺｰﾀﾞ不足");
				jLabel_encoderemptywarn.setForeground(Color.RED);
				return firstenc;
			}
			
			jLabel_encoderemptywarn.setText("");
			if (rsvedTuner != null) {
				// 予約済みなら同じのでいいよね
				return rsvedTuner;
			}
			if ( encs.size() > 0 ) {
				// エンコーダーが残っていればそれらの先頭を返す（裏番組がない場合は除く）
				return encs.get(0);
			}
		}
		else {
			jLabel_encoderemptywarn.setText("空きｴﾝｺｰﾀﾞ検索無効");
			jLabel_encoderemptywarn.setForeground(Color.BLACK);
			return firstenc;
		}
		return null;
	}
	
	ArrayList<String> uraban = new ArrayList<String>();	// 裏番組の一覧
	
	private void showUraList(String myself) {
		if (uraban.size() > 0) {
			for (String ura : uraban) {
				String msg = MSGID+"[裏番組チェック] "+((String) jComboBox_title.getItemAt(0))+" の裏番組: "+ura;
				MWin.appendMessage(msg);
			}
		}
		else {
			// 裏番組がない場合に分かりにくかったので追加
			String msg = MSGID+"[裏番組チェック] "+((String) jComboBox_title.getItemAt(0))+" の裏番組はありません";
			MWin.appendMessage(msg);
		}
	}
	
	/*******************************************************************************
	 * 放送波種別によって利用できるエンコーダを絞り込んでみる
	 ******************************************************************************/
	
	/**
	 * エンコーダを絞る
	 */
	private ArrayList<String> getFilteredEncoders(HDDRecorder recorder, String webChName) {
		
		ArrayList<String> encs = new ArrayList<String>();
		
		if ( recorder.getEncoderList().size() == 0 ) {
			encs.add("■");
			return encs;
		}
		
		if ( recorder.isBroadcastTypeNeeded() ) {
			
			// エンコーダーに地上波・BS/CSの区別のあるとき
			
			String code = recorder.getChCode().getCH_WEB2CODE(webChName);
			if (code != null) {
				for ( TextValueSet enc : recorder.getEncoderList() ) {
					if ((code.startsWith(BroadcastType.TERRA.getName()+":") && enc.getText().startsWith("地上")) ||
							((code.startsWith(BroadcastType.BS.getName()+":")||code.startsWith(BroadcastType.CS.getName()+":")) && enc.getText().startsWith("BS")) ||
							(code.startsWith(BroadcastType.CAPTURE.getName()+":") && enc.getText().startsWith("キャプチャ"))) {
						encs.add(enc.getText());
					}
				}
			}
			if ( encs.size() > 0 ) {
				return encs;
			}
		}

		// エンコーダーに地上波・BS/CSの区別のないとき、フィルタ結果が０件のとき
		
		for ( TextValueSet enc : recorder.getEncoderList() ) {
			encs.add(enc.getText());
		}
		
		return encs;
	}
	
	
	/**
	 * 当初はRD専用だったのですが
	 */
	
	private boolean isRD( String myrecid ) {
		return ( myrecid.startsWith("RD-") || myrecid.startsWith("VARDIA RD-") || myrecid.startsWith("REGZA RD-") || myrecid.startsWith("REGZA DBR-Z") ) ;
	}
	
	private boolean isVARDIA( String myrecid ) {
		return ( myrecid.startsWith("VARDIA RD-") || myrecid.startsWith("REGZA RD-") ) ;
	}
	
	
	/*******************************************************************************
	 * ここから下は古いコード
	 ******************************************************************************/

	
	
	
	
	/*******************************************************************************
	 * エンコーダコンボボックスのリフレッシュ
	 ******************************************************************************/
	
	/**
	 * コンボボックスの内容をリフレッシュする（日付専用）
	 */
	private void refreshComboBox_date(int updown) {
		GregorianCalendar cal = CommonUtils.getCalendar((String) jComboBox_date.getItemAt(0));
		if ( cal == null ) {
			return;
		}
		cal.add(Calendar.DAY_OF_MONTH, updown);
		String date = CommonUtils.getDate(cal);
		String ptrn = String.format("毎%s曜日",date.substring(11,12));
		Integer idx = jComboBox_date.getSelectedIndex();
		jComboBox_date.removeItemAt(0);
		jComboBox_date.insertItemAt(date, 0);
		jComboBox_date.removeItemAt(1);
		jComboBox_date.insertItemAt(ptrn, 1);
		jComboBox_date.setSelectedIndex(idx);
	}

	/*
	 * 項目連動のためのメソッド群
	 */

	/**
	 * エンコーダに連動して画質を変える（RD系）
	 */
	private void setVrateComboBoxByEncoder(String vrate1, String vrate2) {
		int index = -1;
		for (int i=0; i<jCBXPanel_videorate.getItemCount(); i++) {
			if (vrate1 != null && ((String)jCBXPanel_videorate.getItemAt(i)).startsWith(vrate1)) {
				index = i;
				break;
			}
			if (vrate2 != null && ((String)jCBXPanel_videorate.getItemAt(i)).startsWith(vrate2)) {
				index = i;
				break;
			}
		}
		if (index >= 0) {
			jCBXPanel_videorate.setSelectedIndex(index);
		}
	}
	
	/**
	 * 画質に連動してエンコーダを変える（RD系）
	 */
	private void setEncoderComboBoxByVrate(String enc1, String enc2) {
		int index = -1;
		for (int i=0; i<jComboBox_encoder.getItemCount(); i++) {
			if (enc1 != null && jComboBox_encoder.getItemAt(i).equals(enc1)) {
				index = i;
				break;
			}
			if (enc2 != null && jComboBox_encoder.getItemAt(i).equals(enc2)) {
				index = i;
				break;
			}
		}
		if (index >= 0) {
			jComboBox_encoder.setSelectedIndex(index);
		}
	}
	
	/**
	 * 自分のかツールバーのかわかりにくいので名前にMyって付け足した
	 */
	private HDDRecorder getMySelectedRecorder() {
		String myself = (String) jComboBox_recorder.getSelectedItem();
		if ( myself == null ) {
			return null;
		}
		HDDRecorderList recs = recorders.findInstance(myself);
		if ( recs.size() == 0 ) {
			return null;
		}
		return recs.get(0);
	}
	
	private ProgGenre getMySelectedGenre() {
		String mygenre = (String) jCBXPanel_genre.getSelectedItem();
		if ( mygenre == null ) {
			return null;
		}
		return ProgGenre.get(mygenre);
	}
	
	/*******************************************************************************
	 * リスナー
	 ******************************************************************************/
	
	/**
	 * ダイアログを開いたときは
	 */
	private final WindowListener wl_opened = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			
			if (debug) System.out.println(DBGID+"wl_opened/windowClosing "+((vals!=null)?(vals.toString()):("")));
			
			resetWhenWindowClosed();
			
			((AbsReserveDialog) e.getSource()).dispose();
		}
		
		@Override
		public void windowOpened(WindowEvent e) {
			if (debug) System.out.println(DBGID+"wl_opened/windowOpened");
			//　開いたときは、タイトル入力エリアにフォーカスを移します
			jComboBox_title.requestFocusInWindow();
		}
	};
	
	private void resetWhenWindowClosed() {
		
		if (debug) System.out.println(DBGID+"resetWhenWindowClosed "+((vals!=null)?(vals.toString()):("")));
		
		if (vals == null) return;
		
		// リセット
		vals = null;
		
		// リスナー停止
		setEnabledSelectionListeners(false);
		
		// アイテム削除
		removeAllSelectionItems();
		
		// クラス内のコンポーネントを全部setEnabeld(true)にする。個別に変更するのは面倒なので…
		CommonSwingUtils.setEnabledAllComponents(this, AbsReserveDialog.class, true);
	}
	
	/*
	 * 項目連動のためのリスナー群
	 */
	
	/**
	 *  レコーダー／ジャンル／類似予約の変更にＡＶ設定他が連動
	 */
	private final ItemListener il_recorderChanged = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			selectionChangedComm(e.getStateChange(), ChangedSelector.RECORDER);
		}
	};
	
	private final ItemListener il_genreChanged = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			selectionChangedComm(e.getStateChange(), ChangedSelector.GENRE);
		}
	};

	private final MouseListener ml_likelistSelected = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if ( ! likersvtable.isEnabled() ) {
				return;
			}
			if (SwingUtilities.isLeftMouseButton(e)) {
				selectionChangedComm(ItemEvent.SELECTED, ChangedSelector.LIKELIST);
			}
		}
	};
	
	private void selectionChangedComm(int stateChange, ChangedSelector changed) {
		
		if ( stateChange != ItemEvent.SELECTED ) {
			return;
		}
		
		// 選択中の放送局
		String mychname = (String) jComboBox_ch.getSelectedItem();
		
		HDDRecorder myrec = null;
		String myenc = null;
		ReserveList myrsv = null;
		AVs myavs = null;
		
		if ( changed == ChangedSelector.LIKELIST ) {
			// 類似予約の選択の場合
			
			int row = likersvtable.getSelectedRow();
			
			if ( row != LIKERSVTABLE_NOTSELECTED ) {
				LikeReserveItem ll = (LikeReserveItem) vals.likeRsvList.get(row);
				vals.selectedLikeRsv = ll;
				// 選択中のレコーダ
				myrec = ll.getRec();
				// 選択中のエンコーダ
				myenc = ll.getRsv().getTuner();
				// 類似予約の選択
				myrsv = ll.getRsv();
				// ジャンル別ＡＶ設定
				myavs = null;
				MWin.appendMessage(MSGID+"画質・音質は類似予約の設定が継承されます");
				
				if ( ContentIdEDCB.isValid(myrsv.getContentId()) ) {
					// 類似予約の番組IDを利用する
					vals.hide_content_id = myrsv.getContentId();
					setGetEventIdButton(vals.hide_content_id,false);
				}
				else {
					// 番組表の番組IDを利用する
					vals.hide_content_id = null;
					setGetEventIdButton(vals.hide_content_id,true);
				}
			}
			else {
				myrec = vals.hide_default_recorder;
				myenc = null;
				myrsv = null;
				myavs = getSelectedAVs(vals.hide_tvd.genre, mychname, myrec.getRecorderId());
				
				if ( ContentIdEDCB.isValid(vals.hide_tvd.progid) ) {
					// 番組表の番組IDがあれば利用する
					vals.hide_content_id = vals.hide_tvd.progid;
					setGetEventIdButton(vals.hide_content_id,false);
				}
				else {
					// なければ空にする
					vals.hide_content_id = null;
					setGetEventIdButton(vals.hide_content_id,true);
				}
			}
			
			// 状況に応じて"更新"ボタンの有効無効を変更する
			setEnabledUpdateButton(row);
		}
		else {
			// レコーダ、ジャンルの選択の場合
			
			// 選択中のレコーダ
			myrec = getMySelectedRecorder();
			if ( myrec == null ) {
				System.err.println(ERRID+"選択したレコーダの情報が登録されていません： "+(String) jComboBox_recorder.getSelectedItem());
				return;
			}
		
			// 選択中のエンコーダ
			myenc = (String) jComboBox_encoder.getSelectedItem();
			
			if ( env.getGivePriorityToReserved() ) {
				// 類似予約の選択（選択中のレコーダに一致するものがあれば）
				for ( int i=0; i<vals.likeRsvList.size(); i++ ) {
					HDDRecorder rec = vals.likeRsvList.getRec(i);
					if ( rec.isMyself(myrec.Myself()) ) {
						myrsv = vals.likeRsvList.getRsv(i);
						vals.selectedLikeRsv = new LikeReserveItem(rec, myrsv);
						break;
					}
				}
			}
			else {
				myrsv = null;
				vals.selectedLikeRsv = null;
			}
			
			ProgGenre mygenre = getMySelectedGenre();
			if ( (myrsv != null && mygenre == ProgGenre.get(myrsv.getRec_genre())) && env.getGivePriorityToReserved() ) {
				// 類似予約からのＡＶ設定継承
				myavs = null;
				MWin.appendMessage(MSGID+"画質・音質は類似予約の設定が継承されます");
			}
			else {
				// 一致する類似予約があないか、あってもジャンルが違う（ジャンル別ＡＶ設定）
				myavs = getSelectedAVs(mygenre, mychname, myrec.getRecorderId());
				// メッセージはgetSelectedAVs()で出力
			}
		}
		
		// 設定変更
		
		setEnabledSelectionListeners(false);
		
		setInitVariables(myrec);
		
		setSelectedVariables(myrec, myrsv, myavs, mychname, myenc);
		
		setLabels(myrec);
		
		if ( changed != ChangedSelector.LIKELIST ) {
			// ********
			//jComboBox_likelist.setSelectedIndex(vals.likeRsvList.indexOf(myrsv));
		}
		
		setEnabledSelectionListeners(true);
	}
	
	/**
	 *  エンコーダに画質が連動
	 */
	private final ItemListener il_encoderChanged = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() != ItemEvent.SELECTED) {
				return;
			}

			String myself = (String) jComboBox_recorder.getSelectedItem();
			if ( myself == null ) {
				return;
			}
			String myrecId = recorders.findInstance(myself).get(0).getRecorderId();
			if ( ! isVARDIA(myrecId) ) {
				return;
			}
			
			String encoder = (String) jComboBox_encoder.getSelectedItem();
			if ( encoder == null ) {
				return;
			}

			setEnabledSelectionListeners(false);

			// TS1/2では画質に[TS]、DR1/2では[DR]を選ぶ
			if (encoder.startsWith("TS")) {
				setVrateComboBoxByEncoder("[TS]",null);
			}
			else if (encoder.startsWith("DR")) {
				setVrateComboBoxByEncoder("[DR]",null);
			}
			else if (encoder.startsWith("RE")) {
				setVrateComboBoxByEncoder("[TSE] ","[AVC] ");
			}
		
			setEnabledSelectionListeners(true);
		}
	};
	
	/**
	 *  画質にエンコーダと音質が連動
	 */
	private final ItemListener il_videorateChanged = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() != ItemEvent.SELECTED) {
				return;
			}

			String myself = (String) jComboBox_recorder.getSelectedItem();
			if ( myself == null ) {
				return;
			}
			String myrecId = recorders.findInstance(myself).get(0).getRecorderId();
			if ( ! isVARDIA(myrecId) ) {
				return;
			}
			
			String vrate = (String)jCBXPanel_videorate.getSelectedItem(); 
			if (vrate == null) {
				return;
			}

			setEnabledSelectionListeners(false);
			
			// レコーダに連動する画質・音質・エンコーダ・フォルダコンボボックスの設定
			if (vrate.equals("[TS]")) {
				setEncoderComboBoxByVrate("TS1", null);
			}
			else if (vrate.equals("[DR]")) {
				setEncoderComboBoxByVrate("DR1", null);
			}
			else {
				setEncoderComboBoxByVrate("RE", "VR");
			}
			
			// RDのデジタル系では音質は選択不可
			if (jCBXPanel_audiorate.getItemCount() > 0) {
				if (vrate.startsWith("[TS") || vrate.startsWith("[DR]") || vrate.startsWith("[AVC]")) {
					jCBXPanel_audiorate.setEnabled(false);
				}
				else {
					jCBXPanel_audiorate.setEnabled(true);
				}
			}
			
			setEnabledSelectionListeners(true);
		}
	};
	
	/**
	 * 延長警告のＯＮ／ＯＦＦ
	 */
	private final ActionListener al_spoexClicked = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (jCheckBox_spoex_extend.isSelected() == true) {
				int spoexLength = Integer.valueOf(env.getSpoexLength());
				if (spoexLength>0) {
					int zhh = Integer.valueOf(jTextField_zhh.getText());
					int zmm = Integer.valueOf(jTextField_zmm.getText());
					
					int z = zhh*60+zmm+spoexLength;
					zmm = z % 60;
					zhh = (z-zmm)/60%24;
					
					jTextField_zhh.setText(String.format("%02d", zhh));
					jTextField_zmm.setText(String.format("%02d", zmm));
				}
			}
			else {
				int spoexLength = Integer.valueOf(env.getSpoexLength());
				if (spoexLength>0) {
					int zhh = Integer.valueOf(jTextField_zhh.getText());
					int zmm = Integer.valueOf(jTextField_zmm.getText());
					
					int z = zhh*60+zmm-spoexLength;
					if (z < 0) {
						z +=24*60;
					}
					zmm = z % 60;
					zhh = (z-zmm)/60%24;
					
					jTextField_zhh.setText(String.format("%02d", zhh));
					jTextField_zmm.setText(String.format("%02d", zmm));
				}
			}
		}
	};
	
	/**
	 * １分短縮のＯＮ／ＯＦＦ
	 */
	private final ActionListener al_overlapClipClicked = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (jCheckBox_OverlapDown2.isSelected() == true) {
				int zhh = Integer.valueOf(jTextField_zhh.getText());
				int zmm = Integer.valueOf(jTextField_zmm.getText());
				
				int z = zhh*60+zmm-1;
				if (z < 0) {
					z +=24*60;
				}
				zmm = z % 60;
				zhh = (z-zmm)/60%24;
				
				jTextField_zhh.setText(String.format("%02d", zhh));
				jTextField_zmm.setText(String.format("%02d", zmm));
			}
			else {
				int zhh = Integer.valueOf(jTextField_zhh.getText());
				int zmm = Integer.valueOf(jTextField_zmm.getText());
				
				int z = zhh*60+zmm+1;
				zmm = z % 60;
				zhh = (z-zmm)/60%24;
				
				jTextField_zhh.setText(String.format("%02d", zhh));
				jTextField_zmm.setText(String.format("%02d", zmm));
			}
		}
	};
	
	/**
	 * ジャンル別ＡＶ設定のロード
	 */
	private final ActionListener al_loadAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			ProgGenre key_genre = ProgGenre.get((String) jCBXPanel_genre.getSelectedItem());
			String key_webChName = (String) jComboBox_ch.getSelectedItem();
			String recId = recorders.findInstance((String) jComboBox_recorder.getSelectedItem()).get(0).getRecorderId();
			setSelectedAVItems(recId, null, getSelectedAVs(key_genre, key_webChName, recId));
			MWin.appendMessage(MSGID+"画質・音質等の設定を取得しました");
		}
	};
	
	/**
	 * ジャンル別ＡＶ設定のセーブ
	 */
	private final ActionListener al_saveAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Matcher ma = Pattern.compile("^.+?:.+?:(.+?)$").matcher((String)jComboBox_recorder.getSelectedItem());
			if (ma.find()) {
				if (env.getEnableCHAVsetting()) {
					String key_webChName = (String) jComboBox_ch.getSelectedItem();
					_save_avsettings(ma.group(1),key_webChName);
				}
				else {
					String key_genre = (String) jCBXPanel_genre.getSelectedItem();
					_save_avsettings(ma.group(1),key_genre);
				}
			}
		}
	};
	
	/**
	 * 既定ＡＶ設定のセーブ
	 */
	private final ActionListener al_saveDefault = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Matcher ma = Pattern.compile("^.+?:.+?:(.+?)$").matcher((String)jComboBox_recorder.getSelectedItem());
			if (ma.find()) {
				_save_avsettings(ma.group(1),null);
			}
		}
	};
	
	/**
	 *  ジャンル別の画質・音質等の設定の保存の本体
	 */
	private void _save_avsettings(String key_recorderId, String key_genre) {
		AVSetting xavs = avs;
		if (env.getEnableCHAVsetting()) {
			xavs = chavs;	// CHをキーに
		}
		
		AVs c = new AVs();
		
		c.setRecorderId(key_recorderId);
		c.setGenre(key_genre);
		
		c.setVideorate((String)jCBXPanel_videorate.getSelectedItem());
		c.setAudiorate((String)jCBXPanel_audiorate.getSelectedItem());
		c.setDVDCompat((String)jCBXPanel_dvdcompat.getSelectedItem());
		c.setDevice((String)jCBXPanel_device.getSelectedItem());
		c.setXChapter((String)jCBXPanel_xChapter.getSelectedItem());
		c.setMsChapter((String)jCBXPanel_msChapter.getSelectedItem());
		c.setMvChapter((String)jCBXPanel_mvChapter.getSelectedItem());
		c.setAspect((String)jCBXPanel_aspect.getSelectedItem());
		c.setBvperf((String)jCBXPanel_bvperf.getSelectedItem());
		c.setLvoice((String)jCBXPanel_lvoice.getSelectedItem());
		c.setAutodel((String)jCBXPanel_autodel.getSelectedItem());
		c.setFolder((String)jCBXPanel_folder.getSelectedItem());
		c.setPursues(ITEM_YES.equals((String) jCBXPanel_pursues.getSelectedItem()));
		
		xavs.add(key_recorderId, key_genre, c);
		xavs.save();
		
		MWin.appendMessage(MSGID+"画質・音質等の設定を保存しました："+key_recorderId+" & "+((key_genre!=null)?(key_genre):("デフォルト")));
	}

	/**
	 * タイトルが入力されたよ
	 */
	private final ItemListener il_titleEntered = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				// キャレットを先頭へ
				((JTextField)jComboBox_title.getEditor().getEditorComponent()).setCaretPosition(0);
			}
		}
	};
	
	/**
	 * 番組IDを取得する
	 */
	private final ActionListener al_getEventId = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			StWin.clear();
			
			new SwingBackgroundWorker(false) {
				
				@Override
				protected Object doWorks() throws Exception {
					TatCount tc = new TatCount();
					StWin.appendMessage(MSGID+"番組IDを取得します");
					Integer evid = null;
					if ( ContentIdDIMORA.isValid(vals.hide_content_id) ) {
						ContentIdDIMORA.decodeContentId(vals.hide_content_id);
						String chid = ContentIdDIMORA.getChId();
						evid = doGetEventIdById(chid, true);
					}
					else {
						evid = doGetEventIdByName((String) jComboBox_ch.getSelectedItem(), true);
					}
					if ( evid == null ) {
						StWin.appendError(ERRID+String.format("番組IDの取得に失敗しました。所要時間： %.2f秒",tc.end()));
					}
					else {
						StWin.appendMessage(MSGID+String.format("番組IDを取得しました。所要時間： %.2f秒",tc.end()));
						if ( vals.hide_tvd != null ) {
							vals.hide_tvd.progid = vals.hide_content_id;
							vals.hide_tvd.setContentIdStr();
						}
						HDDRecorderList recs = recorders.findInstance((String) jComboBox_recorder.getSelectedItem());
						if ( recs.size() > 0 ) {
							jCBXPanel_pursues.setEnabled(recs.get(0).isPursuesEditable());
						}
					}
					return null;
				}
				
				@Override
				protected void doFinally() {
					StWin.setVisible(false);
				}
			}.execute();
			
			CommonSwingUtils.setLocationCenter(AbsReserveDialog.this, (Component) StWin);
			StWin.setVisible(true);
		}
	};
	
	/**
	 * タイトルに日付を追加する
	 */
	private ActionListener al_addDate = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			String date = (String) jComboBox_date.getSelectedItem();
			if ( ! date.matches("^\\d\\d\\d\\d/\\d\\d/\\d\\d.*$") ) {
				// 日付指定でなければ
				return;
			}
			date = date.substring(5,10).replaceAll("/","_");
			
			String title = (String) jComboBox_title.getSelectedItem();
			if ( title.matches("^.* \\d\\d_\\d\\d$") ) {
				// 日付更新
				title = title.substring(0,title.length()-6);
			}
			title = title + " " + date;
			if ( title.equals((String) jComboBox_title.getSelectedItem()) ) {
				// 変化なければ
				return;
			}
			
			int index = jComboBox_title.getSelectedIndex();
			jComboBox_title.insertItemAt(title,index);
			jComboBox_title.setSelectedIndex(index);
			
			// キャレットを末尾へ
			((JTextField)jComboBox_title.getEditor().getEditorComponent()).setCaretPosition(title.length());
		}
	};

	/**
	 * 開始時刻を１分進める
	 */
	private final ActionListener al_upAmm = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if ( _upmm(jTextField_ahh,jTextField_amm) ) {
				refreshComboBox_date(+1);
			}
		}
	};
	
	/**
	 * 開始時刻を１分戻す
	 */
	private final ActionListener al_downAmm = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if ( _downmm(jTextField_ahh,jTextField_amm) ) {
				refreshComboBox_date(-1);
			}
		}
	};
	
	/**
	 * 終了時刻を１分進める
	 */
	private final ActionListener al_upZmm = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			_upmm(jTextField_zhh,jTextField_zmm);
		}
	};
	
	/**
	 * 終了時刻を１分戻す
	 */
	private final ActionListener al_downZmm = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			_downmm(jTextField_zhh,jTextField_zmm);
		}
	};
	
	private boolean _upmm(JTextField hh, JTextField mm) {
		boolean b = false;
		Integer amm = Integer.valueOf(mm.getText()) + 1;
		Integer ahh = amm / 60;
		if (ahh > 0) {
			ahh += Integer.valueOf(hh.getText());
			ahh %= 24;
			hh.setText(String.format("%02d",ahh));
			// 日付変更線を超えた
			if (ahh == 0) {
				b = true;
			}
		}
		amm = amm % 60;
		mm.setText(String.format("%02d",amm));
		return b;
	}
	
	private boolean _downmm(JTextField hh, JTextField mm) {
		boolean b = false;
		Integer amm = Integer.valueOf(mm.getText()) + 59;
		amm %= 60;
		if (amm == 59) {
			Integer ahh = Integer.valueOf(hh.getText()) + 23;
			ahh %= 24;
			hh.setText(String.format("%02d",ahh));
			// 日付変更線を超えた
			if (ahh == 23) {
				b = true;
			}
		}
		mm.setText(String.format("%02d",amm));
		return b;
	}
	
	/**
	 * 開始終了日時をリセットする
	 */
	private MouseAdapter ml_resetStartEnd = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			int n = jComboBox_date.getSelectedIndex();
			jCheckBox_spoex_extend.setSelected(vals.isExtended);
			jCheckBox_OverlapDown2.setSelected(vals.isClipped);
			jComboBox_date.removeItemAt(0);
			jComboBox_date.insertItemAt(vals.byDateIni, 0);
			jComboBox_date.removeItemAt(1);
			jComboBox_date.insertItemAt(vals.byWeeklyIni, 1);
			jComboBox_date.setSelectedIndex(n);
			jTextField_ahh.setText(jTextField_Xahh.getText());
			jTextField_amm.setText(jTextField_Xamm.getText());
			jTextField_zhh.setText(jTextField_Xzhh.getText());
			jTextField_zmm.setText(jTextField_Xzmm.getText());
		}
	};

	/**
	 * 登録実行
	 */
	private final ActionListener al_doRecord = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			doRecord();
		}
	};

	/**
	 * 更新実行
	 */
	private final ActionListener al_doUpdate = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			doUpdate();
		}
	};
	
	/**
	 * キャンセル
	 */
	private final ActionListener al_doCancel = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			//setVisible(false);
			resetWhenWindowClosed();
			dispose();
		}
	};
	
	/*******************************************************************************
	 * コンポーネント
	 ******************************************************************************/
	
	private JPanel getJContentPane_rsv() {
		if (jContentPane_rsv == null) {
			jContentPane_rsv = new JPanel();
			jContentPane_rsv.setLayout(new SpringLayout());
			
			int y = 0;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJLabel_title("予約名"), 40, PARTS_HEIGHT, 5, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJButton_getEventId("番組ID取得"), LABEL_WIDTH, PARTS_HEIGHT, 55, y);
			
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJButton_addDate("日付追加"), 100, PARTS_HEIGHT, 270, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJLabel_ch("CH"), LABEL_WIDTH, PARTS_HEIGHT, 335+60, y);
			
			int spHeight2 = y;
			
			y += PARTS_HEIGHT;
			int x = 10;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJComboBox_title(), TITLE_WIDTH, PARTS_HEIGHT, x, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJComboBox_ch(), CHNAME_WIDTH, PARTS_HEIGHT, x+=TITLE_WIDTH+SEP_WIDTH, y);

			y += PARTS_HEIGHT;
			x = 35;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJLabel_recorders("レコーダ"), LABEL_WIDTH, PARTS_HEIGHT, x, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJLabel_encoder("エンコーダ"), LABEL_WIDTH, PARTS_HEIGHT, x+=RECORDER_WIDTH, y);
			
			y += PARTS_HEIGHT;
			x = 35+SEP_WIDTH_NARROW;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJComboBox_recorder(), RECORDER_WIDTH, PARTS_HEIGHT, x, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJComboBox_encoder(), ENCODER_WIDTH, PARTS_HEIGHT, x+=RECORDER_WIDTH+SEP_WIDTH, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJLabel_encoderemptywarn(""),LABEL_WIDTH,PARTS_HEIGHT,x+=ENCODER_WIDTH+SEP_WIDTH,y);

			y += PARTS_HEIGHT;
			x = 35;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJLabel_date("録画日付"), LABEL_WIDTH, PARTS_HEIGHT, x, y);
			
			int hmx = 210;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJLabel_ahh("開始時刻"), 75, PARTS_HEIGHT, hmx-SEP_WIDTH_NARROW, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJLabel_zhh("終了時刻"), 75, PARTS_HEIGHT, hmx+120-SEP_WIDTH_NARROW, y);
		
			y += PARTS_HEIGHT;
			x = 35+SEP_WIDTH_NARROW;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJComboBox_date(), LABEL_WIDTH, PARTS_HEIGHT, x, y);
			
			hmx = 210;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJTextField_ahh(), 40, PARTS_HEIGHT, hmx, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJLabel_asep(":"), 10, PARTS_HEIGHT, hmx+=40, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJTextField_amm(), 40, PARTS_HEIGHT, hmx+=10, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJButton_amm_up(), 20, 12, hmx+=42, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJButton_amm_down(), 20, 12, hmx, y+13);
			
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJTextField_zhh(), 40, PARTS_HEIGHT, hmx+=28, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJLabel_zsep(":"), 10, PARTS_HEIGHT, hmx+=40, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJTextField_zmm(), 40, PARTS_HEIGHT, hmx+=10, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJButton_zmm_up(), 20, 12, hmx+=42, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJButton_zmm_down(), 20, 12, hmx, y+13);

			int exy = y-12;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJCheckBox_spoex_extend("スポーツ延長",LABEL_WIDTH,true), 200, PARTS_HEIGHT, hmx+=28, exy);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJCheckBox_OverlapDown2("終了時刻１分短縮",LABEL_WIDTH,true), 200, PARTS_HEIGHT, hmx, exy+PARTS_HEIGHT);

			y += PARTS_HEIGHT+2;
			hmx = 210;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJTextField_Xahh(), 40, 21, hmx, y+2);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJLabel_Xasep(":"), 10, 21, hmx+=40, y+2);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJTextField_Xamm(), 40, 21, hmx+=10, y+2);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJTextField_Xzhh(), 40, 21, hmx+=70, y+2);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJLabel_Xzsep(":"), 10, 21, hmx+=40, y+2);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJTextField_Xzmm(), 40, 21, hmx+=10, y+2);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJButton_Xreset(""), 20, 15, hmx+=42, y+5);
			
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJLabel_detail("番組詳細"), 100, PARTS_HEIGHT, 35, y);
			
			y += PARTS_HEIGHT;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJScrollPane_detail(), 600, 95, 40, y);
			
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCheckBox_Exec = new JCheckBoxPanel("予約実行",LABEL_WIDTH,true), 200, PARTS_HEIGHT, 650, y);
			jCheckBox_Exec.setSelected(true);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCheckBox_Autocomplete = new JCheckBoxPanel("ﾀｲﾄﾙ自動補完",LABEL_WIDTH,true), 200, PARTS_HEIGHT, 650, y+PARTS_HEIGHT);
			jCheckBox_Autocomplete.setSelected(env.getUseAutocomplete());
			//CommonSwingUtils.putComponentOn(jContentPane_rsv, jCheckBox_Pursues = new JCheckBoxPanel("番組追従",LABEL_WIDTH,true), 200, PARTS_HEIGHT, 650, y+50);
			//jCheckBox_Pursues.setSelected(false);
			//jCheckBox_Pursues.setEnabled(false);
			
			y += 95;
			int BoxTop = y+5;
			
			y += 10;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCBXPanel_genre = new JComboBoxPanel("ジャンル",110,150), COMBO_WIDTH_WIDE, COMBO_HEIGHT, 25, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCBXPanel_subgenre = new JComboBoxPanel("サブジャンル",110,150), COMBO_WIDTH_WIDE, COMBO_HEIGHT, 190, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCBXPanel_autodel = new JComboBoxPanel("自動削除",110,100), COMBO_WIDTH, COMBO_HEIGHT, 420, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCBXPanel_xChapter = new JComboBoxPanel("無音部分ﾁｬﾌﾟﾀ分割",110,100), COMBO_WIDTH, COMBO_HEIGHT, 535, y);
			
			y += PARTS_HEIGHT+PARTS_HEIGHT;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCBXPanel_videorate = new JComboBoxPanel("画質",110,150), COMBO_WIDTH_WIDE, COMBO_HEIGHT, 25, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCBXPanel_audiorate = new JComboBoxPanel("音質",110,100), COMBO_WIDTH, COMBO_HEIGHT, 190, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCBXPanel_bvperf = new JComboBoxPanel("高ﾚｰﾄ節約",110,100), COMBO_WIDTH, COMBO_HEIGHT, 305, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCBXPanel_lvoice = new JComboBoxPanel("ﾗｲﾝ音声選択",110,100), COMBO_WIDTH, COMBO_HEIGHT, 420, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCBXPanel_msChapter = new JComboBoxPanel("DVD/ｼｰﾝﾁｬﾌﾟﾀ分割",110,100), COMBO_WIDTH, COMBO_HEIGHT, 535, y);
			
			int spHeight1 = y;
			
			y += PARTS_HEIGHT+PARTS_HEIGHT;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCBXPanel_folder = new JComboBoxPanel("記録先フォルダ",100,150), COMBO_WIDTH_WIDE, COMBO_HEIGHT, 25, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCBXPanel_device = new JComboBoxPanel("記録先デバイス",110,100), COMBO_WIDTH, COMBO_HEIGHT, 190, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCBXPanel_dvdcompat = new JComboBoxPanel("BD/DVD互換モード",110,100), COMBO_WIDTH, COMBO_HEIGHT, 305, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCBXPanel_aspect = new JComboBoxPanel("DVD記録時画面比",110,100), COMBO_WIDTH, COMBO_HEIGHT, 420, y);
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCBXPanel_mvChapter = new JComboBoxPanel("音多/本編ﾁｬﾌﾟﾀ分割",110,100), COMBO_WIDTH, COMBO_HEIGHT, 535, y);

			y += PARTS_HEIGHT+PARTS_HEIGHT;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jCBXPanel_pursues = new JComboBoxPanel("番組追従",110,100), COMBO_WIDTH, COMBO_HEIGHT, 535, y);

			y += PARTS_HEIGHT+PARTS_HEIGHT;

			// 録画設定を囲む枠線
			JLabel jl = null;
			int BoxBottom = y+SEP_HEIGHT;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, jl = new JLabel(), BOX_WIDTH, BoxBottom-BoxTop, 15, BoxTop);
			jl.setBorder(new LineBorder(Color.GRAY,1));

			y = BoxBottom+SEP_HEIGHT_NALLOW;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getLikeRsvPane(), LIKELIST_WIDTH, PARTS_HEIGHT*LIKELIST_ROWS, 15, y);
			
			y += PARTS_HEIGHT*LIKELIST_ROWS+SEP_HEIGHT;
			Dimension d = new Dimension(PANEL_WIDTH,y);
			jContentPane_rsv.setPreferredSize(d);

			// 特殊配置(1)
			y = spHeight1+8;
			if (env.getEnableCHAVsetting()) {
				CommonSwingUtils.putComponentOn(jContentPane_rsv, new JLabel("放送局別"), LABEL_WIDTH, PARTS_HEIGHT, 655, y);
			}
			else {
				CommonSwingUtils.putComponentOn(jContentPane_rsv, new JLabel("ジャンル別"), LABEL_WIDTH, PARTS_HEIGHT, 655, y);
			}
			y+=17;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJLabel_rectype("録画設定の選択"), LABEL_WIDTH, PARTS_HEIGHT, 655, y);
			y += PARTS_HEIGHT;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJButton_load("開く"), 75, PARTS_HEIGHT, 660, y);
			y += 30;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJButton_save("保存"), 75, PARTS_HEIGHT, 660, y);
			y += 40;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJButton_savedefault("既定化"), 75, PARTS_HEIGHT, 660, y);
			
			// 特殊配置(2)
			y = spHeight2+20;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, new JLabel("予約"), LABEL_WIDTH, PARTS_HEIGHT, 655, y);
			y += PARTS_HEIGHT;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJButton_record("新規"), 75, PARTS_HEIGHT, 660, y);
			y += 30;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJButton_update("更新"), 75, PARTS_HEIGHT, 660, y);
			y += 30;
			CommonSwingUtils.putComponentOn(jContentPane_rsv, getJButton_cancel("ｷｬﾝｾﾙ"), 75, PARTS_HEIGHT, 660, y);
		}
		return jContentPane_rsv;
	}
	
	private JLabel getJLabel_date(String s)
	{
		if (jLabel_date == null) {
			jLabel_date = new JLabel();
			jLabel_date.setText(s);
		}
		return(jLabel_date);
	}
	
	private JComboBox getJComboBox_date() {
		if (jComboBox_date == null) {
			jComboBox_date = new JComboBox();
		}
		return jComboBox_date;
	}
	
	private JLabel getJLabel_detail(String s)
	{
		if (jLabel_detail == null) {
			jLabel_detail = new JLabel();
			jLabel_detail.setText(s);
		}
		return(jLabel_detail);
	}

	private JCheckBoxPanel getJCheckBox_OverlapDown2(String s, int labelWidth, boolean rev) {
		if (jCheckBox_OverlapDown2 == null) {
			jCheckBox_OverlapDown2 = new JCheckBoxPanel(s,labelWidth,rev);

			jCheckBox_OverlapDown2.addActionListener(al_overlapClipClicked);
		}
		return(jCheckBox_OverlapDown2);
	}
	
	private JCheckBoxPanel getJCheckBox_spoex_extend(String s, int labelWidth, boolean rev) {
		
		if (jCheckBox_spoex_extend == null) {
			jCheckBox_spoex_extend = new JCheckBoxPanel(s,labelWidth,rev);

			jCheckBox_spoex_extend.addActionListener(al_spoexClicked);
		}
		return(jCheckBox_spoex_extend);
	}
	
	private JScrollPane getJScrollPane_detail()
	{
		if (jScrollPane_detail == null) {
			jScrollPane_detail = new JScrollPane(getJTextArea_detail(),JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPane_detail.setBorder(new LineBorder(Color.BLACK));
		}
		return(jScrollPane_detail);
	}
	private JTextArea getJTextArea_detail()
	{
		if (jTextArea_detail == null) {
			jTextArea_detail = new JTextAreaWithPopup();
			jTextArea_detail.setLineWrap(true);
		}
		return(jTextArea_detail);
	}
	
	private JLabel getJLabel_rectype(String s)
	{
		if (jLabel_rectype == null) {
			jLabel_rectype = new JLabel();
			jLabel_rectype.setText(s);
		}
		return(jLabel_rectype);
	}
	
	private JButton getJButton_load(String s) {
		if (jButton_load == null) {
			jButton_load = new JButton(s);
			jButton_load.addActionListener(al_loadAction);
		}
		return jButton_load;
	}
	
	private JButton getJButton_save(String s) {
		if (jButton_save == null) {
			jButton_save = new JButton(s);
			jButton_save.addActionListener(al_saveAction);
		}
		return jButton_save;
	}
	// デフォルトの画質・音質等の設定の保存
	private JButton getJButton_savedefault(String s) {
		if (jButton_savedefault == null) {
			jButton_savedefault = new JButton(s);
			jButton_savedefault.setForeground(Color.BLUE);
			jButton_savedefault.addActionListener(al_saveDefault);
			
			jButton_savedefault.setToolTipText(TEXT_SAVEDEFAULT);
		}
		return jButton_savedefault;
	}

	private JLabel getJLabel_title(String s)
	{
		if (jLabel_title == null) {
			jLabel_title = new JLabel();
			jLabel_title.setText(s);
		}
		return(jLabel_title);
	}
	
	private JLabel getJLabel_ch(String s)
	{
		if (jLabel_ch== null) {
			jLabel_ch= new JLabel();
			jLabel_ch.setText(s);
		}
		return(jLabel_ch);
	}
	
	private JLabel getJLabel_encoder(String s)
	{
		if (jLabel_encoder == null) {
			jLabel_encoder = new JLabel();
			jLabel_encoder.setText(s);
		}
		return(jLabel_encoder);
	}
	
	private JLabel getJLabel_encoderemptywarn(String s)
	{
		if (jLabel_encoderemptywarn == null) {
			jLabel_encoderemptywarn = new JLabel();
			jLabel_encoderemptywarn.setText(s);
			jLabel_encoderemptywarn.setForeground(Color.RED);
			//Font f= jLabel_encoderemptywarn.getFont();
			//jLabel_encoderemptywarn.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		}
		return(jLabel_encoderemptywarn);
	}

	private JLabel getJLabel_recorders(String s)
	{
		if (jLabel_recorders == null) {
			jLabel_recorders = new JLabel();
			jLabel_recorders.setText(s);
		}
		return(jLabel_recorders);
	}
	
	private JLabel getJLabel_ahh(String s)
	{
		if (jLabel_ahh == null) {
			jLabel_ahh = new JLabel();
			jLabel_ahh.setText(s);
		}
		return(jLabel_ahh);
	}
	
	private JLabel getJLabel_zhh(String s)
	{
		if (jLabel_zhh == null) {
			jLabel_zhh = new JLabel();
			jLabel_zhh.setText(s);
		}
		return(jLabel_zhh);
	}

	private JComboBox getJComboBox_title() {
		if (jComboBox_title == null) {
			jComboBox_title = new JComboBoxWithPopup();
			jComboBox_title.addPopupWidth(150);
			jComboBox_title.setEditable(true);
			
			jComboBox_title.addItemListener(il_titleEntered);
		}
		return jComboBox_title;
	}
	
	/**
	 * EpgDataCap_BonプラグインのCHコードとを利用してテレビ王国から番組IDを取得します
	 * @since 3.14.5β
	 */
	private JButton getJButton_getEventId(String s) {
		if (jButton_getEventId == null) {
			jButton_getEventId = new JButton(s);
			
			jButton_getEventId.addActionListener(al_getEventId);
		}
		return jButton_getEventId;
	}
	
	private JButton getJButton_addDate(String s) {
		if (jButton_addDate == null) {
			jButton_addDate = new JButton(s);
			
			jButton_addDate.addActionListener(al_addDate);
		}
		return jButton_addDate;
	}

	private JComboBox getJComboBox_ch() {
		if (jComboBox_ch== null) {
			jComboBox_ch = new JComboBox();
		}
		return jComboBox_ch;
	}
	
	private JComboBox getJComboBox_encoder() {
		if (jComboBox_encoder == null) {
			jComboBox_encoder = new JWideComboBox();
			jComboBox_encoder.addPopupWidth(100);
		}
		return jComboBox_encoder;
	}
	
	private JComboBox getJComboBox_recorder() {
		if (jComboBox_recorder == null) {
			jComboBox_recorder = new JComboBox();
		}
		return jComboBox_recorder;
	}

	// 開始時刻
	private JTextField getJTextField_Xahh() {
		if (jTextField_Xahh == null) {
			jTextField_Xahh = new JTextField();
			jTextField_Xahh.setEditable(false);
			jTextField_Xahh.setForeground(Color.PINK);
			jTextField_Xahh.setBorder(null);
			jTextField_Xahh.setHorizontalAlignment(JTextField.CENTER);
		}
		return jTextField_Xahh;
	}
	private JTextField getJTextField_Xamm() {
		if (jTextField_Xamm == null) {
			jTextField_Xamm = new JTextField();
			jTextField_Xamm.setEditable(false);
			jTextField_Xamm.setForeground(Color.PINK);
			jTextField_Xamm.setBorder(null);
			jTextField_Xamm.setHorizontalAlignment(JTextField.CENTER);
		}
		return jTextField_Xamm;
	}
	private JLabel getJLabel_Xasep(String s)
	{
		if (jLabel_Xasep == null) {
			jLabel_Xasep = new JLabel(s);
		}
		return(jLabel_Xasep);
	}
	// 開始時刻
	private JTextField getJTextField_ahh() {
		if (jTextField_ahh == null) {
			jTextField_ahh = new JTextField();
			jTextField_ahh.setHorizontalAlignment(JTextField.CENTER);
		}
		return jTextField_ahh;
	}
	private JTextField getJTextField_amm() {
		if (jTextField_amm == null) {
			jTextField_amm = new JTextField();
			jTextField_amm.setHorizontalAlignment(JTextField.CENTER);
		}
		return jTextField_amm;
	}
	private JLabel getJLabel_asep(String s)
	{
		if (jLabel_asep == null) {
			jLabel_asep = new JLabel(s);
		}
		return(jLabel_asep);
	}
	// 開始時刻上げ下げ
	private JButton getJButton_amm_up() {
		if (jButton_amm_up == null) {
			jButton_amm_up = new JButton();
			//
			jButton_amm_up.addActionListener(al_upAmm);
		}
		return jButton_amm_up;
	}
	private JButton getJButton_amm_down() {
		if (jButton_amm_down == null) {
			jButton_amm_down = new JButton();
			//
			jButton_amm_down.addActionListener(al_downAmm);
		}
		return jButton_amm_down;
	}
	
	//
	private JTextField getJTextField_Xzhh() {
		if (jTextField_Xzhh == null) {
			jTextField_Xzhh = new JTextField();
			jTextField_Xzhh.setEditable(false);
			jTextField_Xzhh.setForeground(Color.PINK);
			jTextField_Xzhh.setBorder(null);
			jTextField_Xzhh.setHorizontalAlignment(JTextField.CENTER);
		}
		return jTextField_Xzhh;
	}
	private JTextField getJTextField_Xzmm() {
		if (jTextField_Xzmm == null) {
			jTextField_Xzmm = new JTextField();
			jTextField_Xzmm.setEditable(false);
			jTextField_Xzmm.setForeground(Color.PINK);
			jTextField_Xzmm.setBorder(null);
			jTextField_Xzmm.setHorizontalAlignment(JTextField.CENTER);
		}
		return jTextField_Xzmm;
	}
	private JLabel getJLabel_Xzsep(String s)
	{
		if (jLabel_Xzsep == null) {
			jLabel_Xzsep = new JLabel(s);
		}
		return(jLabel_Xzsep);
	}
	//
	private JTextField getJTextField_zhh() {
		if (jTextField_zhh == null) {
			jTextField_zhh = new JTextField();
			jTextField_zhh.setHorizontalAlignment(JTextField.CENTER);
		}
		return jTextField_zhh;
	}
	private JTextField getJTextField_zmm() {
		if (jTextField_zmm == null) {
			jTextField_zmm = new JTextField();
			jTextField_zmm.setHorizontalAlignment(JTextField.CENTER);
		}
		return jTextField_zmm;
	}
	private JLabel getJLabel_zsep(String s)
	{
		if (jLabel_zsep == null) {
			jLabel_zsep = new JLabel(s);
		}
		return(jLabel_zsep);
	}
	// 終了時刻上げ下げ
	private JButton getJButton_zmm_up() {
		if (jButton_zmm_up == null) {
			jButton_zmm_up = new JButton();
			//
			jButton_zmm_up.addActionListener(al_upZmm);
		}
		return jButton_zmm_up;
	}
	private JButton getJButton_zmm_down() {
		if (jButton_zmm_down == null) {
			jButton_zmm_down = new JButton();
			//
			jButton_zmm_down.addActionListener(al_downZmm);
		}
		return jButton_zmm_down;
	}
	
	//
	private JButton getJButton_Xreset(String s) {
		if (jButton_Xreset == null) {
			jButton_Xreset = new JButton(s);
			//
			jButton_Xreset.addMouseListener(ml_resetStartEnd);
		}
		return(jButton_Xreset);
	}
	
	private JScrollPane getLikeRsvPane() {
		if (likersvpane == null ) {
			likersvpane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			likersvpane.setRowHeaderView(likersvrowheader = new LikeRsvRowHeader());
			likersvpane.setViewportView(getLikeRsvTable());
			
			Dimension d = new Dimension(LRT_HEADER_WIDTH,0);
			likersvpane.getRowHeader().setPreferredSize(d);
			
			likersvpane.getRowHeader().setVisible(true);
		}
		return likersvpane;
	}
	
	private LikeRsvTable getLikeRsvTable() {
		if (likersvtable == null) {
			
			// カラム名の初期化
			ArrayList<String> cola = new ArrayList<String>();
			for ( LikeRsvColumn lc : LikeRsvColumn.values() ) {
				if ( lc.getIniWidth() >= 0 ) {
					cola.add(lc.getName());
				}
			}
			final String[] colname = cola.toArray(new String[0]);
			
			//　テーブルの基本的な設定
			DefaultTableModel model = new DefaultTableModel(colname, 0);
			
			likersvtable = new LikeRsvTable(model);
			likersvtable.setAutoResizeMode(JNETable.AUTO_RESIZE_OFF);
			
			// 各カラムの幅を設定する
			DefaultTableColumnModel columnModel = (DefaultTableColumnModel)likersvtable.getColumnModel();
			TableColumn column = null;
			for ( LikeRsvColumn lc : LikeRsvColumn.values() ) {
				if ( lc.getIniWidth() < 0 ) {
					continue;
				}
				column = columnModel.getColumn(lc.ordinal());
				column.setPreferredWidth(lc.getIniWidth());
			}
		}
		
		return likersvtable;
	}
	
	private JButton getJButton_record(String s) {
		if (jButton_record == null) {
			jButton_record = new JButton(s);
			jButton_record.setForeground(Color.RED);
			jButton_record.addActionListener(al_doRecord);
		}
		return jButton_record;
	}
	
	private JButton getJButton_update(String s) {
		if (jButton_update == null) {
			jButton_update = new JButton();
			jButton_update.setText(s);

			jButton_update.addActionListener(al_doUpdate);
		}
		
		return jButton_update;
	}

	private JButton getJButton_cancel(String s) {
		if (jButton_cancel == null) {
			jButton_cancel = new JButton(s);
			jButton_cancel.addActionListener(al_doCancel);
		}
		return jButton_cancel;
	}
	
	
	
	
	/*******************************************************************************
	 * 独自部品
	 ******************************************************************************/
	
	private class LikeRsvTable extends JNETable {

		private static final long serialVersionUID = 1L;

		public LikeRsvTable(DefaultTableModel model) {
			super(model,true);
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			if ( row == 0 ) {
				if ( column == LikeRsvColumn.TITLE.ordinal() ) {
					if ( vals.likeRsvList.size() == 0 ) {
						return LIKERSVID_NONE;
					}
					else {
						return LIKERSVID_NOTSELECTED;
					}
				}
				else if ( column == LikeRsvColumn.START.ordinal() ) {
					return "-";
				}
				else if ( column == LikeRsvColumn.RECORDER.ordinal() ) {
					return vals.hide_default_recorder.Myself();
				}
				return null;
			}
			if ( column == LikeRsvColumn.TITLE.ordinal() ) {
				return vals.likeRsvList.get(row-1).getRsv().getTitle();
			}
			else if ( column == LikeRsvColumn.START.ordinal() ) {
				return vals.likeRsvList.get(row-1).getRsv().getStartDateTime();
			}
			else if ( column == LikeRsvColumn.RECORDER.ordinal() ) {
				return vals.likeRsvList.get(row-1).getRec().Myself();
			}
			else if ( column == LikeRsvColumn.TUNER.ordinal() ) {
				return vals.likeRsvList.get(row-1).getRsv().getTuner();
			}
			return null;
		}
		
		@Override
		public int getSelectedRow() {
			return super.getSelectedRow()-1;
		}
		
		@Override
		public void setRowSelectionInterval(int index0, int index1) {
			super.setRowSelectionInterval(index0+1, index1+1);
		}
		
		@Override
		public int getRowCount() {
			if ( vals == null || vals.likeRsvList == null ) {
				return 1;
			}
			return vals.likeRsvList.size()+1;
		}
	}
	
	private class LikeRsvRowHeader extends JTable {
		
		private static final long serialVersionUID = 1L;
		
		public LikeRsvRowHeader() {
			super();
			
			String[] colname = {""};
			DefaultTableModel model = new DefaultTableModel(colname,0);
			this.setModel(model);

			//this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			//this.setRowSelectionAllowed(false);
			this.setEnabled(false);

			DefaultTableCellRenderer cr = new DefaultTableCellRenderer() {
				
				private static final long serialVersionUID = 1L;
				
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					c.setBackground(table.getTableHeader().getBackground());
					return c;
				};
			};
			cr.setHorizontalAlignment(JLabel.CENTER);
			cr.setOpaque(true);
			this.getColumnModel().getColumn(0).setCellRenderer(cr);
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			return (row==0) ? "-" : String.valueOf(row);
		}
		
		@Override
		public int getRowCount() {
			if ( vals == null || vals.likeRsvList == null ) {
				return 1;
			}
			return vals.likeRsvList.size()+1;
		}
	}
}
