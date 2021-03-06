
package tainavi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * <P>頻繁に使用する雑多なメソッドをstaticで提供します。
 * <P>パッケージを小分けにしておけばよかった…
 */
public class CommonUtils {

	/*******************************************************************************
	 * CommonUtilsの動作設定
	 ******************************************************************************/

	/**
	 * デバッグログ出力するかどうか
	 */
	public static void setDebug(boolean b) { debug = b; }
	private static boolean debug = false;
	private static boolean debuglv2 = false;	// これは俺用
	
	/**
	 *  深夜の帯予約補正に対応するかどうか
	 */
	public static void setAdjLateNight(boolean b) { adjLateNight = b; }
	private static boolean adjLateNight = false;
	
	/**
	 * Web番組表の８日分取得に対応するかどうか
	 */
	public static void setExpandTo8(boolean b) { dogDays = ((b)?(8):(7)); }
	private static int dogDays = 7;
	
	/**
	 * 過去予約の表示に対応するかどうか
	 */
	public static void setDisplayPassedReserve(boolean b) { displayPassedReserve = b; }
	private static boolean displayPassedReserve = false;
	
	/**
	 * Windows環境下で、アプリケーションに関連付けられたファイルを開くのにrundll32.exeを利用するかどうか
	 */
	public static void setUseRundll32(boolean b) { useRundll32 = b; }
	private static boolean useRundll32 = false;
	
	
	/*******************************************************************************
	 * 日付時刻関連
	 ******************************************************************************/
	
	public static final String[] WDTPTN = {"日","月","火","水","木","金","土"};
	
	/**
	 * 曜日→wday
	 */
	public static int getWday(String s) {
		if ( s == null || s.length() == 0 ) {
			return 0;
		}
		
		String x = s.substring(0,1);
		for ( int i=0; i<WDTPTN.length; i++ ) {
			if ( WDTPTN[i].equals(x) ) {
				return i+1;
			}
		}
		return 0;
	}
	
	/**
	 * 月日に年を補完
	 */
	public static String getDateByMD(int m, int d, int wday, boolean addwstr) {
		return getDate(getCalendarByMD(m,d,wday),addwstr);
	}
	
	/**
	 * 月日に年を補完
	 */
	public static GregorianCalendar getCalendarByMD(int m, int d, int wday) {
		GregorianCalendar c = new GregorianCalendar(
				Calendar.getInstance().get(Calendar.YEAR),
				m-1,
				d
				);
		
		if ( c.get(Calendar.DAY_OF_WEEK) == wday ) {
			// 当年
			return c;
		}
		
		c.add(Calendar.YEAR, 1);
		if ( c.get(Calendar.DAY_OF_WEEK) == wday ) {
			// 翌年
			return c;
		}
		
		return null;
	}

	
	/**
	 * 深夜帯(24:00～28:59)かどうか判定する
	 */
	public static boolean isLateNight(GregorianCalendar A) {
		return isLateNight(A.get(Calendar.HOUR_OF_DAY));
	}
	
	/**
	 * 深夜帯(24:00～28:59)かどうか判定する
	 */
	public static boolean isLateNight(String Ahh) {
		return ("00".compareTo(Ahh) <= 0 && "05".compareTo(Ahh) > 0);
	}
	
	/**
	 * 深夜帯(24:00～28:59)かどうか判定する
	 */
	public static boolean isLateNight(int Ahh) {
		return (Ahh >= 0 && Ahh < 5);
	}
	
	/**
	 *  開始・終了時刻から長さを算出する。引数の前後関係は意識しなくて良い。
	 */
	public static String getRecMin(GregorianCalendar ca, GregorianCalendar cz) {
		return String.valueOf(getRecMinVal(ca, cz));
	}
	
	/**
	 *  開始・終了時刻から長さを算出する。引数の前後関係は意識しなくて良い。
	 */
	public static String getRecMin(String start, String end) {
		return String.valueOf(getRecMinVal(start,end));
	}

	public static String getRecMin(String ahhStr,String ammStr,String zhhStr,String zmmStr)	{
		return String.valueOf(getRecMinVal(ahhStr,ammStr,zhhStr,zmmStr));
	}

	/**
	 *  開始・終了時刻から長さを算出する。引数の前後関係は意識しなくて良い。
	 */
	public static long getRecMinVal(GregorianCalendar ca, GregorianCalendar cz)	{
		return getDiffDateTime(ca, cz)/60000L;
	}
	
	/**
	 * 開始・終了時刻から長さを算出する。引数の前後関係は意識しなくて良い。
	 * @param start hh:mm
	 * @param end hh:mm
	 */
	public static int getRecMinVal(String start, String end) {
		Matcher ma = Pattern.compile("(\\d\\d):(\\d\\d)").matcher(start);
		Matcher mb = Pattern.compile("(\\d\\d):(\\d\\d)").matcher(end);
		if ( ! ma.find() || ! mb.find()) {
			return 0;
		}
		return getRecMinVal(ma.group(1),ma.group(2),mb.group(1),mb.group(2));
	}
	
