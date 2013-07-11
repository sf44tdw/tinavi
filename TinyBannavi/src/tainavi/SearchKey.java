package tainavi;

import java.util.ArrayList;
import java.util.regex.Pattern;


public class SearchKey {
	private String label;
	
	// 0:"次のすべての条件に一致"
	// 1:"次のいずれかの条件に一致"
	private String condition;
	
	// 0:"延長感染源にする"
	// 1:"延長感染源にしない"
	private String infection;
	
	public static enum TargetId {
		TITLEANDDETAIL	("0",	true,	true,	"番組名、内容に"),
		TITLE			("1",	true,	true,	"番組名に"),
		DETAIL			("2",	true,	true,	"番組内容に"),
		CHANNEL			("3",	true,	true,	"チャンネル名に"),
		GENRE			("4",	false,	true,	"ジャンルに"),
		NEW				("5",	false,	false,	"新番組"),
		LAST			("6",	false,	false,	"最終回"),
		REPEAT			("7",	false,	false,	"再放送"),
		FIRST			("8",	false,	false,	"初回放送"),
		LENGTH			("9",	false,	true,	"番組長が"),
		STARTA			("10",	false,	true,	"開始時刻(上限)が"),
		STARTZ			("11",	false,	true,	"開始時刻(下限)が"),
		SPECIAL			("12",	false,	false,	"特番"),
		NOSCRUMBLE		("13",	false,	false,	"無料放送"),
		STARTDATETIME	("14",	true,	true,	"開始日時に"),
		SUBGENRE		("15",	false,	true,	"サブジャンルに"),
		LIVE			("16",	false,	false,	"生放送"),
		BILINGUAL		("17",	false,	false,	"二か国語放送"),
		STANDIN			("18",	false,	false,	"吹替放送"),
		RATING			("19",	false,	false,	"視聴制限"),
		MULTIVOICE		("20",	false,	false,	"副音声/コメンタリ"),
		;
		
		private String id;
		private boolean useregexpr;
		private boolean usekeyword;
		private String name;
		
		private TargetId(String id, boolean useregexpr, boolean usekeyword, String name) {
			this.id = id;
			this.useregexpr = useregexpr;
			this.usekeyword = usekeyword;
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public String getId() {
			return id;
		}
		
		public boolean getUseRegexpr() {
			return useregexpr;
		}
		
		public boolean getUseKeyword() {
			return usekeyword;
		}
		
		public static TargetId getTargetId(String id) {
			for ( TargetId ti : TargetId.values() ) {
				if ( ti.id.equals(id) ) {
					return ti;
				}
			}
			return null;
		}
	}
	
	private String target;
	
	// s\t..:キーワード
	private String keyword;
	
	// 0\t..:"を含む番組"
	// 1\t..:"を含む番組を除く"
	private String contain;
	
	// 1:レベル1
	// 2:レベル2
	// 3:レベル3
	// 4:レベル4
	// 5:レベル5
	private String okiniiri;
	
	// 大小同一視無効
	private boolean caseSensitive;
	
	// 番組追跡表示あり
	private boolean showInStandby = true;
	
	// 正規表現はプリコンパイルしておくべきだ！
	ArrayList<TargetId> alTarget = new ArrayList<TargetId>();
	ArrayList<Pattern> alKeyword_regex = new ArrayList<Pattern>();
	ArrayList<String> alKeyword = new ArrayList<String>();
	ArrayList<String> alKeyword_plane = new ArrayList<String>();
	ArrayList<String> alKeyword_pop = new ArrayList<String>();
	ArrayList<String> alContain = new ArrayList<String>();
	ArrayList<Integer> alLength = new ArrayList<Integer>();

	//
	public void setLabel(String s) { label = s; }
	public String getLabel() { return label; }
	
	public void setCondition(String s) { condition = s; }
	public String getCondition() { return condition; }
	
	public void setInfection(String s) { infection = s; }
	public String getInfection() { return infection; }
	
	public void setTarget(String s) { target = s; }
	public String getTarget() { return target; }
	public void setKeyword(String s) { keyword = s; }
	public String getKeyword() { return keyword; }
	public void setContain(String s) { contain = s; }
	public String getContain() { return contain; }

	public void setOkiniiri(String s) { okiniiri = s; }
	public String getOkiniiri() { return okiniiri; }

	public void setCaseSensitive(boolean b) { caseSensitive = b; }
	public boolean getCaseSensitive() { return caseSensitive; }

	public void setShowInStandby(boolean b) { showInStandby = b; }
	public boolean getShowInStandby() { return showInStandby; }
}
