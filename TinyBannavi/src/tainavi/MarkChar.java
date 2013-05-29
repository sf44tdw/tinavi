package tainavi;

import tainavi.TVProgram.ProgFlags;
import tainavi.TVProgram.ProgOption;
import tainavi.TVProgram.ProgScrumble;

/**
 * 番組情報のマークを扱うクラス
 */
public class MarkChar {

	/*******************************************************************************
	 * 定数
	 ******************************************************************************/
	
	public static enum MarkItem {
		EXTENTION		( "★延長注意★" ),
		EXTENTION_S		( "(延)" ),
		NEWARRIVAL		( "[NEW]" ),
		NEWARRIVAL_S	( "(N)" ),
		MOVED			( "(移)" ),

		NEW				( "【新】" ),
		LAST			( "【終】" ),
		NOSCRUMBLE		( "【無料】" ),
		MODIFIED		( "(更)" ),
		NONREPEATED		( "(初)" ),
		SPECIAL			( "【特】" ),
		RATING			( "【Ｒ】" ),
		FIRST			( "【初】" ),
		NOSYOBO			( "[!]" ),
		LIVE			( "[生]" ),
		PV				( "[PV]" ),
		SUBTITLE		( "[字]" ),
		BILINGUAL		( "[二]" ),
		MULTIVOICE		( "[多]" ),
		STANDIN			( "[吹]" ),
		DATA			( "[デ]" ),
		SORRUND			( "[5.1]" ),
		PRECEDING		( "【先】" ),
		
		REPEATED		( "[再]" ),
		;
		
		private String name;
		
		private MarkItem(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	
	/*******************************************************************************
	 * 部品
	 ******************************************************************************/
	
	private Env env;
	
	/*******************************************************************************
	 * コンストラクタ
	 ******************************************************************************/
	
	public MarkChar(Env env) {
		this.env = env;
	}
	
	/*******************************************************************************
	 * アクション
	 ******************************************************************************/
	
	/**
	 *  延長警告マークの取得
	 */
	public String getExtensionMark(ProgDetailList tvd) {
		return ((tvd.extension) ? ((env.getShortExtMark())?(MarkItem.EXTENTION_S.getName()):(MarkItem.EXTENTION.getName())) : (""));
	}
	
	/**
	 * 新番組と最終回だけわける
	 */
	public String getNewLastMark(ProgDetailList tvd) {
		if (tvd.flag == ProgFlags.NEW && env.getOptMarks().get(ProgOption.HIDDEN_NEW) == Boolean.TRUE) {
			return MarkItem.NEW.getName();
		}
		else if (tvd.flag == ProgFlags.LAST && env.getOptMarks().get(ProgOption.HIDDEN_LAST) == Boolean.TRUE) {
			return MarkItem.LAST.getName();
		}
		return "";
	}
	
	/**
	 *  普通のマークの取得
	 */
	public String getOptionMark(ProgDetailList tvd) {
		
		String mkNewArr = "";
		String mkModified = "";
		String mkNoscr = "";
		String mkSpecial = "";
		String mkNoSyobo = "";
		String mkRating = "";
		
		String mkFirst = "";
		String mkLive = "";
		String mkSubtitle = "";
		String mkBilingual = "";
		String mkTextbc = "";
		String mkMultivoice = "";
		String mkStandin = "";
		String mkPv = "";
		String mkData = "";
		String mkSurround = "";
		String mkNonrepeated = "";
		String mkMoved = "";
		String mkPrec = "";
		
		if (tvd.noscrumble == ProgScrumble.NOSCRUMBLE && env.getOptMarks().get(ProgOption.HIDDEN_NOSCRUMBLE) == Boolean.TRUE) {
			mkNoscr = MarkItem.NOSCRUMBLE.getName();
		}
		
		if ( tvd.newarrival && env.getOptMarks().get(ProgOption.NEWARRIVAL) == Boolean.TRUE ) {
			mkNewArr = ((env.getShortExtMark())?(MarkItem.NEWARRIVAL_S.getName()):(MarkItem.NEWARRIVAL.getName()));
		}
		if ( tvd.modified && env.getOptMarks().get(ProgOption.MODIFIED) == Boolean.TRUE ) {
			mkModified = MarkItem.MODIFIED.getName();
		}
		if ( tvd.nonrepeated && env.getOptMarks().get(ProgOption.NONREPEATED) == Boolean.TRUE ) {
			mkNonrepeated = MarkItem.NONREPEATED.getName();
		}
		for ( ProgOption opt : tvd.getOption() ) {
			if ( env.getOptMarks().get(opt) != null && env.getOptMarks().get(opt) == Boolean.FALSE ) {
				continue;
			}
			switch (opt) {
			case SPECIAL:
				// 特別番組
				mkSpecial = MarkItem.SPECIAL.getName();
				break;
			case RATING:
				// 視聴年齢制限
				mkRating = MarkItem.RATING.getName();
				break;
			case FIRST:
				// 初回放送
				mkFirst = MarkItem.FIRST.getName();
				break;
			
			case NOSYOBO:
				mkNoSyobo = MarkItem.NOSYOBO.getName();
				break;
			
			case LIVE:
				// 生放送
				mkLive = MarkItem.LIVE.getName();
				break;
			case PV:
				// ペイパービュー
				mkPv = MarkItem.PV.getName();
				break;
			case SUBTITLE:
				// 字幕放送
				mkSubtitle = MarkItem.SUBTITLE.getName();
				break;
			case BILINGUAL:
				// 二か国語放送
				mkBilingual = MarkItem.BILINGUAL.getName();
				break;
			case MULTIVOICE:
				// 音声多重放送
				mkMultivoice = MarkItem.MULTIVOICE.getName();
				break;
			case STANDIN:
				// 吹き替え
				mkStandin = MarkItem.STANDIN.getName();
				break;
			case DATA:
				// データ放送
				mkData = MarkItem.DATA.getName();
				break;
			case SURROUND:
				// 5.1chサラウンド
				mkSurround = MarkItem.SORRUND.getName();
				break;
			case MOVED:
				// 先週無かったか時間が違う
				mkMoved = MarkItem.MOVED.getName();
				break;
			case PRECEDING:
				// 先行放送
				mkPrec = MarkItem.PRECEDING.getName();
				break;
			default:
				break;
			}
		}
		
		return(mkNewArr+mkNoSyobo+mkMoved+mkModified+mkFirst+mkNonrepeated+mkNoscr+mkSpecial+mkRating+mkPrec+mkLive+mkPv+mkSubtitle+mkBilingual+mkTextbc+mkMultivoice+mkStandin+mkData+mkSurround);
	}
	
	/**
	 * タイトルの後ろにつくマークの取得
	 */
	public String getPostfixMark(ProgDetailList tvd) {
		
		String mkRep = "";
		
		for ( ProgOption opt : tvd.getOption() ) {
			switch (opt) {
			case REPEAT:
				mkRep = MarkItem.REPEATED.getName();
				break;
			default:
				break;
			}
		}
		
		return(mkRep);
	}

}