	/**
	 *  開始・終了時刻から長さを算出する。引数の前後関係は意識しなくて良い。
	 */
	public static int getRecMinVal(String ahhStr,String ammStr,String zhhStr,String zmmStr)	{
		try {
			return getRecMinVal(Integer.valueOf(ahhStr), Integer.valueOf(ammStr), Integer.valueOf(zhhStr), Integer.valueOf(zmmStr));
		}
		catch ( NumberFormatException e ) {
			System.err.println("[ERROR] at getRecMin()： "+ahhStr+","+ammStr+","+zhhStr+","+zmmStr);
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 *  開始・終了時刻から長さを算出する。引数の前後関係は意識しなくて良い。
	 */
	private static int getRecMinVal(int ahh, int amm, int zhh, int zmm) {
		int min = (zhh*60+zmm) - (ahh*60+amm);
		if (min < 0) min += 24*60;
		return min;
	}
	
	/**
	 * @see #getCritDateTime(int)
	 */
	public static String getCritDateTime() {
		return getCritDateTime(0);
	}
	/**
	 * <P>ここより前は「前日」分であり過去情報であると判断するための基準日時を生成する。
	 * <P>要するに、「当日」の "YYYY/MM/DD 05:00" （「当日」なので、日付は {@link #getDate529} の値）
	 * @param n : n日先の基準日時か。当日の場合は0 
	 */
	public static String getCritDateTime(int n) {
		return getDate529(n*86400,false)+" 05:00";
	}
	/**
	 * 
	 * @param showpassed true:{@link #getCritDateTime()}と等価、false:{@link #getDateTime(int)}と等価
	 * @return
	 */
	public static String getCritDateTime(boolean showpassed) {
		if (showpassed) {
			return getCritDateTime(0);
		}
		else {
			return getDateTime(0);
		}
	}
	
	/**
	 * 次回実行予定日を取得する
	 * @see #getStartEndList(ArrayList, ArrayList, ReserveList)
	 */
	public static String getNextDate(ReserveList r) {
		
		ArrayList<String> starts = new ArrayList<String>();
		ArrayList<String> ends = new ArrayList<String>();
		CommonUtils.getStartEndList(starts, ends, r);
		if (starts.size() > 0) {
			GregorianCalendar c = getCalendar(starts.get(0));
			if (c != null) {
				return(getDate(c));
			}
		}
		
		// エラーの場合は1970/01/01を返す
		return(getDate(new GregorianCalendar(1970,0,1)));
	}
	
	/**
	 * 開始・終了日時のリストを作成する
	 * @see #getNextDate(ReserveList)
	 */
	public static void getStartEndList(ArrayList<String> starts, ArrayList<String> ends, ReserveList r) {
		
		int ptnid = r.getRec_pattern_id();
		
		//boolean isOverDay = (r.getAhh().compareTo(r.getZhh()) > 0);		// 日付をまたいだ
		//boolean isLateNight = (adjLateNight == false && ( ptnid >= 7 && ptnid <= 10 ) && isLateNight(r.getAhh()));	// 深夜帯（毎日＆帯予約のみ利用）
		
		int len = Integer.valueOf(getRecMin(r.getAhh(), r.getAmm(), r.getZhh(), r.getZmm()));
		
		// 予約パターンによりけり対象となる日付は増えたり増えたり
		if (ptnid == 11) {
			// 単日
			GregorianCalendar d = getCalendar(r.getRec_pattern()+" "+r.getAhh()+":"+r.getAmm());
			if (d != null) {
				starts.add(getDateTime(d));
				d.add(Calendar.MINUTE,len);
				ends.add(getDateTime(d));
			}
		}
		else {
			
			// 基準日時
			GregorianCalendar cur = getCalendar(0);
			GregorianCalendar cri = getCalendar(getCritDateTime());
			
			// 切り捨て条件の選択
			GregorianCalendar cond;
			if ( displayPassedReserve ) {
				cond = (GregorianCalendar) cri.clone();	// 過去予約表示
			}
			else {
				cond = (GregorianCalendar) cur.clone();	// 現在日時以上
			}
			
			// ループ終了位置
			GregorianCalendar cz = (GregorianCalendar) cur.clone();
			cz.add(Calendar.DATE, dogDays);
			
			// ループ開始位置
			GregorianCalendar ca = (GregorianCalendar) cri.clone();
			if ( isLateNight(r.getAhh()) ) {
				ca.add(Calendar.DATE, 1);
			}
			ca.set(Calendar.HOUR_OF_DAY, Integer.valueOf(r.getAhh()));
			ca.set(Calendar.MINUTE, Integer.valueOf(r.getAmm()));
			GregorianCalendar cb = (GregorianCalendar) ca.clone();
			cb.set(Calendar.HOUR_OF_DAY, Integer.valueOf(r.getZhh()));
			cb.set(Calendar.MINUTE, Integer.valueOf(r.getZmm()));
			if ( cb.compareTo(ca) < 0 ) {
				cb.add(Calendar.DATE, 1);	// 終了日時より開始日時が大きいならば
			}
			
			// 深夜かな？
			boolean islatenight = isLateNight(r.getAhh());
			
			while (ca.compareTo(cz) < 0)
			{
				if ( cond.compareTo(cb) > 0 ) {
					// 過去情報だにゅ
					ca.add(Calendar.DATE, 1);
					cb.add(Calendar.DATE, 1);
					continue;
				}
				
				boolean isReserved = false;	// 過去の予約＝false

				if (ptnid == 10) {
					// 毎日
					isReserved = true;
				}
				else if (9 >= ptnid && ptnid >= 7) {
					// 帯
					int wd = ca.get(Calendar.DAY_OF_WEEK);
					if ( islatenight ) {
						if ( adjLateNight ) {
							// RDなどの深夜時間帯（月～土）
							if ( Calendar.MONDAY <= wd && wd <= (r.getRec_pattern_id()-2) ) {
								isReserved = true;
							}
						}
						else {
							// 通常の深夜時間帯（火～日）
							if ( Calendar.TUESDAY <= wd && wd <= (r.getRec_pattern_id()-1) ) {
								isReserved = true;
							}
							else if ( ptnid == 9 && wd == Calendar.SUNDAY ) {
								isReserved = true;
							}
						}
					}
					else {
						// 平常時間帯
						if ( Calendar.MONDAY <= wd && wd <= (r.getRec_pattern_id()-2) ) {
							isReserved = true;
						}
					}
				}
				else if (ptnid < 7) {
					// 週次
					if ( ptnid == (ca.get(Calendar.DAY_OF_WEEK)-1) ) {
						isReserved = true;
					}
				}
				
				if (isReserved) {
					GregorianCalendar ct = (GregorianCalendar) ca.clone();
					ct.add(Calendar.MINUTE,len);
					starts.add(getDateTime(ca));
					ends.add(getDateTime(ct));
				}
				
				ca.add(Calendar.DATE, 1);
				cb.add(Calendar.DATE, 1);
			}
		}
	}
	
	/**
	 * <P>aとbの差をミリ秒で返す（正／負）
	 * <P>秒に直すなら 1000、分に直すなら 60000で割る。
	 * @see #getDiffDateTime
	 */
	public static long getCompareDateTime(String a, String b) {
		GregorianCalendar d = getCalendar(a);
		GregorianCalendar e = getCalendar(b);
		if ( d == null || e == null ) {
			return -1;
		}
		return getCompareDateTime(d.getTimeInMillis(), e.getTimeInMillis());
	}

	/**
	 * <P>aとbの差をミリ秒で返す（正／負）
	 * <P>秒に直すなら 1000、分に直すなら 60000で割る。
	 * @see #getDiffDateTime
	 */
	public static long getCompareDateTime(GregorianCalendar a, GregorianCalendar b) {
		return getCompareDateTime(a.getTimeInMillis(), b.getTimeInMillis());
	}

	private static long getCompareDateTime(long x, long y) {
		return x-y;
	}
	
	/**
	 * <P>aとbの差をミリ秒で返す（絶対値）
	 * <P>秒に直すなら 1000、分に直すなら 60000で割る。
	 * @see #getCompareDateTime
	 */
	public static long getDiffDateTime(String a, String b) {
		GregorianCalendar d = getCalendar(a);
		GregorianCalendar e = getCalendar(b);
		if ( d == null || e == null ) {
			return -1;
		}
		return getDiffDateTime(d.getTimeInMillis(), e.getTimeInMillis());
	}
	
	/**
	 * <P>aとbの差をミリ秒で返す
	 * <P>秒に直すなら 1000、分に直すなら 60000で割る。
	 */
	public static long getDiffDateTime(GregorianCalendar a, GregorianCalendar b) {
		return getDiffDateTime(a.getTimeInMillis(), b.getTimeInMillis());
	}
	
	private static long getDiffDateTime(long x, long y) {
		return ((x>y)?(x-y):(y-x));
	}
	

	/**
	 * 現在時刻＋n秒のCalendarを返す
	 */
	public static GregorianCalendar getCalendar(int n) {
		GregorianCalendar c = new GregorianCalendar();
		//c.setTime(new Date());
		c.add(Calendar.SECOND, n);
		return c;
	}
	/**
	 *  日付時刻文字列をCalendarに変換
	 *  @param date YYYY/MM/DD[(.)][ hh:mm[:ss]] or YYYY-MM-DD[Thh:mm[:ss]] or YYYYMMDD[hhmm[ss]]
	 */
	public static GregorianCalendar getCalendar(String date) {
		Matcher ma = Pattern.compile("^(\\d\\d\\d\\d)[/-](\\d\\d)[/-](\\d\\d)(\\(.\\))?([ T](\\d\\d):(\\d\\d)(:\\d\\d)?)?$").matcher(date);
		if ( ! ma.find()) {
			ma = Pattern.compile("^(\\d\\d\\d\\d)(\\d\\d)(\\d\\d)()?((\\d\\d)(\\d\\d)(\\d\\d)?)?$").matcher(date);
			if ( ! ma.find()) {
				return null;
			}
		}
		
		GregorianCalendar c = null;
		if ( ma.group(5) == null ) {
			c = new GregorianCalendar(
					Integer.valueOf(ma.group(1)),
					Integer.valueOf(ma.group(2))-1,
					Integer.valueOf(ma.group(3)),
					0,
					0,
					0);
		}
		else {
			c = new GregorianCalendar(
					Integer.valueOf(ma.group(1)),
					Integer.valueOf(ma.group(2))-1,
					Integer.valueOf(ma.group(3)),
					Integer.valueOf(ma.group(6)),
					Integer.valueOf(ma.group(7)),
					0);
		}
		return c;
	}
	
	/**
	 *  現在日時＋n秒を日付時刻形式に変換。
	 *  @param n : 負の値を許可する
	 *  @return YYYY/MM/DD hh:mm
	 */
	public static String getDateTime(int n) {
		GregorianCalendar c = new GregorianCalendar();
		//c.setTime(new Date());
		c.add(Calendar.SECOND, n);
		return getDateTime(c);
	}
	
	/**
	 *  日時を日付時刻形式に変換。曜日文字がつかない。
	 *  @see #getDateTimeW(GregorianCalendar)
	 *  @see #getIsoDateTime(GregorianCalendar)
	 *  @see #getDateTimeYMD(GregorianCalendar)
	 *  @return YYYY/MM/DD hh:mm
	 */
	public static String getDateTime(GregorianCalendar c) {
		return new SimpleDateFormat("yyyy/MM/dd HH:mm").format(c.getTime());
	}
	
	/**
	 *  現在日時＋n秒を日付時刻形式に変換。曜日文字がつく。
	 *  @param n : 負の値を許可する
	 *  @return YYYY/MM/DD hh:mm
	 */
	public static String getDateTimeW(int n) {
		GregorianCalendar c = new GregorianCalendar();
		//c.setTime(new Date());
		c.add(Calendar.SECOND, n);
		return getDateTimeW(c);
	}
	
	/**
	 *  日時を日付時刻形式に変換。曜日文字がつく。
	 *  @see #getDateTime(GregorianCalendar)
	 *  @return YYYY/MM/DD(WD) hh:mm
	 */
	public static String getDateTimeW(GregorianCalendar c) {
		return new SimpleDateFormat("yyyy/MM/dd('"+WDTPTN[c.get(Calendar.DAY_OF_WEEK)-1]+"') HH:mm").format(c.getTime());
	}
	
	/**
	 *  日時を日付時刻形式に変換。ISO形式。秒まで返却。
	 *  @see #getDateTime(GregorianCalendar)
	 *  @return YYYY-MM-DDThh:mm:ss
	 */
	public static String getIsoDateTime(GregorianCalendar c) {
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(c.getTime());
	}

	/**
	 *  現在日時＋n秒を日付時刻形式に変換。
	 *  @see #getDateTime(GregorianCalendar)
	 *  @param n : 負の値を許可する
	 *  @return YYYYMMDDhhmmss
	 */
	public static String getDateTimeYMD(int n) {
		GregorianCalendar c = new GregorianCalendar();
		//c.setTime(new Date());
		if (n != 0) c.add(Calendar.SECOND, n);
		return getDateTimeYMD(c);
	}
	
	/**
	 *  日時を日付時刻形式に変換。YMD形式。秒まで返却。
	 *  @see #getDateTime(GregorianCalendar)
	 *  @return YYYYMMDDhhmmss
	 */
	public static String getDateTimeYMD(GregorianCalendar c) {
		return new SimpleDateFormat("yyyyMMddHHmmss").format(c.getTime());
	}

	/**
	 *  日時を日付時刻形式に変換。YMD形式。ミリ秒まで返却。
	 *  @see #getDateTime(GregorianCalendar)
	 *  @return YYYYMMDDhhmmssSSS
	 */
	public static String getDateTimeYMDx(GregorianCalendar c) {
		return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(c.getTime());
	}

	/**
	 * <P>「当日」の日付文字列を返します。
	 * <P>ただし、05時～29時を当日として判断するので、<B>24時～29時に実行した場合は前日の日付が返ります</B>。
	 * @param n : 現在日時に対して n秒 加えた日時を返します。負の値も許可されます。
	 * @param addwdstr : trueの場合、日付に曜日文字[これ->(日)]を加えます。
	 * @return YYYY/MM/DD[(WD)] hh:mm:ss
	 */
	public static String getDate529(int n, boolean addwdstr) {
		GregorianCalendar c = getCalendar(0);
		c.add(Calendar.SECOND, n);
		return getDate529(c, addwdstr);
	}
	/**
	 * @see #getDate529(int, boolean)
	 */
	public static String getDate529(String s, boolean addwdstr) {
		GregorianCalendar c = getCalendar(s);
		return getDate529(c, addwdstr);
	}
	/**
	 * @see #getDate529(int, boolean)
	 */
	public static String getDate529(GregorianCalendar c, boolean addwdstr) {
		// 今日の範囲は05:00～04:59まで
		if ( isLateNight(c.get(Calendar.HOUR_OF_DAY)) ) {
			c.add(Calendar.DAY_OF_MONTH, -1);
		}
		return getDate(c, addwdstr);
	}
	
	/**
	 * 日付を日付形式に変換。
	 * @return YYYY/MM/DD(WD)
	 */ 
	public static String getDate(GregorianCalendar c) {
		return getDate(c, true);
	}
	
	/**
	 * 日付を日付形式に変換。
	 * @param addwdstr : trueの場合、日付に曜日文字[これ->(日)]を加えます。
	 * @return YYYY/MM/DD[(WD)]
	 */ 
	public static String getDate(GregorianCalendar c, boolean addwdstr) {
		if ( addwdstr ) {
			return new SimpleDateFormat("yyyy/MM/dd('"+WDTPTN[c.get(Calendar.DAY_OF_WEEK)-1]+"')").format(c.getTime());
		}
		else {
			return new SimpleDateFormat("yyyy/MM/dd").format(c.getTime());
		}
	}
	
	/**
	 * <P>「当日」の日付文字列を返します。
	 * <P>ただし、05時～29時を当日として判断するので、<B>24時～29時に実行した場合は前日の日付が返ります</B>。
	 * @param n : 現在日時に対して n秒 加えた日時を返します。負の値も許可されます。
	 * @return YYYYMMDD
	 */
	public static String getDateYMD529(int n) {
		GregorianCalendar c = new GregorianCalendar();
		//c.setTime(new Date());
		c.add(Calendar.SECOND, n);
		
		// 今日の範囲は05:00～04:59まで
		if (c.get(Calendar.HOUR_OF_DAY) < 5) {
			c.add(Calendar.DAY_OF_MONTH, -1);
		}
		
		return getDateYMD(c);
	}
	
	/**
	 * 現在日付を日付形式に変換。
	 * @param n : 負の値を許可する。
	 * @return YYYYMMDD
	 */
	public static String getDateYMD(int n) {
		GregorianCalendar ca = CommonUtils.getCalendar(n);
		return getDateYMD(ca);
	}
	
	/**
	 * 現在日付を日付形式に変換。
	 * @return YYYYMMDD
	 */
	public static String getDateYMD(GregorianCalendar c) {
		return new SimpleDateFormat("yyyyMMdd").format(c.getTime());
	}
	
	/**
	 *  日時を時刻形式に変換
	 * @return hh:mm
	 */
	public static String getTime(int n) {
		return getTime(getCalendar(n));
	}
	
	/**
	 *  日時を時刻形式に変換
	 * @return hh:mm
	 */
	public static String getTime(GregorianCalendar c) {
		return getTime(c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE));
	}
	
	/**
	 *  日時を時刻形式に変換
	 * @see #getTimeHM(GregorianCalendar)
	 * @return hh:mm
	 */
	public static String getTime(int hh, int mm) {
		return String.format("%02d:%02d",hh,mm);
	}
	
	/**
	 *  日時を時刻形式に変換
	 *  @see #getTime(GregorianCalendar)
	 * @return hhmm
	 */
	public static String getTimeHM(int n) {
		return getTimeHM(getCalendar(n));
	}
	
	/**
	 *  日時を時刻形式に変換
	 *  @see #getTime(GregorianCalendar)
	 * @return hhmm
	 */
	public static String getTimeHM(GregorianCalendar c) {
		return getTimeHM(c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE));
	}
	
