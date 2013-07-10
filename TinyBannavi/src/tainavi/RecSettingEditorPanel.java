package tainavi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.LineBorder;

import tainavi.TVProgram.ProgGenre;
import tainavi.TVProgram.ProgSubgenre;


/**
 * 予約ダイアログを目的ごとに３ブロックにわけたうちの「録画設定」部分のコンポーネント
 * @since 3.22.2β
 */
public class RecSettingEditorPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	public void setDebug(boolean b) { debug = b; }
	private static boolean debug = false;

	/*******************************************************************************
	 * 定数
	 ******************************************************************************/
	
	private static final String ITEM_YES = "する";
	private static final String ITEM_NO = "しない";
	
	private static final int PARTS_HEIGHT = 25;
	private static final int SEP_WIDTH = 10;
	private static final int SEP_WIDTH_NARROW = 5;
	private static final int SEP_HEIGHT = 10;
	private static final int SEP_HEIGHT_NALLOW = 5;
	
	private static final int LABEL_WIDTH = 150;
	private static final int BUTTON_WIDTH = 75;
	
	private static final int COMBO_WIDTH = 115;
	private static final int COMBO_WIDTH_WIDE = 155;
	private static final int COMBO_HEIGHT = 43;

	private static final int RECORDER_WIDTH = COMBO_WIDTH_WIDE*2+SEP_WIDTH_NARROW;
	private static final int ENCODER_WIDTH = COMBO_WIDTH*2+SEP_WIDTH_NARROW;

	private static final String TEXT_SAVEDEFAULT = "<HTML>録画設定を開いた時の枠内のデフォルト値として<BR>現在の値を使用するようにします。<BR><FONT COLOR=#FF0000>※ジャンル別ＡＶ設定があればそちらが優先されます。</FONT></HTML>";

	// ログ関連
	
	private static final String MSGID = "[録画設定編集] ";
	private static final String ERRID = "[ERROR]"+MSGID;
	private static final String DBGID = "[DEBUG]"+MSGID;
	
	/*******************************************************************************
	 * 部品
	 ******************************************************************************/
	
	private JComboBoxPanel jCBXPanel_recorder = null;
	private JComboBoxPanel jCBXPanel_encoder = null;
	private JLabel jLabel_encoderemptywarn = null;
	
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
	private JComboBoxPanel jCBXPanel_xChapter = null;
	private JComboBoxPanel jCBXPanel_msChapter = null;
	private JComboBoxPanel jCBXPanel_mvChapter = null;
	
	private JLabel jLabel_rectype = null;
	private JButton jButton_load = null;
	private JButton jButton_save = null;
	private JButton jButton_savedefault = null;
	
	private JCheckBoxPanel jCheckBox_Exec = null;

	//
	private RecSettingSelectable recsetsel = null;
	
	
	/*******************************************************************************
	 * コンストラクタ
	 ******************************************************************************/
	
	public RecSettingEditorPanel() {

		super();
		setBorder(new LineBorder(Color.BLACK, 1));
		
		addComponents();
		
		// 外部要因に左右されないアイテム群の設定
		setGenreItems();
		
		// 付けたり外したりしないリスナー
		jCBXPanel_genre.addItemListener(f_il_resetSubgenreItems);
		
		jButton_load.addActionListener(f_al_loadAction);
		jButton_save.addActionListener(f_al_saveAction);
		jButton_savedefault.addActionListener(f_al_saveDefaultAction);
		jCBXPanel_audiorate.addItemListener(f_il_arateChanged);
		
		// 付けたり外したりするリスナー
		setEnabledListenerAll(true);
	}
	
	private void addComponents() {
		
		setLayout(new SpringLayout());
		
		int y = 0;
		int x = SEP_WIDTH_NARROW;
		
		CommonSwingUtils.putComponentOn(this, jCBXPanel_recorder = new JComboBoxPanel("",RECORDER_WIDTH,RECORDER_WIDTH),	RECORDER_WIDTH+5,	COMBO_HEIGHT+SEP_HEIGHT, x, y);
		CommonSwingUtils.putComponentOn(this, jCBXPanel_encoder = new JComboBoxPanel("",ENCODER_WIDTH,ENCODER_WIDTH),		ENCODER_WIDTH+5,	COMBO_HEIGHT+SEP_HEIGHT, x+=RECORDER_WIDTH+5+SEP_WIDTH, y);
		CommonSwingUtils.putComponentOn(this, jLabel_encoderemptywarn = new JLabel(""), LABEL_WIDTH, PARTS_HEIGHT, x+=ENCODER_WIDTH+5+SEP_WIDTH+5, y+PARTS_HEIGHT);
		
		jCBXPanel_recorder.getJComboBox().setForeground(Color.BLUE);
		jCBXPanel_encoder.getJComboBox().setForeground(Color.BLUE);
		
		// ポップアップした時に追加される幅
		jCBXPanel_recorder.addPopupWidth(100);
		jCBXPanel_encoder.addPopupWidth(100);

		y += (COMBO_HEIGHT+SEP_HEIGHT)+SEP_HEIGHT;
		x = SEP_WIDTH_NARROW;
		CommonSwingUtils.putComponentOn(this, jCBXPanel_genre = new JComboBoxPanel("",110,150),		COMBO_WIDTH_WIDE,	COMBO_HEIGHT, x, y);
		CommonSwingUtils.putComponentOn(this, jCBXPanel_subgenre = new JComboBoxPanel("",110,150),	COMBO_WIDTH_WIDE,	COMBO_HEIGHT, x+=(COMBO_WIDTH_WIDE+SEP_WIDTH), y);
		CommonSwingUtils.putComponentOn(this, jCBXPanel_autodel = new JComboBoxPanel("",110,110),	COMBO_WIDTH,		COMBO_HEIGHT, x+=(COMBO_WIDTH+SEP_WIDTH)*2, y);
		CommonSwingUtils.putComponentOn(this, jCBXPanel_xChapter = new JComboBoxPanel("",110,110),	COMBO_WIDTH,		COMBO_HEIGHT, x+=(COMBO_WIDTH+SEP_WIDTH), y);
		
		y += COMBO_HEIGHT;
		x = SEP_WIDTH_NARROW;
		CommonSwingUtils.putComponentOn(this, jCBXPanel_videorate = new JComboBoxPanel("",110,150),	COMBO_WIDTH_WIDE,	COMBO_HEIGHT, x, y);
		CommonSwingUtils.putComponentOn(this, jCBXPanel_audiorate = new JComboBoxPanel("",110,110),	COMBO_WIDTH,		COMBO_HEIGHT, x+=(COMBO_WIDTH_WIDE+SEP_WIDTH), y);
		CommonSwingUtils.putComponentOn(this, jCBXPanel_bvperf = new JComboBoxPanel("",110,110),	COMBO_WIDTH,		COMBO_HEIGHT, x+=(COMBO_WIDTH+SEP_WIDTH), y);
		CommonSwingUtils.putComponentOn(this, jCBXPanel_lvoice = new JComboBoxPanel("",110,110),	COMBO_WIDTH,		COMBO_HEIGHT, x+=(COMBO_WIDTH+SEP_WIDTH), y);
		CommonSwingUtils.putComponentOn(this, jCBXPanel_msChapter = new JComboBoxPanel("",110,110),	COMBO_WIDTH,		COMBO_HEIGHT, x+=(COMBO_WIDTH+SEP_WIDTH), y);
		
		y += COMBO_HEIGHT;
		x = SEP_WIDTH_NARROW;
		CommonSwingUtils.putComponentOn(this, jCBXPanel_folder = new JComboBoxPanel("",100,150),	COMBO_WIDTH_WIDE,	COMBO_HEIGHT, x, y);
		CommonSwingUtils.putComponentOn(this, jCBXPanel_device = new JComboBoxPanel("",110,110),	COMBO_WIDTH,		COMBO_HEIGHT, x+=(COMBO_WIDTH_WIDE+SEP_WIDTH), y);
		CommonSwingUtils.putComponentOn(this, jCBXPanel_dvdcompat = new JComboBoxPanel("",110,110),	COMBO_WIDTH,		COMBO_HEIGHT, x+=(COMBO_WIDTH+SEP_WIDTH), y);
		CommonSwingUtils.putComponentOn(this, jCBXPanel_aspect = new JComboBoxPanel("",110,110),	COMBO_WIDTH,		COMBO_HEIGHT, x+=(COMBO_WIDTH+SEP_WIDTH), y);
		CommonSwingUtils.putComponentOn(this, jCBXPanel_mvChapter = new JComboBoxPanel("",110,110),	COMBO_WIDTH,		COMBO_HEIGHT, x+=(COMBO_WIDTH+SEP_WIDTH), y);

		y += COMBO_HEIGHT;
		CommonSwingUtils.putComponentOn(this, jCBXPanel_pursues = new JComboBoxPanel("",110,110), 	COMBO_WIDTH, 		COMBO_HEIGHT, x, y);
		
		y += COMBO_HEIGHT;
		
		// 特殊配置
		{
			int spy = SEP_HEIGHT_NALLOW+(PARTS_HEIGHT+SEP_HEIGHT_NALLOW)*2;
			x = SEP_WIDTH_NARROW+(COMBO_WIDTH_WIDE+SEP_WIDTH)+(COMBO_WIDTH+SEP_WIDTH)*4+SEP_WIDTH;
			
			CommonSwingUtils.putComponentOn(this, jLabel_rectype = new JLabel("ジャンル別の"), LABEL_WIDTH, PARTS_HEIGHT, x, spy);
			
			spy+=PARTS_HEIGHT-5;
			
			CommonSwingUtils.putComponentOn(this, new JLabel("録画設定の選択"),	LABEL_WIDTH,  PARTS_HEIGHT, x, spy);
			CommonSwingUtils.putComponentOn(this, jButton_load = new JButton("開く"),				BUTTON_WIDTH, PARTS_HEIGHT, x+SEP_WIDTH_NARROW, spy+=PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(this, jButton_save = new JButton("保存"),			BUTTON_WIDTH, PARTS_HEIGHT, x+SEP_WIDTH_NARROW, spy+=PARTS_HEIGHT);
			CommonSwingUtils.putComponentOn(this, jButton_savedefault = new JButton("既定化"),	BUTTON_WIDTH, PARTS_HEIGHT, x+SEP_WIDTH_NARROW, spy+=(PARTS_HEIGHT+SEP_HEIGHT));
			
			jButton_savedefault.setToolTipText(TEXT_SAVEDEFAULT);
			
			CommonSwingUtils.putComponentOn(this, jCheckBox_Exec = new JCheckBoxPanel("予約実行",75,true), 75, PARTS_HEIGHT, x+SEP_WIDTH_NARROW, spy+=(PARTS_HEIGHT+SEP_HEIGHT));
			setExecValue(true);
		}
		
		x+=BUTTON_WIDTH+SEP_WIDTH_NARROW*2;
		
		Dimension d = new Dimension(x,y);
		setPreferredSize(d);
	}
	
	public void setRecSettingSelector(RecSettingSelectable o) {
		recsetsel = o;
	}
	
	
	/*******************************************************************************
	 * アイテムの設定
	 ******************************************************************************/
	
	/***************************************
	 * 項目ラベルの設定
	 **************************************/
	
	/**
	 * レコーダが選択されたら各コンポーネントラベルを設定する
	 * @param recorder
	 */
	public void setLabels(HDDRecorder recorder) {
		
		// 固定ラベル
		setLabel(jCBXPanel_recorder,	null,							"レコーダ");
		setLabel(jCBXPanel_encoder,		null,							"エンコーダ");
		setLabel(jCBXPanel_genre,		null,							"ジャンル");
		setLabel(jCBXPanel_subgenre,	null,							"サブジャンル");
		setLabel(jCBXPanel_pursues,		null,							"番組追従");
		
		// 可変ラベル
		setLabel(jCBXPanel_videorate,	recorder.getLabel_Videorate(),	"画質");
		setLabel(jCBXPanel_audiorate,	recorder.getLabel_Audiorate(),	"音質");
		
		setLabel(jCBXPanel_folder,		recorder.getLabel_Folder(),		"記録先フォルダ");
		setLabel(jCBXPanel_device,		recorder.getLabel_Device(),		"記録先デバイス");
		
 		setLabel(jCBXPanel_bvperf,		recorder.getLabel_BVperf(),		"録画優先度");		// "高ﾚｰﾄ節約"？
		setLabel(jCBXPanel_dvdcompat,	recorder.getLabel_DVDCompat(),	"BD/DVD互換モード");
		
		setLabel(jCBXPanel_autodel,		recorder.getLabel_Autodel(),	"自動削除");
		setLabel(jCBXPanel_lvoice,		recorder.getLabel_LVoice(),		"ﾗｲﾝ音声選択");
		setLabel(jCBXPanel_aspect,		recorder.getLabel_Aspect(),		"録画のりしろ");		// "DVD記録時画面比"？
		
		setLabel(jCBXPanel_msChapter,	recorder.getLabel_MsChapter(),	"ﾏｼﾞｯｸﾁｬﾌﾟﾀ(ｼｰﾝ)");	// "DVD/ｼｰﾝﾁｬﾌﾟﾀ分割"？
		setLabel(jCBXPanel_mvChapter,	recorder.getLabel_MvChapter(),	"ﾏｼﾞｯｸﾁｬﾌﾟﾀ(本編)");	// "音多/本編ﾁｬﾌﾟﾀ分割"？
		setLabel(jCBXPanel_xChapter,	recorder.getLabel_XChapter(),	"無音部分ﾁｬﾌﾟﾀ分割");
	}

	/**
	 * ジャンル別AV設定か、CH別AV設定かを選ぶ
	 */
	public void setAVCHSetting(boolean enabled) {
		jLabel_rectype.setText(enabled ? "放送局別の" : "ジャンル別の");
	}
	
	/***************************************
	 * 固定アイテムの設定
	 **************************************/
	
	/**
	 * 固定のアイテムを設定する
	 * @see #setFlexItems(HDDRecorder, String)
	 */
	public void setFixedItems(HDDRecorderList recorders) {
		
		setEnabledListenerAll(false);	// リスナー停止
		
		setRecorderItems(recorders);
		
		setEnabledListenerAll(true);	// リスナー再開
	}
	
	/**
	 * レコーダアイテムを設定する
	 * @see #setFixedItems(HDDRecorderList)
	 */
	private void setRecorderItems(HDDRecorderList recorders) {
		String selected = (String) jCBXPanel_recorder.getSelectedItem();
		ArrayList<String> items = new ArrayList<String>();
		for ( HDDRecorder rec : recorders ) {
			if ( rec.isBackgroundOnly() ) {
				continue;	// Googleカレンダープラグインとかははずす
			}
			items.add(rec.Myself());
		}
		
		setComboItems(jCBXPanel_recorder, items);
		
		if ( selected != null ) {
			// 入れ替え後に元に戻してみる
			jCBXPanel_recorder.setSelectedItem(selected);
		}
	}
	
	/**
	 * ジャンルアイテムを設定する
	 * @see #setFixedItems(HDDRecorderList)
	 */
	private void setGenreItems() {
		ArrayList<String> items = new ArrayList<String>();
		for ( ProgGenre g : ProgGenre.values() ) {
			items.add(g.toString());
		}
		
		setComboItems(jCBXPanel_genre, items);
	}
	
	
	/***************************************
	 * 可変アイテムの設定
	 **************************************/
	
	/**
	 * レコーダが選択されたらそれにあわせて各コンポーネントアイテムを設定する
	 */
	public void setFlexItems(HDDRecorder recorder, String webChName) {
		
		setEnabledListenerAll(false);	// リスナー停止 
		
		// エンコーダ
		setComboItems(jCBXPanel_encoder, recorder.getFilteredEncoders(webChName));
		
		// 設定値
		setComboItems(jCBXPanel_videorate, recorder.getVideoRateList());
		setComboItems(jCBXPanel_audiorate, recorder.getAudioRateList());
		setComboItems(jCBXPanel_folder, recorder.getFolderList());
		
		setComboItems(jCBXPanel_device, recorder.getDeviceList());
		setComboItems(jCBXPanel_bvperf, recorder.getBVperf());
		setComboItems(jCBXPanel_dvdcompat, recorder.getDVDCompatList());
		
		setComboItems(jCBXPanel_autodel, recorder.getAutodel());
		setComboItems(jCBXPanel_lvoice, recorder.getLVoice());
		setComboItems(jCBXPanel_aspect, recorder.getAspect());
		
		setComboItems(jCBXPanel_msChapter, recorder.getMsChapter());
		setComboItems(jCBXPanel_mvChapter, recorder.getMvChapter());
		setComboItems(jCBXPanel_xChapter, recorder.getXChapter());

		setComboItems(jCBXPanel_pursues, null);
		
		setEnabledListenerAll(true);	// リスナー再開
	}
	
	/**
	 * ジャンルが選択されたらそれにあわせてサブジャンルアイテムを設定する
	 * @see #setGenreItems()
	 */
	private void setSubgenreItems(ProgGenre genre) {
		ArrayList<String> items = new ArrayList<String>();
		for ( ProgSubgenre sg : ProgSubgenre.values(genre) ) {
			items.add(sg.toString());
		}
		
		setComboItems(jCBXPanel_subgenre, items);
	}
	
	/**
	 * 優先的に使用するチューナーを前に持ってくる
	 */
	public void sortEncoderItems(ArrayList<TextValueSet> preferred) {
		
		ArrayList<String> tmpList = new ArrayList<String>(); 
		for ( int i=0; i<jCBXPanel_encoder.getItemCount(); i++ ) {
			tmpList.add((String) jCBXPanel_encoder.getItemAt(i));
		}
		
		ArrayList<String> items = new ArrayList<String>();
		for ( String enc : tmpList ) {
			for ( TextValueSet tv : preferred ) {
				if ( tv.getText().equals(enc) ) {
					// 見つかったからついかー
					items.add(enc);
					break;
				}
			}
		}
		for ( String enc : items ) {
			tmpList.remove(enc);
		}
		
		for ( String enc : tmpList ) {
			items.add(enc);
		}
		
		setComboItems(jCBXPanel_encoder, items);
	}
	
	/*******************************************************************************
	 * 共通部品的な
	 ******************************************************************************/

	/**
	 * コンボボックスのアイテム登録を行う
	 */
	private <T> int setComboItems(JComboBoxPanel combo, ArrayList<T> items) {
		
		combo.removeAllItems();
		
		if ( items == null ) {
			// ここにくるのは番組追従のみかな？
			combo.addItem(ITEM_YES);
			combo.addItem(ITEM_NO);
			combo.setEnabled(true);
			return combo.getItemCount();
		}

		if ( items.size() == 0 ) {
			combo.setEnabled(false);
			return 0;
		}
	
		// うひー
		for ( T enc : items ) {
			if ( enc.getClass() == TextValueSet.class ) {
				TextValueSet t = (TextValueSet) enc;
				combo.addItem(t.getText());
				
				if (t.getDefval()) combo.setSelectedIndex(combo.getItemCount()-1);	// デフォルト値があるならば
			}
			else if ( enc.getClass() == String.class ) {
				// レコーダ・エンコーダのみかな？
				combo.addItem((String) enc);
			}
		}
		combo.setEnabled(combo.getItemCount() > 1);
		return combo.getItemCount();
	}

	private void setLabel(JComboBoxPanel combo, String overrideLabel, String defaultLabel) {
		combo.setText((overrideLabel!=null)?overrideLabel:defaultLabel);
	}
	
	/*******************************************************************************
	 * 外部とのやり取り（設定反映系）
	 ******************************************************************************/

	/***************************************
	 * 設定の一括反映３種
	 **************************************/
	
	/**
	 * 番組情報によるアイテム選択
	 */
	public void setSelectedValues(ProgDetailList tvd, ReserveList r) {
		
		setEnabledListenerAll(false);
		
		// サブジャンルアイテム群はジャンル決定後に埋まる
		setSelectedGenreValues((tvd.genre!=null?tvd.genre.toString():null), (tvd.subgenre!=null?tvd.subgenre.toString():null));
		
		// チューナー
		setSelectedEncoderValue(r.getTuner());	// encがnullかどうかはメソッドの中で確認するよ
		
		// 番組追従（これは予約種別[arate]より先に設定しておかないといけない）
		setSelectedValue(jCBXPanel_pursues, r.getPursues() ? ITEM_YES : ITEM_NO);
		
		// 画質・音質
		setSelectedValue(jCBXPanel_videorate, r.getRec_mode());
		setSelectedValue(jCBXPanel_audiorate, r.getRec_audio());
		setSelectedValue(jCBXPanel_folder, r.getRec_folder());
		// サブジャンルは番組情報から
		setSelectedValue(jCBXPanel_dvdcompat, r.getRec_dvdcompat());
		setSelectedValue(jCBXPanel_device, r.getRec_device());
		
		// 自動チャプタ関連
		setSelectedValue(jCBXPanel_xChapter, r.getRec_xchapter());
		setSelectedValue(jCBXPanel_msChapter, r.getRec_mschapter());
		setSelectedValue(jCBXPanel_mvChapter, r.getRec_mvchapter());
		
		// その他
		setSelectedValue(jCBXPanel_aspect, r.getRec_aspect());
		setSelectedValue(jCBXPanel_bvperf, r.getRec_bvperf());
		setSelectedValue(jCBXPanel_lvoice, r.getRec_lvoice());
		setSelectedValue(jCBXPanel_autodel, r.getRec_autodel());
		
		// 実行ON・OFF
		setExecValue(r.getExec());
		
		setEnabledListenerAll(true);
	}
	
	/**
	 * 類似予約情報によるアイテム選択
	 */
	public void setSelectedValues(ReserveList r) {

		setEnabledListenerAll(false);

		// サブジャンルアイテム群はジャンル決定後に埋まる
		setSelectedGenreValues(r.getRec_genre(), r.getRec_genre());

		// チューナー
		setSelectedValue(jCBXPanel_encoder, r.getTuner());
		
		// 番組追従（これは予約種別[arate]より先に設定しておかないといけない）
		setSelectedValue(jCBXPanel_pursues, r.getPursues() ? ITEM_YES : ITEM_NO);
		
		// 画質・音質
		setSelectedValue(jCBXPanel_videorate, r.getRec_mode());
		setSelectedValue(jCBXPanel_audiorate, r.getRec_audio());
		setSelectedValue(jCBXPanel_folder, r.getRec_folder());
		setSelectedValue(jCBXPanel_subgenre, r.getRec_subgenre());
		setSelectedValue(jCBXPanel_dvdcompat, r.getRec_dvdcompat());
		setSelectedValue(jCBXPanel_device, r.getRec_device());
		
		// 自動チャプタ関連
		setSelectedValue(jCBXPanel_xChapter, r.getRec_xchapter());
		setSelectedValue(jCBXPanel_msChapter, r.getRec_mschapter());
		setSelectedValue(jCBXPanel_mvChapter, r.getRec_mvchapter());
		
		// その他
		setSelectedValue(jCBXPanel_aspect, r.getRec_aspect());
		setSelectedValue(jCBXPanel_bvperf, r.getRec_bvperf());
		setSelectedValue(jCBXPanel_lvoice, r.getRec_lvoice());
		setSelectedValue(jCBXPanel_autodel, r.getRec_autodel());
		
		// 実行ON・OFF
		setExecValue(r.getExec());
		
		setEnabledListenerAll(true);
	}
	
	/**
	 * ジャンル別ＡＶ設定によるアイテム選択
	 */
	public void setSelectedValues(AVs avs) {
		
		setEnabledListenerAll(false);
		
		// 画質・音質
		setSelectedValue(jCBXPanel_videorate, avs.getVideorate());
		setSelectedValue(jCBXPanel_audiorate, avs.getAudiorate());
		setSelectedValue(jCBXPanel_folder, avs.getFolder());
		// サブジャンルは確定済み
		setSelectedValue(jCBXPanel_dvdcompat, avs.getDVDCompat());
		setSelectedValue(jCBXPanel_device, avs.getDevice());
		
		// 自動チャプタ関連
		setSelectedValue(jCBXPanel_xChapter, avs.getXChapter());
		setSelectedValue(jCBXPanel_msChapter, avs.getMsChapter());
		setSelectedValue(jCBXPanel_mvChapter, avs.getMvChapter());
		
		// その他
		setSelectedValue(jCBXPanel_aspect, avs.getAspect());
		setSelectedValue(jCBXPanel_bvperf, avs.getBvperf());
		setSelectedValue(jCBXPanel_lvoice, avs.getLvoice());
		setSelectedValue(jCBXPanel_autodel, avs.getAutodel());
		
		setEnabledListenerAll(true);
	}
	
	/***************************************
	 * 設定の部分反映各種 
	 **************************************/
	
	/**
	 * 実行ON・OFFの強制設定
	 */
	public void setExecValue(boolean b) {
		jCheckBox_Exec.setSelected(b);
		jCheckBox_Exec.setForeground(b ? Color.BLACK : Color.RED);
	}
	
	/**
	 * レコーダのアイテム選択（中から呼んじゃだめだよ）
	 * <P>これを呼び出した場合、レコーダ選択イベントは起きないので選択結果は呼び出し元で保存しておく必要がある
	 * <P>これは他のコンポーネントがトリガーとなって起きるイベントから呼び出されるのでリスナーは動かしておかないといけない 
	 */
	public String setSelectedRecorderValue(String myself) {
		return setSelectedValue(jCBXPanel_recorder, myself);
	}
	
	/**
	 * エンコーダのアイテム選択
	 */
	public String setSelectedEncoderValue(String enc) {
		
		if ( enc == null ) {
			jCBXPanel_encoder.setSelectedIndex(0);
			jLabel_encoderemptywarn.setText("空きｴﾝｺｰﾀﾞ検索無効");
			jLabel_encoderemptywarn.setForeground(Color.CYAN);
		}
		else if ( enc.length() == 0 ) {
			jCBXPanel_encoder.setSelectedIndex(0);
			jLabel_encoderemptywarn.setText("空きｴﾝｺｰﾀﾞ不足");
			jLabel_encoderemptywarn.setForeground(Color.RED);
		}
		else {
			jCBXPanel_encoder.setSelectedItem(enc);
			jLabel_encoderemptywarn.setText("空きｴﾝｺｰﾀﾞあり");
			jLabel_encoderemptywarn.setForeground(Color.BLUE);
		}
		
		return (String) jCBXPanel_encoder.getSelectedItem();
	}
	
	/**
	 * Vrateのアイテム選択（中から呼んじゃだめだよ）
	 */
	public String setSelectedVrateValue(String vrate) {
		
		if ( vrate == null ) {
			return null;
		}
		
		return setSelectedValue(jCBXPanel_videorate, vrate);
	}
	
	/**
	 * ジャンルアイテムの選択
	 */
	private String setSelectedGenreValues(String genre, String subgenre) {
		
		if ( genre == null || genre.length() == 0 ) {
			genre = ProgGenre.NOGENRE.toString();
		}
		jCBXPanel_genre.setSelectedItem(genre);
		
		if ( subgenre == null || subgenre.length() == 0 ) {
			jCBXPanel_subgenre.setSelectedIndex(0);
		}
		else {
			jCBXPanel_subgenre.setSelectedItem(subgenre);
		}
		
		return (String) jCBXPanel_genre.getSelectedItem();
	}
	
	/**
	 * アイテム選択
	 */
	private String setSelectedValue(JComboBoxPanel comp, String value) {
		
		if ( value != null ) {
			comp.setSelectedItem(value);
		}
		else if ( comp.getItemCount() > 0 ){
			comp.setSelectedIndex(0);
		}
		
		return (String) comp.getSelectedItem();
	}
	
	/*******************************************************************************
	 * 外部とのやり取り（設定取得系）
	 ******************************************************************************/
	
	/***************************************
	 * 取得系
	 **************************************/
	
	/**
	 * 選択値を予約情報に代入する
	 */
	public ReserveList getSelectedValues(ReserveList r) {
		
		r.setTuner((String) jCBXPanel_encoder.getSelectedItem());
		
		r.setRec_mode((String) jCBXPanel_videorate.getSelectedItem());
		r.setRec_audio((String) jCBXPanel_audiorate.getSelectedItem());
		r.setRec_folder((String) jCBXPanel_folder.getSelectedItem());
		r.setRec_genre((String) jCBXPanel_genre.getSelectedItem());
		r.setRec_subgenre((String) jCBXPanel_subgenre.getSelectedItem());
		r.setRec_dvdcompat((String) jCBXPanel_dvdcompat.getSelectedItem());
		r.setRec_device((String) jCBXPanel_device.getSelectedItem());
		
		// 自動チャプタ関連
		r.setRec_xchapter((String) jCBXPanel_xChapter.getSelectedItem());
		r.setRec_mschapter((String) jCBXPanel_msChapter.getSelectedItem());
		r.setRec_mvchapter((String) jCBXPanel_mvChapter.getSelectedItem());
		
		// その他
		r.setRec_aspect((String) jCBXPanel_aspect.getSelectedItem());
		r.setRec_bvperf((String) jCBXPanel_bvperf.getSelectedItem());
		r.setRec_lvoice((String) jCBXPanel_lvoice.getSelectedItem());
		r.setRec_autodel((String) jCBXPanel_autodel.getSelectedItem());
		
		// 番組追従
		r.setPursues(ITEM_YES == jCBXPanel_pursues.getSelectedItem());
		
		// 実行ON・OFF
		r.setExec(jCheckBox_Exec.isSelected());
		
		return r;
	}
	
	public ReserveList getSelectedValues() {
		return getSelectedValues(new ReserveList());
	}
	
	/**
	 * 選択値をジャンル別録画設定情報に代入する
	 */
	public AVs getSelectedSetting(AVs c) {

		c.setGenre((String) jCBXPanel_genre.getSelectedItem());
		
		c.setVideorate((String) jCBXPanel_videorate.getSelectedItem());
		c.setAudiorate((String) jCBXPanel_audiorate.getSelectedItem());
		c.setFolder((String) jCBXPanel_folder.getSelectedItem());
		c.setDVDCompat((String) jCBXPanel_dvdcompat.getSelectedItem());
		c.setDevice((String) jCBXPanel_device.getSelectedItem());
		
		c.setXChapter((String) jCBXPanel_xChapter.getSelectedItem());
		c.setMsChapter((String) jCBXPanel_msChapter.getSelectedItem());
		c.setMvChapter((String) jCBXPanel_mvChapter.getSelectedItem());
		
		c.setAspect((String) jCBXPanel_aspect.getSelectedItem());
		c.setBvperf((String) jCBXPanel_bvperf.getSelectedItem());
		c.setLvoice((String) jCBXPanel_lvoice.getSelectedItem());
		c.setAutodel((String) jCBXPanel_autodel.getSelectedItem());
		
		c.setPursues(ITEM_YES == jCBXPanel_pursues.getSelectedItem());
		
		return c;
	}

	public AVs getSelectedSetting() {
		return getSelectedSetting(new AVs());
	}

	
	/*******************************************************************************
	 * リスナー
	 ******************************************************************************/
	
	/***************************************
	 * 永続的なリスナー
	 **************************************/
	
	private final ItemListener f_il_resetSubgenreItems = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			// サブジャンルのアイテムをリセットする
			setSubgenreItems(ProgGenre.get((String) jCBXPanel_genre.getSelectedItem()));
		}
	};
	
	/**
	 * ジャンル別ＡＶ設定のロード
	 */
	private final ActionListener f_al_loadAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if ( recsetsel != null ) {
				setEnabledListenerAll(false);
				
				recsetsel.doSetAVSettings();

				setEnabledListenerAll(true);
			}
		}
	};
	
	/**
	 * ジャンル別ＡＶ設定のセーブ
	 */
	private final ActionListener f_al_saveAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if ( recsetsel != null ) recsetsel.doSaveAVSettings(false);
		}
	};
	
	/**
	 * 既定ＡＶ設定のセーブ
	 */
	private final ActionListener f_al_saveDefaultAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if ( recsetsel != null ) recsetsel.doSaveAVSettings(true);
		}
	};
	
	/**
	 * EPG予約以外では番組追従が設定できないようにしたいな
	 */
	private final ItemListener f_il_arateChanged = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if ( e.getStateChange() != ItemEvent.SELECTED ) {
				return;
			}

			String pgtype = (String) jCBXPanel_audiorate.getSelectedItem();
			if ( pgtype == HDDRecorder.ITEM_REC_TYPE_PROG ) {
				// "ﾌﾟﾗｸﾞﾗﾑ予約"なら触る必要なし
				jCBXPanel_pursues.setSelectedItem(ITEM_NO);
				jCBXPanel_pursues.setEnabled(false);
			}
			else {
				jCBXPanel_pursues.setEnabled(true);
			}
		}
	};
	

	/***************************************
	 * つけたり外したりするリスナーをつけたり外したりするメソッド
	 **************************************/
	
	/**
	 * イベントトリガーでアイテムを操作する際に、さらにイベントをキックされてはたまらないのでリスナーを付けたり外したりする
	 */
	private void setEnabledListenerAll(boolean enabled) {

		// あとで削除
		if ( debug ) {
			if ( enabled ) {
				Object o = null;
				for ( ItemListener il : jCBXPanel_recorder.getJComboBox().getItemListeners() ) {
					if ( il == il_recorderChanged ) {
						System.out.println(DBGID+"****** リスナーの重複登録 ******");
						//CommonUtils.printStackTrace();
						o = il;
						break;
					}
				}
				if ( o == null ) {
					System.out.println("+++ リスナーの登録 +++");
					//CommonUtils.printStackTrace();
				}
			}
			else {
				System.out.println("... リスナーの削除 ...");
			}
		}
		
		setEnabledItemListener(jCBXPanel_recorder, il_recorderChanged, enabled);
		setEnabledItemListener(jCBXPanel_encoder, il_encoderChanged, enabled);
		setEnabledItemListener(jCBXPanel_videorate, il_vrateChanged, enabled);
		setEnabledItemListener(jCBXPanel_genre, il_genreChanged, enabled);
	}
	private void setEnabledItemListener(ItemSelectable comp, ItemListener il, boolean b) {
		comp.removeItemListener(il);
		if ( b ) {
			comp.addItemListener(il);
		}
	}
	
	/***************************************
	 * つけたり外したりするリスナー群 
	 **************************************/
	
	/**
	 *  レコーダーが選択された
	 */
	private final ItemListener il_recorderChanged = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if ( e.getStateChange() != ItemEvent.SELECTED ) {
				return;
			}
			
			if ( recsetsel != null ) {
				setEnabledListenerAll(false);	// 停止
				
				recsetsel.doSelectRecorder((String) jCBXPanel_recorder.getSelectedItem());
				
				setEnabledListenerAll(true);	// 再開
			}
		}
	};
	
	/**
	 *  エンコーダが選択された（ので利用可能な画質を選びなおす）
	 */
	private final ItemListener il_encoderChanged = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if ( e.getStateChange() != ItemEvent.SELECTED ) {
				return;
			}

			if ( recsetsel != null ) {
				setEnabledListenerAll(false);	// 停止
				
				recsetsel.doSelectEncoder((String) jCBXPanel_encoder.getSelectedItem());
				
				setEnabledListenerAll(true);	// 再開
			}
		}
	};
	

	/**
	 *  画質が選択された（ので利用可能なエンコーダを選びなおす）
	 */
	private final ItemListener il_vrateChanged = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if ( e.getStateChange() != ItemEvent.SELECTED ) {
				return;
			}

			if ( recsetsel != null ) {
				setEnabledListenerAll(false);	// 停止
				
				recsetsel.doSelectVrate((String) jCBXPanel_videorate.getSelectedItem());
				
				setEnabledListenerAll(true);	// 再開
			}
		}
	};
	

	/**
	 * ジャンルが選択された
	 */
	private final ItemListener il_genreChanged = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() != ItemEvent.SELECTED) {
				return;
			}
			
			// サブジャンルの選択肢を入れ替える
			setSubgenreItems(ProgGenre.get((String) jCBXPanel_genre.getSelectedItem()));
			
			// ＡＶ設定変更してーん
			if ( recsetsel != null ) recsetsel.doSetAVSettings();
		}
	};

	/*******************************************************************************
	 * コンポーネント
	 ******************************************************************************/

	// あれ？getJXX()が一個もないよ！
	
}
