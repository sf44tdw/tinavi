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


public class PlugIn_TVPTVGuide extends TVProgramUtils implements TVProgram,Cloneable {

	public PlugIn_TVPTVGuide clone() {
		return (PlugIn_TVPTVGuide) super.clone();
	}
	
	private static final String thisEncoding = "UTF-8"; 
	
	
	/*******************************************************************************
	 * 種族の特性
	 ******************************************************************************/
	
	@Override
	public String getTVProgramId() { return "インターネットTVガイド"; }
	
	@Override
	public ProgType getType() { return ProgType.PROG; }
	@Override
	public ProgSubtype getSubtype() { return ProgSubtype.TERRA; }

	
	/*******************************************************************************
	 * 個体の特性
	 ******************************************************************************/
	
	@Override
	public int getTimeBarStart() {return 5;}

	private int getDogDays() { return ((getExpandTo8())?(8):(7)); }
	
	
	/*******************************************************************************
	 * 定数
	 ******************************************************************************/
	
	private final String MSGID = "["+getTVProgramId()+"] ";
	private final String ERRID = "[ERROR]"+MSGID;
	private final String DBGID = "[DEBUG]"+MSGID;
	
	/*******************************************************************************
	 * 部品
	 ******************************************************************************/

	// 新しい入れ物の臨時格納場所
	protected final ArrayList<ProgList> newplist = new ArrayList<ProgList>();
	
	// 未定義のフラグの回収場所
	private final HashMap<String,String> nf = new HashMap<String, String>();
	
	
	/*******************************************************************************
	 * コンストラクタ
	 ******************************************************************************/
	
	
	/*******************************************************************************
	 * 番組情報を取得する
	 ******************************************************************************/
	@Override
	public void loadProgram(String areaCode, boolean force) {
		
		// 入れ物を空にする
		newplist.clear();
		nf.clear(); 
		
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
		
		// 日付の下に番組情報ごとのリストを生やす（ＴＶガイドシンプル版は１ページに複数局存在する）
		int counter = 1;
		for ( String ac : pages.keySet() ) {
			cale = (GregorianCalendar) cal.clone();
			for ( int i=0; i<getDogDays(); i++ ) {
				String date = CommonUtils.getDateYMD(cale);
				for ( int d=1; d<=pages.get(ac) && d<=REFPAGESMAX; d++ ) {	// 最大{REFPAGESMAX}ページまでしか参照しない
					String url;
					if ( ac.equals(bsCode) ) {
						url = "http://www.tvguide.or.jp/TF1101LS.php?mediaId=2&date="+date+"&time=05&dispflg=0&page="+String.valueOf(d)+"&time=24";
					}
					else {
						url = "http://www.tvguide.or.jp/TF1101LS.php?regionId="+ac+"&mediaId=1&date="+date+"&time=05&dispflg=0&page="+String.valueOf(d)+"&time=24";
					}
					_loadProgram(ac, d, pages.get(ac), url, force, i, cale.get(Calendar.MONTH)+1, cale.get(Calendar.DATE), counter++, counterMax);
				}
				
				cale.add(Calendar.DATE, 1);
			}
		}
		
		// 当日分のプログラムリストのみrowを再計算（現在時刻以前の情報がない関係で）
		for ( ProgList pl : newplist ) {
			if ( pl.enabled ) {
				if ( pl.pdate.size() > 0 ) {
					ProgDateList pcl = pl.pdate.get(0);
					pcl.row = 0;
					for ( ProgDetailList pdl : pcl.pdetail ) {
						pcl.row += pdl.length;
					}
				}
			}
		}
		
		// 開始・終了日時を正しい値に計算しなおす
		for ( ProgList pl : newplist ) {
			setAccurateDate(pl.pdate);
		}
		
		// 古いデータから補完できないかな？
		CompensatesPrograms(newplist);
		
		// 古い番組データを置き換える
		pcenter = newplist;
	}
	
	/* ここまで */
	
	
	
	/*
	 * 非公開メソッド
	 */

