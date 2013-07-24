package tainavi;

import java.io.File;
import java.util.ArrayList;

public class TraceProgram {

	private static final String MSGID = "[番組追跡設定] ";
	private static final String ERRID = "[ERROR]"+MSGID;
	
	//
	private final String traceKeyFile = "env"+File.separator+"tracekey.xml";
	private ArrayList<TraceKey> traceKeys = new ArrayList<TraceKey>();
	
	public static String getNewLabel(String title, String center) { return title.trim()+" ("+center+")"; }
	
	// 設定ファイルに書き出し
	public boolean save() {
		System.out.println(MSGID+"保存します: "+traceKeyFile);
		if ( ! CommonUtils.writeXML(traceKeyFile, traceKeys) ) {
			System.err.println(ERRID+"保存に失敗しました： "+traceKeyFile);
			return false;
		}
		return true;
	}
	
	// 設定ファイルから読み出し
	@SuppressWarnings("unchecked")
	public void load() {
		if ( ! new File(traceKeyFile).exists() ) {
			System.out.println(MSGID+"設定を読み込めなかったので登録なしで起動します： "+traceKeyFile);
			return;
		}
		
		System.out.println(MSGID+"読み込みます: "+traceKeyFile);
		
		traceKeys = (ArrayList<TraceKey>)CommonUtils.readXML(traceKeyFile);
		if ( traceKeys == null ) {
    		System.out.println(ERRID+"設定を読み込めなかったので登録なしで起動します： "+traceKeyFile);
    		return;
		}
		
		// ファイルに保存しないようにするので
        for (TraceKey tr : traceKeys) {
        	if ( tr.getTitle() == null ) {
        		int index = tr._getLabel().indexOf(" (");
        		tr.setTitle(index > 0 ? tr._getLabel().substring(0,index) : "");
        	}
        	
        	tr.setTitlePop(replacePop(tr.getTitle()));
        	
        	tr.setSearchStrKeys(splitKeys(tr._getTitlePop()));
        	
        	tr.setLabel(getNewLabel(tr.getTitle(), tr.getCenter()));
		}
	}
	
	// 検索用
	public ArrayList<TraceKey> getTraceKeys() {
		return(traceKeys);
	}
	
	// 番組追跡の追加
	public void add(TraceKey newkey) {
		traceKeys.add(newkey);
	}
	
	// 番組追跡の削除
	public void remove(String key) {
		for ( TraceKey k : traceKeys ) {
			if (k._getLabel().equals(key)) {
				traceKeys.remove(k);
	        	break;
			}
		}
	}
	
	private static final String popSrc = "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゐゆゑよらりるれろわをんぁぃぅぇぉっゃゅょがぎぐげござじずぜぞだぢづでどばびぶべぼぱぴぷぺぽァィゥェォッャュョガギグゲゴザジズゼゾダヂヅデドバビブベボパピプペポヴｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜｦﾝｧｨｩｪｫｯｬｭｮａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺabcdefghijklmnopqrstuvwxyz１２３４５６７８９０！＠＃＄％＾＆＊（）－＿＝＋「｛」｝￥｜；：’”、＜。＞・？‘～　一二三四五六七八九〇ⅠⅡⅢⅣⅤⅥⅦⅧⅨ①②③④⑤⑥⑦⑧⑨壱弐肆零";
	private static final String popDst = "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤイユエヨラリルレロワヲンアイウエオツヤユヨカキクケコサシスセソタチツテトハヒフヘホハヒフヘホアイウエオツヤユヨカキクケコサシスセソタチツテトハヒフヘホハヒフヘホウアイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲンアイウエオツヤユヨABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?`~ 12345678901234567891234567891240";
	private static char[] popDstA = null;
	
	// 文字列を比較しやすい値にする
	public static boolean isOmittedChar(char ch) {
		return (ch == ' ' || ch == '　' || ch == 'ー' || ch == 'ﾞ' || ch == 'ﾟ');
	}
	public static String replacePop(String src)
	{
		if (popDstA == null) {
			popDstA = popDst.toCharArray();
		}
		
		src = src.replaceAll("[ 　ーﾞﾟ]", "");
		
		StringBuilder sb = new StringBuilder();
		for ( char c : src.toCharArray() ) {
			int index = popSrc.indexOf(c);
			if ( index >= 0 ) {
				sb.append(popDstA[index]);
			}
			else {
				sb.append(c);
			}
		}
		return(sb.toString());
	}

	// あいまい検索用キー文字列群の生成
	public static ArrayList<String> splitKeys(String s)
	{
		ArrayList<String> SearchStrKeys = new ArrayList<String>();
	    
	    int countStr=s.length();
	    if (countStr == 1) {
	    	SearchStrKeys.add(s);
	    }
	    else {
		    for (int i=1; i<countStr; i++) {
		    	SearchStrKeys.add(s.substring(i-1, i+1));
		    }
	    }
	    
	    return(SearchStrKeys);
	}
	
	// 2つの文字を比較してスコアを計算する(special thanks to ◆kzz0PzTAMM)
	public static int sumScore(String SearchStr1, String SearchStr2)
	{
	    // 検索ワードが空なら検索終了
		if (SearchStr1.equals("") || SearchStr2.equals("")) {
			return 0;
		}
		
	    // 検索ワードを基準に検索する
	    return sumScore(splitKeys(SearchStr1), SearchStr2);
	}
	public static int sumScore(ArrayList<String> SearchStr1Keys, String SearchStr2)
	{
	
	    // 検索ワードが空なら検索終了
		if (SearchStr1Keys.size() == 0 || "".equals(SearchStr2)) {
			return 0;
		}

	    // 検索ワードを基準に検索する
	    int searchCount=0;
	    int score=0;
	    for (String key : SearchStr1Keys) {
	    	if (SearchStr2.indexOf(key) >= 0) {
	    		score++;
	    	}
	    	searchCount++;
	    }

	    score=Math.round(score*100/searchCount);
	    return(score);
    }
	
	
	
	// コンストラクタ
	public TraceProgram() {
		traceKeys = new ArrayList<TraceKey>();
	}
}
