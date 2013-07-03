package tainavi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.LineBorder;


/**
 * 予約ダイアログを目的ごとに３ブロックにわけたうちの「番組情報」部分のコンポーネント
 * @since 3.22.2β
 */
public class TitleEditorPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/*******************************************************************************
	 * 定数
	 ******************************************************************************/

	//
	private static final String ITEM_EVIDNEEDED = "番組ID取得";
	
	// レイアウト関連
	private static final int PARTS_HEIGHT = 25;
	private static final int SEP_WIDTH = 10;
	private static final int SEP_WIDTH_NARROW = 5;
	private static final int SEP_HEIGHT = 10;
	private static final int SEP_HEIGHT_NALLOW = 5;
	
	private static final int COMBO_WIDTH = 115;
	private static final int COMBO_WIDTH_WIDE = 155;
	private static final int COMBO_HEIGHT = 43;

	private static final int TITLE_WIDTH = COMBO_WIDTH_WIDE+COMBO_WIDTH*2+SEP_WIDTH*2;
	private static final int CHNAME_WIDTH = COMBO_WIDTH*2+SEP_WIDTH;
	private static final int DETAIL_WIDTH = TITLE_WIDTH+CHNAME_WIDTH+SEP_WIDTH;
	private static final int DETAIL_HEIGHT = 100;
	private static final int DATE_WIDTH = 175;
	
	private static final int HOUR_WIDTH = 50;
	
	
	private static final int LABEL_WIDTH = 150;
	private static final int BUTTON_WIDTH = 75;

	
	/*******************************************************************************
	 * コンポーネント
	 ******************************************************************************/
	
	private JLabel jLabel_title = null;
	private JButton jButton_getEventId = null;
	private JTXTLabel jLabel_eventId = null;
	private JButton jButton_addDate = null;
	private JComboBoxWithPopup jComboBox_title = null;
	private JLabel jLabel_ch = null;
	private JWideComboBox jComboBox_ch = null;
	
	private JLabel jLabel_date = null;
	private JComboBox jComboBox_date = null;
	
	private JLabel jLabel_aTime = null;
	private JComboBoxWithPopup jComboBox_ahh = null;
	private JLabel jLabel_asep = null;
	private JComboBoxWithPopup jComboBox_amm = null;
	private JButton jButton_amm_up = null;
	private JButton jButton_amm_down = null;
	
	private JLabel jLabel_zTime = null;
	private JComboBoxWithPopup jComboBox_zhh = null;
	private JLabel jLabel_zsep = null;
	private JComboBoxWithPopup jComboBox_zmm = null;
	private JButton jButton_zmm_up = null;
	private JButton jButton_zmm_down = null;
	
	private JLabel jLabel_recmin = null;
	
	private JLabel jLabel_detail = null;
	private JScrollPane jScrollPane_detail = null;
	private JTextAreaWithPopup jTextArea_detail = null;
	
	private JCheckBoxPanel jCheckBox_OverlapDown2 = null;
	private JCheckBoxPanel jCheckBox_spoex_extend = null;
	
	private JCheckBoxPanel jCheckBox_Autocomplete = null;

	private JButton jButton_update = null;
	private JButton jButton_record = null;
	private JButton jButton_cancel = null;
	

	/*******************************************************************************
	 * 部品
	 ******************************************************************************/

	private RecordExecutable recexec = null;
	
	TimeVal tVal = null;
	
	public static class TimeVal {
		String date = null;
		Integer ahh = null;
		Integer amm = null;
		Integer zhh = null;
		Integer zmm = null;
		
		String startDateTime = null; 
		String endDateTime = null; 
				
		Boolean margined = null;	// 開始時間前倒し
		Boolean clipped = null;	// 終了時間短縮
		
		Boolean spoex = null;
		int spoexlen = 0;
	}
	
	
	/*******************************************************************************
	 * コンストラクタ
	 ******************************************************************************/
	
	public TitleEditorPanel() {

		super();
		setBorder(new LineBorder(Color.BLACK, 1));

		addComponents();
	}
	
	private void addComponents() {
		
		setLayout(new SpringLayout());
		
		int y = SEP_HEIGHT_NALLOW;
		int x = SEP_WIDTH_NARROW;
		
		CommonSwingUtils.putComponentOn(this, getJLabel_title("予約名"), 40, PARTS_HEIGHT, x, y);
		CommonSwingUtils.putComponentOn(this, getJComboBox_title(), TITLE_WIDTH, PARTS_HEIGHT, x+SEP_WIDTH_NARROW, y+PARTS_HEIGHT);
		
		CommonSwingUtils.putComponentOn(this, getJButton_getEventId(ITEM_EVIDNEEDED),	100, PARTS_HEIGHT, x+50, y);
		CommonSwingUtils.putComponentOn(this, getJLabel_eventId(""),					75,  PARTS_HEIGHT-2, x+50+100+SEP_WIDTH_NARROW, y+1);
		CommonSwingUtils.putComponentOn(this, getJButton_addDate("日付追加"),			100, PARTS_HEIGHT, x+SEP_WIDTH_NARROW+TITLE_WIDTH-100-SEP_HEIGHT_NALLOW, y);
		
		CommonSwingUtils.putComponentOn(this, getJLabel_ch("CH"), LABEL_WIDTH, PARTS_HEIGHT, x+TITLE_WIDTH+SEP_WIDTH, y);
		CommonSwingUtils.putComponentOn(this, getJComboBox_ch(), CHNAME_WIDTH, PARTS_HEIGHT, x+TITLE_WIDTH+SEP_WIDTH+SEP_WIDTH_NARROW, y+PARTS_HEIGHT);

		y += PARTS_HEIGHT*2+SEP_HEIGHT_NALLOW;
		
		{
			int hmx = x;
			CommonSwingUtils.putComponentOn(this, getJLabel_date("録画日付"),	LABEL_WIDTH,	PARTS_HEIGHT,	hmx,y);
			CommonSwingUtils.putComponentOn(this, getJComboBox_date(),		DATE_WIDTH,		PARTS_HEIGHT,	hmx+=SEP_WIDTH_NARROW, y+PARTS_HEIGHT);
			
			hmx += DATE_WIDTH+SEP_WIDTH;
			CommonSwingUtils.putComponentOn(this, getJLabel_aTime("開始時刻"),	75,	PARTS_HEIGHT,	hmx, y);
			CommonSwingUtils.putComponentOn(this, getJTextField_ahh(),		HOUR_WIDTH,	PARTS_HEIGHT,	hmx+=SEP_WIDTH_NARROW, y+PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(this, getJLabel_asep(":"),		10,			PARTS_HEIGHT,	hmx+=HOUR_WIDTH, y+PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(this, getJTextField_amm(),		HOUR_WIDTH,	PARTS_HEIGHT,	hmx+=10, y+PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(this, getJButton_amm_up(),		20,			12,				hmx+=HOUR_WIDTH+2, y+PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(this, getJButton_amm_down(),	20,			12,				hmx, y+PARTS_HEIGHT+13);
			
			hmx += 20+SEP_WIDTH_NARROW;
			
			CommonSwingUtils.putComponentOn(this, getJLabel_zTime("終了時刻"),	75,	PARTS_HEIGHT,	hmx, y);
			{
				CommonSwingUtils.putComponentOn(this, new JLabel("録画時間"),	75,	PARTS_HEIGHT,	hmx, y+PARTS_HEIGHT*2+2);
			}
			CommonSwingUtils.putComponentOn(this, getJTextField_zhh(),		HOUR_WIDTH,	PARTS_HEIGHT,	hmx+=SEP_WIDTH_NARROW, y+PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(this, getJLabel_zsep(":"),		10,			PARTS_HEIGHT,	hmx+=HOUR_WIDTH, y+PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(this, getJTextField_zmm(),		HOUR_WIDTH,	PARTS_HEIGHT,	hmx+=10, y+PARTS_HEIGHT);
			{		
				CommonSwingUtils.putComponentOn(this, getJLabel_recmin(""),					HOUR_WIDTH+10,	PARTS_HEIGHT-2,	hmx-10,			y+PARTS_HEIGHT*2+2+1);
			}
			CommonSwingUtils.putComponentOn(this, getJButton_zmm_up(),		20,			12,				hmx+=HOUR_WIDTH+2, y+PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(this, getJButton_zmm_down(),	20,			12,				hmx, y+PARTS_HEIGHT+13);
		
			hmx += 20+SEP_WIDTH;
			
			CommonSwingUtils.putComponentOn(this, jCheckBox_Autocomplete = new JCheckBoxPanel("タイトル自動補完(RDのみ)",LABEL_WIDTH,true),	200, PARTS_HEIGHT, hmx, y);
			CommonSwingUtils.putComponentOn(this, getJCheckBox_spoex_extend("延長警告での終了時刻延長",LABEL_WIDTH,true),			200, PARTS_HEIGHT, hmx, y+PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(this, getJCheckBox_OverlapDown2("終了時刻１分短縮",LABEL_WIDTH,true),					200, PARTS_HEIGHT, hmx, y+PARTS_HEIGHT*2);
		}
		
		y += PARTS_HEIGHT*2+SEP_WIDTH_NARROW;

		CommonSwingUtils.putComponentOn(this, getJLabel_detail("番組詳細"), 100, PARTS_HEIGHT, x, y);
		CommonSwingUtils.putComponentOn(this, getJScrollPane_detail(), DETAIL_WIDTH, DETAIL_HEIGHT, x+SEP_WIDTH_NARROW, y+PARTS_HEIGHT);
		
		y += PARTS_HEIGHT;
		
		
		
		y += DETAIL_HEIGHT+SEP_HEIGHT_NALLOW;
		
		// 特殊配置
		{
			int spy = SEP_HEIGHT_NALLOW+PARTS_HEIGHT;
			x = SEP_WIDTH_NARROW+DETAIL_WIDTH+SEP_WIDTH*2;
			
			CommonSwingUtils.putComponentOn(this, new JLabel("予約"),				BUTTON_WIDTH, PARTS_HEIGHT, x, spy);
			CommonSwingUtils.putComponentOn(this, getJButton_record("新規"),		BUTTON_WIDTH, PARTS_HEIGHT, x+SEP_WIDTH_NARROW, spy+=PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(this, getJButton_update("更新"),		BUTTON_WIDTH, PARTS_HEIGHT, x+SEP_WIDTH_NARROW, spy+=PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(this, getJButton_cancel("ｷｬﾝｾﾙ"),	BUTTON_WIDTH, PARTS_HEIGHT, x+SEP_WIDTH_NARROW, spy+=PARTS_HEIGHT);
		}
		
		x+=BUTTON_WIDTH+SEP_WIDTH_NARROW*2;
		
		Dimension d = new Dimension(x,y);
		setPreferredSize(d);
	}

	public void setRecordExecuter(RecordExecutable o) {
		recexec = o;
	}
	
	/*******************************************************************************
	 * アイテムの設定
	 ******************************************************************************/

	/***************************************
	 * 番組IDの設定
	 **************************************/

	/**
	 * @param enabled 番組IDの取得ができないシチュエーション（予約一覧から開くとか）では取得ボタンをfalseに。
	 */
	public boolean setContentIdValue(String content_id, boolean enabled) {
		
		Integer evid = null;
		if ( content_id != null ) {
			if ( ContentIdEDCB.decodeContentId(content_id) ) {
				evid = ContentIdEDCB.getEvId();
			}
			else if ( ContentIdREGZA.decodeContentId(content_id) && ContentIdREGZA.getEvId() != 0x0000 && ContentIdREGZA.getEvId() != 0xFFFF ) {
				evid = ContentIdREGZA.getEvId();
			}
			else if ( ContentIdDIMORA.decodeContentId(content_id) && ContentIdDIMORA.getEvId() != 0x0000 && ContentIdDIMORA.getEvId() != 0xFFFF ) {
				evid = ContentIdDIMORA.getEvId();
			}
		}

		// みつからにゃい
		if ( evid == null ) {
			jLabel_eventId.setValue(null);
			jLabel_eventId.setText(null);
			jButton_getEventId.setForeground(enabled ? Color.BLUE : Color.GRAY);
			jButton_getEventId.setEnabled(enabled);
			
			return false;
		}
		
		// みつかったじぇい
		jLabel_eventId.setValue(content_id);
		jLabel_eventId.setText(String.format("%04X",evid));
		jButton_getEventId.setForeground(Color.GRAY);
		jButton_getEventId.setEnabled(false);
		
		return true;
	}
	
	/***************************************
	 * まとめて設定
	 **************************************/
	
	/**
	 * まとめて設定：番組情報編
	 */
	public void setSelectedValues(ProgDetailList tvd) {
		// 番組ID
		setContentIdValue(tvd.progid,true);
		
		// タイトル
		{
			int index = jComboBox_title.indexOf(tvd.title);
			if ( index != -1 ) {
				jComboBox_title.setSelectedIndex(index);
			}
		}
		
		// 日付・時刻はここじゃないお
		
		// チャンネル
		{
			int index = jComboBox_ch.indexOf(tvd.center);
			if ( index != -1 ) {
				jComboBox_ch.setSelectedIndex(index);
			}
		}
		
		// 番組詳細
		{
			jTextArea_detail.setText(tvd.detail+"\n"+tvd.getAddedDetail());
			jTextArea_detail.setCaretPosition(0);
		}
	}
	
	/**
	 * まとめて設定：類似予約編
	 */
	public void setSelectedValues(ReserveList r) {
		// 番組ID
		setContentIdValue(r.getContentId(),true);
		
		// タイトル
		{
			int index = jComboBox_title.indexOf(r.getTitle());
			if ( index != -1 ) {
				jComboBox_title.setSelectedIndex(index);
			}
		}
		
		// 日付
		{
			int index = 0;
			if ( r.getRec_pattern_id() == HDDRecorder.RPTPTN_ID_BYDATE ) {
				index = 0;
			}
			else if ( r.getRec_pattern_id() == HDDRecorder.RPTPTN_ID_MON2THU ) {
				index = 2;
			}
			else if ( r.getRec_pattern_id() == HDDRecorder.RPTPTN_ID_MON2FRI ) {
				index = 3;
			}
			else if ( r.getRec_pattern_id() == HDDRecorder.RPTPTN_ID_MON2SAT ) {
				index = 4;
			}
			else {
				index = 1;
			}
			jComboBox_date.setSelectedIndex(index);
		}
		
		// チャンネル
		{
			int index = jComboBox_ch.indexOf(r.getCh_name());
			if ( index != -1 ) {
				jComboBox_ch.setSelectedIndex(index);
			}
		}
		
		// 番組詳細
		{
			jTextArea_detail.setText(r.getDetail());
			jTextArea_detail.setCaretPosition(0);
		}
	}
	
	
	/***************************************
	 * タイトルの設定
	 **************************************/
	
	public void setTitleItems(ProgDetailList tvd, LikeReserveList lrl, boolean autocomp ) {
		
		jComboBox_title.removeAllItems();
		
		// 番組表のタイトルを追加
		jComboBox_title.addItem(tvd.title);
		
		// 類似予約のタイトルも追加
		for ( LikeReserveItem ll : lrl ) {
			String l = ll.getRsv().getTitle();
			int index = jComboBox_title.indexOf(l);
			if ( index == -1 ) {
				// ダブりがなければ追加
				jComboBox_title.addItem(l);
			}
		}

		// タイトル自動設定(RDのみ)
		jCheckBox_Autocomplete.setSelected(autocomp);
		
		// 番組詳細
		jTextArea_detail.setText(tvd.detail+"\n"+tvd.getAddedDetail());
		jTextArea_detail.setCaretPosition(0);
	}
	
	/***************************************
	 * 放送局名の設定
	 **************************************/
	
	public void setChItem(HDDRecorder myrec, ProgDetailList tvd) {
		
		jComboBox_ch.removeAllItems();
		
		jComboBox_ch.addItem(tvd.center);
		for ( TextValueSet t : myrec.getChValue() ) {
			if ( t.getText().startsWith("外部") ) {
				jComboBox_ch.addItem(t.getText());
			}
		}
		
		jComboBox_ch.setEnabled( jComboBox_ch.getItemCount() > 1 );
	}
	
	/***************************************
	 * 時刻と長さの設定
	 **************************************/
	
	public void setTimeValue(TimeVal tValues) {
		
		tVal = tValues;
		
		jComboBox_ahh.setSelectedIndex(tVal.ahh);
		jComboBox_amm.setSelectedIndex(tVal.amm);
		jComboBox_zhh.setSelectedIndex(tVal.zhh);
		jComboBox_zmm.setSelectedIndex(tVal.zmm);
		
		if ( tVal.spoex != null && tVal.spoex ) {
			jCheckBox_spoex_extend.setEnabled(true);
			jCheckBox_spoex_extend.setForeground(Color.RED);
			jCheckBox_spoex_extend.setSelected(true);
		}
		else {
			jCheckBox_spoex_extend.setEnabled(false);
			jCheckBox_spoex_extend.setForeground(Color.BLACK);
			jCheckBox_spoex_extend.setSelected(false);
		}
			
		if ( tVal.clipped != null ) {
			jCheckBox_OverlapDown2.setEnabled(true);
			jCheckBox_OverlapDown2.setForeground(tVal.clipped ? Color.RED : Color.BLACK);
			jCheckBox_OverlapDown2.setSelected(tVal.clipped);
		}
		else {
			jCheckBox_OverlapDown2.setEnabled(false);
			jCheckBox_OverlapDown2.setForeground(Color.BLACK);
			jCheckBox_OverlapDown2.setSelected(false);
		}
	}
	
	private void setRecMinItem() {
		int recmin = CommonUtils.getRecMinVal(
				jComboBox_ahh.getSelectedIndex(),
				jComboBox_amm.getSelectedIndex(),
				jComboBox_zhh.getSelectedIndex(),
				jComboBox_zmm.getSelectedIndex());
		
		jLabel_recmin.setText(String.valueOf(recmin)+" 分 ");
	}
	
	/***************************************
	 * 日付の設定
	 **************************************/
	
	public void setDateItems(ProgDetailList tvd, TimeVal tValues) {
		
		jComboBox_date.removeAllItems();
		
		// リスト／新聞形式からの呼び出しなら番組情報をもとに
		GregorianCalendar c = CommonUtils.getCalendar(tValues.date);
		if ( c != null ) {
			jComboBox_date.addItem(CommonUtils.getDate(c));
			jComboBox_date.addItem(HDDRecorder.RPTPTN[c.get(Calendar.DAY_OF_WEEK)-Calendar.SUNDAY]);
		}
		
		setDateRepeatItems();
	}
	
	public void setDateItems(ReserveList myrsv, TimeVal tValues) {
		
		jComboBox_date.removeAllItems();
		
		// 本体予約一覧からの呼び出しなら既存の予約情報からの引継ぎ
		GregorianCalendar c = CommonUtils.getCalendar(myrsv.getRec_pattern());
		if ( c != null ) {
			// 単日予約の場合
			jComboBox_date.addItem(myrsv.getRec_pattern());
			jComboBox_date.addItem(HDDRecorder.RPTPTN[c.get(Calendar.DAY_OF_WEEK)-Calendar.SUNDAY]);
		}
		else {
			// 繰り返し予約の場合
			jComboBox_date.addItem(myrsv.getRec_nextdate());
			jComboBox_date.addItem(myrsv.getRec_pattern());
		}
		
		setDateRepeatItems();
	}
	
	private void setDateRepeatItems() {
		jComboBox_date.addItem(HDDRecorder.RPTPTN[7]);
		jComboBox_date.addItem(HDDRecorder.RPTPTN[8]);
		jComboBox_date.addItem(HDDRecorder.RPTPTN[9]);
		jComboBox_date.addItem(HDDRecorder.RPTPTN[10]);
	}

	/**
	 * 登録ボタンの有効・無効
	 */
	public void setEnabledRecordButton(boolean enabled) {
		jButton_record.setForeground(enabled ? Color.RED : Color.GRAY);
		jButton_record.setEnabled(enabled);
	}
	
	/**
	 * 更新ボタンの有効・無効
	 */
	public void setEnabledUpdateButton(boolean enabled) {
		jButton_update.setForeground(enabled ? Color.RED : Color.GRAY);
		jButton_update.setEnabled(enabled);
	}
	
	/*******************************************************************************
	 * リスナー
	 ******************************************************************************/
	
	/***************************************
	 * タイトル関連のリスナー
	 **************************************/

	/**
	 * タイトルが設定（！入力）されたらキャレットを先頭に戻す
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
	 * 日付ボタンでタイトルの末尾に日付を追加する
	 */
	private ActionListener al_addDate = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			GregorianCalendar ca = CommonUtils.getCalendar((String) jComboBox_date.getSelectedItem());
			if ( ca == null ) {
				// 日付指定でなければ
				return;
			}
			
			String date = String.format(" %02d_%02d", ca.get(Calendar.MONTH), ca.get(Calendar.DAY_OF_MONTH));
			String title = (String) jComboBox_title.getSelectedItem();
			if ( title.endsWith(date) ) {
				// 同じ日付が追加済みなら
				return;
			}
			
			String newTitle = title.replaceFirst(" \\d\\d_\\d\\d$", date);
			if ( title.equals(newTitle) ) {
				// 日付がついてなかった場合は
				newTitle = title+date;
			}
			
			// なければ追加して選択、あれば選択のみ
			int index = -1;
			for ( int i=0; i<jComboBox_title.getItemCount(); i++ ) {
				if ( newTitle.equals((String) jComboBox_title.getItemAt(i)) ) {
					index = i;
					break;
				}
			}
			if ( index == -1 ) {
				jComboBox_title.addItem(newTitle);
				index = jComboBox_title.getItemCount()-1;
			}
			jComboBox_title.setSelectedIndex(index);
			
			// キャレットを末尾へ
			((JTextField)jComboBox_title.getEditor().getEditorComponent()).setCaretPosition(newTitle.length());
		}
	};
	
	
	/***************************************
	 * 時刻を上下するリスナー
	 **************************************/

	/**
	 * 開始時刻を１分進める
	 */
	private final ActionListener al_upAmm = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			_updownMm(jComboBox_date,jComboBox_ahh,jComboBox_amm,-1);
		}
	};
	
	/**
	 * 開始時刻を１分戻す
	 */
	private final ActionListener al_downAmm = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			_updownMm(jComboBox_date,jComboBox_ahh,jComboBox_amm,+1);
		}
	};
	
	/**
	 * 終了時刻を１分進める
	 */
	private final ActionListener al_upZmm = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			_updownMm(null,jComboBox_zhh,jComboBox_zmm,-1);
		}
	};
	
	/**
	 * 終了時刻を１分戻す
	 */
	private final ActionListener al_downZmm = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			_updownMm(null,jComboBox_zhh,jComboBox_zmm,+1);
		}
	};
	
	/**
	 * updown共通部品
	 */
	private void _updownMm(JComboBox dt, JComboBox hh, JComboBox mm, int n) {
		
		String olddate;
		if ( dt != null ) {
			olddate = (String) jComboBox_date.getItemAt(0);
		}
		else {
			olddate = CommonUtils.getDate();
		}
		GregorianCalendar cal = CommonUtils.getCalendar(olddate+" "+hh.getSelectedItem()+":"+mm.getSelectedItem());
		cal.add(Calendar.MINUTE, n);
		hh.setSelectedIndex(cal.get(Calendar.HOUR_OF_DAY));
		mm.setSelectedIndex(cal.get(Calendar.MINUTE));

		if ( dt == null ) {
			return;
		}

		// 開始時刻の場合は日付変更線をまたぐかも
		
		String newdate = CommonUtils.getDate(cal);
		String ptrn = HDDRecorder.RPTPTN[cal.get(Calendar.DAY_OF_WEEK)-1];
		
		int idx = dt.getSelectedIndex();
		
		dt.removeItemAt(0);
		dt.insertItemAt(newdate, 0);
		dt.removeItemAt(1);
		dt.insertItemAt(ptrn, 1);
		
		dt.setSelectedIndex(idx);
	}
	
	/**
	 * 時刻アイテムが変更された
	 */
	private final ItemListener il_timemod = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if ( e.getStateChange() == ItemEvent.SELECTED ) {
				setRecMinItem();
			}			
		}
	};

	
	/***************************************
	 * 延長警告のリスナー
	 **************************************/
	
	/**
	 * 延長警告のＯＮ／ＯＦＦ
	 */
	private final ActionListener al_spoexClicked = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			GregorianCalendar cz = CommonUtils.getCalendar((String) jComboBox_date.getItemAt(0));
			cz.add(Calendar.HOUR_OF_DAY, jComboBox_zhh.getSelectedIndex()-cz.get(Calendar.HOUR_OF_DAY));
			cz.add(Calendar.MINUTE, jComboBox_zmm.getSelectedIndex()-cz.get(Calendar.MINUTE));
			
			if ( tVal.spoexlen > 0 ) {
				cz.add(Calendar.MINUTE, (jCheckBox_spoex_extend.isSelected() ? +tVal.spoexlen : -tVal.spoexlen));
				
				jComboBox_zhh.setSelectedIndex(cz.get(Calendar.HOUR_OF_DAY));
				jComboBox_zmm.setSelectedIndex(cz.get(Calendar.MINUTE));
			}
		}
	};
	
	
	/***************************************
	 * １分短縮のリスナー
	 **************************************/

	/**
	 * １分短縮のＯＮ／ＯＦＦ
	 */
	private final ActionListener al_overlapClipClicked = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			GregorianCalendar cz = CommonUtils.getCalendar((String) jComboBox_date.getItemAt(0));
			cz.add(Calendar.HOUR_OF_DAY, jComboBox_zhh.getSelectedIndex()-cz.get(Calendar.HOUR_OF_DAY));
			cz.add(Calendar.MINUTE, jComboBox_zmm.getSelectedIndex()-cz.get(Calendar.MINUTE));
			
			if (jCheckBox_OverlapDown2.isSelected() == true) {
				cz.add(Calendar.MINUTE,-1);
			}
			else {
				cz.add(Calendar.MINUTE,+1);
			}
			
			jComboBox_zhh.setSelectedIndex(cz.get(Calendar.HOUR_OF_DAY));
			jComboBox_zmm.setSelectedIndex(cz.get(Calendar.MINUTE));
		}
	};


	/***************************************
	 * 予約ボタン３種
	 **************************************/

	/**
	 * 登録実行
	 */
	private final ActionListener al_doRecord = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (recexec!=null) recexec.doRecord();
		}
	};

	/**
	 * 更新実行
	 */
	private final ActionListener al_doUpdate = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (recexec!=null) recexec.doUpdate();
		}
	};
	
	/**
	 * キャンセル
	 */
	private final ActionListener al_doCancel = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (recexec!=null) recexec.doCancel();
		}
	};
	
	/***************************************
	 * 番組ID取得
	 **************************************/
	
	/**
	 * 番組IDを取得する
	 */
	private final ActionListener al_getEventId = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			if ( recexec != null ) {
				
				String content_id = recexec.doGetEventId();
				
				if ( content_id != null ) {
					setContentIdValue(content_id, false);
				}
			}
		}
	};
	
	
	/*******************************************************************************
	 * 選択された値を呼び出し元に戻す
	 ******************************************************************************/
	
	public ReserveList getSelectedValues(ReserveList r) {
		
		r.setTitle((String)jComboBox_title.getSelectedItem());
		r.setTitlePop(TraceProgram.replacePop(r.getTitle()));
		r.setDetail(jTextArea_detail.getText());
		
		r.setCh_name((String)jComboBox_ch.getSelectedItem());
		r.setChannel("");			// PostRdEntry()中で取得するのでここはダミー
		
		r.setRec_pattern((String)jComboBox_date.getSelectedItem());
		r.setRec_pattern_id(-1);	// PostRdEntry()中で取得するのでここはダミー
		
		r.setRec_nextdate((String)jComboBox_date.getItemAt(0));	// PostRdEntry()中で取得するのでここはダミー(日付を入れるのはDIGA用)
		
		r.setAhh((String) jComboBox_ahh.getSelectedItem());
		r.setAmm((String) jComboBox_amm.getSelectedItem());
		r.setZhh((String) jComboBox_zhh.getSelectedItem());
		r.setZmm((String) jComboBox_zmm.getSelectedItem());
		
		r.setRec_min("");			// PostRdEntry()中で取得するのでここはダミー
		
		r.setStartDateTime("");		// PostRdEntry()中で取得するのでここはダミー
		r.setEndDateTime("");		// PostRdEntry()中で取得するのでここはダミー

		if ( jLabel_eventId.getText() != null && ! (jLabel_eventId.getText().endsWith("0000") ||jLabel_eventId.getText().endsWith("FFFF")) ) {
			r.setContentId(jLabel_eventId.getValue());
		}
		else {
			r.setContentId("");
		}
		
		r.setAutocomplete(jCheckBox_Autocomplete.isSelected());
		
		return r;
	}
	
	
	/*******************************************************************************
	 * コンポーネント
	 ******************************************************************************/
	
	private JLabel getJLabel_date(String s) {
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
	
	private JLabel getJLabel_detail(String s) {
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
	
	private JScrollPane getJScrollPane_detail()	{
		if (jScrollPane_detail == null) {
			jScrollPane_detail = new JScrollPane(getJTextArea_detail(),JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			
			jScrollPane_detail.setBorder(new LineBorder(Color.BLACK));
		}
		return(jScrollPane_detail);
	}
	
	private JTextArea getJTextArea_detail() {
		if (jTextArea_detail == null) {
			jTextArea_detail = new JTextAreaWithPopup();
			jTextArea_detail.setLineWrap(true);
		}
		return(jTextArea_detail);
	}

	private JLabel getJLabel_title(String s) {
		if (jLabel_title == null) {
			jLabel_title = new JLabel();
			jLabel_title.setText(s);
		}
		return(jLabel_title);
	}
	
	private JLabel getJLabel_ch(String s) {
		if (jLabel_ch == null) {
			jLabel_ch = new JLabel();
			jLabel_ch.setText(s);
		}
		return(jLabel_ch);
	}
	
	private JLabel getJLabel_aTime(String s) {
		if (jLabel_aTime == null) {
			jLabel_aTime = new JLabel();
			jLabel_aTime.setText(s);
		}
		return(jLabel_aTime);
	}
	
	private JLabel getJLabel_zTime(String s) {
		if (jLabel_zTime == null) {
			jLabel_zTime = new JLabel();
			jLabel_zTime.setText(s);
		}
		return(jLabel_zTime);
	}

	private JLabel getJLabel_recmin(String s) {
		if (jLabel_recmin == null) {
			jLabel_recmin = new JLabel();
			jLabel_recmin.setText(s);
			jLabel_recmin.setHorizontalAlignment(JLabel.RIGHT);
			jLabel_recmin.setForeground(Color.BLUE);
			
			jLabel_recmin.setBorder(new LineBorder(Color.LIGHT_GRAY));
		}
		return(jLabel_recmin);
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
	
	private JTXTLabel getJLabel_eventId(String s) {
		if ( jLabel_eventId == null ) {
			jLabel_eventId = new JTXTLabel();
			jLabel_eventId.setHorizontalAlignment(JLabel.CENTER);
			jLabel_eventId.setForeground(Color.BLUE);
			
			jLabel_eventId.setBorder(new LineBorder(Color.LIGHT_GRAY));
		}
		return jLabel_eventId;
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
			jComboBox_ch = new JWideComboBox();
			jComboBox_ch.addPopupWidth(150);
		}
		return jComboBox_ch;
	}
	
	// 開始時刻
	private JComboBox getJTextField_ahh() {
		if (jComboBox_ahh == null) {
			jComboBox_ahh = new JComboBoxWithPopup();
			jComboBox_ahh.addPopupWidth(HOUR_WIDTH);
			for ( int h=0; h<24; h++ ) {
				jComboBox_ahh.addItem(String.format("%02d", h));
			}
			
			jComboBox_ahh.addItemListener(il_timemod);
		}
		return jComboBox_ahh;
	}
	private JComboBox getJTextField_amm() {
		if (jComboBox_amm == null) {
			jComboBox_amm = new JComboBoxWithPopup();
			jComboBox_amm.addPopupWidth(HOUR_WIDTH);
			for ( int m=0; m<60; m++ ) {
				jComboBox_amm.addItem(String.format("%02d", m));
			}
			
			jComboBox_amm.addItemListener(il_timemod);
		}
		return jComboBox_amm;
	}
	private JLabel getJLabel_asep(String s)
	{
		if (jLabel_asep == null) {
			jLabel_asep = new JLabel(s,JLabel.CENTER);
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
	private JComboBox getJTextField_zhh() {
		if (jComboBox_zhh == null) {
			jComboBox_zhh = new JComboBoxWithPopup();
			jComboBox_zhh.addPopupWidth(HOUR_WIDTH);
			for ( int h=0; h<24; h++ ) {
				jComboBox_zhh.addItem(String.format("%02d", h));
			}
			
			jComboBox_zhh.addItemListener(il_timemod);
		}
		return jComboBox_zhh;
	}
	private JComboBox getJTextField_zmm() {
		if (jComboBox_zmm == null) {
			jComboBox_zmm = new JComboBoxWithPopup();
			jComboBox_zmm.addPopupWidth(HOUR_WIDTH);
			for ( int m=0; m<60; m++ ) {
				jComboBox_zmm.addItem(String.format("%02d", m));
			}
			
			jComboBox_zmm.addItemListener(il_timemod);
		}
		return jComboBox_zmm;
	}
	private JLabel getJLabel_zsep(String s)
	{
		if (jLabel_zsep == null) {
			jLabel_zsep = new JLabel(s,JLabel.CENTER);
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
	
}