	private void _loadProgram(String areacode, int page, int pmax, String url, boolean force, int wdaycol, int month, int day, int counter, int counterMax) {
		//　progfilesの読み出し
		final String progCacheFile = String.format(getProgDir()+File.separator+"TVGuide_%s_%d_%s.html", areacode, day, page);
		try {
			File f = new File(progCacheFile);
			if (force == true ||
					(f.exists() == true && isCacheOld(progCacheFile) == true) ||
					(f.exists() == false && isCacheOld(null) == true)) {
				webToFile(url, progCacheFile, thisEncoding);
				reportProgress(String.format("%s (オンライン)を取得しました: (%d/%d) %d日[%d/%d] %s",getTVProgramId(),counter,counterMax,day,page,pmax,url));
			}
			else if (CommonUtils.isFileAvailable(f,10)) {
				reportProgress(String.format("%s (キャッシュ)を取得しました: (%d/%d) %d日[%d/%d] %s",getTVProgramId(),counter,counterMax,day,page,pmax,progCacheFile));
			}
			else {
				reportProgress(String.format("%s (キャッシュ)がみつかりません: (%d/%d) %d日[%d/%d] %s",getTVProgramId(),counter,counterMax,day,page,pmax,progCacheFile));
				return;
			}

			// キャッシュファイルの読み込み
			String response = CommonUtils.read4file(progCacheFile, true);
			
			// キャッシュが不整合を起こしていたら投げ捨てる
			Matcher ma = Pattern.compile(String.format("class=\"txt01 display_now\">%d月%d日",month,day)).matcher(response);
			if ( ! ma.find() ) {
				reportProgress(String.format("%s (キャッシュ)が無効です: (%d/%d) %d日[%d/%d] %s",getTVProgramId(),counter,counterMax,day,page,pmax,progCacheFile));
				return;
			}
			
			// 番組リストの追加
			getPrograms(areacode, String.valueOf(page), wdaycol, response);
		}
		catch (Exception e) {
			// 例外
			System.out.println("Exception: _loadProgram()");
			e.printStackTrace();
		}
	}
	
