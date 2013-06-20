package tainavi;

import java.util.ArrayList;

import taiSync.ReserveInfo;
import tainavi.TVProgram.ProgOption;
import tainavi.TVProgram.ProgSubgenre;


public class AutoReserveInfo implements Cloneable {
	
	/*******************************************************************************
	 * コンストラクタ
	 ******************************************************************************/

	// デフォルトコンストラクタ
	public AutoReserveInfo() {
		super();
		
		this.timeslots = new ArrayList<String>(TIMESLOTSIZE);
	}
	
	
	/*******************************************************************************
	 * clone(ディープコピー)
	 ******************************************************************************/
	
	@Override
	public AutoReserveInfo clone() {
		try {
			AutoReserveInfo p = (AutoReserveInfo) super.clone();
			
			p.chNames = new ArrayList<String>();
			for ( String ch : chNames ) {
				p.chNames.add(ch);
			}
			
			p.subgenres = new ArrayList<ProgSubgenre>();
			for ( ProgSubgenre sg : subgenres ) {
				p.subgenres.add(sg);
			}
			
			return p;
		}
		catch ( Exception e ) {
			throw new InternalError(e.toString());
		}
	}

	
	/*******************************************************************************
	 * 定数
	 ******************************************************************************/
	
	public static final int TIMESLOTSIZE = 7;
	
	
	/*******************************************************************************
	 * メンバー変数
	 ******************************************************************************/

	/*
	 *  HIDDEN PARAMS
	 */
	
	private String id;						// ID　※レコーダが一意に割り当てるID
	
	/*
	 *  SHOWN PARAMS
	 */
	
	private String label;					// 一覧表示用
	
	private String keyword;					// 絞り込みキーワード
	private String exKeyword;				// 追加キーワード　※[E]排他キーワード、[T]詳細キーワード
	
	private boolean regularExpression;		// キーワードは正規表現
	private boolean fazzySearch;			// あいまい検索する
	private boolean titleOnly;				// 検索対象はタイトルのみ[E]

	private boolean uniqTimeslot;			// 全曜日に同じ時間範囲を利用する ※[E]選択可、[T]true強制
	private ArrayList<String> timeslots;	// 時間範囲（７日分）　※使用しない曜日にはnullを設定する
	
	private boolean recordedCheck;			// 録画済み無効[E]
	private int recordedCheckTerm;			// 録画済み無効遡り範囲[E]
	
	private String adate;					// 開始日[T]
	private String zdate;					// 終了日[T]
	
	private ArrayList<String> chNames = new ArrayList<String>();				// チャンネル名　※CHコード：[E]予約操作時と同じ、[T]コントローラの値と同じ（ただしこちらはHEX表記）
	private ArrayList<ProgSubgenre> subgenres = new ArrayList<ProgSubgenre>();	// ジャンル
	private ArrayList<ProgOption> options =  new ArrayList<ProgOption>();		// 番組属性　※[E]無料／有料のみ
	
	private ReserveInfo recSetting;				// 録画設定

	
	/*******************************************************************************
	 * getter/setter
	 ******************************************************************************/
	
	public String getId() { return id; }
	public void setId(String s) { id = s; }
	
	public String getLabel() { return label; }
	public void setLabel(String s) { label = s; }

	public String getKeyword() { return keyword; }
	public void setKeyword(String s) { keyword = s; }
	public String getExKeyword() { return exKeyword; }
	public void setExKeyword(String s) { exKeyword = s; }

	public boolean getRegularExpression() { return regularExpression; }
	public void setRegularExpression(boolean b) { regularExpression = b; }
	public boolean getFazzySearch() { return fazzySearch; }
	public void setFazzySearch(boolean b) { fazzySearch = b; }
	public boolean getTitleOnly() { return titleOnly; }
	public void setTitleOnly(boolean b) { titleOnly = b; }
	
	public boolean getUniqTimeslot() { return uniqTimeslot; }
	public void setUniqTimeslot(boolean b) { uniqTimeslot = b; }
	
	public int getRecordedCheckTerm() { return recordedCheckTerm; }
	public void setRecordedCheckTerm(int n) { recordedCheckTerm = n; }
	
	/**
	 * 使用しない曜日の場合はnullを代入する
	 */
	public ArrayList<String> getTimeslots() { return timeslots; }
	public ArrayList<String> getChannels() { return chNames; }
	
	public ReserveInfo getRecSetting() { return recSetting; }
	public void setRecSetting(ReserveInfo r) { recSetting = r; } 
	
	/*******************************************************************************
	 * extra
	 ******************************************************************************/
	
	public String getChNames() { return (chNames.size() > 0) ? chNames.get(0) : null; }


}
