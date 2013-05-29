
package tainavi;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlugIn_TVPMSN extends TVProgramUtils implements TVProgram,Cloneable {

	private static final String thisEncoding = "UTF-8"; 
	
	/* 必須コード  - ここから */
	
	// 種族の特性
	@Override
	public String getTVProgramId() { return "MSNエンタメ"; }
	
	@Override
	public ProgType getType() { return ProgType.PROG; }
	@Override
	public ProgSubtype getSubtype() { return ProgSubtype.TERRA; }

	//
	@Override
	public PlugIn_TVPMSN clone() {
		return (PlugIn_TVPMSN) super.clone();
	}
	
	// 個体の特性

	@Override
	public int getTimeBarStart() {return 5;}
	
	private int getDogDays() { return ((getExpandTo8())?(8):(7)); }

	private static final int bscntMax = 10;
	private int bscnt = 4;
	public void setBscnt(int n) { bscnt = n; }
	
	private String getBscntFile() { return String.format("env"+File.separator+"bscnt.%s",getTVProgramId()); }
	
	/*******************************************************************************
	 * 定数
	 ******************************************************************************/
	
	private final String MSGID = "["+getTVProgramId()+"] ";
	private final String ERRID = "[ERROR]"+MSGID;
	private final String DBGID = "[DEBUG]"+MSGID;
	
	// 新しい入れ物の臨時格納場所
	private ArrayList<ProgList> newplist = null;
	
	private HashMap<String,String> nf = null;
	
	
	@Override
	public void loadProgram(String areaCode, boolean force) {
		
		// 新しい入れ物（トップ）を用意する
		newplist = new ArrayList<ProgList>();
		
		nf = new HashMap<String, String>();
		
		// 地域コードごとの参照ページ数の入れ物を用意する
		LinkedHashMap<String,Integer> pages = new LinkedHashMap<String, Integer>();
		
		// 参照する地域コードをまとめる
		if ( areaCode.equals(allCode) ) {
			// 「全国」
			for ( Center cr : crlist ) {
				if ( cr.getOrder() > 0 ) {
					// 有効局の地域コードのみ集める
					pages.put(cr.getAreaCode(),0);
				}
			}
		}
		else {
			// 地域個別
			pages.put(areaCode,0);
			pages.put(bsCode,0);
		}
		
		// トップの下に局ごとのリストを生やす
		for ( String ac : pages.keySet() ) {
			for ( Center cr : crlist ) {
				if ( ac.equals(cr.getAreaCode()) ) {
					ProgList pl = new ProgList();
					pl.Area = cr.getAreaCode();
					pl.SubArea = cr.getType();
					pl.Center = cr.getCenter();
					pl.BgColor = cr.getBgColor();
					
					// <TABLE>タグの列数を決め打ちで処理するので、設定上無効な局も内部的には列の１つとして必要
					pl.enabled = (cr.getOrder()>0)?(true):(false);
					
					newplist.add(pl);
					
					int pg = Integer.valueOf(cr.getType());
					if ( pl.enabled && pages.get(ac) < pg ) {
						// 地域コードごとの最大参照ページ数を格納する
						pages.put(ac,pg);
					}
				}
			}
		}

		// 局の下に日付ごとのリストを生やす
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		if ( CommonUtils.isLateNight(cal) ) {
			// ４時までは当日扱いにする
			cal.add(Calendar.DATE, -1);
		}
		GregorianCalendar cale = (GregorianCalendar) cal.clone();
		for (int i=0; i<getDogDays(); i++) {
			String date = CommonUtils.getDate(cale);
			for ( ProgList pl : newplist ) {
				ProgDateList cl = new ProgDateList();
				cl.Date = date;
				pl.pdate.add(cl);
			}
			cale.add(Calendar.DATE, 1);
		}

		// 参照する総ページ数を計算
		int counterMax = 0;
		for ( String ac : pages.keySet() ) {
			counterMax += pages.get(ac)*getDogDays();
		}
		
		// 日付の下に番組情報ごとのリストを生やす（ＭＳＮは１ページに複数局存在する）
		int counter = 1;
		for ( String ac : pages.keySet() ) {
			cale = (GregorianCalendar) cal.clone();
			for ( int i=0; i<getDogDays(); i++ ) {
				String date = CommonUtils.getDateYMD(cale);
				for ( int d=1; d<=pages.get(ac) && d<=REFPAGESMAX; d++ ) {	// 最大{REFPAGESMAX}ページまでしか参照しない
					String url;
					if ( ac.equals(bsCode) ) {
						url = "http://program.tv.jp.msn.com/tv.php?site=032&mode=06&category=d"+d+"&area="+ac+"&template=program&sdate="+date+"&shour=05&lhour=24";
					}
					else {
						if ( d == 1 ) {
							url = "http://program.tv.jp.msn.com/tv.php?site=032&mode=06&category=g&area="+ac+"&template=program&sdate="+date+"&shour=05&lhour=24";
						}
						else {
							url = "http://program.tv.jp.msn.com/tv.php?site=032&mode=06&category=s&area="+ac+"&template=program&sdate="+date+"&shour=05&lhour=24";
						}
					}
					_loadProgram(ac, String.valueOf(d), url, force, i, cale.get(Calendar.MONTH)+1, cale.get(Calendar.DATE), counter++, counterMax);
				}
				
				cale.add(Calendar.DATE, 1);
			}
		}
		
		// 開始・終了日時を正しい値に計算しなおす
		for (ProgList pl : newplist) {
			setAccurateDate(pl.pdate);
		}
		
		// 解析用
		{
			for ( String f : nf.keySet() ) {
				System.err.println(String.format("【デバッグ情報】未定義のフラグは[？]と表示されます。: [%s]",f));
			}
		}
		
		// 古い番組データを置き換える
		pcenter = newplist;
	}
	
	/* ここまで */
	
	
	
	/*
	 * 非公開メソッド等
	 */
	
	private void _loadProgram(String areacode, String page, String url, boolean force, int wdaycol, int month, int day, int counter, int counterMax) {
		//　progfilesの読み出し
		//
		final String progCacheFile = String.format(getProgDir()+File.separator+"TVMSN_%s_%s_%04d.html", areacode, page, day);
		try {
			File f = new File(progCacheFile);
			if (force == true ||
					(f.exists() == true && isCacheOld(progCacheFile) == true) ||
					(f.exists() == false && isCacheOld(null) == true)) {
				webToFile(url, progCacheFile, thisEncoding);
				reportProgress(String.format("%s(%s)を取得しました[%s-%02d日-%sページ]: (%d/%d) %s",getTVProgramId(),"オンライン",getArea(areacode),day,page,counter,counterMax,url));
			}
			else if (CommonUtils.isFileAvailable(f,10)) {
				reportProgress(String.format("%s(%s)を取得しました[%s-%02d日-%sページ]: (%d/%d) %s",getTVProgramId(),"キャッシュ",getArea(areacode),day,page,counter,counterMax,url));
			}
			else {
				reportProgress(String.format("%s(%s)がみつかりません[%s-%02d日-%sページ]: (%d/%d) %s",getTVProgramId(),"キャッシュ",getArea(areacode),day,page,counter,counterMax,url));
				return;
			}

			// キャッシュファイルの読み込み
			String response = CommonUtils.read4file(progCacheFile, false);
			
			// キャッシュが不整合を起こしていたら投げ捨てる
			Matcher ma = Pattern.compile(String.format("<h1 class=\"headtext\">%d月%d日",month,day)).matcher(response);
			if ( ! ma.find() ) {
				reportProgress(getTVProgramId()+"(キャッシュ)が無効です: ("+counter+"/"+counterMax+") "+progCacheFile);
				return;
			}
		
			// 番組リストの追加
			getPrograms(areacode, page, wdaycol, response);
		}
		catch (Exception e) {
			// 例外
		}
	}
	
	//
	
	private void getPrograms(String areacode, String page, int wdaycol, String src) {
		Matcher ma = Pattern.compile("\n(<TD ROWSPAN =(\\d+) CLASS = \".*?\">&nbsp;</TD>|<td width=\".*?\" valign=\".*?\" id=\".*?\" ROWSPAN =.*?</A></DIV></TD>)").matcher(src);
		while (ma.find()) {
			
			
			//
			ProgDetailList pdl = new ProgDetailList();
			//
			if (ma.group(1).startsWith("<TD")) {
				//System.err.println(ma.group(1));
				pdl.title = "番組情報がありません";
				pdl.length = Integer.valueOf(ma.group(2));
			}
			else {
				// 1 : length
				// 2 : genre
				// 3 : link
				// 4 : title
				// 5,6 : start-hour,min
				// 7,8 : end-hour,min
				// 9,10 : detail1,detail2
				Matcher mb = Pattern.compile("<td width=\".*?\" valign=\".*?\" id=\".*?\" ROWSPAN =(\\d+) CLASS = \"(.*?)\"><DIV [^>]*?><a href=.*?onClick=\"xClickACT\\(\'\\.(.+?)\'\\);.*?<h1>(.*?)</h1><h2>.*?(\\d\\d):(\\d\\d)～(\\d\\d):(\\d\\d).*?</h2><p>(.*?)</p><p>(.*?)</p>.*?</A></DIV></TD>").matcher(ma.group(1));
				if ( ! mb.find() ) {
					System.err.println("TVMSN: unexpected string= "+ma.group(1));
					continue;
				}
				
				// タイトル
				{
					pdl.title = replaceMarks(pdl, CommonUtils.unEscape(mb.group(4)));
					pdl.splitted_title = pdl.title;
				}
					
				
				// 番組詳細
				{
					if (mb.group(9).length() > 0) {
						pdl.detail = CommonUtils.unEscape(String.format("%s\n%s", mb.group(9),mb.group(10)));
					}
					else {
						pdl.detail = CommonUtils.unEscape(mb.group(10));
					}
					
					pdl.detail = replaceMarks(pdl, pdl.detail);
					
					//
					pdl.detail = pdl.detail.replaceAll("<a[^>]*?>", "");
					pdl.detail = pdl.detail.replaceAll("</a>", "");
					
					pdl.splitted_detail = pdl.detail;
				}
				
				// 検索用インデックス
				pdl.titlePop = TraceProgram.replacePop(pdl.title);
				pdl.detailPop = TraceProgram.replacePop(pdl.detail);
				pdl.SearchStrKeys = TraceProgram.splitKeys(pdl.titlePop);
				
				// 詳細へのリンク
				pdl.link = "http://program.tv.jp.msn.com"+mb.group(3);
				
				// 番組長
				pdl.length = Integer.valueOf(mb.group(1));
				
				
				// ジャンル
				if (mb.group(2).equals(allCode)) {
					pdl.genre = ProgGenre.NOGENRE;
				}
				else if (mb.group(2).equals("anime")) {
					pdl.genre = ProgGenre.ANIME;
				}
				else if (mb.group(2).equals("movie")) {
					pdl.genre = ProgGenre.MOVIE;
				}
				else if (mb.group(2).equals("dorama")) {
					pdl.genre = ProgGenre.DORAMA;
				}
				else if (mb.group(2).equals("sports")) {
					pdl.genre = ProgGenre.SPORTS;
				}
				else {
					pdl.genre = ProgGenre.NOGENRE;
				}
				
				// 開始・終了時刻
				GregorianCalendar c = new GregorianCalendar();
				c.setTime(new Date());
				c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(mb.group(5)));
				c.set(Calendar.MINUTE, Integer.valueOf(mb.group(6)));
				pdl.start = String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
				
				c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(mb.group(7)));
				c.set(Calendar.MINUTE, Integer.valueOf(mb.group(8)));
				pdl.end = String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
			}

			//
			int col = -1;
			int rowMin = 9999;
			for ( int i=0; i<newplist.size(); i++ ) {
				ProgList pl = newplist.get(i);
				if ( ! (pl.Area.equals(areacode) && pl.SubArea.equals(page)) ) {
					continue;
				}
				if (pl.pdate.get(wdaycol).row < rowMin) {
					col = i;
					rowMin = pl.pdate.get(wdaycol).row;
				}
			}
			if (col < 0) {
				continue;
			}
			ProgDateList pcl = newplist.get(col).pdate.get(wdaycol);
			pcl.pdetail.add(pdl);
			pcl.row += pdl.length;
		}
	}
	
	private String replaceMarks(ProgDetailList pdl, String text) {
		
		HashMap<String, Integer> xf = new HashMap<String, Integer>();
		
		// フラグを拾う
		{
			String[] marks = new String[] {
					"shin",		// 新番組
					"saisyu",	// 最終回
					"sai",		// 再放送
					"hatsu",	// 初回放送
					"ppv",		// [PV]ペイパービュー
					"moji",		// [字]文字多重放送
					"nikakoku",	// [二]二か国語放送
					"taju",		// [多]音声多重放送
					"dubbed",	// [吹]吹き替え
					"data"		// [デ]データ放送
								// [生]生放送(ないよ)
			};
			
			for (int i=0; i<marks.length; i++) {
				String mark = marks[i];
				String expr = String.format("/ico_%s\\.gif>", mark);
				Matcher mx = Pattern.compile(expr, Pattern.DOTALL).matcher(text);
				if ( mx.find() ) {
					xf.put(mark, i);
				}
			}
			
			for ( String mark : xf.keySet() ) {
				String expr = String.format("<img src=http://img.tv.msn.co.jp/s/ico_%s\\.gif>", mark);
				text = text.replaceAll(expr,"");
				switch (xf.get(mark)) {
				case 0:
					pdl.flag = ProgFlags.NEW;
					break;
				case 1:
					pdl.flag = ProgFlags.LAST;
					break;
				case 2:
					pdl.addOption(ProgOption.REPEAT);
					break;
				case 3:
					pdl.addOption(ProgOption.FIRST);
					break;
				case 4:
					pdl.addOption(ProgOption.PV);
					break;
				case 5:
					pdl.addOption(ProgOption.SUBTITLE);
					break;
				case 6:
					pdl.addOption(ProgOption.BILINGUAL);
					break;
				case 7:
					pdl.addOption(ProgOption.MULTIVOICE);
					break;
				case 8:
					pdl.addOption(ProgOption.STANDIN);
					break;
				case 9:
					pdl.addOption(ProgOption.DATA);
					break;
				}
			}
		}

		// 置換する
		{
			HashMap<String,String> marks = new HashMap<String,String>();
			// 置換するコード
			marks.put("director", "[監]");
			marks.put("guest", "[ゲ]");
			marks.put("jikkyo", "[実]");
			marks.put("kaisetsusya", "[解]");
			marks.put("katari", "[語]");
			marks.put("koe", "[声]");
			marks.put("org", "[原]");
			marks.put("plot", "[脚]");
			marks.put("shikai", "[司]");
			marks.put("syutsuen", "[出]");
			marks.put("n", "[Ｎ]");
			marks.put("tenki", "[天]");
			marks.put("zen", "(前編)");
			marks.put("kou", "(後編)");
			// 無視するコード
			marks.put("s", "");
			marks.put("3d", "");
			marks.put("eiga", "");
			marks.put("hand", "");
			
			for ( String mark : marks.keySet() ) {
				String expr = String.format("<img src=http://img.tv.msn.co.jp/s/ico_%s\\.gif>", mark);
				text = text.replaceAll(expr,marks.get(mark));
			}
		}
		
		// 未解析のマーク
		Matcher mx = Pattern.compile("/ico_(.*?)\\.gif>", Pattern.DOTALL).matcher(text);
		while ( mx.find() ) {
			nf.put(mx.group(1), null);
		}
		for ( String mark : nf.keySet() ) {
			String expr = String.format("<img src=http://img.tv.msn.co.jp/s/ico_%s\\.gif>", mark);
			text = text.replaceAll(expr,"[？]");
		}

		return text;
	}
	
	/*
	 * ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
	 * ★★★★★　放送地域を取得する（TVAreaから降格）－ここから　★★★★★
	 * ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
	 */

	/*
	 * 公開メソッド
	 */
	
	//
	@Override
	public String getDefaultArea() {return "東京";}
	
	//
	@Override
	public void loadAreaCode(){
		
		// 設定ファイルが存在していればファイルから
		File f = new File(getAreaSelectedFile());
		if ( f.exists() ) {
			@SuppressWarnings("unchecked")
			ArrayList<AreaCode> tmp = (ArrayList<AreaCode>) CommonUtils.readXML(getAreaSelectedFile());
			if ( tmp != null ) {
				System.out.println("地域リストを読み込みました: "+getAreaSelectedFile());
				aclist = tmp;
	            return;
			}

			System.out.println("地域リストの読み込みに失敗しました: "+getAreaSelectedFile());
		}
		
		// 地域一覧の作成
		ArrayList<AreaCode> newaclist = new ArrayList<AreaCode>();

		// 存在していなければWeb上から
		String response = "";
		{
			String uri = "http://program.tv.jp.msn.com/tv.php?site=032&mode=06&template=program&category=g&shour=05&lhour=24";
			response = webToBuffer(uri, thisEncoding, true);
			if ( response == null ) {
				System.out.println("地域情報の取得に失敗しました: "+uri);
				return;
			}
		}
		
		Matcher ma = Pattern.compile("<select name=\"area\" size=\"1\">(.+?)</select>").matcher(response);
		if (ma.find()) {
			Matcher mb = Pattern.compile("<option value=\"([^\"]+?)\" ?(selected=\"selected\")?>(.+?)</option>").matcher(ma.group(1));
			while (mb.find()) {
				AreaCode ac = new AreaCode();
				ac.setArea(mb.group(3));
				ac.setCode(mb.group(1));
				newaclist.add(ac);
			}
		}
		
		if ( newaclist.size() == 0 ) {
			System.err.println(ERRID+"地域一覧の取得結果が０件だったため情報を更新しません");
			return;
		}
		
		{
			{
				AreaCode ac = new AreaCode();
				ac.setArea("全国");
				ac.setCode(allCode);
				newaclist.add(0,ac);
			}
			{
				AreaCode ac = new AreaCode();
				ac.setArea("ＢＳ");
				ac.setCode(bsCode);
				newaclist.add(ac);
			}
		}
		
		aclist = newaclist;
		saveAreaCode();
	}

	/*
	 * ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
	 * ★★★★★　放送地域を取得する（TVAreaから降格）－ここまで　★★★★★
	 * ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
	 */
	
	
	
	/*
	 * ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
	 * ★★★★★　放送局を選択する（TVCenterから降格）－ここから　★★★★★
	 * ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
	 */
	
	/*
	 * 公開メソッド
	 */

	// 設定ファイルがなければWebから取得
	@Override
	public void loadCenter(String code, boolean force) {
		
		if ( code == null ) {
			System.out.println(ERRID+"地域コードがnullです.");
			return;
		}
		
		// BSのページ数の初期化(事前に判明していない場合は2)
		int bscntTmp = CommonUtils.loadCnt(getBscntFile());
		bscntTmp = bscnt = (bscntTmp > 0)?(bscntTmp):(3);
		
		//
		String centerListFile = getCenterListFile(getTVProgramId(), code);
		
		if (force) {
			File f = new File(centerListFile);
			f.delete();
		}
		
		File f = new File(centerListFile);
		if (f.exists() == true) {
			@SuppressWarnings("unchecked")
			ArrayList<Center> tmp = (ArrayList<Center>) CommonUtils.readXML(centerListFile);
			if ( tmp != null ) {
	            
				crlist = tmp;
				
		        // 放送局名変換
	    		attachChFilters();
		        
				System.out.println("放送局リストを読み込みました: "+centerListFile);
	            return;
	        }

			System.out.println("放送局リストの読み込みに失敗しました: "+centerListFile);
		}
		
		// 放送局をつくるよ
		ArrayList<Center> newcrlist = new ArrayList<Center>();
		
		// 地上派・UHFは地域別に扱う

		int cntMax = ((allCode.equals(code))?(aclist.size()-2):(1))*2 + bscnt;
		int cnt = 1;
		for (AreaCode ac : aclist) {
			if (ac.getCode().equals(bsCode)) {
				continue;
			}
			else if (code.equals(allCode) && ac.getCode().equals(allCode)) {
				continue;
			}
			else if ( ! code.equals(allCode) && ! ac.getCode().equals(code)) {
				continue;
			}
				
			String url;
			
			// 地上波
			url = "http://program.tv.jp.msn.com/tv.php?site=032&mode=06&template=program&category=g&area="+ac.getCode()+"&shour=05&lhour=24";
			if ( _loadCenter(newcrlist, ac.getCode(), "1", url) ) {
				reportProgress(String.format("放送局情報を取得しました[%s%d]: (%d/%d) %s","地上波",cnt,cnt,cntMax,url));
			}
			cnt++;
			
			// UHF・BSアナログ
			url = "http://program.tv.jp.msn.com/tv.php?site=032&mode=06&template=program&category=s&area="+ac.getCode()+"&shour=05&lhour=24";
			if ( _loadCenter(newcrlist, ac.getCode(), "2", url) ) {
				reportProgress(String.format("放送局情報を取得しました[%s%d]: (%d/%d) %s","地上波",cnt,cnt,cntMax,url));
			}
			cnt++;
		}
		
		// BS1・BS2は共通にする(bscntは_loadCenter()中で増加する可能性あり)
		
		for ( int d=1; d<=bscnt; d++ )
		{
			String url = "http://program.tv.jp.msn.com/tv.php?site=032&mode=06&template=program&category=d"+d+"&shour=05&lhour=24";
			if ( _loadCenter(newcrlist, bsCode, String.valueOf(d), url) ) {
				reportProgress(String.format("放送局情報を取得しました[%s%d]: (%d/%d) %s","BS",d,cnt,cntMax,url));
			}
			cnt++;
		}
		
		// BSのページ数を記録する
		
		if ( bscntTmp < bscnt ) {
			reportProgress(String.format("BSのページ数が変更されました: %d→%d (最大%dまで)",bscntTmp,bscnt,bscntMax));
			CommonUtils.saveCnt(bscnt,getBscntFile());
		}
		
		if ( newcrlist.size() == 0 ) {
			System.err.println(ERRID+"放送局情報の取得結果が０件だったため情報を更新しません");
			return;
		}
		
		crlist = newcrlist;
		attachChFilters();	// 放送局名変換
		saveCenter();
	}
		
	private boolean _loadCenter(ArrayList<Center> newcrlist, String code, String page, String uri) {
		String response = null;
		{
			response = webToBuffer(uri, thisEncoding, true);
			if ( response == null ) {
				System.out.println("放送局情報の取得に失敗しました: "+uri);
				return false;
			}
		}
		
		// BSのページ数を計算する
		
		for ( int i=bscnt+1; i<=bscntMax; i++ ) {
			if ( ! response.matches(".*&category=d"+i+"\".*") ) {
				if ( bscnt < i-1 ) {
					bscnt = i-1;
				}
				break;
			}
		}
		
		// 局名リストに追加する
		
		Matcher ma = Pattern.compile("<!-- ↓ Station -->([\\s\\S]+?)<!-- ↑ Station -->").matcher(response);
		if (ma.find()) {
			Matcher mb = Pattern.compile("<TH width=\".*?\">\\s*(.+?)\\s*</th>").matcher(ma.group(1));
			while (mb.find()) {
				String centerName;
				String link;
				Matcher mc = Pattern.compile("<a href =\"(.*?)\" id=\'ch01_h\' class=\"th_a\" target=\"_blank\"\\s*>\\s*(.+?)(<BR>|</a>)").matcher(mb.group(1));
				if (mc.find()) {
					centerName = CommonUtils.unEscape(mc.group(2));
					link = mb.group(1);
				}
				else {
					centerName = CommonUtils.unEscape(mb.group(1).replaceAll("<BR>", ""));
					link = "";
				}
				
				// NHK総合・NHK教育
				centerName = centerName.replaceFirst("^NHK$", "ＮＨＫ総合");
				centerName = centerName.replaceFirst("^NHK Ｅテレ", "ＮＨＫ Ｅテレ");
				if ( ! code.startsWith(bsCode) && page.equals("1")) {
					if (centerName.startsWith("ＮＨＫ")) {
						centerName = centerName+"・"+getArea(code);
					}
				}
				
				Center cr = new Center();
				cr.setAreaCode(code);
				cr.setCenterOrig(centerName);
				cr.setLink(link);
				cr.setType(page);
				cr.setEnabled(true);
				newcrlist.add(cr);
			}
		}
		return true;
	}
	
	/*
	 * ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
	 * ★★★★★　放送局を選択する（TVCenterから降格）－ここまで　★★★★★
	 * ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
	 */
}