	//
	private void getPrograms(String areacode, String page, int wdaycol, String src) {
		
		HashMap<String,ProgGenre> genres = new HashMap<String, TVProgram.ProgGenre>();
		genres.put("ccccff", ProgGenre.SPORTS);
		genres.put("ccffcc", ProgGenre.MUSIC);
		genres.put("ccffff", ProgGenre.ANIME);
		genres.put("ffcccc", ProgGenre.MOVIE);
		genres.put("ffffcc", ProgGenre.DORAMA);
		genres.put("ffcc99", ProgGenre.VARIETY);
		genres.put("", ProgGenre.NOGENRE);
		
		HashMap<String,Integer> marks = new HashMap<String,Integer>();
		marks.put("26", 0);	// 新番組
		marks.put("30", 1);	// 最終回
		marks.put("29", 2);	// 再放送
							// 初回放送（みつからないよ）
		marks.put("53", 4);	// [PV]ペイパービュー
		marks.put("54", 4);	// [PV]ペイパービュー
		marks.put("17", 5);	// [字]文字多重放送
		marks.put("12", 6);	// [二]二か国語放送
		marks.put("11", 7);	// [多]音声多重放送
		marks.put("52", 8);	// [吹]吹き替え
		marks.put("45", 9);	// [デ]データ放送
		marks.put("49", 10);	// [生]生放送
		marks.put("20", 11);	// - ノースクランブル
		
		// 番組枠の開始位置を決定する
		Matcher ma = Pattern.compile("<th rowspan=\"60\" class=\"txt02\"[\\s\\S]*?>\\s*(\\d+)\\s*</th>").matcher(src);
		if ( ! ma.find() ) {
			return;
		}
		int curHour = Integer.valueOf(ma.group(1));
		if ( CommonUtils.isLateNight(curHour) ) {
			curHour += 24;
		}
		
		ma = Pattern.compile("<td\\s+rowspan=\"(\\d+?)\"\\s+valign=\"top\"\\s+(class=\"frame\"\\s+)?bgcolor=\"#(.*?)\">([\\s\\S]+?)</table>\\s*?</td>").matcher(src);
		while (ma.find()) {
			//
			ProgDetailList pdl = new ProgDetailList();

			int hh = 0;
			int mm = 0;
			
			int plen = pdl.length = Integer.valueOf(ma.group(1));
			
			String bgcolor = ma.group(3);
			
			if ( ma.group(4).indexOf("番組データがありません。") != -1 ) {
				// 詳細がないよ
				pdl.title = "番組情報がありません";
			}
			else {
				Matcher mb = Pattern.compile("<div class=\"program_text_1\">\\s*(\\d+):(\\d+)\\s*</div>").matcher(ma.group(4));
				if ( ! mb.find()) {
					continue;
				}
				
				// 開始・終了時刻
				{
					hh = Integer.valueOf(mb.group(1));
					mm  = Integer.valueOf(mb.group(2));
					pdl.start = CommonUtils.getTime(hh, mm);
					
					int nn = mm + pdl.length;
					mm = nn % 60;
					hh = (hh + (nn-mm)/60) % 24;
					pdl.end = CommonUtils.getTime(hh, mm);
				}
				
				mb = Pattern.compile("javascript:popup\\('(.+?)'\\);return false;\">([\\s\\S]*?)</A>").matcher(ma.group(4));
				if ( ! mb.find()) {
					continue;
				}
				
				// リンク
				pdl.link = "http://www.tvguide.or.jp/"+mb.group(1);
				
				// タイトル
				pdl.title = mb.group(2).replaceAll("<br>", "");
				
				// ジャンル
				{
					if (genres.containsKey(bgcolor)) {
						pdl.genre = genres.get(bgcolor);
					}
					else {
						System.err.println("unexpected genre code: "+bgcolor);
						pdl.genre = ProgGenre.NOGENRE;
					}
				}
				
				mb = Pattern.compile("<div class=\"program_text_3\">(.*?)</div>").matcher(ma.group(4));
				if ( ! mb.find()) {
					continue;
				}
				
				// 詳細
				pdl.detail = mb.group(1).replaceAll("<br>", "\n").trim();
				
				// 各種マーク
				mb = Pattern.compile("/image/icon/(\\d+?)\\.gif").matcher(ma.group(4));
				while (mb.find()) {
					if (marks.containsKey(mb.group(1))) {
						switch (marks.get(mb.group(1))) {
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
						case 10:
							pdl.addOption(ProgOption.LIVE);
							break;
						case 11:
							pdl.noscrumble = ProgScrumble.NOSCRUMBLE;
							break;
						}
					}
				}
					
				// タイトルから各種フラグを分離する
				doSplitFlags(pdl, nf);
				
				// サブタイトル分離
				doSplitSubtitle(pdl);
			}

			// 挿入位置
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

			// 挿入…
			ProgDateList pcl = newplist.get(col).pdate.get(wdaycol);
			
			// １つめの番組は状態によって修正が必要
			if ( pcl.pdetail.size() == 0 && curHour != getTimeBarStart() ) {
				if ( pdl.start.length() == 0 ) {
					// 番組情報がない、は伸ばすだけでよい
					pdl.length += (curHour-getTimeBarStart())*60;
				}
				else {
					// なにかの番組情報
					if ( curHour >= 24 ) {
						if ( CommonUtils.isLateNight(hh) ) {
							hh += 24;
						}
					}
					int ttop = getTimeBarStart() * 60;
					int tcur = curHour*60;
					int tstart = hh*60+mm;
					if ( tstart <= ttop ) {
						// 日をまたぐ番組()
						pdl.length += (tcur-ttop);	// 先頭時刻～現在時刻までのスペース分上に伸ばす
					}
					else {
						// 直前までの情報がない番組
						{
							// ダミー挿入
							ProgDetailList NullPdl = new ProgDetailList();
							NullPdl.title = "番組情報がありません";
							NullPdl.length = (tstart-ttop);	// 先頭時刻～開始時刻までのスペースを確保
							pcl.pdetail.add(NullPdl);
						}
						pdl.length += (tcur-tstart);	// 開始時刻～現在時刻までのスペース分上に伸ばす
					}
				}
			}
			
			// 挿入
			pcl.pdetail.add(pdl);
			pcl.row += plen;
		}
	}
	


	/*******************************************************************************
	 * 地域情報を取得する
	 ******************************************************************************/
	
	//
	@Override
	public String getDefaultArea() {return "東京";}
	
