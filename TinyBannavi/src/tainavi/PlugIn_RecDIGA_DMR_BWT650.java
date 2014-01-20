package tainavi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
 * 
 */

public class PlugIn_RecDIGA_DMR_BWT650 extends PlugIn_RecDIGA_DMR_BZT720 implements HDDRecorder,Cloneable {

	@Override
	public PlugIn_RecDIGA_DMR_BWT650 clone() {
		return (PlugIn_RecDIGA_DMR_BWT650) super.clone();
	}

	/* 必須コード  - ここから */
	
	// 種族の特性
	@Override
	public String getRecorderId() { return "DIGA DMR-BWT650"; }
	@Override
	public RecType getType() { return RecType.RECORDER; }
	
	@Override
	protected int get_com_try_count() { return 5; }
	
	public PlugIn_RecDIGA_DMR_BWT650() {
		super();
		this.setTunerNum(3);
	}
	
	/*
	 * 公開メソッド
	 */
	
	/*
	 * 非公開メソッド
	 */
	
	/**
	 * 予約全エントリを通しての処理
	 */
	@Override
	protected int _getDigaReserveList(ArrayList<ReserveList> newReserveList, String response) {
		// 予約詳細を作る
		Matcher mx = Pattern.compile("<table class=\"reclist\">.+?<tbody>(.+?)</tbody>",Pattern.DOTALL).matcher(response);
		if ( mx.find() ) {
			Matcher ma = Pattern.compile("<tr>(.+?)</tr>",Pattern.DOTALL).matcher(mx.group(1));
			while ( ma.find() ) {
				_getDigaReserveProg(newReserveList, ma.group(1));
			}
		}
		return(RETCODE_SUCCESS);
	}
}
