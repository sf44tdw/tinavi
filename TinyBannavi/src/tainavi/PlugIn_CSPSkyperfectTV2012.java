package tainavi;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlugIn_CSPSkyperfectTV2012 extends TVProgramUtils implements TVProgram,Cloneable {

	public PlugIn_CSPSkyperfectTV2012 clone() {
		return (PlugIn_CSPSkyperfectTV2012) super.clone();
	}
	
	private static final String thisEncoding = "UTF-8";
	
	
	/*******************************************************************************
	 * 種族の特性
	 ******************************************************************************/
	
	@Override
	public String getTVProgramId() { return "スカパー！"; }
	
	@Override
	public ProgType getType() { return ProgType.PROG; }
	@Override
	public ProgSubtype getSubtype() { return ProgSubtype.CS; }
	
	// エリア選択はない
	@Override
	public boolean isAreaSelectSupported() { return false; }

	/*******************************************************************************
	 * 個体の特性
	 ******************************************************************************/
	
	@Override
	public int getTimeBarStart() {return 5;}
	
	private int getDogDays() { return ((getExpandTo8())?(8):(7)); }
	
	
	/*******************************************************************************
	 * 定数
	 ******************************************************************************/
	
	private static final String XTYPE_BASIC = "e2";
	private static final String XTYPE_PREMIUM = "HD";
	
	private static final String CHNM_PREFIX_BS = "BS";
	private static final String CHNM_PREFIX_CS = "CS";
	private static final String CHNM_PREFIX_PR = "Ch.";
	
	private static final String CHID_PREFIX_BS = "BS";
	private static final String CHID_PREFIX_CS = "CS";
	private static final String CHID_PREFIX_PR = "HD";
	
	private final String MSGID = "["+getTVProgramId()+"] ";
	private final String ERRID = "[ERROR]"+MSGID;
	private final String DBGID = "[DEBUG]"+MSGID;
	
	
	/*******************************************************************************
	 * 部品
	 ******************************************************************************/
	
	protected ArrayList<ProgList> newplist;
	
	private HashMap<String,String> nf = null;
	private HashMap<String,String> gf = null;
	
	
	//
	private static final HashMap<String,ProgGenre> genremap = new HashMap<String, TVProgram.ProgGenre>() {

		private static final long serialVersionUID = 1L;

		{
			put("アニメ／特撮",ProgGenre.ANIME);
			put("こども向け／教育",ProgGenre.ANIME);
			
			put("映画",ProgGenre.MOVIE);
			put("洋画",ProgGenre.MOVIE);
			put("邦画",ProgGenre.MOVIE);
			
			put("ドラマ／演劇",ProgGenre.DORAMA);
			put("ドラマ",ProgGenre.DORAMA);
			
			put("劇場／公演",ProgGenre.THEATER);
			
			put("ドキュメンタリー／教養",ProgGenre.DOCUMENTARY);

			put("音楽",ProgGenre.MUSIC);
			
			put("バラエティー",ProgGenre.VARIETYSHOW);
			put("バラエティ",ProgGenre.VARIETYSHOW);
			
			put("スポーツ",ProgGenre.SPORTS);
			
			put("ニュース／報道",ProgGenre.NEWS);
			
			put("アダルト",ProgGenre.NOGENRE);
		}
	};
	
	//
	@Override
	public void loadProgram(String areaCode, boolean force) {
		// 新しい入れ物（トップ）を用意する
		newplist = new ArrayList<ProgList>();
		
		nf = new HashMap<String, String>();
		gf = new HashMap<String, String>();
		
		// 最終日の24時以降の情報は、＋１したとこから取得する
		int counterMax = getSortedCRlist().size() * (getDogDays()+1);
		int counter=1;
		for ( Center c : getSortedCRlist() ) {
			if (getDebug()) System.err.println(DBGID+"load program: "+c.getCenter());
			_loadProgram(c, force, counter, counterMax);
			counter += getDogDays()+1;
		}
		
		// 古いデータから補完できないかな？
		CompensatesPrograms(newplist);
		
		// 解析用
		{
			for ( String f : nf.keySet() ) {
				System.err.println(String.format("【デバッグ情報】未定義のフラグです: [%s]",f));
			}
			for ( String g : gf.keySet() ) {
				System.err.println(String.format("【デバッグ情報】未定義のジャンルです: [%s]",g));
			}
		}
		
		// 古い番組データを置き換える
		pcenter = newplist;
	}
	
	/* ここまで */

	
	
	/*
	 * 非公開メソッド
	 */
	protected void _loadProgram(Center cr, boolean force, int counter, int counterMax) {
		//
		try {
			//　progfilesの読み出し
			
			// 局リストの追加
			ProgList pl = new ProgList();
			pl.Center = cr.getCenter();
			pl.CenterId = cr.getLink();
			pl.ChId = "";
			{
				int onid = -1;
				if ( cr.getCenterOrig().startsWith(CHNM_PREFIX_BS) ) {
					onid = 4;
				}
				else if ( cr.getCenterOrig().startsWith(CHNM_PREFIX_CS) ) {
					onid = 7;
				}
				if ( onid != -1 ) {
					Matcher ma = Pattern.compile("(\\d+)").matcher(cr.getCenterOrig());
					if ( ma.find() ) {
						ContentIdDIMORA.decodeChId(String.format("%04X%04X%04X", onid,0,Integer.valueOf(ma.group(1))));
						pl.ChId = ContentIdDIMORA.getContentId(0,"");
					}
				}
			}
			pl.Area = cr.getAreaCode();
			pl.BgColor = cr.getBgColor();
			pl.enabled = true;
			newplist.add(pl);
			
			// 日付リストの追加
			getDate(pl);

			//
			for ( int i=0; i<pl.pdate.size(); i++ ) {
				//
				GregorianCalendar cal = CommonUtils.getCalendar(pl.pdate.get(i).Date);
				final String progCacheFile = String.format("%s%sSKP2012_%s_%d.txt", getProgDir(), File.separator, pl.CenterId, cal.get(Calendar.DAY_OF_MONTH));
				//
				File f = new File(progCacheFile);
				if (force == true ||
						(f.exists() == true && isCacheOld(progCacheFile) == true) ||
						(f.exists() == false && isCacheOld(null) == true)) {
					//
					String xtype = (pl.CenterId.startsWith(CHID_PREFIX_BS) || pl.CenterId.startsWith(CHID_PREFIX_CS)) ? XTYPE_BASIC : XTYPE_PREMIUM;
					String chid = xtype != XTYPE_PREMIUM ? pl.CenterId : pl.CenterId.replaceFirst("^"+CHID_PREFIX_PR, "");
					String dt = CommonUtils.getDateYMD(cal);
					String url = "http://bangumi.skyperfectv.co.jp/"+xtype+"/channel:"+chid+"/date:"+dt.substring(2)+"/";
					/*
					if ( pl.ChId.length() == 0 ) {
						url = "http://bangumi.skyperfectv.co.jp/api/version:3/search/date:"+dt.substring(2)+"/channel:"+pl.CenterId+"/?api_key=336eec3423";
					}
					else {
						url = "http://www.skyperfectv.co.jp/xml/"+dt+"_"+pl.ChId.substring(2)+".xml";
					}
					*/
					webToFile(url, progCacheFile, thisEncoding);
					
					reportProgress(getTVProgramId()+"(オンライン)を取得しました: ("+(counter+i)+"/"+counterMax+") "+pl.Center+"["+cal.get(Calendar.DAY_OF_MONTH)+"日]    "+url);
				}
				else if (CommonUtils.isFileAvailable(f,10)) {
					reportProgress(getTVProgramId()+"(キャッシュ)を取得しました: ("+(counter+i)+"/"+counterMax+") "+pl.Center+"["+cal.get(Calendar.DAY_OF_MONTH)+"日]    "+progCacheFile);
				}
				else {
					reportProgress(getTVProgramId()+"(キャッシュ)がみつかりません: ("+(counter+i)+"/"+counterMax+") "+pl.Center+"["+cal.get(Calendar.DAY_OF_MONTH)+"日]    "+progCacheFile);
					continue;
				}
	
				String response = CommonUtils.read4file(progCacheFile, true);
				
				// 番組リストの追加
				try {
					getPrograms(pl, i, response);
					/*
					if ( pl.ChId.length() == 0 ) {
						getPrograms(pl, i, response);
					}
					else {
						getPrograms_basic(pl, i, response);
					}
					*/
				}
				catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			
			// 日付の調整
			refreshList(pl.pdate);
		}
		catch (Exception e) {
			// 例外
			System.out.println("Exception: _loadProgram()");
		}
	}
	
	//
	private void getDate(ProgList pl) {
		// 日付の処理
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		if ( CommonUtils.isLateNight(c) ) {
			// ４時までは当日扱いにする
			c.add(Calendar.DATE, -1);
		}
		// 最終日の24時以降の情報は、＋１したとこから取得する
		for ( int i=0; i<getDogDays()+1; i++ ) {
			ProgDateList cl = new ProgDateList();
			cl.Date = CommonUtils.getDate(c);
			pl.pdate.add(cl);
			
			c.add(Calendar.DATE,1);
		}
	}
	
	//
	private void refreshList(ArrayList<ProgDateList> pcenter) {
		// 前日分の情報は前日のリストに入れ替える
		for ( int i=0; i<pcenter.size(); i++ ) {
			ProgDateList pl = pcenter.get(i);
			ArrayList<ProgDetailList> pre = new ArrayList<ProgDetailList>();
			ArrayList<ProgDetailList> cur = new ArrayList<ProgDetailList>();
			for ( ProgDetailList pdl : pl.pdetail ) {
				if ( pl.Date.compareTo(pdl.accurateDate) >= 0 && CommonUtils.isLateNight(pdl.start.substring(0, 2)) ) {
					// 前日分とする
					pre.add(pdl);
				}
				else {
					// 当日分とする
					cur.add(pdl);
				}
			}
			if ( i > 0 ) {
				// 前日のリストに混ぜる
				for ( ProgDetailList pdl : pre ) {
					pcenter.get(i-1).pdetail.add(pdl);
				}
			}
			String tbstart = CommonUtils.getTime(getTimeBarStart(),0);
			if ( pre.size() > 0 && pre.get(pre.size()-1).end.compareTo(tbstart) > 0 ) {
				if (getDebug()) System.err.println(DBGID+"overlap "+pre.get(pre.size()-1).end+" "+pre.get(pre.size()-1).title);
				// 日またがり（２９時またがり）のものを当日のリストにもコピーする
				ProgDetailList pdl = pre.get(pre.size()-1).clone();
				pdl.length = (int)(CommonUtils.getDiffDateTime(pdl.accurateDate+" "+tbstart, pdl.accurateDate+" "+pdl.end)/60000L);
				cur.add(0,pdl);
			}
			pl.pdetail = cur;
		}
		// 隙間を埋める
		for ( ProgDateList pl : pcenter ) {
			ArrayList<ProgDetailList> cur = new ArrayList<ProgDetailList>();
			String preend = pl.Date.substring(0,10)+" "+CommonUtils.getTime(getTimeBarStart(),0);	// 最初の"前番組のおしり"は05:00
			for ( int i=0; i<pl.pdetail.size(); i++ ) {
				ProgDetailList pdl = pl.pdetail.get(i);
				if ( preend.compareTo(pdl.startDateTime) < 0 ) {
					// 前の番組との間に隙間があれば埋める
					ProgDetailList npdl = new ProgDetailList();
					npdl.title = npdl.splitted_title = "番組情報がありません";
					npdl.length = (int)(CommonUtils.getDiffDateTime(preend, pdl.startDateTime)/60000L);
					cur.add(npdl);
				}
				cur.add(pdl);
				preend = pdl.endDateTime;
			}
			pl.pdetail = cur;
		}
		
		// 24時以降の情報は日付＋１したとこから取得しているので、最終日の次のリストは削除
		pcenter.remove(pcenter.size()-1);
		
		// 総時間数等を整理する
		for ( ProgDateList pl : pcenter ) {
			// １日の合計分数を足し合わせる
			pl.row = 0;
			for ( ProgDetailList pdl : pl.pdetail ) {
				pl.row += pdl.length;
			}
			// おしりがとどかない場合（デメリット：これをやると、サイト側のエラーで欠けてるのか、そもそも休止なのかの区別がつかなくなる）
			if ( pl.row < 24*60 ) {
				ProgDetailList npdl = new ProgDetailList();
				npdl.title = npdl.splitted_title = "番組情報がありません";
				npdl.length = 24*60 - pl.row;
				pl.pdetail.add(npdl);
				pl.row += npdl.length;
			}
		}
	}
	
	//
	private void getPrograms(ProgList pl, int dtidx, String response) {
		
		ProgDateList pcl = pl.pdate.get(dtidx);
		
		String[][] keys = {
				{ "class", "pg-title" },
				{ "class", "start-time" },
				{ "class", "end-time" },
				{ "class", "pg-genre" },
				{ "class", "pg-explanation" },
				{ "id", "actor-name" },
		};
		
		Matcher ma = Pattern.compile("<tbody\\s+id=\"event-\\d+\"[^>]*?>(.+?)</tbody>",Pattern.DOTALL).matcher(response);
		for ( int cnt = 0; ma.find(); cnt++ ) {
			ProgDetailList pdl = new ProgDetailList();
			String subtitle = "";
			String person = "";
				
			for ( String[] k : keys ) {
				Matcher mb = Pattern.compile("<span\\s+"+k[0]+"=\""+k[1]+"\"[^>]*?>\\s*(.+?)\\s*</span>",Pattern.DOTALL).matcher(ma.group(1));
				while ( mb.find() ) {
					if ( mb.group(1) == null ) {
						continue;
					}
					
					if ( k[1].equals("pg-title") ) {
						pdl.title = CommonUtils.unEscape(mb.group(1)).trim();
					}
					else if ( k[1].equals("start-time") ) {
						pdl.start = mb.group(1);
					}
					else if ( k[1].equals("end-time") ) {
						pdl.end = mb.group(1);
						
						GregorianCalendar c = CommonUtils.getCalendar(pcl.Date);
						
						if ( cnt == 0 && pdl.start.compareTo(pdl.end) > 0 ) {
							c.add(Calendar.DATE, -1);
						}
						pdl.accurateDate = CommonUtils.getDate(c);
						pdl.startDateTime = CommonUtils.getDate(c,false)+" "+pdl.start;
						
						if ( pdl.start.compareTo(pdl.end) > 0 ) {
							c.add(Calendar.DATE, 1);
						}
						pdl.endDateTime = CommonUtils.getDate(c,false)+" "+pdl.end;
	
						pdl.length = CommonUtils.getRecMinVal(pdl.start, pdl.end);
					}
					else if ( k[1].equals("pg-genre") ) {
						Matcher mc = Pattern.compile("/large_genre:(.+?)/medium_genre:(.+?)/",Pattern.DOTALL).matcher(mb.group(1));
						if ( mc.find() ) {
							try {
								String grstr = URLDecoder.decode(mc.group(1),"utf8").replaceAll("／", "/");
								ProgGenre gr = ProgGenre.get(grstr);
								if ( gr == null ) {
									gr = genremap.get(grstr);
									if (gr == null) {
										// 未定義のジャンルです！
										gr = ProgGenre.NOGENRE;
										gf.put(grstr,null);
									}
								}
								if ( pdl.genre == null || (pdl.genre == ProgGenre.NOGENRE && gr != ProgGenre.NOGENRE) ) {
									pdl.genre = gr;
								}
								
								String sgstr = URLDecoder.decode(mc.group(2),"utf8").replaceAll("ィー", "ィ");
								ProgSubgenre sg = ProgSubgenre.get(gr, sgstr);
								if ( sg == null ) {
									// 未定義のサブジャンルです！
									ArrayList<ProgSubgenre> vals = ProgSubgenre.values(gr);
									sg = vals.get(vals.size()-1);
								}
								pdl.subgenre = sg;
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
								pdl.genre = ProgGenre.NOGENRE;
								pdl.subgenre = ProgSubgenre.NOGENRE_ETC;
							}
						}
						else {
							pdl.genre = ProgGenre.NOGENRE;
							pdl.subgenre = ProgSubgenre.NOGENRE_ETC;
						}
					}
					else if ( k[1].equals("pg-explanation") ) {
						pdl.detail += CommonUtils.decBr(CommonUtils.unEscape(mb.group(1))).trim()+"\n";
					}
					else if ( k[1].equals("actor-name") ) {
						person += "、"+CommonUtils.unEscape(mb.group(1)).trim();
					}
				}
			}
			
			// 出演者
			if ( person.length() > 0 ) {
				person = person.substring(1);
			}
			
			// くっつけてみる
			pdl.detail =
					((subtitle.length()>0)?(subtitle+DETAIL_SEP):(""))
					+pdl.detail
					+((person.length()>0)?(DETAIL_SEP+person):(""));
			pdl.detail = pdl.detail.replaceFirst("[\r\n]+$", "");

			Matcher mb = Pattern.compile("<img\\s+src=\"/i/icon_(.+?)\\.gif",Pattern.DOTALL).matcher(ma.group(1));
			while ( mb.find() ) {
				if ( mb.group(1).equals("5.1") ) {
					pdl.addOption(ProgOption.SURROUND);
				}
				else if ( mb.group(1).equals("jimaku") ) {
					pdl.addOption(ProgOption.SUBTITLE);
				}
				else if ( mb.group(1).equals("2kakoku") ) {
					pdl.addOption(ProgOption.BILINGUAL);
				}
				else if ( mb.group(1).equals("fukikae") ) {
					pdl.addOption(ProgOption.STANDIN);
				}
				else if ( mb.group(1).equals("tajuu") ) {
					pdl.addOption(ProgOption.MULTIVOICE);
				}
				else if ( mb.group(1).equals("r15") || mb.group(1).equals("r18") || mb.group(1).equals("adult") ) {
					pdl.addOption(ProgOption.RATING);
				}
				else if ( mb.group(1).equals("ppv") ) {
					pdl.addOption(ProgOption.PV);
				}
				else if ( mb.group(1).equals("nama") ) {
					pdl.addOption(ProgOption.LIVE);
				}
				else {
					nf.put(mb.group(1), null);
				}
			}
			
			// タイトルから各種フラグを分離する
			doSplitFlags(pdl, nf);
			
			// サブタイトル分離（ポインタを活用してメモリを節約する）
			doSplitSubtitle(pdl);
			
			// 番組ID
			if ( ContentIdDIMORA.isValid(pl.ChId) ) {
				pdl.progid = pl.ChId;
			}
			
			// その他フラグ
			pdl.extension = false;
			pdl.nosyobo = false;
			
			pcl.pdetail.add(pdl);
			
			if (getDebug()) System.err.println(DBGID+"program: "+pdl.startDateTime+" - "+pdl.endDateTime+" "+pdl.length+"m "+pdl.noscrumble+" "+pdl.title);
		}
	}

	
	/**
	 * こちらは番組IDがとれる代わりに出演者情報がとれなくなるので保留とする
	 */
	private void getPrograms_basic(ProgList pl, int dtidx, String response) {
		
		Matcher ma = Pattern.compile("<SIInformation(.+?)</SIInformation>",Pattern.DOTALL).matcher(response);
		while ( ma.find() ) {
			Matcher mb = Pattern.compile("eventId=\"(.+?)\".+?broadCastStartDate=\"(\\d{8})(\\d{4})",Pattern.DOTALL).matcher(ma.group(1));
			if ( mb.find() ) {
				
			}
			/*
			mb = Pattern.compile("<(.+?)>(.+?)</\\1>",Pattern.DOTALL).matcher(ma.group(1));
			while ( mb.find() ) {
				if ( mb.group(1).equals("ChannelName") ) {
					
				}
				else if ( mb.group(1).equals("Title") ) {
					
				}
				else if ( mb.group(1).equals("Synopsis") ) {
					
				}
				else if ( mb.group(1).equals("Genres") ) {
					Matcher mc = Pattern.compile("<Genre majorGenreId=\".+?\" minorGenreId=\".+?\"",Pattern.DOTALL).matcher(mb.group(2));
					while ( mc.find() ) {
						
					}
				}
			}
			*/
		}
	}

	
	/*
	 * ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
	 * ★★★★★　放送地域を取得する（TVAreaから降格）－ここから　★★★★★
	 * ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
	 */
	
	/*
	 * 公開メソッド
	 */
	
	@Override
	public String getDefaultArea() {return "全国";}
	private String getDefaultCode() {return "SKP2012";}
	
	@Override
	public void loadAreaCode() {
		aclist = new ArrayList<AreaCode>();
		AreaCode ac = new AreaCode();
		ac.setArea(getDefaultArea());
		ac.setCode(getDefaultCode());
		ac.setSelected(true);
		aclist.add(ac);
	}
	@Override
	public void saveAreaCode() {}
	
	@Override
	public String getArea(String code) { return(getDefaultArea()); }
	@Override
	public String getCode(String area) { return(getDefaultCode()); }
	@Override
	public String setSelectedAreaByName(String area) { return(getDefaultCode()); }
	@Override
	public String getSelectedArea() { return(getDefaultArea()); }
	@Override
	public String getSelectedCode() { return(getDefaultCode()); }
	
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
		
		String centerListFile = getCenterListFile(getTVProgramId(), code);
		
		if (force) {
			File f = new File(centerListFile);
			f.delete();
		}
		
		File f = new File(centerListFile);
		if (f.exists() == true) {
			@SuppressWarnings("unchecked")
			ArrayList<Center> tmp = (ArrayList<Center>)CommonUtils.readXML(centerListFile);
			if ( tmp != null ) {
				crlist = tmp;

				String[][] prs = {
						{CHNM_PREFIX_BS, CHID_PREFIX_BS},
						{CHNM_PREFIX_CS, CHID_PREFIX_CS},
						{CHNM_PREFIX_PR, CHID_PREFIX_PR},
				};
				
				// 2013.11 新旧変換
				for ( Center cr : crlist ) {
					for ( String[] a : prs ) {
						if ( cr.getCenterOrig().startsWith(a[0]) ) {
							if ( ! cr.getLink().startsWith(a[1]) ) {
								Matcher ma = Pattern.compile("^.*?(\\d+)",Pattern.DOTALL).matcher(cr.getCenterOrig());
								if ( ma.find() ) {
									String chid = a[1]+ma.group(1);
									System.err.println(DBGID+"converted: "+cr.getCenterOrig()+", "+cr.getLink()+" -> "+chid);
									cr.setLink(chid);
								}
							}
							
							break;
						}
					}
				}
				
				// 放送局名変換
				attachChFilters();
				System.out.println("放送局リストを読み込みました: "+centerListFile);
				return;
			}
			else {
				System.out.println("放送局リストの読み込みに失敗しました: "+centerListFile);
			}
		}

		// 放送局をつくるよ
		ArrayList<Center> newcrlist = new ArrayList<Center>();
		
		for ( String xtype : new String[] { XTYPE_BASIC,XTYPE_PREMIUM } ) {
			ArrayList<Center> crl = getCenters(xtype,code);
			if ( crl != null ) {
				reportProgress("放送局情報を取得しました: ("+xtype+") "+crl.size()+"ch");
			}
			
			for ( Center cr : crl ) {
				newcrlist.add(cr);
			}
		}
		
		if ( newcrlist.size() == 0 ) {
			System.err.println(ERRID+"放送局情報の取得結果が０件だったため情報を更新しません");
			return;
		}
		
		crlist = newcrlist;
		attachChFilters();	// 放送局名にフィルタをかける
		saveCenter();
	}
	
	private ArrayList<Center> getCenters(String xtype, String areacode) {
		
		ArrayList<Center> crl = new ArrayList<Center>();
		
		String url = String.format("http://bangumi.skyperfectv.co.jp/index/channel/%s/",xtype);
		if (getDebug()) System.err.println(DBGID+"get page: "+url);
		String response = webToBuffer(url,thisEncoding,true);
		if ( response == null ) {
			reportProgress("放送局情報の取得に失敗しました: "+xtype);
			return null;
		}
		
		Matcher ma = Pattern.compile("<td class=\"channel-icon\">\\s*<a href=\".*?/channel:(.+?)/\".*?>\\s*<img src=\".*?\" alt=\"(.+?)\"",Pattern.DOTALL).matcher(response);
		while ( ma.find() ) {
			String chid = (xtype.equals(XTYPE_PREMIUM) ? CHID_PREFIX_PR : "") + ma.group(1);
			String chnm = CommonUtils.toHANALNUM(CommonUtils.unEscape(ma.group(2))).replaceFirst("[ 　\\t]+▲$", "");
			
			// 統一性がない
			if ( xtype.equals(XTYPE_PREMIUM) && ! chnm.startsWith(CHNM_PREFIX_PR) ) {
				chnm = CHNM_PREFIX_PR+chnm;
			}
			
			Center cr = new Center();
			cr.setAreaCode(areacode);
			cr.setType("");
			cr.setEnabled(true);
			cr.setCenterOrig(chnm);
			cr.setLink(chid);
			
			int idx = 0;
			for ( Center ct : crl ) {
				if ( ct.getCenterOrig().compareTo(cr.getCenterOrig()) > 0 ) {
					break;
				}
				idx++;
			}
			crl.add(idx, cr);
			
			if (getDebug()) System.err.println(DBGID+"center: "+cr.getCenterOrig()+", "+cr.getLink());
		}
		
		return crl;
	}
	
	/*
	 * ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
	 * ★★★★★　放送局を選択する（TVCenterから降格）－ここまで　★★★★★
	 * ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
	 */
}