	/**
	 *  日時を時刻形式に変換
	 *  @see #getTime(GregorianCalendar)
	 * @return hhmm
	 */
	public static String getTimeHM(int hh, int mm) {
		return String.format("%02d%02d",hh,mm);
	}
	
	/**
	 * 時間帯の重複があるかどうかを判定します。
	 * @param adjnotrep : falseの場合、終了時刻と開始時刻が重なるものを重複として扱います。
	 */
	public static boolean isOverlap(String start1, String end1, String start2, String end2, boolean adjnotrep) {
		if ( adjnotrep ) {
			return( ! (end1.compareTo(start2) <= 0 || end2.compareTo(start1) <= 0));
		}
		else {
			return( ! (end1.compareTo(start2) < 0 || end2.compareTo(start1) < 0));
		}
	}
	
	
	/*******************************************************************************
	 * Color関連
	 ******************************************************************************/
	
	/**
	 * 文字列を色化します。
	 * @param s : 色化する文字列 "#xxxxxx;" (Ｒ、Ｇ、Ｂ　各１６進２ケタ）
	 * @return {@link Color}
	 * @see #color2str(Color c)
	 */
	public static Color str2color(String s) {
		try {
			Matcher ma = Pattern.compile("#(..)(..)(..)").matcher(s);
			if ( ma.find() ) {
				int r = Integer.decode("0x"+ma.group(1));
				int g = Integer.decode("0x"+ma.group(2));
				int b = Integer.decode("0x"+ma.group(3));
				return(new Color(r,g,b));
			}
			return new Color(0,0,0);
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 色を文字列化します。
	 * @param c : 文字列化する色({@link Color})
	 * @return "#xxxxxx;" (Ｒ、Ｇ、Ｂ　各１６進２ケタ）
	 * @see #str2color(String) 
	 */
	public static String color2str(Color c) {
		return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
	}
	
	
	/*******************************************************************************
	 * 文字列操作関連
	 ******************************************************************************/
	 
	public static String joinPath(String... path) {
		return joinStr(File.separator, path);
	}

	public static String joinStr(String s, ArrayList<String> a) {
		return joinStr(s,a.toArray(new String[0]));
	}
	
	public static String joinStr(String s, String... a) {
		if (a.length <= 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<a.length-1; i++) {
			sb.append(a[i]);
			sb.append(s);
		}
		sb.append(a[a.length-1]);
		return(sb.toString());
	}
	
	/**
	 * 文字数ではなく、バイト数でのsubstringを行います。
	 * @param s
	 * @param length
	 * @return
	 */
	public static String substringrb(String s, int length) {
		StringBuilder sb = new StringBuilder();
		int len = 0;
		for ( Character c : s.toCharArray() ) {
			len += (c<256)?(1):(2);
			if (len > length) {
				break;
			}
			sb.append(c.toString());
		}
		return(sb.toString());
	}
	
	private static final char[] NGCharList = "\\/:*?\"<>|".toCharArray();
	
	/**
	 * ファイル名に使用できない文字の一覧を返します。
	 */
	public static String getNGCharList() {
		return new String(NGCharList);
	}
	
	/**
	 * ファイル名に使用できない文字が含まれているかどうか判定します。
	 */
	public static boolean isNGChar(String s) {
		for (int i=0; i<NGCharList.length; i++) {
			if (s.indexOf(NGCharList[i]) >= 0) {
				return(true);
			}
		}
		return(false);
	}
	
	/**
	 * ファイル名に使用できない文字をHTMLエスケープ（&#ddd;）します。
	 * @see #unEscape(String)
	 */
	public static String escapeFilename(String src) {
		for (int i=0; i<NGCharList.length; i++) {
			src = src.replaceAll(String.format("\\%s",String.valueOf(NGCharList[i])), String.format("&#%03d;", (int)NGCharList[i]));
		}
		return src;
	}

	/**
	 * HTMLエスケープをデコードします。
	 */
	public static String unEscape(String src) {
		String dst = src.replaceAll("&quot;", "\"");
		dst = dst.replaceAll("&lt;", "<");
		dst = dst.replaceAll("&gt;", ">");
		dst = dst.replaceAll("&amp;", "&");
		dst = dst.replaceAll("&nbsp;", " ");
		dst = dst.replaceAll("〜", "～");
		dst = dst.replaceAll("−", "－");
		HashMap<String, String> ek = new HashMap<String, String>();
		Matcher ma = Pattern.compile("(&#(\\d+);)").matcher(src);
		while (ma.find()) {
			ek.put(ma.group(1), Character.valueOf((char)(int)Integer.valueOf(ma.group(2))).toString());
		}
		for (Entry<String, String> kv : ek.entrySet()) {
			dst = dst.replaceAll(kv.getKey(), kv.getValue());
		}
		return dst;
	}
	
	/**
	 *  Unicodeエスケープをデコードします。
	 */
	public static String unUniEscape(String src) {
		Matcher ma = Pattern.compile("\\\\u[0-9a-f]{4}").matcher(src);
		StringBuffer sb = new StringBuffer(src.length());
		while (ma.find()) {
			char[] chars = ma.group().substring(2, 6).toCharArray();
			int hex = 0;
			for (char c : chars) {
				hex = hex << 4;
				if ('a' <= c && c <= 'f') {
					hex += c - 'a' + 10;
				}
				else {
					hex += c - '0';
				}
			}
			ma.appendReplacement(sb, ""+(char)hex);
		}
		ma.appendTail(sb);
		return sb.toString();
	}
	
	
	/**
	 * 文字列中の全角数字を半角数字に置き換えます
	 */
	public static String toHANUM(String numTmp) {
		if (numTmp == null) return null;
		
		StringBuilder sb = new StringBuilder();
		for ( int i=0; i<numTmp.length(); i++ ) {
			char c = numTmp.charAt(i);
			if ( '０' <= c && c <= '９' ) {
				c = (char)(c -  '０' + '0' );
			}
			sb.append(c);
		}
		return sb.toString();
	}
	
	public static String toHANALNUM(String alnumTmp) {
		if (alnumTmp == null) return null;
		
		StringBuilder sb = new StringBuilder();
		for ( int i=0; i<alnumTmp.length(); i++ ) {
			char c = alnumTmp.charAt(i);
			if ( 'ａ' <= c && c <= 'ｚ' ) {
				c = (char)(c - 'ａ' + 'a' );
			}
			else if ( 'Ａ' <= c && c <= 'Ｚ' ) {
				c = (char)(c -  'Ａ' + 'A' );
			}
			else if ( '０' <= c && c <= '９' ) {
				c = (char)(c -  '０' + '0' );
			}
			else if ( c == '（' ) {
				c = '(';
			}
			else if ( c == '）' ) {
				c = ')';
			}
			else if ( c == '－' ) {
				c = '-';
			}
			else if ( c == '　' ) {
				c = ' ';
			}
			sb.append(c);
		}
		return sb.toString();
	}

	/*******************************************************************************
	 * オブジェクト操作関連
	 ******************************************************************************/

	/**
	 * <P>オブジェクトからオブジェクトへフィールドのコピー（ディープコピー）を行います。
	 * <P>新しく作ったインスタンスに入れ替えたいけど、ポインタを変えたくないので中身だけコピーできないか？という時に使います。
	 * <P>fromにあってtoにないフィールドについては無視して最後まで処理を続行します（というか型違いとかも無視します）。
	 * <P>多分遅いので、設定ファイルの読み出しなど使用頻度の少ないところで利用します。
	 * @param to : HashMapクラスの場合は、コピー先にインスタンスが存在している必要があります（nullはだめ）
	 * @param from
	 * @version 3.15.4β～
	 */
	public static boolean FieldCopy(final Object to, final Object from) {
		try {
			return FieldCopy(to,from,null);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	private static boolean FieldCopy(final Object to, final Object from, final Field fn) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ConcurrentModificationException, InstantiationException  {
		
		// 継承しているクラスのリストを作成する
		ArrayList<Class> fromCL = new ArrayList<Class>();
		ArrayList<Class> toCL = new ArrayList<Class>();
		for ( Class c=from.getClass(); c!=null && ! Object.class.equals(c); c=c.getSuperclass() ) {
			//if (debug) System.out.println("[DEBUG] FROM "+c.getName());
			fromCL.add(c);
		}
		for ( Class c=to.getClass(); c!=null && ! Object.class.equals(c); c=c.getSuperclass() ) {
			//if (debug) System.out.println("[DEBUG] TO "+c.getName());
			toCL.add(c);
		}

		// 両方の開始位置をそろえる
		while ( fromCL.size() > 0 && ! toCL.contains(fromCL.get(0)) ) {
			if (debuglv2) System.out.println("[DEBUG] removed FROM "+fromCL.get(0).getName());
			fromCL.remove(0);
		}
		while ( toCL.size() > 0 && ! fromCL.contains(toCL.get(0)) ) {
			if (debuglv2) System.out.println("[DEBUG] removed TO "+toCL.get(0).getName());
			toCL.remove(0);
		}
		
		int i=0;
		for ( Class c : fromCL ) {
			
			i++;
			
			String ctype = (i>1)?(" (super class)"):("");
			String cname = c.getName();
			String fname = (fn!=null)?(" name="+fn.getName()):("");
			
			if ( isHashMap(to,from,c,fn) ) {
				if (debuglv2) System.err.println("[DEBUG] FieldCopy("+cname+") *** TERM *** deep copy"+ctype+fname);
				return true;
			}
			
			if ( isLeaf(to,from,c,fn) ) {
				if (debuglv2) System.err.println("[DEBUG] FieldCopy("+cname+") *** TERM *** leaf"+ctype+fname);
				return true;
			}
			
			Field[] fd = c.getDeclaredFields();
			
			// フィールドなんかねーよ
			if ( fd.length == 0 ) {
				if ( fn == null ) {
					// 継承だけして追加のフィールドがない場合にここに入る
					if (debug) System.err.println("[DEBUG] FieldCopy("+cname+") no field"+ctype+fname);
					continue;
				}
				
				if (debug) System.err.println("[DEBUG] FieldCopy("+cname+") *** TERM *** no field"+ctype+fname);
				return isCopyable(to,from,c,fn);	// 終端
			}
			
			// （フィールド）たんけんぼくのまち。チョーさん生きてるよ！
			for ( Field f : fd ) {
				f.setAccessible(true);
				
				String xcname = f.getType().getName();
				String xfname = " name="+f.getName();
				
				int mod = f.getModifiers();
				
				if ( Modifier.isFinal(mod) ) {
					if (debuglv2) System.err.println("[DEBUG] FieldCopy("+xcname+") : FINAL field "+ctype+xfname);
					continue;
				}
				if ( Modifier.isStatic(mod) ) {
					if (debug) System.err.println("[DEBUG] FieldCopy("+xcname+") : STATIC field "+ctype+xfname);
					continue;
				}
				
				
				Object o = f.get(from);
				if ( o == null ) {
					if (debug) System.err.println("[DEBUG] FieldCopy("+xcname+") *** TERM *** null value FROM"+ctype+xfname);
					f.set(to, null);
					continue;	// 終端
				}
				
				Class xc = o.getClass();
				xcname = xc.getName();
				
				if ( isHashMap(to,o,xc,f) ) {
					if (debuglv2) System.err.println("[DEBUG] FieldCopy("+xcname+") *** TERM *** deep copy"+ctype+xfname);
					continue;
				}
				
				if ( isLeaf(to,o,xc,f) ) {
					if (debuglv2) System.err.println("[DEBUG] FieldCopy("+xcname+") *** TERM *** leaf"+ctype+xfname);
					continue;
				}
				
				isCopyable(to,o,xc,f);	// 終端
			}
		}
		
		return true;
	}
	
	// HashMapとか、コピーする
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean isHashMap(Object to, Object from, Class c, Field f) throws InstantiationException, IllegalAccessException, ConcurrentModificationException  {
		
		String cname = c.getName();
		String fname = " name="+((f==null)?("<TOP>"):(f.getName()));
		
		for ( Class cx : unCloneables ) {
			if ( cx.equals(c) ) {
				if ( HashMap.class.equals(c) || ArrayList.class.equals(c) ) {
		
					final Object px = (f==null)?(to):(f.get(to));
					if ( px == null ) {
						// コピー先にインスタンスが存在している必要がある
						if (debug) System.err.println("[DEBUG] FieldCopy("+cname+") / isCloneable() : must have been initialized"+fname);
						return true;
					}
					
					// 個別（キャスト！）
		
					if ( HashMap.class.equals(c) ) {
						HashMap<Object, Object> o = (HashMap<Object, Object>) from;
						HashMap<Object, Object> p;
						if ( f != null ) {
							// フィールドならclone()では社ローコピーのせいで同じものを指してるに違いない、新しいインスタンスを作らないといけない
							p = (HashMap<Object, Object>) c.newInstance();
						}
						else {
							// 自身（orスーパークラス）なら別物だろうからキャストで大丈夫だろう
							p = (HashMap<Object, Object>) to;
						}
						
						p.clear(); // とりあえず消す
						if (debug) System.err.println("[DEBUG] FieldCopy("+cname+") / isHashMap() : HashMap "+cname+fname+" size="+o.size());
						for ( Entry<Object, Object> entry: o.entrySet() ) {
							if (debuglv2) System.err.println("[DEBUG] FieldCopy("+cname+") / isHashMap() : copy"+fname+" key="+entry.getKey()+" value="+entry.getValue());
							p.put(entry.getKey(), entry.getValue());
						}
						
						if ( f != null ) {
							f.set(to, p);
						}
					}
					else if ( ArrayList.class.equals(c) ) {
						ArrayList<Object> o = (ArrayList<Object>) from;
						ArrayList<Object> p = null;
						if ( f != null ) {
							p = (ArrayList<Object>) c.newInstance();
						}
						else {
							p = (ArrayList<Object>) to;
						}
						
						p.clear(); // とりあえず消す
						if (debug) System.err.println("[DEBUG] FieldCopy("+cname+") / isHashMap() : ArrayList "+cname+fname+" size="+o.size());
						for ( Object entry: o ) {
							if (debuglv2) System.err.println("[DEBUG] FieldCopy("+cname+") / isHashMap() : copy"+fname+" value="+entry);
							p.add(entry);
						}
						
						if ( f != null ) {
							f.set(to, p);
						}
					}
				}
				else {
					if (debug) System.err.println("[DEBUG] FieldCopy("+cname+") / isHashMap() : unsupported"+fname);
				}
				return true;
			}			
		}
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	private static final Class[] unCloneables = { HashMap.class, AbstractMap.class, ArrayList.class, AbstractList.class };
	
	// Integerとか、コピーする
	@SuppressWarnings("rawtypes")
	private static boolean isLeaf(Object to, Object from, Class c, Field f) throws IllegalArgumentException, IllegalAccessException {
		
		String cname = c.getName();
		String fname = " name="+((f==null)?("<TOP>"):(f.getName()));
		
		if ( from instanceof String || from instanceof Boolean || from instanceof Number || from instanceof Color || from instanceof Enum || from instanceof Component ) {
			if ( f == null ) {
				if (debug) System.err.println("[DEBUG] FieldCopy("+cname+") / isLeaf() : <Top> is not allowed");
				return true;
			}
				
			//if (debuglv2) System.err.println("[DEBUG] FieldCopy("+cname+") / isLeaf() : leaf field"+fname);
			f.set(to, from);
			return true;
		}
		
		return false;
	}
	
	//
	@SuppressWarnings("rawtypes")
	private static boolean isCopyable(Object to, Object from, Class c, Field f) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
		
		String cname = c.getName();
		String fname = " name="+((f==null)?("<TOP>"):(f.getName()));
		
		// コピーに失敗して例外の発生したフィールドについては無視します
		
		if ( c.isPrimitive() ) {
			if (debug) System.err.println("[DEBUG] FieldCopy("+cname+") / isCopyable() : primitive"+fname);
			f.set(to, from);
		}
		else if ( c.isArray() ) {
			if ( f == null ) {
				// 自分自身だとコピーするしかないが、しない。redim()があればなんとかできたのに！
				if (debug) System.err.println("[DEBUG] FieldCopy("+cname+") / isCopyable() : not copyable array"+fname);
			}
			else {
				// フィールドなら入れ物は作る。要素はコピー
				Class comp = c.getComponentType();
				if (debug) System.err.println("[DEBUG] FieldCopy("+cname+") / isCopyable() : array of "+comp.getName()+fname+" lenth="+Array.getLength(from));
				Object[] o = (Object[]) from;
				Object[] p = (Object[]) Array.newInstance(comp, Array.getLength(from));
				
				for ( int i=o.length-1; i>=0; i-- ) {
					if (debuglv2) System.err.println("[DEBUG] FieldCopy("+comp.getName()+") / isCopyable() : copy"+fname+" value="+o[i]);
					p[i] = o[i];
				}
			}
		}
		else {
			// そのほかは可能な限りcloneする
			invokeClone(to,from,c,f);
		}
		
		return true;
	}
	
	//
	@SuppressWarnings("rawtypes")
	private static boolean invokeClone(Object to, Object from, Class c, Field f) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		final String cname = c.getName();
		final String fname = " name="+((f==null)?("<TOP>"):(f.getName())); // よく考えたらここにf==nullで入ってくることはない
		
		try {
			@SuppressWarnings("unchecked")
			Method m = c.getMethod("clone");
			f.set(to, m.invoke(from));
			if (debug) System.err.println("[DEBUG] FieldCopy("+cname+") / invokeClone() : invoke clone "+cname+fname);
			return true;
		}
		catch (NoSuchMethodException e) {
			// ちょっとこわいのでコピーせずに無視
			if (debug) System.err.println("[DEBUG] FieldCopy("+cname+") / invokeClone() : clone unsupported and ignored "+fname);
		}
		return false;
	}
	
	/*******************************************************************************
	 * JavaVM関連
	 ******************************************************************************/

	/**
	 * デバッグ用にスタックトレースをとりたいなー
	 */
	public static void printStackTrace() {
		if ( ! debug) return;
		
		new Throwable().printStackTrace();
	}
	
	/*******************************************************************************
	 * OS操作関連
	 ******************************************************************************/

	// なぜJavaなのに機種別のコードをかかなければならないのか…
	public static boolean isMac() { return ismac; }
	public static boolean isLinux() { return islinux; }
	public static boolean isWindows() { return iswindows; }
	public static boolean isWindowsXP() { return iswindowsxp; }
	
	private static final boolean ismac		= ((String)System.getProperty("os.name")).toLowerCase().replaceAll(" ", "").startsWith("macosx");
	private static final boolean islinux	= ((String)System.getProperty("os.name")).toLowerCase().replaceAll(" ", "").startsWith("linux");
	private static final boolean iswindows	= ((String)System.getProperty("os.name")).toLowerCase().replaceAll(" ", "").startsWith("windows");
	private static final boolean iswindowsxp= ((String)System.getProperty("os.name")).toLowerCase().replaceAll(" ", "").startsWith("windowsxp");
	
	// ミリ秒単位でsleep
	public static void milSleep(int i) {
		try {
			Thread.sleep(i);
		}
		catch (InterruptedException e) {
			System.err.println("[スリープ] なんかあったのか");
			e.printStackTrace();
		}
	}
	
	/**
	 * コマンドを実行する
	 * @see #getCommandResult()
	 * @param run : コマンドの指定
	 * @return -1 : 失敗、-1以外 ： コマンドのexit値
	 */
	public static int executeCommand(String run)	{
		
		commandResult = null;
		
		ArrayList<String> list = new ArrayList<String>();
		Matcher ma = Pattern.compile("((\"([^\"]*?)\"|([^\"]+?))(\\s+|$))").matcher(run);
		while (ma.find()) {
			if (ma.group(3) != null) {
				list.add(ma.group(3));
			}
			else {
				list.add(ma.group(4));
			}
		}
		if ( list.size() == 0 ) {
			System.err.println("[外部コマンド] 実行できません: "+run);
			return -1;
		}
		else {
			ProcessBuilder pb = null;
			Process p = null;
			
			InputStream is = null;
			InputStreamReader isr = null;
			BufferedReader br =  null;
	
			try {
				pb = new ProcessBuilder(list);
				pb.redirectErrorStream(true);
				
				p = pb.start();
				p.getOutputStream().close();	// 入力はしない
				
				String encoding = (isWindows())?("MS932"):("UTF-8");
				
				StringBuilder sb = new StringBuilder();
				is = p.getInputStream();
				isr = new InputStreamReader(is,encoding);
				br =  new BufferedReader(isr);
				String s;
				while ((s=br.readLine()) != null) {
					sb.append(s);
					sb.append("\n");
				}
				commandResult = sb.toString();
				
				System.out.println("--- 外部コマンドを実行します ---");
				System.out.println(commandResult);
				System.out.println("--- 外部コマンドを実行しました ---");
				
				p.waitFor();	// is.read()で判定できるので不要
				
				int retval = p.exitValue();
				
				return retval;
			}
			catch ( IOException e ) {
				System.err.println("[外部コマンド] 失敗しました: "+run);
				e.printStackTrace();
			}
			catch ( InterruptedException e ) {
				System.err.println("[外部コマンド] 失敗しました: "+run);
				e.printStackTrace();
			}
			finally {
				closing(br);
				closing(isr);
				closing(is);
				
				if ( p != null ) {
					closing(p.getInputStream());
					closing(p.getErrorStream());
					p.destroy();
				}
			}
		}
		
		return -1;
	}
	
	public static String getCommandResult() { return commandResult; }
	
	private static String commandResult;

	/**
	 * 登録されたアプリケーションでファイルを開く
	 */
	public static String openFile(String file)	{
		String errmsg = null;
		try {
			if ( useRundll32 && isWindows() ) {
				Runtime runtime = Runtime.getRuntime(); 
				runtime.exec("rundll32 url.dll,FileProtocolHandler "+new File(file).getAbsolutePath());
			}
			else {
				Desktop desktop = Desktop.getDesktop();
				desktop.open(new File(file));
			}
		}
		catch (UnsupportedOperationException e) {
			System.err.println("[アプリケーションでファイルを開く] 失敗しました: "+file);
			e.printStackTrace();
			errmsg = e.toString();
		}
		catch (IOException e) {
			System.err.println("[アプリケーションでファイルを開く] 失敗しました: "+file);
			e.printStackTrace();
			errmsg = e.toString();
		}
		return errmsg;
	}
	
	
	/*******************************************************************************
	 * ファイルI/O関連
	 ******************************************************************************/
	
	// 古くないかどうか
	public static boolean isFileAvailable(File f, int days) {
		// 存在するか
		if ( ! f.exists() ) {
			return false;
		}
		
		// 最近更新されているか
		long lm = f.lastModified();
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(lm);
		if ( getCompareDateTime(cal, getCalendar(-days*86400)) < 0 ) {
			return false;
		}
			
		return true;
	}
	
	// ディレクトリをまるっと削除
	public static boolean rmdir(File f) {
		if( ! f.exists()) {
			return false;
		}
		
		if (f.isFile()){
			return f.delete();
		}
		else if (f.isDirectory()) {
			File[] files = f.listFiles();
			for (File file : files){
				if ( ! rmdir(file)) {
					return false;
				}
			}
			return f.delete();
		}
		
		return false;	// ここには来ないはず
	}
	
	// カウンタの記録
	public static boolean saveCnt(int cnt, String filename) {
		if ( ! write2file(filename, String.valueOf(cnt)) ) {
			System.out.println("[カウンタファイル] 書き込めませんでした: "+filename);
			return false;
		}
		return true;
	}
	public static int loadCnt(String filename) {
		if ( ! new File(filename).exists() ) {
			return -1;
		}
		
		try {
			String str = read4file(filename, true);
			if ( str != null ) {
				return Integer.valueOf(str);
			}
		}
		catch ( NumberFormatException e ) {
			System.err.println("[カウンタファイル] 内容が不正です: "+filename);
		}
		
		System.err.println("[カウンタファイル] 読み込めませんでした: "+filename);
		return -1;
	}
	
	// ロック・アンロック
	
	/**
	 *  ロック
	 */
	public static boolean getLock() {
		FileOutputStream fos = null;
		FileChannel fc = null;
		try {
			fos = new FileOutputStream(new File("_lock_"));
			fc = fos.getChannel();
			lock = fc.tryLock();
			if ( lock == null ) {
				System.err.println("[多重起動禁止] ロックされています.");
				return false;
			}
			else {
				return true;
			}
		}
		catch (FileNotFoundException e) {
			System.err.println("[多重起動禁止] ロックの取得に失敗しました.");
			e.printStackTrace();
		}
		catch (IOException e) {
			System.err.println("[多重起動禁止] ロックの取得に失敗しました.");
			e.printStackTrace();
		}
		finally {
			//closing(fos);
			//closing(fc);
		}
		return false;
	}
	
	/**
	 *  アンロック
	 */
	public static void getUnlock() {
		try {
			if ( lock != null ) {
				lock.release();
			}
		} catch (IOException e) {
			System.err.println("[多重起動禁止] ロックの解放に失敗しました.");
			e.printStackTrace();
		}
	}

	private static FileLock lock = null;

	/**
	 *  テキストをファイルに書き出す（エンコードはデフォルト）
	 */
	public static boolean write2file(String fname, String dat) {
		return write2file(fname, dat, null);
	}
	
	/**
	 *  テキストをファイルに書き出す（エンコードは指定による）
	 */
	public static boolean write2file(String fname, String dat, String encoding) {
		if ( fname.matches(".*\\.zip$") ) {
			return _write2zip(fname, dat, encoding);
		}
		else {
			return _write2file(fname, dat, encoding);
		}
	}
	private static boolean _write2zip(String fname, String dat, String encoding) {
		{
			FileOutputStream os = null;
			BufferedOutputStream bos = null;
			ZipOutputStream zos = null;
			try {
				// 一時ファイルに書き出し\
				os = new FileOutputStream(fname+".tmp");
				bos = new BufferedOutputStream(os);
				zos = new ZipOutputStream(bos);
				
				byte[] buf = null;
				if ( encoding == null ) {
					// デフォルトエンコーディングで
					buf = dat.getBytes();
				}
				else {
					// 指定エンコーディングで
					buf = dat.getBytes(encoding);
				}
				
				ZipEntry ze = new ZipEntry(ZIPENTRY);
				ze.setSize(buf.length);
				
				zos.putNextEntry(ze);
				zos.write(buf);
				
		        // 処理が続くので閉じないとアカン！
				zos.closeEntry();
			    zos.close(); zos = null;
		        if (bos!=null) { bos.close(); bos = null; }
		        if (os!=null) { os.close(); os = null; }
				
				// キャッシュファイルに変換
			    File o = new File(fname);
			    if ( o.exists() && ! o.delete() ) {
			    	System.err.println("削除できないよ： "+fname);
			    }
			    File n = new File(fname+".tmp");
			    if ( ! n.renameTo(o) ) {
			    	System.err.println("リネームできないよ： "+fname);
			    }
			}
			catch (Exception e) {
				// 例外
				System.out.println("書けないよ: "+e.toString());
				return false;
			}
			finally {
				closing(zos);
				closing(bos);
				closing(os);
			}
		}
		return true;
	}
	private static boolean _write2file(String fname, String dat, String encoding) {
		{
			Writer wr = null;
			BufferedWriter bw = null;
			FileOutputStream os = null;
			try {
				// 一時ファイルに書き出し
				if ( encoding == null ) {
					// デフォルトエンコーディングで
					wr = new FileWriter(fname+".tmp");
				}
				else {
					// 指定エンコーディングで
					os = new FileOutputStream(new File(fname+".tmp"));
					wr = new OutputStreamWriter(os, encoding); 
				}
				bw = new BufferedWriter(wr);
		        bw.write(dat);
		        
		        // 処理が続くので閉じないとアカン！
			    bw.close(); bw = null;
		        wr.close(); wr = null;
		        if (os!=null) { os.close(); os = null; }
				
				// キャッシュファイルに変換
			    File o = new File(fname);
			    if ( o.exists() && ! o.delete() ) {
			    	System.err.println("削除できないよ： "+fname);
			    }
			    File n = new File(fname+".tmp");
			    if ( ! n.renameTo(o) ) {
			    	System.err.println("リネームできないよ： "+fname);
			    }
			}
			catch (Exception e) {
				// 例外
				System.out.println("書けないよ: "+e.toString());
				return false;
			}
			finally {
				closing(bw);
				closing(wr);
				closing(os);
			}
		}
		return true;
	}
	
	private static final String ZIPENTRY = "compressed.dat";
	
	/**
	 *  テキストをファイルを読み出す（エンコードはデフォルト）
	 */
	public static String read4file(String fname, boolean nocr) {
		return read4file(fname, nocr, null);
	}

	/**
	 *  テキストをファイルを読み出す（エンコードは指定による）
	 */
	public static String read4file(String fname, boolean nocr, String encoding) {
		if ( fname.matches(".*\\.zip$") ) {
			return _read4zip(fname, nocr, null);
		}
		else {
			return _read4file(fname, nocr, null);
		}
	}
	private static String _read4zip(String fname, boolean nocr, String encoding) {
		String response = null;
		{
			ZipFile zf = null;
			Reader rd = null;
			InputStream is = null;
			BufferedReader br = null;
			try {
				// ファイルから読み出し
				zf = new ZipFile(fname);
				ZipEntry ze = zf.getEntry(ZIPENTRY);
				if ( ze == null ) {
					System.out.println("ZIPファイルがおかしい");
					return null;
				}
				
				is = zf.getInputStream(ze);
				
				// ファイルから読み出し
				if ( encoding == null ) {
					// デフォルトエンコーディングで
					rd = new InputStreamReader(is); 
				}
				else {
					// 指定エンコーディングで
					rd = new InputStreamReader(is, encoding); 
				}
				br = new BufferedReader(rd);
				
				String str;
				StringBuilder sb = new StringBuilder();
				while ((str = br.readLine()) != null) {
					sb.append(str);
					if ( ! nocr ) sb.append("\n");
				}
				
				response = sb.toString();
			}
			catch (Exception e) {
				// 例外
				System.out.println("読めないよ: "+e.toString());
				return null;
			}
			finally {
				closing(is);
				closing(br);
				closing(rd);
				closing(zf);
			}
		}
		return response;
	}
	private static String _read4file(String fname, boolean nocr, String encoding) {
		String response = null;
		{
			Reader rd = null;
			BufferedReader br = null;
			FileInputStream is = null;
			try {
				// ファイルから読み出し
				if ( encoding == null ) {
					// デフォルトエンコーディングで
					rd = new FileReader(fname);
				}
				else {
					// 指定エンコーディングで
					is = new FileInputStream(new File(fname));
					rd = new InputStreamReader(is, encoding); 
				}
				br = new BufferedReader(rd);
				
				String str;
				StringBuilder sb = new StringBuilder();
				while ((str = br.readLine()) != null) {
					sb.append(str);
					if ( ! nocr ) sb.append("\n");
				}
				
				response = sb.toString();
			}
			catch (Exception e) {
				// 例外
				System.out.println("読めないよ: "+e.toString());
				return null;
			}
			finally {
				closing(br);
				closing(rd);
				closing(is);
			}
		}
		return response;
	}

	/**
	 *  XMLEncoderでシリアライズしてファイルに出力する
	 */
	public static boolean writeXML(String fname, Object obj) {
		boolean done = true;
		{
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			XMLEncoder enc = null;
			try {
				fos = new FileOutputStream(fname+".tmp");
				bos = new BufferedOutputStream(fos);
				enc = new XMLEncoder(bos);
				enc.writeObject(obj);
				
		        // 処理が続くので閉じないとアカン！
				enc.close(); enc = null;
				bos.close(); bos = null;
				fos.close(); fos = null;
				
				// キャッシュファイルに変換
			    File o = new File(fname);
			    if ( o.exists() && ! o.delete() ) {
			    	System.err.println("削除できないよ： "+fname);
			    }
			    File n = new File(fname+".tmp");
			    if ( ! n.renameTo(o) ) {
			    	System.err.println("リネームできないよ： "+fname);
			    }
			}
			catch (Exception e) {
				// 例外
				done = false;
				System.err.println(e.toString());
			}
			finally {
				closing(enc);
				closing(fos);
				closing(bos);
			}
		}
		return done;
	}
	
	/**
	 *  XMLDecorderでシリアライズしたファイルを読みだす
	 */
	public static Object readXML(String fname) {
		{
			XMLDecoder dec = null;
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			try {
				fis = new FileInputStream(fname);
				bis = new BufferedInputStream(fis);
				dec = new XMLDecoder(bis);
				return dec.readObject();
			}
			catch (Exception e) {
				// 例外
				System.err.println(e.toString());
			}
			finally {
				closing(dec);
				closing(bis);
				closing(fis);
			}
		}
		return null;
	}
	
	/*
	 * finallyブロックで書き間違えそうなので
	 */
	public static void closing(Reader r) {
		if ( r != null ) try { r.close(); } catch (Exception e) {}
	}
	public static void closing(InputStream r) {
		if ( r != null ) try { r.close(); } catch (Exception e) {}
	}
	public static void closing(ZipFile r) {
		if ( r != null ) try { r.close(); } catch (Exception e) {}
	}
	public static void closing(XMLDecoder r) {
		if ( r != null ) r.close();
	}
	
	public static void closing(Writer w) {
		if ( w != null ) try { w.close(); } catch (Exception e) {}
	}
	public static void closing(OutputStream w) {
		if ( w != null ) try { w.close(); } catch (Exception e) {}
	}
	public static void closing(XMLEncoder w) {
		if ( w != null ) w.close();
	}
	
	public static void closing(FileChannel fc) {
		if ( fc != null ) try { fc.close(); } catch (Exception e) {}
	}
	
	public static void closing(Socket sock) {
		if ( sock != null ) try { sock.close(); } catch (Exception e) {}
	}
	public static void closing(HttpURLConnection ucon) {
		if ( ucon != null ) ucon.disconnect();
	}
}