	//
	public void loadAreaCode(){
		
		// 設定ファイルが存在していればファイルから
		File f = new File(getAreaSelectedFile());
		if (f.exists() == true) {
			@SuppressWarnings("unchecked")
			ArrayList<AreaCode> tmp = (ArrayList<AreaCode>) CommonUtils.readXML(getAreaSelectedFile());
			if ( tmp != null ) {
				
				aclist = tmp;
				
				// 後方互換
				for ( AreaCode ac : aclist ) {
					String[] aid = ac.getCode().split(",",2);
					ac.setCode(aid[0]);
				}

				return;
			}
			else {
				System.err.println(ERRID+"地域リストの読み込みに失敗しました: "+getAreaSelectedFile());
			}
		}
		
		// 地域一覧の作成
		ArrayList<AreaCode> newaclist = new ArrayList<AreaCode>();

		// 存在していなければWeb上から
		String uri = "http://www.tvguide.or.jp/TF1101LS.php";
		String response = webToBuffer(uri,thisEncoding,true);
		if ( response == null ) {
			System.out.println(ERRID+"地域情報の取得に失敗しました: "+uri);
			return;
		}
		
		Matcher ma = Pattern.compile("<select name=\"area\"([\\s\\S]+?)</select>").matcher(response);
		if ( ma.find() ) {
			Matcher mb = Pattern.compile("&regionId=(.+?)\" (selected)?>(.+?)</option>").matcher(ma.group(1));
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


	/*******************************************************************************
	 * 放送局情報を取得する
	 ******************************************************************************/

	// 設定ファイルがなければWebから取得
	public void loadCenter(String code, boolean force) {
		
		if ( code == null ) {
			System.out.println(ERRID+"地域コードがnullです.");
			return;
		}
		
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
				
				System.out.println(MSGID+"放送局リストを読み込みました: "+centerListFile);
				return;
			}
			else {
				System.out.println(MSGID+"放送局リストの読み込みに失敗しました: "+centerListFile);
			} 
		}
		
		// 放送局をつくるよ
		ArrayList<Center> newcrlist = new ArrayList<Center>();
		
		// 地上派・UHFは地域別に扱う
		
		int cntMax = ((code.equals(allCode))?(aclist.size()-2):(1)) + 1;
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

			// 地上波
			String url = "http://www.tvguide.or.jp/TF1101LS.php?regionId="+ac.getCode()+"&mediaId=1";
			if ( _loadCenter(newcrlist, ac.getCode(), url) ) {
				reportProgress(MSGID+"放送局情報を取得しました: ("+cnt+"/"+cntMax+") "+url);
			}
			cnt++;
		}
		
		// BS1・BS2は共通にする
		
		{
			// BSデジタル
			String url = "http://www.tvguide.or.jp/TF1101LS.php?mediaId=2";
			if ( _loadCenter(newcrlist, bsCode, url) ) {
				reportProgress(MSGID+"放送局情報を取得しました: ("+cnt+"/"+cntMax+") "+url);
			}
			cnt++;
		}
		
		if ( newcrlist.size() == 0 ) {
			System.err.println(ERRID+"放送局情報の取得結果が０件だったため情報を更新しません");
			return;
		}

		crlist = newcrlist;
		attachChFilters();	// 放送局名変換
		saveCenter();
	}
		
	private boolean _loadCenter(ArrayList<Center> newcrlist, String code, String uri) {
		
		String response = webToBuffer(uri,null,null,null,thisEncoding,true);
		if ( response == null ) {
			System.out.println(ERRID+"放送局情報の取得に失敗しました: "+uri);
			return false;
		}
		
		// 局名リストに追加する

		Matcher ma = Pattern.compile("(<select name=\"ch\"[\\s\\S]+?</select>)").matcher(response);
		if ( ma.find() ) {
			Matcher mb = Pattern.compile("&stationId=(.+?)&page=(.+?)\" (selected)?>([\\s\\S]+?)</option>").matcher(ma.group(1));
			while ( mb.find() ) {
				String centerName = CommonUtils.unUniEscape(mb.group(4));
				String centerId = mb.group(1);
				String page = mb.group(2);
				
				// NHK総合・NHK教育
				centerName = centerName.replaceFirst("^NHK総合", "ＮＨＫ総合");
				centerName = centerName.replaceFirst("^NHK Eテレ", "ＮＨＫ Ｅテレ");
				if ( ! code.startsWith(bsCode)) {
					if (centerName.startsWith("ＮＨＫ")) {
						centerName = centerName.replaceFirst("・.*$", "");
						centerName = centerName+"・"+getArea(code);
					}
				}
				else {
					if (centerName.equals("放送大学")) {
						centerName = "放送大学BS1";
					}
					else if (centerName.equals("放送大学2")) {
						centerName = "放送大学BS2";
					}
					else if (centerName.equals("放送大学3")) {
						centerName = "放送大学BS3";
					}
				}
				
				Center cr = new Center();
				cr.setAreaCode(code);
				cr.setCenterOrig(centerName);
				cr.setLink(centerId);
				cr.setType(page);
				cr.setEnabled(true);
				newcrlist.add(cr);
			}
		}
		
		return true;
	}

}
